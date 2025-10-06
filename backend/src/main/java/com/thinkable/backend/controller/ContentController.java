package com.thinkable.backend.controller;

import com.thinkable.backend.entity.LearningContent;
import com.thinkable.backend.service.TutorContentService;
import com.thinkable.backend.service.TextExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    
    @Autowired
    private TutorContentService contentService;
    
    @Autowired
    private TextExtractionService textExtractionService;
    
    /**
     * Extract text content from a document for accessibility features
     * This endpoint serves the TextExtractionService frontend requests
     */
    @PostMapping("/{contentId}/extract-text")
    public ResponseEntity<Map<String, Object>> extractTextFromContent(
            @PathVariable Long contentId,
            @RequestBody Map<String, Object> options) {
        try {
            System.out.println("Text extraction requested for content ID: " + contentId);
            
            // Get the content first
            LearningContent content = contentService.getContentById(contentId);
            if (content == null) {
                System.out.println("Content not found: " + contentId);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Found content: " + content.getTitle() + " (" + content.getFileName() + ")");
            
            // Extract text using the text extraction service
            Map<String, Object> extractedContent = textExtractionService.extractTextFromContent(content, options);
            
            System.out.println("Text extraction completed for: " + content.getTitle());
            return ResponseEntity.ok(extractedContent);
            
        } catch (Exception e) {
            System.err.println("Error extracting text for content " + contentId + ": " + e.getMessage());
            e.printStackTrace();
            
            // Return fallback response
            Map<String, Object> fallbackResponse = new HashMap<>();
            fallbackResponse.put("title", "Content");
            fallbackResponse.put("text", "Text extraction is currently unavailable. Please try again later.");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("contentType", "unknown");
            metadata.put("fileName", "unknown");
            metadata.put("extractionMethod", "fallback");
            metadata.put("confidence", 0.1);
            metadata.put("wordCount", 12);
            metadata.put("readingTime", 1);
            fallbackResponse.put("metadata", metadata);
            
            Map<String, Object> accessibility = new HashMap<>();
            accessibility.put("hasImages", false);
            accessibility.put("hasStructure", false);
            accessibility.put("hasFormats", false);
            accessibility.put("imageDescriptions", new String[0]);
            fallbackResponse.put("accessibility", accessibility);
            
            return ResponseEntity.ok(fallbackResponse);
        }
    }

    /**
     * OCR text extraction from images
     */
    @PostMapping("/ocr/extract")
    public ResponseEntity<Map<String, Object>> extractFromImage(@RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("imageUrl");
            System.out.println("OCR extraction requested for image: " + imageUrl);
            
            // For now, return a placeholder response
            Map<String, Object> ocrResponse = new HashMap<>();
            ocrResponse.put("title", "Image Content");
            ocrResponse.put("text", "OCR text extraction is not yet implemented. This feature will be available soon.");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("contentType", "image");
            metadata.put("fileName", "image");
            metadata.put("extractionMethod", "ocr-placeholder");
            metadata.put("confidence", 0.5);
            metadata.put("wordCount", 15);
            metadata.put("readingTime", 1);
            ocrResponse.put("metadata", metadata);
            
            Map<String, Object> accessibility = new HashMap<>();
            accessibility.put("hasImages", true);
            accessibility.put("hasStructure", false);
            accessibility.put("hasFormats", false);
            accessibility.put("imageDescriptions", new String[]{"Image content analyzed via OCR"});
            ocrResponse.put("accessibility", accessibility);
            
            return ResponseEntity.ok(ocrResponse);
            
        } catch (Exception e) {
            System.err.println("Error in OCR extraction: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
