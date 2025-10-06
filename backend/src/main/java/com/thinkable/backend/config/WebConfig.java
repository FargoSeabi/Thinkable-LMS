package com.thinkable.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.uploads.dir}")
    private String uploadBaseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve books from the uploads directory
        registry.addResourceHandler("/books/**")
                .addResourceLocations("file:" + uploadBaseDir + "/books/")
                .setCachePeriod(3600); // Cache for 1 hour
                
        // Also serve any other uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadBaseDir + "/")
                .setCachePeriod(3600);
    }
}
