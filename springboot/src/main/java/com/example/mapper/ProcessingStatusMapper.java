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
    
    @Select("SELECT id, task_id, file_name, status, progress, current_step, error_message, " +
            "extracted_title, extracted_authors, extracted_institution, extracted_year, extracted_source, " +
            "extracted_keywords, extracted_doi, extracted_abstract, extracted_summary, " +
            "extracted_custom_concept1, extracted_custom_concept2, extracted_custom_concept3, " +
            "file_path, created_time, updated_time, completed_time " +
            "FROM processing_status WHERE task_id=#{taskId}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "taskId", column = "task_id"),
        @Result(property = "fileName", column = "file_name"),
        @Result(property = "status", column = "status"),
        @Result(property = "progress", column = "progress"),
        @Result(property = "currentStep", column = "current_step"),
        @Result(property = "errorMessage", column = "error_message"),
        @Result(property = "extractedTitle", column = "extracted_title"),
        @Result(property = "extractedAuthors", column = "extracted_authors"),
        @Result(property = "extractedInstitution", column = "extracted_institution"),
        @Result(property = "extractedYear", column = "extracted_year"),
        @Result(property = "extractedSource", column = "extracted_source"),
        @Result(property = "extractedKeywords", column = "extracted_keywords"),
        @Result(property = "extractedDoi", column = "extracted_doi"),
        @Result(property = "extractedAbstract", column = "extracted_abstract"),
        @Result(property = "extractedSummary", column = "extracted_summary"),
        @Result(property = "extractedCustomConcept1", column = "extracted_custom_concept1"),
        @Result(property = "extractedCustomConcept2", column = "extracted_custom_concept2"),
        @Result(property = "extractedCustomConcept3", column = "extracted_custom_concept3"),
        @Result(property = "filePath", column = "file_path"),
        @Result(property = "createdTime", column = "created_time"),
        @Result(property = "updatedTime", column = "updated_time"),
        @Result(property = "completedTime", column = "completed_time")
    })
    ProcessingStatus selectByTaskId(String taskId);
    
    @Delete("DELETE FROM processing_status WHERE task_id=#{taskId}")
    int deleteByTaskId(String taskId);
}
