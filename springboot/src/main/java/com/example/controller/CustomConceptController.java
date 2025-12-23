package com.example.controller;

import com.example.common.Result;
import com.example.entity.CustomConcept;
import com.example.service.impl.CustomConceptService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Controller for custom concept management
 */
@RestController
@RequestMapping("/custom-concepts")
public class CustomConceptController {
    
    @Resource
    private CustomConceptService customConceptService;
    
    /**
     * Get all custom concepts
     */
    @GetMapping("/list")
    public Result<List<CustomConcept>> listAll() {
        try {
            List<CustomConcept> concepts = customConceptService.getAllConcepts();
            return Result.success(concepts);
        } catch (Exception e) {
            return Result.error("500", "获取失败：" + e.getMessage());
        }
    }
    
    /**
     * Get custom concept by display order
     */
    @GetMapping("/{displayOrder}")
    public Result<CustomConcept> getByDisplayOrder(@PathVariable Integer displayOrder) {
        try {
            CustomConcept concept = customConceptService.getByDisplayOrder(displayOrder);
            return Result.success(concept);
        } catch (Exception e) {
            return Result.error("500", "获取失败：" + e.getMessage());
        }
    }
    
    /**
     * Save or update custom concept
     */
    @PostMapping("/save")
    public Result<String> saveOrUpdate(@RequestBody CustomConcept customConcept) {
        try {
            customConceptService.saveOrUpdate(customConcept);
            return Result.success("保存成功");
        } catch (IllegalArgumentException e) {
            return Result.error("400", e.getMessage());
        } catch (Exception e) {
            return Result.error("500", "保存失败：" + e.getMessage());
        }
    }
    
    /**
     * Delete custom concept by display order
     */
    @DeleteMapping("/{displayOrder}")
    public Result<String> deleteByDisplayOrder(@PathVariable Integer displayOrder) {
        try {
            customConceptService.deleteByDisplayOrder(displayOrder);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("500", "删除失败：" + e.getMessage());
        }
    }
}
