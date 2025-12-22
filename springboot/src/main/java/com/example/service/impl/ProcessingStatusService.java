package com.example.service.impl;

import com.example.entity.ProcessingStatus;
import com.example.mapper.ProcessingStatusMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProcessingStatusService {
    
    @Resource
    private ProcessingStatusMapper processingStatusMapper;
    
    public void createStatus(ProcessingStatus status) {
        processingStatusMapper.insert(status);
    }
    
    public void updateStatus(ProcessingStatus status) {
        processingStatusMapper.updateByTaskId(status);
    }
    
    public void markCompleted(String taskId) {
        processingStatusMapper.markCompleted(taskId);
    }
    
    public ProcessingStatus getStatus(String taskId) {
        return processingStatusMapper.selectByTaskId(taskId);
    }
    
    public void deleteStatus(String taskId) {
        processingStatusMapper.deleteByTaskId(taskId);
    }
}
