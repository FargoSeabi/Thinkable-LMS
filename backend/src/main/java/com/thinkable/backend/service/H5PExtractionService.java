package com.thinkable.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Service for extracting and managing H5P content files
 * Following H5P best practices for content structure and serving
 */
@Service
public class H5PExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(H5PExtractionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private GoogleCloudStorageService gcsService;

    /**
     * Extract H5P content and upload individual files to GCS
     * @param h5pFile The H5P ZIP file
     * @param contentId Unique content identifier
     * @return H5PExtractionResult containing extracted file information
     */
    public H5PExtractionResult extractAndUploadH5PContent(MultipartFile h5pFile, String contentId) throws IOException {
        logger.info("Starting H5P content extraction for content ID: {}", contentId);
        
        Path tempFile = null;
        try {
            // Create temporary file for ZIP processing
            tempFile = Files.createTempFile("h5p-extract-", ".zip");
            Files.write(tempFile, h5pFile.getBytes());
            
            H5PExtractionResult result = new H5PExtractionResult();
            result.setContentId(contentId);
            
            // Extract files and upload to GCS
            try (ZipFile zipFile = new ZipFile(tempFile.toFile())) {
                
                // First pass: Extract and analyze h5p.json
                JsonNode h5pJson = extractH5PJson(zipFile);
                result.setH5pJson(h5pJson);
                
                // Second pass: Extract content.json
                JsonNode contentJson = extractContentJson(zipFile);
                result.setContentJson(contentJson);
                
                // Third pass: Extract and upload all files
                Map<String, String> uploadedFiles = extractAndUploadAllFiles(zipFile, contentId);
                result.setUploadedFiles(uploadedFiles);
                
                // Generate H5P URLs
                result.setH5pJsonUrl(uploadedFiles.get("h5p.json"));
                result.setContentJsonUrl(uploadedFiles.get("content/content.json"));
                
                logger.info("H5P extraction completed successfully. Extracted {} files", uploadedFiles.size());
                return result;
            }
            
        } finally {
            // Clean up temporary file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    logger.warn("Failed to delete temporary file: {}", tempFile, e);
                }
            }
        }
    }

    /**
     * Extract h5p.json from ZIP file
     */
    private JsonNode extractH5PJson(ZipFile zipFile) throws IOException {
        ZipEntry h5pEntry = zipFile.getEntry("h5p.json");
        if (h5pEntry == null) {
            throw new IOException("h5p.json not found in H5P package");
        }
        
        try (InputStream inputStream = zipFile.getInputStream(h5pEntry)) {
            String content = new String(inputStream.readAllBytes(), "UTF-8");
            return objectMapper.readTree(content);
        }
    }

    /**
     * Extract content/content.json from ZIP file
     */
    private JsonNode extractContentJson(ZipFile zipFile) throws IOException {
        ZipEntry contentEntry = zipFile.getEntry("content/content.json");
        if (contentEntry == null) {
            throw new IOException("content/content.json not found in H5P package");
        }
        
        try (InputStream inputStream = zipFile.getInputStream(contentEntry)) {
            String content = new String(inputStream.readAllBytes(), "UTF-8");
            return objectMapper.readTree(content);
        }
    }

    /**
     * Extract all files from H5P ZIP and upload to GCS
     */
    private Map<String, String> extractAndUploadAllFiles(ZipFile zipFile, String contentId) throws IOException {
        Map<String, String> uploadedFiles = new HashMap<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            
            // Skip directories
            if (entry.isDirectory()) {
                continue;
            }
            
            String fileName = entry.getName();
            logger.debug("Extracting file: {}", fileName);
            
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                // Create GCS object name with proper H5P structure
                String gcsObjectName = String.format("h5p-extracted/%s/%s", contentId, fileName);
                
                // Upload to GCS
                String uploadedUrl = gcsService.uploadH5PFile(
                    inputStream.readAllBytes(),
                    gcsObjectName,
                    getContentType(fileName)
                );
                
                uploadedFiles.put(fileName, uploadedUrl);
                logger.debug("Uploaded {} to GCS: {}", fileName, gcsObjectName);
            }
        }
        
        return uploadedFiles;
    }

    /**
     * Determine content type based on file extension
     */
    private String getContentType(String fileName) {
        String extension = fileName.toLowerCase();
        
        if (extension.endsWith(".json")) return "application/json";
        if (extension.endsWith(".js")) return "application/javascript";
        if (extension.endsWith(".css")) return "text/css";
        if (extension.endsWith(".html")) return "text/html";
        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) return "image/jpeg";
        if (extension.endsWith(".png")) return "image/png";
        if (extension.endsWith(".gif")) return "image/gif";
        if (extension.endsWith(".svg")) return "image/svg+xml";
        if (extension.endsWith(".mp4")) return "video/mp4";
        if (extension.endsWith(".webm")) return "video/webm";
        if (extension.endsWith(".mp3")) return "audio/mpeg";
        if (extension.endsWith(".wav")) return "audio/wav";
        if (extension.endsWith(".ogg")) return "audio/ogg";
        
        return "application/octet-stream";
    }

    /**
     * Generate H5P player HTML for rendering content
     */
    public String generateH5PPlayerHTML(String contentId, H5PExtractionResult extractionResult) {
        try {
            // Get the main library from h5p.json
            JsonNode h5pJson = extractionResult.getH5pJson();
            String mainLibrary = h5pJson.get("mainLibrary").asText();
            
            // Generate the H5P player HTML
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n");
            html.append("<head>\n");
            html.append("  <meta charset=\"utf-8\">\n");
            html.append("  <title>").append(h5pJson.get("title").asText("H5P Content")).append("</title>\n");
            html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            
            // H5P Core CSS and JS (you'll need to serve these from your CDN or local)
            html.append("  <link rel=\"stylesheet\" type=\"text/css\" href=\"https://h5p.org/sites/all/modules/h5p/library/styles/h5p.css\">\n");
            html.append("  <script type=\"text/javascript\" src=\"https://h5p.org/sites/all/modules/h5p/library/js/h5p.js\"></script>\n");
            
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("  <div class=\"h5p-content\" data-content-id=\"").append(contentId).append("\"></div>\n");
            
            // H5P Integration script
            html.append("  <script type=\"text/javascript\">\n");
            html.append("    H5P.getContentPath = function(contentId) {\n");
            html.append("      return '/api/h5p/content/' + contentId + '/';\n");
            html.append("    };\n");
            html.append("    \n");
            html.append("    // Initialize H5P content\n");
            html.append("    H5P.init(document.querySelector('.h5p-content'));\n");
            html.append("  </script>\n");
            
            html.append("</body>\n");
            html.append("</html>");
            
            return html.toString();
            
        } catch (Exception e) {
            logger.error("Failed to generate H5P player HTML for content: {}", contentId, e);
            return generateErrorHTML("Failed to load H5P content");
        }
    }

    private String generateErrorHTML(String message) {
        return String.format(
            "<!DOCTYPE html><html><head><title>H5P Error</title></head>" +
            "<body><div style=\"text-align:center;padding:50px;\">" +
            "<h2>Content Unavailable</h2><p>%s</p></div></body></html>",
            message
        );
    }

    /**
     * Result class for H5P extraction
     */
    public static class H5PExtractionResult {
        private String contentId;
        private JsonNode h5pJson;
        private JsonNode contentJson;
        private Map<String, String> uploadedFiles;
        private String h5pJsonUrl;
        private String contentJsonUrl;

        // Getters and setters
        public String getContentId() { return contentId; }
        public void setContentId(String contentId) { this.contentId = contentId; }

        public JsonNode getH5pJson() { return h5pJson; }
        public void setH5pJson(JsonNode h5pJson) { this.h5pJson = h5pJson; }

        public JsonNode getContentJson() { return contentJson; }
        public void setContentJson(JsonNode contentJson) { this.contentJson = contentJson; }

        public Map<String, String> getUploadedFiles() { return uploadedFiles; }
        public void setUploadedFiles(Map<String, String> uploadedFiles) { this.uploadedFiles = uploadedFiles; }

        public String getH5pJsonUrl() { return h5pJsonUrl; }
        public void setH5pJsonUrl(String h5pJsonUrl) { this.h5pJsonUrl = h5pJsonUrl; }

        public String getContentJsonUrl() { return contentJsonUrl; }
        public void setContentJsonUrl(String contentJsonUrl) { this.contentJsonUrl = contentJsonUrl; }
    }
}
