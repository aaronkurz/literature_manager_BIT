package com.example.mapper;

import com.example.entity.ProcessingStatus;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProcessingStatusMapper {
    
    @Insert("INSERT INTO processing_status (task_id, file_name, status, progress, current_step, file_path, created_time, updated_time) " +
            "VALUES (#{taskId}, #{fileName}, #{status}, #{progress}, #{currentStep}, #{filePath}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProcessingStatus status);
    
    @Update("UPDATE processing_status SET status=#{status}, progress=#{progress}, current_step=#{currentStep}, " +
            "error_message=#{errorMessage}, extracted_title=#{extractedTitle}, extracted_authors=#{extractedAuthors}, " +
            "extracted_institution=#{extractedInstitution}, extracted_year=#{extractedYear}, extracted_source=#{extractedSource}, " +
            "extracted_keywords=#{extractedKeywords}, extracted_doi=#{extractedDoi}, extracted_abstract=#{extractedAbstract}, " +
            "extracted_summary=#{extractedSummary}, extracted_custom_concept1=#{extractedCustomConcept1}, " +
            "extracted_custom_concept2=#{extractedCustomConcept2}, extracted_custom_concept3=#{extractedCustomConcept3}, " +
            "updated_time=NOW() WHERE task_id=#{taskId}")
    int updateByTaskId(ProcessingStatus status);
    
    @Update("UPDATE processing_status SET completed_time=NOW() WHERE task_id=#{taskId}")
    int markCompleted(String taskId);
    
    @Select("SELECT * FROM processing_status WHERE task_id=#{taskId}")
    ProcessingStatus selectByTaskId(String taskId);
    
    @Delete("DELETE FROM processing_status WHERE task_id=#{taskId}")
    int deleteByTaskId(String taskId);
}
