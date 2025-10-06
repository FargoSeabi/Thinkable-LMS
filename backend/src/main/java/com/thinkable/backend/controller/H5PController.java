package com.thinkable.backend.controller;

import com.thinkable.backend.service.H5PExtractionService;
import com.thinkable.backend.service.GoogleCloudStorageService;
import com.thinkable.backend.entity.LearningContent;
import com.thinkable.backend.repository.LearningContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * Controller for serving H5P content files and player
 * Handles H5P content rendering following H5P best practices
 */
@RestController
@RequestMapping("/api/h5p")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class H5PController {

    private static final Logger logger = LoggerFactory.getLogger(H5PController.class);

    @Autowired
    private LearningContentRepository contentRepository;

    @Autowired
    private H5PExtractionService h5pExtractionService;

    @Autowired
    private GoogleCloudStorageService gcsService;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Serve H5P content files (h5p.json, content.json, media files, etc.)
     * This endpoint serves extracted H5P files from GCS with proper CORS headers
     * Supports H5P Standalone file structure expectations
     */
    @GetMapping("/content/{contentId}/**")
    public ResponseEntity<?> serveH5PFile(
            @PathVariable String contentId,
            HttpServletRequest request) {

        try {
            // Extract the file path from the request URL
            String requestURI = request.getRequestURI();
            String filePath = requestURI.substring(requestURI.indexOf("/content/" + contentId + "/") + ("/content/" + contentId + "/").length());

            logger.info("H5P Standalone request - Content: {}, File: {}", contentId, filePath);

            // Get the content from database
            Optional<LearningContent> contentOpt = contentRepository.findById(Long.parseLong(contentId));
            if (contentOpt.isEmpty()) {
                logger.warn("Content not found: {}", contentId);
                return ResponseEntity.notFound().build();
            }

            LearningContent content = contentOpt.get();
            if (!"interactive".equals(content.getContentType()) || content.getH5pContentId() == null) {
                logger.warn("Content {} is not H5P interactive content", contentId);
                return ResponseEntity.badRequest()
                    .body("{\"error\":\"Content is not H5P interactive content\"}");
            }

            // Extract the base UUID from h5pContentId for GCS path lookup
            String extractionId = extractH5PExtractionId(content.getH5pContentId());
            String gcsObjectName = String.format("h5p-extracted/%s/%s", extractionId, filePath);

            logger.debug("Looking for file in GCS: {}", gcsObjectName);

            // Get signed URL from GCS
            String signedUrl = gcsService.generateSignedUrl(gcsObjectName, 60);
            if (signedUrl == null) {
                logger.warn("File not found in GCS: {} (extraction ID: {})", gcsObjectName, extractionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"File not found: " + filePath + "\"}");
            }

            // Proxy the file from GCS with proper headers for H5P Standalone
            return proxyFileFromGCS(signedUrl, filePath);

        } catch (NumberFormatException e) {
            logger.warn("Invalid content ID format: {}", contentId);
            return ResponseEntity.badRequest()
                .body("{\"error\":\"Invalid content ID\"}");
        } catch (Exception e) {
            logger.error("Error serving H5P file for content {}: {}", contentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Failed to serve H5P file: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Serve H5P player HTML for rendering content
     * This creates a complete H5P player page that can render the interactive content
     */
    @GetMapping("/player/{contentId:[0-9]+}")
    public ResponseEntity<String> serveH5PPlayer(@PathVariable String contentId) {
        try {
            logger.info("Serving H5P player for content: {}", contentId);

            // Get the content from database
            Optional<LearningContent> contentOpt = contentRepository.findById(Long.parseLong(contentId));
            if (contentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LearningContent content = contentOpt.get();
            if (!"interactive".equals(content.getContentType()) || content.getH5pContentId() == null) {
                return ResponseEntity.badRequest().build();
            }

            // Generate H5P player HTML
            String playerHTML = generateH5PPlayerHTML(content, contentId);

            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header("X-Frame-Options", "ALLOWALL")
                .header("Content-Security-Policy", "frame-ancestors *")
                .body(playerHTML);

        } catch (Exception e) {
            logger.error("Error serving H5P player for content {}: {}", contentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("<html><body><h2>Error loading H5P content</h2></body></html>");
        }
    }

    /**
     * Serve H5P core files (styles, scripts) that H5P Standalone expects
     * This serves static H5P framework files from CDN or embedded resources
     */
    @GetMapping({"/player/styles/**", "/player/scripts/**", "/player/*.js", "/player/*.css"})
    public ResponseEntity<?> serveH5PCoreFiles(HttpServletRequest request) {
        try {
            // Extract the file path from the request URL
            String requestURI = request.getRequestURI();
            String filePath = requestURI.substring(requestURI.indexOf("/player/") + "/player/".length());

            logger.debug("Serving H5P core file: {}", filePath);

            // Handle common H5P core files
            if ("styles/h5p.css".equals(filePath)) {
                // Return minimal H5P CSS - H5P Standalone should handle most styling
                String css = "/* H5P Core CSS - handled by H5P Standalone */\n" +
                           ".h5p-container { width: 100%; }\n" +
                           ".h5p-content { width: 100%; height: auto; }\n";
                return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("text/css"))
                    .header("Access-Control-Allow-Origin", "*")
                    .body(css);
            }

            // Handle JavaScript files
            if (filePath.endsWith(".js")) {
                // Return minimal JavaScript - H5P Standalone should handle most functionality
                String js = "// H5P Core JS - handled by H5P Standalone\n" +
                           "console.log('H5P core file: " + filePath + "');";
                return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("application/javascript"))
                    .header("Access-Control-Allow-Origin", "*")
                    .body(js);
            }

            // For other files, return a basic response to prevent errors
            logger.warn("H5P core file not found: {}", filePath);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error serving H5P core file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Proxy file from Google Cloud Storage with proper headers for H5P Standalone
     */
    private ResponseEntity<?> proxyFileFromGCS(String gcsUrl, String filePath) {
        try {
            // Make request to GCS
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> gcsResponse = restTemplate.exchange(
                gcsUrl, HttpMethod.GET, entity, byte[].class
            );

            if (gcsResponse.getStatusCode() != HttpStatus.OK) {
                logger.warn("GCS returned non-OK status: {} for file: {}", gcsResponse.getStatusCode(), filePath);
                return ResponseEntity.status(gcsResponse.getStatusCode()).build();
            }

            // Determine content type
            String contentType = getContentTypeForFile(filePath);

            // Build response with proper CORS headers for H5P Standalone
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType(contentType));
            responseHeaders.set("Access-Control-Allow-Origin", "*");
            responseHeaders.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            responseHeaders.set("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");
            responseHeaders.set("Access-Control-Max-Age", "3600");
            responseHeaders.set("Cache-Control", "public, max-age=3600");

            // Add content length if available
            if (gcsResponse.getBody() != null) {
                responseHeaders.setContentLength(gcsResponse.getBody().length);
            }

            logger.debug("Successfully served H5P file: {} (type: {}, size: {} bytes)",
                filePath, contentType, gcsResponse.getBody() != null ? gcsResponse.getBody().length : 0);

            return new ResponseEntity<>(gcsResponse.getBody(), responseHeaders, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error proxying file from GCS: {} for path: {}", e.getMessage(), filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Failed to proxy file from storage\"}");
        }
    }

    /**
     * Generate H5P player HTML with proper H5P integration
     */
    private String generateH5PPlayerHTML(LearningContent content, String contentId) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"utf-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("  <title>").append(content.getTitle()).append("</title>\n");

        html.append("  <style>\n");
        html.append("    body { margin: 0; padding: 20px; font-family: Arial, sans-serif; background: #f5f5f5; }\n");
        html.append("    .h5p-container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 8px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("    .h5p-content { width: 100%; min-height: 400px; }\n");
        html.append("    .loading { text-align: center; padding: 50px; }\n");
        html.append("    .error { text-align: center; padding: 50px; color: #d32f2f; }\n");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        html.append("  <div class=\"h5p-container\">\n");
        html.append("    <div id=\"h5p-content-").append(contentId).append("\" class=\"h5p-content\">\n");
        html.append("    </div>\n");
        html.append("  </div>\n");

        // Load H5P Standalone from local files
        html.append("  <script src=\"/h5p-standalone/main.bundle.js\"></script>\n");
        html.append("  <script>\n");
        html.append("    const contentId = '").append(contentId).append("';\n");
        html.append("    \n");
        html.append("    // H5P Standalone expects static file structure\n");
        html.append("    // We serve extracted H5P content through our API endpoints\n");
        html.append("    const h5pPath = '/api/h5p/content/' + contentId;\n");
        html.append("    \n");
        html.append("    // Initialize H5P Standalone with correct configuration\n");
        html.append("    const element = document.getElementById('h5p-content-' + contentId);\n");
        html.append("    \n");
        html.append("    // H5P Standalone configuration with local framework files\n");
        html.append("    const options = {\n");
        html.append("      h5pJsonPath: h5pPath,  // Will load h5pPath + '/h5p.json'\n");
        html.append("      contentJsonPath: h5pPath + '/content', // Will load h5pPath + '/content/content.json'\n");
        html.append("      librariesPath: h5pPath,  // Base path for libraries\n");
        html.append("      id: contentId,\n");
        html.append("      frameJs: '/h5p-standalone/frame.bundle.js',  // Local framework file\n");
        html.append("      frameCss: '/h5p-standalone/styles/h5p.css',  // Local CSS file\n");
        html.append("      displayOptions: {\n");
        html.append("        frame: false,\n");
        html.append("        export: false,\n");
        html.append("        embed: false,\n");
        html.append("        copyright: false\n");
        html.append("      }\n");
        html.append("    };\n");
        html.append("    \n");
        html.append("    console.log('Initializing H5P Standalone with options:', options);\n");
        html.append("    \n");
        html.append("    // Initialize H5P\n");
        html.append("    new H5PStandalone.H5P(element, options)\n");
        html.append("      .then((h5p) => {\n");
        html.append("        console.log('H5P content loaded successfully:', h5p);\n");
        html.append("        \n");
        html.append("        // Track successful load\n");
        html.append("        fetch('/api/student/content/' + contentId + '/interact?studentId=1', {\n");
        html.append("          method: 'POST',\n");
        html.append("          headers: { 'Content-Type': 'application/json' },\n");
        html.append("          body: JSON.stringify({\n");
        html.append("            interactionType: 'content_loaded',\n");
        html.append("            interactionData: JSON.stringify({ timestamp: new Date().toISOString() }),\n");
        html.append("            duration: 0\n");
        html.append("          })\n");
        html.append("        }).catch(console.warn);\n");
        html.append("        \n");
        html.append("        // Listen for H5P events\n");
        html.append("        if (h5p && h5p.on) {\n");
        html.append("          h5p.on('xAPI', (event) => {\n");
        html.append("            console.log('H5P xAPI Event:', event);\n");
        html.append("            \n");
        html.append("            // Send interaction data to backend\n");
        html.append("            fetch('/api/student/content/' + contentId + '/interact?studentId=1', {\n");
        html.append("              method: 'POST',\n");
        html.append("              headers: { 'Content-Type': 'application/json' },\n");
        html.append("              body: JSON.stringify({\n");
        html.append("                interactionType: event.data.statement.verb.id || 'interaction',\n");
        html.append("                interactionData: JSON.stringify(event.data),\n");
        html.append("                duration: 0\n");
        html.append("              })\n");
        html.append("            }).catch(console.warn);\n");
        html.append("          });\n");
        html.append("        }\n");
        html.append("      })\n");
        html.append("      .catch((error) => {\n");
        html.append("        console.error('Failed to initialize H5P player:', error);\n");
        html.append("        element.innerHTML = '<div class=\"error\">Failed to load interactive content: ' + error.message + '</div>';\n");
        html.append("      });\n");
        html.append("  </script>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }


    /**
     * Determine content type for H5P files
     */
    private String getContentTypeForFile(String filePath) {
        String extension = filePath.toLowerCase();
        
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
     * Extract the base UUID from h5pContentId for GCS path lookup
     * Handles formats like: "h5p-h5p-content-b64735b4-0027-4afa-83ec-4f7aaa17ad63-h5p-1757755845964"
     * Returns: "b64735b4-0027-4afa-83ec-4f7aaa17ad63"
     */
    private String extractH5PExtractionId(String h5pContentId) {
        // Pattern to match UUID format: 8-4-4-4-12 hex chars
        java.util.regex.Pattern uuidPattern = java.util.regex.Pattern.compile(
            "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
        );
        
        java.util.regex.Matcher matcher = uuidPattern.matcher(h5pContentId);
        if (matcher.find()) {
            return matcher.group();
        }
        
        // Fallback: if no UUID found, return the original ID
        logger.warn("Could not extract UUID from h5pContentId: {}", h5pContentId);
        return h5pContentId;
    }
}
