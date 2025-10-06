package com.thinkable.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

//    @Value("${cors.allowed.origins:http://localhost:3000,http://localhost:8080}")
    @Value("${cors.allowed.origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("CORS Configuration - Raw allowedOrigins value: '{}'", allowedOrigins);
        
        String[] origins = allowedOrigins.split(",");
        logger.info("CORS Configuration - Split origins array: {}", Arrays.toString(origins));
        
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
        
        // Add CORS configuration for font files
        registry.addMapping("/fonts/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        logger.info("CORS Bean Configuration - Raw allowedOrigins value: '{}'", allowedOrigins);
        
        // Split the comma-separated origins
        String[] origins = allowedOrigins.split(",");
        logger.info("CORS Bean Configuration - Split origins array: {}", Arrays.toString(origins));
        
        configuration.setAllowedOrigins(Arrays.asList(origins));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        // Font files configuration (no credentials needed for static assets)
        CorsConfiguration fontConfiguration = new CorsConfiguration();
        fontConfiguration.setAllowedOrigins(Arrays.asList(origins));
        fontConfiguration.setAllowedMethods(Arrays.asList("GET", "OPTIONS"));
        fontConfiguration.setAllowedHeaders(Arrays.asList("*"));
        fontConfiguration.setAllowCredentials(false);
        source.registerCorsConfiguration("/fonts/**", fontConfiguration);
        
        return source;
    }
}
