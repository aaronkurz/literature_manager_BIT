package com.example.service.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TaskService {

    // Single thread pool for sequential task execution
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Execute task asynchronously
     * @param task The task to execute
     */
    @Async
    public void executeAsync(Runnable task) {
        executor.submit(task);
    }

    /**
     * Shutdown thread pool (optional, called on app shutdown)
     */
    public void shutdown() {
        executor.shutdown();
    }
}