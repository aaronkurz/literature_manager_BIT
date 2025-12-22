package com.example.entity;

import java.util.Date;

/**
 * Processing status entity for tracking paper processing workflow
 */
public class ProcessingStatus {
    private Integer id;
    private String taskId;           // Unique task identifier
    private String fileName;         // Original file name
    private String status;           // UPLOADING, CONVERTING, EXTRACTING, ANALYZING, PENDING_APPROVAL, APPROVED, REJECTED, FAILED
    private Integer progress;        // 0-100
    private String currentStep;      // Current processing step description
    private String errorMessage;     // Error message if failed
    
    // Extracted metadata (pending approval)
    private String extractedTitle;
    private String extractedAuthors;
    private String extractedInstitution;
    private String extractedYear;
    private String extractedSource;
    private String extractedKeywords;
    private String extractedDoi;
    private String extractedAbstract;
    private String extractedSummary;
    
    // Processing info
    private String filePath;
    private Date createdTime;
    private Date updatedTime;
    private Date completedTime;
    
    public ProcessingStatus() {
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
    
    public String getCurrentStep() {
        return currentStep;
    }
    
    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getExtractedTitle() {
        return extractedTitle;
    }
    
    public void setExtractedTitle(String extractedTitle) {
        this.extractedTitle = extractedTitle;
    }
    
    public String getExtractedAuthors() {
        return extractedAuthors;
    }
    
    public void setExtractedAuthors(String extractedAuthors) {
        this.extractedAuthors = extractedAuthors;
    }
    
    public String getExtractedInstitution() {
        return extractedInstitution;
    }
    
    public void setExtractedInstitution(String extractedInstitution) {
        this.extractedInstitution = extractedInstitution;
    }
    
    public String getExtractedYear() {
        return extractedYear;
    }
    
    public void setExtractedYear(String extractedYear) {
        this.extractedYear = extractedYear;
    }
    
    public String getExtractedSource() {
        return extractedSource;
    }
    
    public void setExtractedSource(String extractedSource) {
        this.extractedSource = extractedSource;
    }
    
    public String getExtractedKeywords() {
        return extractedKeywords;
    }
    
    public void setExtractedKeywords(String extractedKeywords) {
        this.extractedKeywords = extractedKeywords;
    }
    
    public String getExtractedDoi() {
        return extractedDoi;
    }
    
    public void setExtractedDoi(String extractedDoi) {
        this.extractedDoi = extractedDoi;
    }
    
    public String getExtractedAbstract() {
        return extractedAbstract;
    }
    
    public void setExtractedAbstract(String extractedAbstract) {
        this.extractedAbstract = extractedAbstract;
    }
    
    public String getExtractedSummary() {
        return extractedSummary;
    }
    
    public void setExtractedSummary(String extractedSummary) {
        this.extractedSummary = extractedSummary;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Date getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    
    public Date getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
    
    public Date getCompletedTime() {
        return completedTime;
    }
    
    public void setCompletedTime(Date completedTime) {
        this.completedTime = completedTime;
    }
}
