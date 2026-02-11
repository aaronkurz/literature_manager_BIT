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
            System.out.println("=== Starting paper processing: " + paperFilePath + " ===");
            
            // Update status: Converting
            status.setStatus("CONVERTING");
            status.setProgress(20);
            status.setCurrentStep("Converting file format...");
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
            
            System.out.println("File format conversion complete");
            
            // Read text content
            String content = "";
            if (new File(txtpath).exists()) {
                content = new String(Files.readAllBytes(Paths.get(txtpath)));
                System.out.println("Text content extracted, length: " + content.length() + " chars");
            }
            if (content.isEmpty()) {
                throw new Exception("Failed to extract text content");
            }
            
            // Update status: Extracting metadata
            status.setStatus("EXTRACTING");
            status.setProgress(40);
            status.setCurrentStep("Extracting paper metadata...");
            processingStatusService.updateStatus(status);
            
            // Extract metadata using Ollama (first 8000 chars usually contain all metadata)
            String metadataText = content.length() > 8000 ? content.substring(0, 8000) : content;
            System.out.println("Calling Ollama for metadata extraction (input length: " + metadataText.length() + " chars)");
            JsonObject metadata = extractMetadata(metadataText);
            
            // Store extracted metadata in status (with truncation for long fields)
            status.setExtractedTitle(getStringValue(metadata, "title"));
            status.setExtractedAuthors(getStringValue(metadata, "author"));
            status.setExtractedInstitution(getStringValueWithLimit(metadata, "organ", 255));
            status.setExtractedYear(getStringValue(metadata, "year"));
            status.setExtractedSource(getStringValueWithLimit(metadata, "source", 255));
            status.setExtractedKeywords(getStringValue(metadata, "keyword"));
            status.setExtractedDoi(getStringValue(metadata, "doi"));
            status.setExtractedAbstract(getStringValue(metadata, "summary"));
            
            System.out.println("Metadata extraction complete:");
            System.out.println("  Title: " + status.getExtractedTitle());
            System.out.println("  Author: " + status.getExtractedAuthors());
            System.out.println("  Abstract: " + status.getExtractedAbstract());
            
            // Use the extracted abstract as the summary (no need for second AI call)
            status.setExtractedSummary(status.getExtractedAbstract());
            
            // Update status to show we're extracting custom concepts (if any are defined)
            status.setStatus("EXTRACTING");
            status.setProgress(60);
            status.setCurrentStep("Identifying custom concepts...");
            processingStatusService.updateStatus(status);
            
            // Extract custom concepts if any are defined
            extractCustomConcepts(status, metadataText);
            
            // Update status: Pending approval
            status.setStatus("PENDING_APPROVAL");
            status.setProgress(100);
            status.setCurrentStep("Extraction complete, waiting for user review...");
            processingStatusService.updateStatus(status);
            
            System.out.println("=== Metadata extraction complete, waiting for user review ===");
            
        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setProgress(0);
            status.setCurrentStep("Processing failed");
            status.setErrorMessage(e.getMessage());
            processingStatusService.updateStatus(status);
            System.err.println("Processing failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extract metadata from paper content using Ollama
     */
    private JsonObject extractMetadata(String content) throws Exception {
        System.out.println("=== Starting metadata extraction ===");
        String prompt = "You are an academic paper metadata extraction expert. Extract metadata from the following paper text and return it strictly in the following JSON format, without any Markdown formatting or extra explanation:\n\n" +
            "{\n" +
            "  \"title\": \"Paper title\",\n" +
            "  \"author\": \"Author1; Author2; Author3\",\n" +
            "  \"organ\": \"Author institution\",\n" +
            "  \"year\": \"Publication year (numbers only)\",\n" +
            "  \"source\": \"Journal or conference name\",\n" +
            "  \"keyword\": \"Keyword1; Keyword2; Keyword3\",\n" +
            "  \"doi\": \"DOI number\",\n" +
            "  \"summary\": \"Paper abstract content\"\n" +
            "}\n\n" +
            "If a field cannot be extracted, use an empty string \"\". Now extract metadata from the following paper:\n\n" +
            content;
        
        System.out.println("Calling BigModelUtil.ollamaTextGeneration...");
        String response = BigModelUtil.ollamaTextGeneration(prompt);
        System.out.println("BigModelUtil returned, response length: " + (response != null ? response.length() : "null"));
        
        JsonObject result = parseJsonSafely(response);
        System.out.println("JSON parsing complete, field count: " + result.size());
        System.out.println("=== Metadata extraction complete ===");
        
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
                System.out.println("No custom concepts configured, skipping extraction");
                return;
            }
            
            System.out.println("Starting custom concept extraction, " + customConcepts.size() + " relationships");
            
            // Use a shorter content for faster processing (first 4000 chars should be enough)
            String shortContent = content.length() > 4000 ? content.substring(0, 4000) : content;
            
            // Process each custom concept with timeout protection
            for (int i = 0; i < customConcepts.size(); i++) {
                CustomConcept concept = customConcepts.get(i);
                String relationshipName = concept.getRelationshipName();
                List<String> concepts = concept.getConceptsList();
                
                System.out.println("Extracting custom concept " + (i + 1) + ": " + relationshipName + " - " + concepts);
                
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
                    
                    System.out.println("Custom concept " + (i + 1) + " result: " + resultJson);
                } catch (Exception conceptError) {
                    System.err.println("Custom concept " + (i + 1) + " extraction failed: " + conceptError.getMessage());
                    // Continue with next concept even if this one fails
                }
            }
            
        } catch (Exception e) {
            System.err.println("Custom concept extraction failed: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the whole process if custom concept extraction fails
        }
    }
    
    /**
     * Build optimized prompt for custom concept extraction
     */
    private String buildCustomConceptPrompt(String relationshipName, List<String> concepts, String content) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("From the following paper abstract, determine if the paper uses any of these concepts.\n\n");
        prompt.append("Relationship type: ").append(relationshipName).append("\n");
        prompt.append("Possible concepts: ").append(String.join(", ", concepts)).append("\n\n");
        prompt.append("Return only JSON format (no markdown): {\"concepts\": [\"matching_concept1\", \"matching_concept2\"]}\n");
        prompt.append("If no match, return: {\"concepts\": []}\n");
        prompt.append("Only return concept names from the given list.\n\n");
        prompt.append("Paper content:\n").append(content);
        
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
            System.err.println("JSON parsing failed, response: " + response);
            System.err.println("Error: " + e.getMessage());
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
                return value.isEmpty() ? "Not extracted" : value;
            }
        } catch (Exception e) {
            System.err.println("Failed to get field " + key + ": " + e.getMessage());
        }
        return "Not extracted";
    }
    
    /**
     * Get string value with maximum length truncation
     */
    private String getStringValueWithLimit(JsonObject json, String key, int maxLength) {
        String value = getStringValue(json, key);
        if (value != null && value.length() > maxLength) {
            System.out.println("Warning: field '" + key + "' exceeds max length " + maxLength + ", truncated (original length: " + value.length() + ")");
            return value.substring(0, maxLength);
        }
        return value;
    }
    
    /**
     * Save approved article to database
     */
    public void saveApprovedArticle(ArticleInfo articleInfo, ProcessingStatus status) {
        try {
            System.out.println("=== Saving approved paper: " + articleInfo.getTitle() + " ===");
            
            // Get file paths
            String oripath = status.getFilePath().split("\\.")[0];
            articleInfo.setPathpdf(oripath + ".pdf");
            articleInfo.setPathdocx(oripath + ".docx");
            articleInfo.setPathtxt(oripath + ".txt");
            
            // Copy custom concepts from status to article info (only if not already set from frontend)
            System.out.println("Processing custom concepts:");
            System.out.println("  From frontend:");
            System.out.println("    customConcept1: " + articleInfo.getCustomConcept1());
            System.out.println("    customConcept2: " + articleInfo.getCustomConcept2());
            System.out.println("    customConcept3: " + articleInfo.getCustomConcept3());
            System.out.println("  From ProcessingStatus:");
            System.out.println("    extractedCustomConcept1: " + status.getExtractedCustomConcept1());
            System.out.println("    extractedCustomConcept2: " + status.getExtractedCustomConcept2());
            System.out.println("    extractedCustomConcept3: " + status.getExtractedCustomConcept3());
            
            // Use values from frontend if present, otherwise fallback to status
            if (articleInfo.getCustomConcept1() == null && status.getExtractedCustomConcept1() != null) {
                articleInfo.setCustomConcept1(status.getExtractedCustomConcept1());
            }
            if (articleInfo.getCustomConcept2() == null && status.getExtractedCustomConcept2() != null) {
                articleInfo.setCustomConcept2(status.getExtractedCustomConcept2());
            }
            if (articleInfo.getCustomConcept3() == null && status.getExtractedCustomConcept3() != null) {
                articleInfo.setCustomConcept3(status.getExtractedCustomConcept3());
            }
            
            System.out.println("  Final values:");
            System.out.println("    customConcept1: " + articleInfo.getCustomConcept1());
            System.out.println("    customConcept2: " + articleInfo.getCustomConcept2());
            System.out.println("    customConcept3: " + articleInfo.getCustomConcept3());
            
            // Save article info
            articleService.saveArticle(articleInfo);
            System.out.println("Paper info saved to MySQL");
            
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
            System.out.println("Summary saved to database");
            
            // Update Neo4j graph
            runNeo4jLoader(false, articleInfo.getTitle());
            System.out.println("Graph updated");
            System.out.println("=== Paper save complete ===");
            
        } catch (Exception e) {
            System.err.println("Save failed: " + e.getMessage());
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
