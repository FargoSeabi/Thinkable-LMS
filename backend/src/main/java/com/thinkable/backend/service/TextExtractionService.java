package com.thinkable.backend.service;

import com.thinkable.backend.entity.LearningContent;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class TextExtractionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TextExtractionService.class);
    private final GoogleCloudStorageService gcsService;
    
    @Autowired
    public TextExtractionService(GoogleCloudStorageService gcsService) {
        this.gcsService = gcsService;
    }
    
    
    /**
     * Extract text from various document formats
     */
    public String extractTextFromFile(String filePath, String fileName) {
        try {
            // Construct full path - files are stored in uploads/content (relative to backend directory)
            Path fullPath = Paths.get("uploads/content/" + filePath);
            
            logger.info("Looking for file at: {}", fullPath.toAbsolutePath());
            
            if (!Files.exists(fullPath)) {
                logger.error("File not found: {}", fullPath.toAbsolutePath());
                // Try alternative path
                Path altPath = Paths.get("uploads/" + filePath);
                logger.info("Trying alternative path: {}", altPath.toAbsolutePath());
                if (Files.exists(altPath)) {
                    fullPath = altPath;
                    logger.info("Found file at alternative path: {}", fullPath.toAbsolutePath());
                } else {
                    return "Error: File not found.";
                }
            }
            
            // Get file extension to determine extraction method
            String extension = getFileExtension(fileName).toLowerCase();
            
            switch (extension) {
                case "pdf":
                    return extractTextFromPDF(fullPath.toFile());
                case "txt":
                    return extractTextFromTXT(fullPath);
                case "docx":
                    return extractTextFromDOCX(fullPath);
                default:
                    return String.format("Text extraction not supported for %s files. " +
                            "Supported formats: PDF, TXT, DOCX", extension.toUpperCase());
            }
            
        } catch (Exception e) {
            logger.error("Error extracting text from file: {}", filePath, e);
            return "Error extracting text: " + e.getMessage();
        }
    }
    
    /**
     * Extract text from PDF files using PDFBox
     */
    private String extractTextFromPDF(File pdfFile) throws IOException {
        logger.info("Starting PDF text extraction from: {}", pdfFile.getAbsolutePath());
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            
            // Configure text extraction settings
            textStripper.setSortByPosition(true);
            textStripper.setStartPage(1);
            textStripper.setEndPage(document.getNumberOfPages());
            
            logger.info("PDF has {} pages", document.getNumberOfPages());
            
            String extractedText = textStripper.getText(document);
            
            logger.info("Raw extracted text length: {} characters", extractedText != null ? extractedText.length() : 0);
            if (extractedText != null && extractedText.length() > 0) {
                logger.info("First 200 characters of extracted text: {}", 
                    extractedText.length() > 200 ? extractedText.substring(0, 200) + "..." : extractedText);
            }
            
            // Clean up the text
            String cleanedText = cleanExtractedText(extractedText, document.getNumberOfPages());
            logger.info("Cleaned text length: {} characters", cleanedText != null ? cleanedText.length() : 0);
            
            return cleanedText;
            
        } catch (IOException e) {
            logger.error("Error extracting text from PDF: {}", pdfFile.getAbsolutePath(), e);
            throw new IOException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract text from plain text files
     */
    private String extractTextFromTXT(Path filePath) throws IOException {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            logger.error("Error reading text file: {}", filePath.toAbsolutePath(), e);
            throw new IOException("Failed to read text file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract text from DOCX files (placeholder - would need Apache POI)
     */
    private String extractTextFromDOCX(Path filePath) {
        return "DOCX text extraction would require Apache POI library. " +
                "Currently not implemented. Please convert to PDF or use the document viewer.";
    }
    
    /**
     * Clean up extracted text
     */
    private String cleanExtractedText(String text, int pageCount) {
        if (text == null || text.trim().isEmpty()) {
            return "This PDF appears to be image-based or scanned. Text extraction from image-based PDFs requires OCR (Optical Character Recognition) which is currently not available. Please contact your instructor for an accessible text version of this document.";
        }
        
        // Check if extracted text is mostly whitespace (indicating a scanned/image PDF)
        String trimmedText = text.trim();
        if (trimmedText.length() < 50 || trimmedText.replaceAll("\\s", "").length() < 10) {
            return "This PDF appears to be image-based or scanned. The document contains " + pageCount + " pages but only minimal extractable text was found. For full accessibility, please request a text-based version of this document from your instructor.";
        }
        
        // Clean up common PDF extraction artifacts
        return trimmedText
                // Remove excessive whitespace
                .replaceAll("\\s{3,}", "\n\n")
                // Remove excessive line breaks
                .replaceAll("\\n{4,}", "\n\n\n")
                // Clean up common PDF artifacts
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                // Trim whitespace
                .trim();
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    /**
     * Get text summary for preview
     */
    public String getTextSummary(String fullText, int maxLength) {
        if (fullText == null || fullText.isEmpty()) {
            return fullText;
        }
        
        if (fullText.length() <= maxLength) {
            return fullText;
        }
        
        // Try to break at a word boundary
        String truncated = fullText.substring(0, maxLength);
        int lastSpace = truncated.lastIndexOf(' ');
        
        if (lastSpace > maxLength - 100) { // Only break at word if it's reasonably close
            truncated = truncated.substring(0, lastSpace);
        }
        
        return truncated + "...";
    }
    
    /**
     * Check if file type supports text extraction
     */
    public boolean isTextExtractionSupported(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals("pdf") || extension.equals("txt") || extension.equals("docx");
    }
    
    /**
     * Extract text from LearningContent with options for the frontend API
     */
    public Map<String, Object> extractTextFromContent(LearningContent content, Map<String, Object> options) {
        try {
            logger.info("Extracting text from content: {} ({})", content.getTitle(), content.getFileName());
            
            // Extract the raw text - check if file is stored in Cloudinary or locally
            String extractedText;
            if (content.getCloudinarySecureUrl() != null && !content.getCloudinarySecureUrl().isEmpty()) {
                logger.info("Extracting text from Cloudinary file: {}", content.getCloudinarySecureUrl());
                extractedText = extractTextFromCloudinaryFile(content.getCloudinarySecureUrl(), content.getFileName());
            } else {
                logger.info("Extracting text from local file: {}", content.getFilePath());
                extractedText = extractTextFromFile(content.getFilePath(), content.getFileName());
            }
            
            // Build response matching frontend expectations
            Map<String, Object> response = new HashMap<>();
            response.put("title", content.getTitle());
            response.put("text", extractedText);
            
            // Metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("contentType", content.getContentType());
            metadata.put("fileName", content.getFileName());
            metadata.put("extractionMethod", isTextExtractionSupported(content.getFileName()) ? "direct" : "fallback");
            metadata.put("confidence", isTextExtractionSupported(content.getFileName()) ? 0.9 : 0.1);
            metadata.put("wordCount", countWords(extractedText));
            metadata.put("readingTime", calculateReadingTime(extractedText));
            response.put("metadata", metadata);
            
            // Accessibility info
            Map<String, Object> accessibility = new HashMap<>();
            accessibility.put("hasImages", false); // Would need image detection
            accessibility.put("hasStructure", extractedText.contains("\n\n")); // Basic structure detection
            accessibility.put("hasFormats", false); // Would need format detection
            accessibility.put("imageDescriptions", new String[0]);
            response.put("accessibility", accessibility);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error extracting text from content {}: {}", content.getId(), e.getMessage(), e);
            
            // Return fallback response
            Map<String, Object> fallbackResponse = new HashMap<>();
            fallbackResponse.put("title", content.getTitle());
            fallbackResponse.put("text", "This " + content.getContentType() + " file contains content that requires text extraction. Please ensure the backend extraction service is running for full text access.\nExtracted via fallback\nConfidence: 10%");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("contentType", content.getContentType());
            metadata.put("fileName", content.getFileName());
            metadata.put("extractionMethod", "fallback");
            metadata.put("confidence", 0.1);
            metadata.put("wordCount", 25);
            metadata.put("readingTime", 1);
            fallbackResponse.put("metadata", metadata);
            
            Map<String, Object> accessibility = new HashMap<>();
            accessibility.put("hasImages", false);
            accessibility.put("hasStructure", false);
            accessibility.put("hasFormats", false);
            accessibility.put("imageDescriptions", new String[0]);
            fallbackResponse.put("accessibility", accessibility);
            
            return fallbackResponse;
        }
    }
    
    /**
     * Count words in text
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
    
    /**
     * Calculate reading time in minutes (assuming 200 words per minute)
     */
    private int calculateReadingTime(String text) {
        int wordCount = countWords(text);
        return Math.max(1, (int) Math.ceil(wordCount / 200.0));
    }
    
    /**
     * Extract text from a file stored in GCS (previously Cloudinary)
     */
    private String extractTextFromCloudinaryFile(String cloudinaryUrl, String fileName) {
        try {
            // Check if this is a GCS URL and generate signed URL if needed
            String downloadUrl = cloudinaryUrl;
            if (cloudinaryUrl.contains("storage.googleapis.com")) {
                logger.info("Detected GCS file, generating signed URL for: {}", cloudinaryUrl);
                
                // Extract object name from GCS media link
                // Format: https://storage.googleapis.com/download/storage/v1/b/bucket-name/o/object-name?...
                String objectName = extractGCSObjectName(cloudinaryUrl);
                if (objectName != null) {
                    downloadUrl = gcsService.generateSignedUrl(objectName, 60);
                    logger.info("Generated GCS signed URL for text extraction");
                } else {
                    logger.warn("Could not extract object name from GCS URL: {}", cloudinaryUrl);
                }
            }
            
            logger.info("Downloading file for text extraction from: {}", downloadUrl.substring(0, Math.min(100, downloadUrl.length())) + "...");
            
            // Download the file
            URL url = new URL(downloadUrl);
            URLConnection connection = url.openConnection();
            
            // Set user agent to avoid 401 errors
            connection.setRequestProperty("User-Agent", "ThinkAble-TextExtractor/1.0");
            
            try (InputStream inputStream = connection.getInputStream()) {
                // Get file extension to determine extraction method
                String extension = getFileExtension(fileName).toLowerCase();
                
                switch (extension) {
                    case "pdf":
                        return extractTextFromPDFStream(inputStream);
                    case "txt":
                        return extractTextFromTXTStream(inputStream);
                    case "docx":
                        return extractTextFromDOCXStream(inputStream);
                    default:
                        return String.format("Text extraction not supported for %s files. " +
                                "Supported formats: PDF, TXT, DOCX", extension.toUpperCase());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error extracting text from Cloudinary file: {}", cloudinaryUrl, e);
            return "Error extracting text from Cloudinary file: " + e.getMessage();
        }
    }
    
    /**
     * Extract text from PDF using InputStream
     */
    private String extractTextFromPDFStream(InputStream inputStream) throws IOException {
        logger.info("Starting PDF text extraction from stream");
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            
            // Configure text extraction settings
            textStripper.setSortByPosition(true);
            textStripper.setStartPage(1);
            textStripper.setEndPage(document.getNumberOfPages());
            
            logger.info("PDF has {} pages", document.getNumberOfPages());
            
            String extractedText = textStripper.getText(document);
            
            logger.info("Raw extracted text length: {} characters", extractedText != null ? extractedText.length() : 0);
            if (extractedText != null && extractedText.length() > 0) {
                logger.info("First 200 characters of extracted text: {}", 
                    extractedText.length() > 200 ? extractedText.substring(0, 200) + "..." : extractedText);
            }
            
            // Clean up the text
            String cleanedText = cleanExtractedText(extractedText, document.getNumberOfPages());
            logger.info("Cleaned text length: {} characters", cleanedText != null ? cleanedText.length() : 0);
            
            return cleanedText;
            
        } catch (IOException e) {
            logger.error("Error extracting text from PDF stream: {}", e.getMessage(), e);
            throw new IOException("Failed to extract text from PDF stream: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract text from TXT using InputStream
     */
    private String extractTextFromTXTStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            logger.error("Error reading text from stream: {}", e.getMessage(), e);
            throw new IOException("Failed to read text from stream: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract text from DOCX using InputStream (placeholder)
     */
    private String extractTextFromDOCXStream(InputStream inputStream) {
        return "DOCX text extraction would require Apache POI library. " +
                "Currently not implemented. Please convert to PDF or use the document viewer.";
    }
    
    /**
     * Extract GCS object name from media link URL
     * Format: https://storage.googleapis.com/download/storage/v1/b/bucket-name/o/object-name?...
     */
    private String extractGCSObjectName(String gcsUrl) {
        try {
            if (gcsUrl.contains("/o/")) {
                String[] parts = gcsUrl.split("/o/");
                if (parts.length > 1) {
                    String objectPart = parts[1];
                    // Remove query parameters
                    int queryIndex = objectPart.indexOf('?');
                    if (queryIndex > 0) {
                        objectPart = objectPart.substring(0, queryIndex);
                    }
                    // URL decode the object name
                    return java.net.URLDecoder.decode(objectPart, "UTF-8");
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting object name from GCS URL: {}", gcsUrl, e);
        }
        return null;
    }
}
