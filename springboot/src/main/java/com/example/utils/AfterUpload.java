package com.example.utils;

import com.example.entity.ArticleInfo;
import com.example.entity.CustomConcept;
import com.example.entity.ProcessingStatus;
import com.example.service.ArticleService;
import com.example.service.impl.CustomConceptService;
import com.example.service.impl.ProcessingStatusService;
import com.example.utils.bigmodel.BigModelUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
    
    @Autowired
    private CustomConceptService customConceptService;
    
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
            System.out.println("  摘要: " + status.getExtractedAbstract());
            
            // Use the extracted abstract as the summary (no need for second AI call)
            status.setExtractedSummary(status.getExtractedAbstract());
            
            // Update status to show we're extracting custom concepts (if any are defined)
            status.setStatus("EXTRACTING");
            status.setProgress(60);
            status.setCurrentStep("正在识别自定义概念...");
            processingStatusService.updateStatus(status);
            
            // Extract custom concepts if any are defined
            extractCustomConcepts(status, metadataText);
            
            // Update status: Pending approval
            status.setStatus("PENDING_APPROVAL");
            status.setProgress(100);
            status.setCurrentStep("提取完成，等待用户审核...");
            processingStatusService.updateStatus(status);
            
            System.out.println("=== 元数据提取完成，等待用户审核 ===");
            
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
        System.out.println("=== 开始提取元数据 ===");
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
        
        System.out.println("调用 BigModelUtil.ollamaTextGeneration...");
        String response = BigModelUtil.ollamaTextGeneration(prompt);
        System.out.println("BigModelUtil 返回，响应长度: " + (response != null ? response.length() : "null"));
        
        JsonObject result = parseJsonSafely(response);
        System.out.println("JSON 解析完成，字段数: " + result.size());
        System.out.println("=== 元数据提取完成 ===");
        
        return result;
    }
    
    /**
     * Extract custom concepts from paper content using Ollama
     * This is called after metadata extraction to identify which user-defined concepts apply
     * Optimized to reduce timeout issues
     */
    private void extractCustomConcepts(ProcessingStatus status, String content) {
        try {
            // Get all custom concepts
            List<CustomConcept> customConcepts = customConceptService.getAllConcepts();
            
            if (customConcepts.isEmpty()) {
                System.out.println("没有自定义概念配置，跳过自定义概念提取");
                return;
            }
            
            System.out.println("开始提取自定义概念，共 " + customConcepts.size() + " 个关系");
            
            // Use a shorter content for faster processing (first 4000 chars should be enough)
            String shortContent = content.length() > 4000 ? content.substring(0, 4000) : content;
            
            // Process each custom concept with timeout protection
            for (int i = 0; i < customConcepts.size(); i++) {
                CustomConcept concept = customConcepts.get(i);
                String relationshipName = concept.getRelationshipName();
                List<String> concepts = concept.getConceptsList();
                
                System.out.println("提取自定义概念 " + (i + 1) + ": " + relationshipName + " - " + concepts);
                
                try {
                    // Build optimized prompt for this relationship
                    String prompt = buildCustomConceptPrompt(relationshipName, concepts, shortContent);
                    
                    // Call LLM with timeout protection
                    String response = BigModelUtil.ollamaTextGeneration(prompt);
                    JsonObject result = parseJsonSafely(response);
                    
                    // Extract matching concepts
                    JsonArray matchingConcepts = new JsonArray();
                    if (result.has("concepts") && result.get("concepts").isJsonArray()) {
                        matchingConcepts = result.getAsJsonArray("concepts");
                    }
                    
                    // Build JSON result for this custom concept
                    JsonObject customConceptResult = new JsonObject();
                    customConceptResult.addProperty("relationshipName", relationshipName);
                    customConceptResult.add("matchingConcepts", matchingConcepts);
                    
                    // Store in appropriate field
                    String resultJson = gson.toJson(customConceptResult);
                    switch (i) {
                        case 0:
                            status.setExtractedCustomConcept1(resultJson);
                            break;
                        case 1:
                            status.setExtractedCustomConcept2(resultJson);
                            break;
                        case 2:
                            status.setExtractedCustomConcept3(resultJson);
                            break;
                    }
                    
                    System.out.println("自定义概念 " + (i + 1) + " 提取结果: " + resultJson);
                } catch (Exception conceptError) {
                    System.err.println("提取自定义概念 " + (i + 1) + " 失败: " + conceptError.getMessage());
                    // Continue with next concept even if this one fails
                }
            }
            
        } catch (Exception e) {
            System.err.println("提取自定义概念失败: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the whole process if custom concept extraction fails
        }
    }
    
    /**
     * Build optimized prompt for custom concept extraction
     */
    private String buildCustomConceptPrompt(String relationshipName, List<String> concepts, String content) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("从以下论文摘要中，判断该论文是否使用了这些概念。\n\n");
        prompt.append("关系类型: ").append(relationshipName).append("\n");
        prompt.append("可能的概念: ").append(String.join(", ", concepts)).append("\n\n");
        prompt.append("只返回JSON格式（不要markdown标记）：{\"concepts\": [\"匹配的概念1\", \"匹配的概念2\"]}\n");
        prompt.append("如果没有匹配，返回：{\"concepts\": []}\n");
        prompt.append("只返回列表中存在的概念名称。\n\n");
        prompt.append("论文内容：\n").append(content);
        
        return prompt.toString();
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
            
            // Copy custom concepts from status to article info
            articleInfo.setCustomConcept1(status.getExtractedCustomConcept1());
            articleInfo.setCustomConcept2(status.getExtractedCustomConcept2());
            articleInfo.setCustomConcept3(status.getExtractedCustomConcept3());
            
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
