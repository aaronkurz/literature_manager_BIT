package com.example.mapper;

import com.example.entity.CustomConcept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomConceptMapper {
    
    List<CustomConcept> selectAll();
    
    CustomConcept selectById(@Param("id") Integer id);
    
    CustomConcept selectByDisplayOrder(@Param("displayOrder") Integer displayOrder);
    
    int insert(CustomConcept customConcept);
    
    int updateById(CustomConcept customConcept);
    
    int deleteById(@Param("id") Integer id);
    
    int deleteByDisplayOrder(@Param("displayOrder") Integer displayOrder);
    
    int countAll();
}
