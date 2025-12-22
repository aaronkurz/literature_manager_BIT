package com.example.utils;

import com.example.entity.ArticleInfo;
import com.example.entity.ProcessingStatus;
import com.example.service.ArticleService;
import com.example.service.impl.ProcessingStatusService;
import com.example.utils.bigmodel.BigModelUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.example.utils.Caj2pdf.Caj2pdf.runCajToPdf;
import static com.example.utils.neo4jloader.Neo4jLoader.runNeo4jLoader;
import static com.example.utils.pdf2docx.Pdf2docx.runPdfToDocx;
import static com.example.utils.pdf2txt.Pdf2txt.runpdf2txt;
import static com.example.utils.result2mysql.PaperSummarySaver.saveSummary;

/**
 * Post-upload processing - using local Ollama with Ministral 3B
 * Now supports status tracking and approval workflow
 */
@Component
public class AfterUpload {
    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private ProcessingStatusService processingStatusService;
    
    private final Gson gson = new Gson();
    
    /**
     * Process paper with status tracking
     */
    public void processWithStatus(String taskId, String paperFilePath) {
        ProcessingStatus status = processingStatusService.getStatus(taskId);
        
        try {
            // Update status: Converting
            status.setStatus("CONVERTING");
            status.setProgress(20);
            status.setCurrentStep("正在转换文件格式...");
            processingStatusService.updateStatus(status);
            
            // Get file paths
            String oripath = paperFilePath.split("\\.")[0];
            String pdfpath = oripath + ".pdf";
            String docxpath = oripath + ".docx";
            String txtpath = oripath + ".txt";
            
            // Convert formats
            try {
                runCajToPdf();
            } catch (IOException | InterruptedException e) {
                // Ignore caj conversion errors
            }
            runPdfToDocx();
            runpdf2txt();
            
            System.out.println("文件格式转换完成");
            
            // Update status: Extracting metadata
            status.setStatus("EXTRACTING");
            status.setProgress(40);
            status.setCurrentStep("正在提取论文元数据...");
            processingStatusService.updateStatus(status);
            
            // Read text content
            String content = "";
            if (new File(txtpath).exists()) {
                content = new String(Files.readAllBytes(Paths.get(txtpath)));
                System.out.println("文本内容长度: " + content.length());
            }
            
            if (content.isEmpty()) {
                throw new Exception("无法提取文本内容");
            }
            
            // Extract metadata using Ollama
            String metadataResult = extractMetadata(content);
            JsonObject metadata = gson.fromJson(metadataResult, JsonObject.class);
            
            // Store extracted metadata in status
            status.setExtractedTitle(getStringOrDefault(metadata, "title", "未提取"));
            status.setExtractedAuthors(getStringOrDefault(metadata, "author", "未提取"));
            status.setExtractedInstitution(getStringOrDefault(metadata, "organ", "未提取"));
            status.setExtractedYear(getStringOrDefault(metadata, "year", "未提取"));
            status.setExtractedSource(getStringOrDefault(metadata, "source", "未提取"));
            status.setExtractedKeywords(getStringOrDefault(metadata, "keyword", "未提取"));
            status.setExtractedDoi(getStringOrDefault(metadata, "doi", "未提取"));
            status.setExtractedAbstract(getStringOrDefault(metadata, "summary", "未提取"));
            
            // Update status: Analyzing with AI
            status.setStatus("ANALYZING");
            status.setProgress(70);
            status.setCurrentStep("正在使用AI分析论文内容...");
            processingStatusService.updateStatus(status);
            
            // Generate summary using Ollama
            String summaryResult = generateSummary(content);
            JsonObject summaryJson = gson.fromJson(summaryResult, JsonObject.class);
            status.setExtractedSummary(getStringOrDefault(summaryJson, "summary1", "未生成"));
            
            // Update status: Pending approval
            status.setStatus("PENDING_APPROVAL");
            status.setProgress(90);
            status.setCurrentStep("提取完成，等待用户审核...");
            processingStatusService.updateStatus(status);
            
            System.out.println("处理完成，等待用户审核。Task ID: " + taskId);
            
        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setProgress(0);
            status.setCurrentStep("处理失败");
            status.setErrorMessage(e.getMessage());
            processingStatusService.updateStatus(status);
            System.out.println("处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String getStringOrDefault(JsonObject json, String key, String defaultValue) {
        try {
            if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsString();
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return defaultValue;
    }
    
    /**
     * Extract metadata from paper content
     */
    private String extractMetadata(String content) throws Exception {
        String prompt = Config.METADATA_EXTRACTION_JSON + 
            "\n请从以下论文内容中提取元数据，以JSON格式返回:\n" + 
            content.substring(0, Math.min(content.length(), 16000)); // Limit to 16K chars
        
        return BigModelUtil.ollamaTextGeneration(prompt);
    }
    
    /**
     * Generate summary from paper content
     */
    private String generateSummary(String content) throws Exception {
        String prompt = Config.SUMMARY_JSON + 
            "\n请对以下论文内容生成摘要:\n" + 
            content.substring(0, Math.min(content.length(), 16000)); // Limit to 16K chars
        
        return BigModelUtil.ollamaTextGeneration(prompt);
    }
    
    /**
     * Save approved article to database
     */
    public void saveApprovedArticle(ArticleInfo articleInfo, ProcessingStatus status) {
        try {
            // Get file paths
            String oripath = status.getFilePath().split("\\.")[0];
            articleInfo.setPathpdf(oripath + ".pdf");
            articleInfo.setPathdocx(oripath + ".docx");
            articleInfo.setPathtxt(oripath + ".txt");
            
            // Save article info
            articleService.saveArticle(articleInfo);
            System.out.println("成功将论文信息存入mysql");
            
            // Save summary (create simplified JSON from extracted data)
            String summaryJson = createSummaryJson(status);
            saveSummary(Config.OLLAMA_MODEL, articleInfo.getTitle(), summaryJson, "0");
            System.out.println("成功将摘要存入数据库");
            
            // Update Neo4j graph
            runNeo4jLoader(false, articleInfo.getTitle());
            System.out.println("图谱更新完毕");
            
        } catch (Exception e) {
            System.out.println("保存失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Create summary JSON from extracted data
     */
    private String createSummaryJson(ProcessingStatus status) {
        JsonObject json = new JsonObject();
        json.addProperty("summary1", status.getExtractedSummary() != null ? status.getExtractedSummary() : "");
        json.addProperty("summary2", "");
        json.addProperty("summary3", "");
        json.addProperty("summary4", "");
        json.addProperty("summary5", "");
        json.addProperty("summary6", "");
        json.addProperty("algorithm1", "");
        json.addProperty("algorithm2", "");
        json.addProperty("algorithm3", "");
        json.addProperty("algorithm4", "");
        json.addProperty("target", "");
        json.addProperty("environment", "");
        json.addProperty("tools", "");
        json.addProperty("datas", "");
        json.addProperty("standard", "");
        json.addProperty("result", "");
        json.addProperty("future", "");
        json.addProperty("weekpoint", "");
        json.addProperty("keyword", status.getExtractedKeywords() != null ? status.getExtractedKeywords() : "");
        return gson.toJson(json);
    }
    
    /**
     * Legacy method for backward compatibility (deprecated)
     */
    @Deprecated
    public void file_task(ArticleInfo articleInfo) {
        System.out.println("Warning: Using deprecated file_task method. Please use processWithStatus instead.");
        // This method is kept for compatibility but should not be used
    }
}