package com.thinkable.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PDFTextExtractionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PDFTextExtractionService.class);
    
    @Value("${app.uploads.dir}")
    private String uploadBaseDir;
    
    // In-memory cache for extracted text (in production, use Redis or database)
    private final Map<String, Map<Integer, String>> textCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> pageCountCache = new ConcurrentHashMap<>();
    
    /**
     * Extract text from a specific page of a PDF
     * @param fileName PDF file name
     * @param pageNumber Page number (1-based)
     * @return Extracted text content
     */
    public PDFPageText extractPageText(String fileName, int pageNumber) {
        try {
            String cacheKey = fileName + "_page_" + pageNumber;
            
            // Check cache first
            if (textCache.containsKey(fileName) && textCache.get(fileName).containsKey(pageNumber)) {
                String cachedText = textCache.get(fileName).get(pageNumber);
                int totalPages = pageCountCache.getOrDefault(fileName, 1);
                logger.debug("Retrieved cached text for {} page {}", fileName, pageNumber);
                return new PDFPageText(cachedText, pageNumber, totalPages, true);
            }
            
            // Extract text from PDF
            // Ensure fileName has .pdf extension
            String pdfFileName = fileName.endsWith(".pdf") ? fileName : fileName + ".pdf";
            File pdfFile = new File(uploadBaseDir + "/books/" + pdfFileName);
            if (!pdfFile.exists()) {
                logger.error("PDF file not found: {}", pdfFile.getAbsolutePath());
                return new PDFPageText("PDF file not found", pageNumber, 1, false);
            }
            
            try (PDDocument document = PDDocument.load(pdfFile)) {
                int totalPages = document.getNumberOfPages();
                
                // Validate page number
                if (pageNumber < 1 || pageNumber > totalPages) {
                    logger.warn("Invalid page number {} for PDF {} (total pages: {})", pageNumber, fileName, totalPages);
                    return new PDFPageText("Invalid page number", pageNumber, totalPages, false);
                }
                
                PDFTextStripper textStripper = new PDFTextStripper();
                textStripper.setStartPage(pageNumber);
                textStripper.setEndPage(pageNumber);
                
                String extractedText = textStripper.getText(document);
                
                // Clean up the extracted text
                String cleanedText = cleanExtractedText(extractedText);
                
                // Cache the results
                textCache.computeIfAbsent(fileName, k -> new ConcurrentHashMap<>()).put(pageNumber, cleanedText);
                pageCountCache.put(fileName, totalPages);
                
                logger.info("Successfully extracted text from {} page {} ({} characters)", fileName, pageNumber, cleanedText.length());
                return new PDFPageText(cleanedText, pageNumber, totalPages, true);
                
            } catch (IOException e) {
                logger.error("Error reading PDF file {}: {}", fileName, e.getMessage());
                return new PDFPageText("Error reading PDF: " + e.getMessage(), pageNumber, 1, false);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error extracting text from {} page {}: {}", fileName, pageNumber, e.getMessage());
            return new PDFPageText("Unexpected error: " + e.getMessage(), pageNumber, 1, false);
        }
    }
    
    /**
     * Extract text from all pages of a PDF
     * @param fileName PDF file name
     * @return Map of page numbers to extracted text
     */
    public PDFFullText extractFullText(String fileName) {
        try {
            // Ensure fileName has .pdf extension
            String pdfFileName = fileName.endsWith(".pdf") ? fileName : fileName + ".pdf";
            File pdfFile = new File(uploadBaseDir + "/books/" + pdfFileName);
            if (!pdfFile.exists()) {
                logger.error("PDF file not found: {}", pdfFile.getAbsolutePath());
                return new PDFFullText(new HashMap<>(), 0, false, "PDF file not found");
            }
            
            try (PDDocument document = PDDocument.load(pdfFile)) {
                int totalPages = document.getNumberOfPages();
                Map<Integer, String> allPagesText = new HashMap<>();
                
                PDFTextStripper textStripper = new PDFTextStripper();
                
                for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                    // Check cache first
                    if (textCache.containsKey(fileName) && textCache.get(fileName).containsKey(pageNum)) {
                        allPagesText.put(pageNum, textCache.get(fileName).get(pageNum));
                        continue;
                    }
                    
                    textStripper.setStartPage(pageNum);
                    textStripper.setEndPage(pageNum);
                    
                    String extractedText = textStripper.getText(document);
                    String cleanedText = cleanExtractedText(extractedText);
                    
                    allPagesText.put(pageNum, cleanedText);
                    
                    // Cache the result
                    textCache.computeIfAbsent(fileName, k -> new ConcurrentHashMap<>()).put(pageNum, cleanedText);
                }
                
                pageCountCache.put(fileName, totalPages);
                
                logger.info("Successfully extracted full text from {} ({} pages)", fileName, totalPages);
                return new PDFFullText(allPagesText, totalPages, true, null);
                
            } catch (IOException e) {
                logger.error("Error reading PDF file {}: {}", fileName, e.getMessage());
                return new PDFFullText(new HashMap<>(), 0, false, "Error reading PDF: " + e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error extracting full text from {}: {}", fileName, e.getMessage());
            return new PDFFullText(new HashMap<>(), 0, false, "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Get basic PDF metadata
     * @param fileName PDF file name
     * @return PDF metadata
     */
    public PDFMetadata getPDFMetadata(String fileName) {
        try {
            // Ensure fileName has .pdf extension
            String pdfFileName = fileName.endsWith(".pdf") ? fileName : fileName + ".pdf";
            File pdfFile = new File(uploadBaseDir + "/books/" + pdfFileName);
            if (!pdfFile.exists()) {
                return new PDFMetadata(fileName, 0, false, "File not found");
            }
            
            // Check cache first
            if (pageCountCache.containsKey(fileName)) {
                int totalPages = pageCountCache.get(fileName);
                return new PDFMetadata(fileName, totalPages, true, null);
            }
            
            try (PDDocument document = PDDocument.load(pdfFile)) {
                int totalPages = document.getNumberOfPages();
                pageCountCache.put(fileName, totalPages);
                
                return new PDFMetadata(fileName, totalPages, true, null);
                
            } catch (IOException e) {
                logger.error("Error reading PDF metadata for {}: {}", fileName, e.getMessage());
                return new PDFMetadata(fileName, 0, false, "Error reading PDF: " + e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error getting PDF metadata for {}: {}", fileName, e.getMessage());
            return new PDFMetadata(fileName, 0, false, "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Clear cached text for a specific PDF
     * @param fileName PDF file name
     */
    public void clearCache(String fileName) {
        textCache.remove(fileName);
        pageCountCache.remove(fileName);
        logger.info("Cleared cache for PDF: {}", fileName);
    }
    
    /**
     * Clear all cached text
     */
    public void clearAllCache() {
        textCache.clear();
        pageCountCache.clear();
        logger.info("Cleared all PDF text cache");
    }
    
    /**
     * Clean and format extracted text for better readability
     * @param rawText Raw extracted text
     * @return Cleaned text
     */
    private String cleanExtractedText(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return "";
        }
        
        return rawText
            // Remove excessive whitespace
            .replaceAll("\\s+", " ")
            // Remove page breaks and form feeds
            .replaceAll("[\\f\\r]", "")
            // Normalize line breaks
            .replaceAll("\\n\\s*\\n", "\n\n")
            // Trim whitespace
            .trim();
    }
    
    // Data classes for returning structured results
    public static class PDFPageText {
        private final String text;
        private final int pageNumber;
        private final int totalPages;
        private final boolean success;
        private final String error;
        
        public PDFPageText(String text, int pageNumber, int totalPages, boolean success) {
            this.text = text;
            this.pageNumber = pageNumber;
            this.totalPages = totalPages;
            this.success = success;
            this.error = success ? null : text; // If not successful, text contains error message
        }
        
        // Getters
        public String getText() { return text; }
        public int getPageNumber() { return pageNumber; }
        public int getTotalPages() { return totalPages; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
    }
    
    public static class PDFFullText {
        private final Map<Integer, String> pagesText;
        private final int totalPages;
        private final boolean success;
        private final String error;
        
        public PDFFullText(Map<Integer, String> pagesText, int totalPages, boolean success, String error) {
            this.pagesText = pagesText;
            this.totalPages = totalPages;
            this.success = success;
            this.error = error;
        }
        
        // Getters
        public Map<Integer, String> getPagesText() { return pagesText; }
        public int getTotalPages() { return totalPages; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
    }
    
    public static class PDFMetadata {
        private final String fileName;
        private final int totalPages;
        private final boolean success;
        private final String error;
        
        public PDFMetadata(String fileName, int totalPages, boolean success, String error) {
            this.fileName = fileName;
            this.totalPages = totalPages;
            this.success = success;
            this.error = error;
        }
        
        // Getters
        public String getFileName() { return fileName; }
        public int getTotalPages() { return totalPages; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
    }
}
