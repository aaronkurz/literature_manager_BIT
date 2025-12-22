package com.example.utils;

import com.example.entity.ArticleInfo;
import com.example.entity.ProcessingStatus;
import com.example.service.ArticleService;
import com.example.service.impl.ProcessingStatusService;
import com.example.utils.docling.DoclingExtractor;
import com.example.utils.bigmodel.BigModelUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
            String doclingJsonPath = DoclingExtractor.runDocling(pdfpath);
            
            System.out.println("文件格式转换完成");
            
            // Update status: Extracting metadata
            status.setStatus("EXTRACTING");
            status.setProgress(40);
            status.setCurrentStep("正在提取论文元数据...");
            processingStatusService.updateStatus(status);
            
            // Read text content (fallback) and build docling-condensed context
            String content = "";
            if (new File(txtpath).exists()) {
                content = new String(Files.readAllBytes(Paths.get(txtpath)));
                System.out.println("文本内容长度: " + content.length());
            }
            if (content.isEmpty()) {
                throw new Exception("无法提取文本内容");
            }

            String llmContext = buildDoclingContext(doclingJsonPath, content);
            
            // Extract metadata using Ollama
            String metadataResult = extractMetadata(llmContext);
            JsonObject metadata = parseJsonObjectSafe(metadataResult);
            
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
            String summaryResult = generateSummary(llmContext);
            JsonObject summaryJson = parseJsonObjectSafe(summaryResult);
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
            "\n请从以下论文内容中提取元数据，并严格以JSON对象返回，键名必须与模板一致，不要添加额外字段或文本。未知字段置为空字符串。严禁输出Markdown或额外说明。\n" + 
            content.substring(0, Math.min(content.length(), 12000)); // Trim to reduce LLM load
        
        return BigModelUtil.ollamaTextGeneration(prompt);
    }
    
    /**
     * Generate summary from paper content
     */
    private String generateSummary(String content) throws Exception {
        String prompt = Config.SUMMARY_JSON + 
            "\n请对以下论文内容生成摘要，并严格以JSON对象返回，键名必须与模板一致，不要添加额外字段或文本。未知字段置为空字符串。严禁输出Markdown或额外说明。\n" + 
            content.substring(0, Math.min(content.length(), 12000)); // Trim to reduce LLM load
        
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
     * Build a condensed context for the LLM using docling output if available; otherwise fallback to raw text.
     */
    private String buildDoclingContext(String doclingJsonPath, String fallbackText) {
        if (doclingJsonPath != null && new File(doclingJsonPath).exists()) {
            try {
                String raw = Files.readString(Paths.get(doclingJsonPath));
                JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
                StringBuilder sb = new StringBuilder();
                appendField(sb, "Title", getStringOrDefault(obj, "title", ""));
                appendField(sb, "Authors", String.join("; ", getAuthors(obj)));
                appendField(sb, "Abstract", getStringOrDefault(obj, "abstract", ""));
                appendField(sb, "Introduction", getStringOrDefault(obj, "introduction", ""));
                appendField(sb, "Conclusion", getStringOrDefault(obj, "conclusion", ""));

                if (obj.has("sections") && obj.get("sections").isJsonArray()) {
                    obj.get("sections").getAsJsonArray().forEach(secElem -> {
                        try {
                            JsonObject sec = secElem.getAsJsonObject();
                            String title = getStringOrDefault(sec, "title", "");
                            String para = getStringOrDefault(sec, "first_paragraph", "");
                            if (!title.isEmpty() && !para.isEmpty()) {
                                sb.append("Section: ").append(title).append("\n");
                                sb.append("First paragraph: ").append(para).append("\n\n");
                            }
                        } catch (Exception ignored) {
                        }
                    });
                }

                // Add a small markdown head if present
                appendField(sb, "Markdown head", getStringOrDefault(obj, "markdown_head", ""));

                String condensed = sb.toString();
                int maxLen = 12000;
                if (condensed.length() > maxLen) {
                    return condensed.substring(0, maxLen) + "\n...(内容已截断)";
                }
                return condensed;
            } catch (Exception e) {
                System.out.println("Docling context构建失败，使用原始文本: " + e.getMessage());
            }
        }

        // Fallback to truncated raw text
        int maxLen = 12000;
        if (fallbackText.length() > maxLen) {
            return fallbackText.substring(0, maxLen) + "\n...(内容已截断)";
        }
        return fallbackText;
    }

    private void appendField(StringBuilder sb, String label, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(label).append(": ").append(value).append("\n\n");
        }
    }

    private String[] getAuthors(JsonObject obj) {
        try {
            if (obj.has("authors") && obj.get("authors").isJsonArray()) {
                return gson.fromJson(obj.get("authors"), String[].class);
            }
        } catch (Exception ignored) {
        }
        return new String[]{};
    }

    // Defensive JSON parsing: handles plain strings or nested JSON strings
    private JsonObject parseJsonObjectSafe(String content) {
        try {
            JsonElement element = JsonParser.parseString(content);
            if (element.isJsonObject()) {
                return element.getAsJsonObject();
            }
            if (element.isJsonPrimitive()) {
                String inner = element.getAsString();
                try {
                    JsonElement innerElement = JsonParser.parseString(inner);
                    if (innerElement.isJsonObject()) {
                        return innerElement.getAsJsonObject();
                    }
                } catch (Exception ignored) {
                    // Fall through to wrapping raw content
                }
                JsonObject fallback = new JsonObject();
                fallback.addProperty("raw", inner);
                return fallback;
            }
        } catch (Exception ignored) {
            // Fall through to wrapping raw content
        }
        JsonObject fallback = new JsonObject();
        fallback.addProperty("raw", content);
        return fallback;
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