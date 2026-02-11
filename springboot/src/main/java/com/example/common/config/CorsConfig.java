package com.example.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS configuration
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 1 Allow all origins
        corsConfiguration.addAllowedHeader("*"); // 2 Allow all headers
        corsConfiguration.addAllowedMethod("*"); // 3 Allow all methods
        source.registerCorsConfiguration("/**", corsConfiguration); // 4 Apply CORS to all endpoints
        return new CorsFilter(source);
    }
}