package com.example.utils;

import com.example.entity.ArticleInfo;
import com.example.entity.ProcessingStatus;
import com.example.service.ArticleService;
import com.example.service.impl.ProcessingStatusService;
import com.example.utils.bigmodel.BigModelUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.example.utils.Caj2pdf.Caj2pdf.runCajToPdf;
import static com.example.utils.neo4jloader.Neo4jLoader.runNeo4jLoader;
import static com.example.utils.pdf2docx.Pdf2docx.runPdfToDocx;
import static com.example.utils.pdf2txt.Pdf2txt.runpdf2txt;
import static com.example.utils.result2mysql.PaperSummarySaver.saveSummary;

/**
 * Post-upload processing - using local Ollama with Ministral-3:3b
 * Simplified and optimized for fast, accurate metadata extraction
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
            System.out.println("=== 开始处理论文: " + paperFilePath + " ===");
            
            // Update status: Converting
            status.setStatus("CONVERTING");
            status.setProgress(20);
            status.setCurrentStep("正在转换文件格式...");
            processingStatusService.updateStatus(status);
            
            // Get file paths
            String oripath = paperFilePath.split("\\.")[0];
            String pdfpath = oripath + ".pdf";
            String txtpath = oripath + ".txt";
            
            // Convert formats
            try {
                runCajToPdf();
            } catch (Exception e) {
                // Ignore caj conversion errors
            }
            runPdfToDocx();
            runpdf2txt();
            
            System.out.println("文件格式转换完成");
            
            // Read text content
            String content = "";
            if (new File(txtpath).exists()) {
                content = new String(Files.readAllBytes(Paths.get(txtpath)));
                System.out.println("提取文本内容，长度: " + content.length() + " 字符");
            }
            if (content.isEmpty()) {
                throw new Exception("无法提取文本内容");
            }
            
            // Update status: Extracting metadata
            status.setStatus("EXTRACTING");
            status.setProgress(40);
            status.setCurrentStep("正在提取论文元数据...");
            processingStatusService.updateStatus(status);
            
            // Extract metadata using Ollama (first 8000 chars usually contain all metadata)
            String metadataText = content.length() > 8000 ? content.substring(0, 8000) : content;
            System.out.println("调用Ollama提取元数据 (输入长度: " + metadataText.length() + " 字符)");
            JsonObject metadata = extractMetadata(metadataText);
            
            // Store extracted metadata in status
            status.setExtractedTitle(getStringValue(metadata, "title"));
            status.setExtractedAuthors(getStringValue(metadata, "author"));
            status.setExtractedInstitution(getStringValue(metadata, "organ"));
            status.setExtractedYear(getStringValue(metadata, "year"));
            status.setExtractedSource(getStringValue(metadata, "source"));
            status.setExtractedKeywords(getStringValue(metadata, "keyword"));
            status.setExtractedDoi(getStringValue(metadata, "doi"));
            status.setExtractedAbstract(getStringValue(metadata, "summary"));
            
            System.out.println("元数据提取完成:");
            System.out.println("  标题: " + status.getExtractedTitle());
            System.out.println("  作者: " + status.getExtractedAuthors());
            
            // Update status: Analyzing with AI
            status.setStatus("ANALYZING");
            status.setProgress(70);
            status.setCurrentStep("正在使用AI分析论文内容...");
            processingStatusService.updateStatus(status);
            
            // Generate summary using Ollama (first 12000 chars for context)
            String summaryText = content.length() > 12000 ? content.substring(0, 12000) : content;
            System.out.println("调用Ollama生成摘要 (输入长度: " + summaryText.length() + " 字符)");
            JsonObject summaryJson = generateSummary(summaryText);
            status.setExtractedSummary(getStringValue(summaryJson, "summary1"));
            
            System.out.println("AI摘要生成完成");
            
            // Update status: Pending approval
            status.setStatus("PENDING_APPROVAL");
            status.setProgress(90);
            status.setCurrentStep("提取完成，等待用户审核...");
            processingStatusService.updateStatus(status);
            
            System.out.println("=== 处理完成，等待用户审核 ===");
            
        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setProgress(0);
            status.setCurrentStep("处理失败");
            status.setErrorMessage(e.getMessage());
            processingStatusService.updateStatus(status);
            System.err.println("处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extract metadata from paper content using Ollama
     */
    private JsonObject extractMetadata(String content) throws Exception {
        String prompt = "你是一个学术论文元数据提取专家。请从下面的论文文本中提取元数据，并严格按照以下JSON格式返回，不要添加任何Markdown标记或额外说明：\n\n" +
            "{\n" +
            "  \"title\": \"论文标题\",\n" +
            "  \"author\": \"作者1; 作者2; 作者3\",\n" +
            "  \"organ\": \"作者单位\",\n" +
            "  \"year\": \"发表年份(仅数字)\",\n" +
            "  \"source\": \"期刊或会议名称\",\n" +
            "  \"keyword\": \"关键词1; 关键词2; 关键词3\",\n" +
            "  \"doi\": \"DOI编号\",\n" +
            "  \"summary\": \"论文摘要内容\"\n" +
            "}\n\n" +
            "如果某个字段无法提取，请使用空字符串\"\"。现在开始提取以下论文的元数据：\n\n" +
            content;
        
        String response = BigModelUtil.ollamaTextGeneration(prompt);
        return parseJsonSafely(response);
    }
    
    /**
     * Generate summary from paper content using Ollama
     */
    private JsonObject generateSummary(String content) throws Exception {
        String prompt = "你是一个学术论文分析专家。请对下面的论文内容生成一个简洁的摘要(约200字)，并严格按照以下JSON格式返回，不要添加任何Markdown标记或额外说明：\n\n" +
            "{\n" +
            "  \"summary1\": \"论文摘要内容\"\n" +
            "}\n\n" +
            "现在开始分析以下论文：\n\n" +
            content;
        
        String response = BigModelUtil.ollamaTextGeneration(prompt);
        return parseJsonSafely(response);
    }
    
    /**
     * Safely parse JSON from Ollama response, handling various formats
     */
    private JsonObject parseJsonSafely(String response) {
        try {
            // Remove markdown code fences if present
            String cleaned = response.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();
            
            // Try to parse as JSON
            return JsonParser.parseString(cleaned).getAsJsonObject();
        } catch (Exception e) {
            System.err.println("JSON解析失败，响应内容: " + response);
            System.err.println("错误: " + e.getMessage());
            // Return empty JSON object as fallback
            return new JsonObject();
        }
    }
    
    /**
     * Get string value from JSON object with fallback
     */
    private String getStringValue(JsonObject json, String key) {
        try {
            if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
                String value = json.get(key).getAsString().trim();
                return value.isEmpty() ? "未提取" : value;
            }
        } catch (Exception e) {
            System.err.println("获取字段 " + key + " 失败: " + e.getMessage());
        }
        return "未提取";
    }
    
    /**
     * Save approved article to database
     */
    public void saveApprovedArticle(ArticleInfo articleInfo, ProcessingStatus status) {
        try {
            System.out.println("=== 保存已批准的论文: " + articleInfo.getTitle() + " ===");
            
            // Get file paths
            String oripath = status.getFilePath().split("\\.")[0];
            articleInfo.setPathpdf(oripath + ".pdf");
            articleInfo.setPathdocx(oripath + ".docx");
            articleInfo.setPathtxt(oripath + ".txt");
            
            // Save article info
            articleService.saveArticle(articleInfo);
            System.out.println("成功将论文信息存入mysql");
            
            // Create summary JSON
            JsonObject summaryJson = new JsonObject();
            summaryJson.addProperty("summary1", articleInfo.getSummary() != null ? articleInfo.getSummary() : "");
            summaryJson.addProperty("summary2", "");
            summaryJson.addProperty("summary3", "");
            summaryJson.addProperty("summary4", "");
            summaryJson.addProperty("summary5", "");
            summaryJson.addProperty("summary6", "");
            summaryJson.addProperty("algorithm1", "");
            summaryJson.addProperty("algorithm2", "");
            summaryJson.addProperty("algorithm3", "");
            summaryJson.addProperty("algorithm4", "");
            summaryJson.addProperty("target", "");
            summaryJson.addProperty("environment", "");
            summaryJson.addProperty("tools", "");
            summaryJson.addProperty("datas", "");
            summaryJson.addProperty("standard", "");
            summaryJson.addProperty("result", "");
            summaryJson.addProperty("future", "");
            summaryJson.addProperty("weekpoint", "");
            summaryJson.addProperty("keyword", articleInfo.getKeyword() != null ? articleInfo.getKeyword() : "");
            
            saveSummary(Config.OLLAMA_MODEL, articleInfo.getTitle(), gson.toJson(summaryJson), "0");
            System.out.println("成功将摘要存入数据库");
            
            // Update Neo4j graph
            runNeo4jLoader(false, articleInfo.getTitle());
            System.out.println("图谱更新完毕");
            System.out.println("=== 论文保存完成 ===");
            
        } catch (Exception e) {
            System.err.println("保存失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Legacy method for backward compatibility (deprecated)
     */
    @Deprecated
    public void file_task(ArticleInfo articleInfo) {
        System.out.println("Warning: Using deprecated file_task method. Please use processWithStatus instead.");
    }
}
