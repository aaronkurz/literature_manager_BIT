package com.example.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Custom concept entity for graph personalization
 * Users can define up to 3 custom concept combinations
 * Each combination: Paper - relationship_name - concepts (max 5)
 */
public class CustomConcept implements Serializable {
    private Integer id;
    private String relationshipName;  // e.g., "method"
    private String concepts;          // Semicolon-separated list, e.g., "RCT;Retrospective_Cohort;Prospective_Cohort"
    private Integer displayOrder;     // 1, 2, or 3 (max 3 combinations)
    private Date createdTime;
    private Date updatedTime;

    public CustomConcept() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    public String getConcepts() {
        return concepts;
    }

    public void setConcepts(String concepts) {
        this.concepts = concepts;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
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

    /**
     * Parse concepts string into list
     */
    public List<String> getConceptsList() {
        if (concepts == null || concepts.trim().isEmpty()) {
            return List.of();
        }
        return List.of(concepts.split(";"));
    }

    /**
     * Set concepts from list
     */
    public void setConceptsList(List<String> conceptsList) {
        if (conceptsList == null || conceptsList.isEmpty()) {
            this.concepts = "";
        } else {
            this.concepts = String.join(";", conceptsList);
        }
    }
}
