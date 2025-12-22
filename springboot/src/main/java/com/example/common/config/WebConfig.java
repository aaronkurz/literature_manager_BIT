package com.example.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration - No authentication required for local research tool
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // No interceptors needed for local tool
}