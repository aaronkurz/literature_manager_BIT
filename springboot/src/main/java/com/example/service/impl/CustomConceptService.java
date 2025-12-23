package com.example.service.impl;

import com.example.entity.CustomConcept;
import com.example.mapper.CustomConceptMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Service for managing custom concepts
 */
@Service
public class CustomConceptService {
    
    @Resource
    private CustomConceptMapper customConceptMapper;
    
    /**
     * Get all custom concepts (max 3)
     */
    public List<CustomConcept> getAllConcepts() {
        return customConceptMapper.selectAll();
    }
    
    /**
     * Get custom concept by display order
     */
    public CustomConcept getByDisplayOrder(Integer displayOrder) {
        return customConceptMapper.selectByDisplayOrder(displayOrder);
    }
    
    /**
     * Save or update custom concept
     * Validates constraints: max 3 combinations, max 5 concepts each
     */
    public void saveOrUpdate(CustomConcept customConcept) {
        // Validate display order (1-3)
        if (customConcept.getDisplayOrder() < 1 || customConcept.getDisplayOrder() > 3) {
            throw new IllegalArgumentException("Display order must be between 1 and 3");
        }
        
        // Validate concepts count (max 5)
        List<String> conceptsList = customConcept.getConceptsList();
        if (conceptsList.size() > 5) {
            throw new IllegalArgumentException("Maximum 5 concepts allowed per combination");
        }
        
        // Validate relationship name
        if (customConcept.getRelationshipName() == null || customConcept.getRelationshipName().trim().isEmpty()) {
            throw new IllegalArgumentException("Relationship name cannot be empty");
        }
        
        // Check if concept exists at this display order
        CustomConcept existing = customConceptMapper.selectByDisplayOrder(customConcept.getDisplayOrder());
        
        if (existing != null) {
            // Update existing
            customConcept.setId(existing.getId());
            customConceptMapper.updateById(customConcept);
        } else {
            // Check total count (max 3)
            int count = customConceptMapper.countAll();
            if (count >= 3) {
                throw new IllegalArgumentException("Maximum 3 custom concept combinations allowed");
            }
            // Insert new
            customConceptMapper.insert(customConcept);
        }
    }
    
    /**
     * Delete custom concept by display order
     */
    public void deleteByDisplayOrder(Integer displayOrder) {
        customConceptMapper.deleteByDisplayOrder(displayOrder);
    }
    
    /**
     * Get count of custom concepts
     */
    public int getCount() {
        return customConceptMapper.countAll();
    }
}
