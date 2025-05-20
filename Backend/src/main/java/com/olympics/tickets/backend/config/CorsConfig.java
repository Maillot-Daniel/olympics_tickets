package com.olympics.tickets.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")  // Limité aux endpoints API
                        .allowedOrigins(
                                "http://localhost:3000",  // Dev
                                "https://votre-domaine-production.com"  // Prod
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
                        .exposedHeaders("Authorization", "X-Refresh-Token")
                        .allowCredentials(true)
                        .maxAge(3600);  // 1 heure
            }
        };
    }
}