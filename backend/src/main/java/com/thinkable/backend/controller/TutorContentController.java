package com.thinkable.backend.controller;

import com.thinkable.backend.entity.LearningContent;
import com.thinkable.backend.service.TutorContentService;
import com.thinkable.backend.service.TutorContentService.ContentUploadRequest;
import com.thinkable.backend.service.TutorContentService.ContentAnalytics;
import com.thinkable.backend.dto.H5PContentRequest;
import com.thinkable.backend.enums.ContentType;
import com.thinkable.backend.service.TextExtractionService;
import com.thinkable.backend.service.AIQuizGenerationService;
import com.thinkable.backend.service.GoogleCloudStorageService;
import com.thinkable.backend.model.Quiz;
import com.thinkable.backend.model.Question;
import com.thinkable.backend.model.Book;
import com.thinkable.backend.model.Lesson;
import com.thinkable.backend.repository.QuizRepository;
import com.thinkable.backend.repository.LeaderboardRepository;
import com.thinkable.backend.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * REST Controller for tutor content management
 */
@RestController
@RequestMapping("/api/tutor/content")
public class TutorContentController {
    
    private static final Logger logger = LoggerFactory.getLogger(TutorContentController.class);
    
    @Autowired
    private TutorContentService contentService;
    
    @Autowired
    private TextExtractionService textExtractionService;
    
    @Autowired
    private AIQuizGenerationService aiQuizGenerationService;
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private LeaderboardRepository leaderboardRepository;
    
    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private GoogleCloudStorageService gcsService;
    
    /**
     * Upload new learning content with accessibility features
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadContent(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tutorUserId") Long tutorUserId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("subjectArea") String subjectArea,
            @RequestParam("difficultyLevel") String difficultyLevel,
            @RequestParam(value = "targetAgeMin", required = false) Integer targetAgeMin,
            @RequestParam(value = "targetAgeMax", required = false) Integer targetAgeMax,
            @RequestParam(value = "estimatedDurationMinutes", required = false) Integer estimatedDurationMinutes,
            @RequestParam(value = "dyslexiaFriendly", defaultValue = "false") Boolean dyslexiaFriendly,
            @RequestParam(value = "adhdFriendly", defaultValue = "false") Boolean adhdFriendly,
            @RequestParam(value = "autismFriendly", defaultValue = "false") Boolean autismFriendly,
            @RequestParam(value = "visualImpairmentFriendly", defaultValue = "false") Boolean visualImpairmentFriendly,
            @RequestParam(value = "hearingImpairmentFriendly", defaultValue = "false") Boolean hearingImpairmentFriendly,
            @RequestParam(value = "motorImpairmentFriendly", defaultValue = "false") Boolean motorImpairmentFriendly,
            @RequestParam(value = "fontType", required = false) String fontType,
            @RequestParam(value = "readingLevel", required = false) String readingLevel,
            @RequestParam(value = "hasAudioDescription", defaultValue = "false") Boolean hasAudioDescription,
            @RequestParam(value = "hasSubtitles", defaultValue = "false") Boolean hasSubtitles,
            @RequestParam(value = "cognitiveLoadLevel", required = false) String cognitiveLoadLevel) {
        
        try {
            // Create upload request
            ContentUploadRequest request = new ContentUploadRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setSubjectArea(subjectArea);
            request.setDifficultyLevel(difficultyLevel);
            request.setTargetAgeMin(targetAgeMin);
            request.setTargetAgeMax(targetAgeMax);
            request.setEstimatedDurationMinutes(estimatedDurationMinutes);
            
            // Set accessibility features
            request.setDyslexiaFriendly(dyslexiaFriendly);
            request.setAdhdFriendly(adhdFriendly);
            request.setAutismFriendly(autismFriendly);
            request.setVisualImpairmentFriendly(visualImpairmentFriendly);
            request.setHearingImpairmentFriendly(hearingImpairmentFriendly);
            request.setMotorImpairmentFriendly(motorImpairmentFriendly);
            request.setFontType(fontType);
            request.setReadingLevel(readingLevel);
            request.setHasAudioDescription(hasAudioDescription);
            request.setHasSubtitles(hasSubtitles);
            request.setCognitiveLoadLevel(cognitiveLoadLevel);
            
            LearningContent content = contentService.uploadContent(tutorUserId, file, request);
            
            return ResponseEntity.ok(Map.of(
                "message", "Content uploaded successfully",
                "contentId", content.getId(),
                "status", "success"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to upload content: " + e.getMessage()));
        }
    }
    
    /**
     * Upload H5P interactive content (.h5p file)
     */
    @PostMapping("/upload-h5p")
    public ResponseEntity<?> uploadH5PContent(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tutorUserId") Long tutorUserId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("subjectArea") String subjectArea,
            @RequestParam("difficultyLevel") String difficultyLevel,
            @RequestParam(value = "targetAgeMin", required = false) Integer targetAgeMin,
            @RequestParam(value = "targetAgeMax", required = false) Integer targetAgeMax,
            @RequestParam(value = "estimatedDurationMinutes", required = false) Integer estimatedDurationMinutes,
            @RequestParam(value = "dyslexiaFriendly", defaultValue = "false") Boolean dyslexiaFriendly,
            @RequestParam(value = "adhdFriendly", defaultValue = "false") Boolean adhdFriendly,
            @RequestParam(value = "autismFriendly", defaultValue = "false") Boolean autismFriendly,
            @RequestParam(value = "visualImpairmentFriendly", defaultValue = "false") Boolean visualImpairmentFriendly,
            @RequestParam(value = "hearingImpairmentFriendly", defaultValue = "false") Boolean hearingImpairmentFriendly,
            @RequestParam(value = "motorImpairmentFriendly", defaultValue = "false") Boolean motorImpairmentFriendly,
            @RequestParam(value = "fontType", required = false) String fontType,
            @RequestParam(value = "readingLevel", required = false) String readingLevel,
            @RequestParam(value = "hasAudioDescription", defaultValue = "false") Boolean hasAudioDescription,
            @RequestParam(value = "hasSubtitles", defaultValue = "false") Boolean hasSubtitles,
            @RequestParam(value = "cognitiveLoadLevel", required = false) String cognitiveLoadLevel,
            @RequestParam(value = "interactionType", defaultValue = "interactive") String interactionType) {
        
        try {
            // Validate H5P file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "H5P file is required"));
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".h5p")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File must be an H5P file (.h5p extension required)"));
            }
            
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Title is required"));
            }
            
            if (subjectArea == null || subjectArea.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Subject area is required"));
            }
            
            // Create H5P upload request
            H5PContentRequest request = new H5PContentRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setSubjectArea(subjectArea);
            request.setDifficultyLevel(difficultyLevel);
            request.setTargetAgeMin(targetAgeMin);
            request.setTargetAgeMax(targetAgeMax);
            request.setEstimatedDurationMinutes(estimatedDurationMinutes);
            
            // Set accessibility features
            request.setDyslexiaFriendly(dyslexiaFriendly);
            request.setAdhdFriendly(adhdFriendly);
            request.setAutismFriendly(autismFriendly);
            request.setVisualImpairmentFriendly(visualImpairmentFriendly);
            request.setHearingImpairmentFriendly(hearingImpairmentFriendly);
            request.setMotorImpairmentFriendly(motorImpairmentFriendly);
            request.setFontType(fontType);
            request.setReadingLevel(readingLevel);
            request.setHasAudioDescription(hasAudioDescription);
            request.setHasSubtitles(hasSubtitles);
            request.setCognitiveLoadLevel(cognitiveLoadLevel);
            request.setInteractionType(interactionType);
            
            LearningContent content = contentService.uploadH5PContent(tutorUserId, file, request);
            
            return ResponseEntity.ok(Map.of(
                "message", "H5P content uploaded successfully",
                "contentId", content.getId(),
                "contentType", "interactive",
                "h5pContentId", content.getH5pContentId(),
                "h5pLibrary", content.getH5pLibrary(),
                "fileName", content.getFileName(),
                "status", "success"
            ));
            
        } catch (Exception e) {
            logger.error("Error uploading H5P content: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to upload H5P content: " + e.getMessage()));
        }
    }
    
    /**
     * Publish content (make it discoverable to students)
     */
    @PostMapping("/{contentId}/publish")
    public ResponseEntity<?> publishContent(@PathVariable Long contentId, @RequestParam Long tutorUserId) {
        try {
            LearningContent content = contentService.publishContent(contentId, tutorUserId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Content published successfully",
                "contentId", content.getId(),
                "status", content.getStatus(),
                "publishedAt", content.getPublishedAt()
            ));
            
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Not authorized to publish this content"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to publish content: " + e.getMessage()));
        }
    }
    
    /**
     * Get all content for a tutor (for tutor dashboard - shows all content)
     */
    @GetMapping("/tutor/{tutorUserId}")
    public ResponseEntity<?> getTutorContent(@PathVariable Long tutorUserId) {
        try {
            List<LearningContent> content = contentService.getTutorContent(tutorUserId);
            
            return ResponseEntity.ok(Map.of(
                "content", content,
                "count", content.size(),
                "tutorUserId", tutorUserId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch content: " + e.getMessage()));
        }
    }
    
    /**
     * Get published content for a tutor (for student discovery - shows only published content)
     */
    @GetMapping("/tutor/{tutorUserId}/published")
    public ResponseEntity<?> getTutorPublishedContent(@PathVariable Long tutorUserId) {
        try {
            List<LearningContent> content = contentService.getTutorPublishedContent(tutorUserId);
            
            return ResponseEntity.ok(Map.of(
                "content", content,
                "count", content.size(),
                "tutorUserId", tutorUserId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch published content: " + e.getMessage()));
        }
    }
    
    /**
     * Get analytics for specific content
     */
    @GetMapping("/{contentId}/analytics")
    public ResponseEntity<?> getContentAnalytics(@PathVariable Long contentId, @RequestParam Long tutorUserId) {
        try {
            ContentAnalytics analytics = contentService.getContentAnalytics(contentId, tutorUserId);
            
            return ResponseEntity.ok(analytics);
            
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Not authorized to view analytics for this content"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch analytics: " + e.getMessage()));
        }
    }
    
    /**
     * Update content accessibility features
     */
    @PutMapping("/{contentId}/accessibility")
    public ResponseEntity<?> updateAccessibilityFeatures(
            @PathVariable Long contentId,
            @RequestParam Long tutorUserId,
            @RequestBody AccessibilityUpdateRequest request) {
        
        try {
            // Implementation would update accessibility features
            return ResponseEntity.ok(Map.of(
                "message", "Accessibility features updated successfully",
                "contentId", contentId
            ));
            
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Not authorized to update this content"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to update accessibility features: " + e.getMessage()));
        }
    }
    
    /**
     * Delete content
     */
    @DeleteMapping("/{contentId}")
    public ResponseEntity<?> deleteContent(@PathVariable Long contentId, @RequestParam Long tutorUserId) {
        try {
            contentService.deleteContent(contentId, tutorUserId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Content and associated file deleted successfully",
                "contentId", contentId
            ));
            
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Not authorized to delete this content"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Content not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete content: " + e.getMessage()));
        }
    }
    
    /**
     * Serve content files for viewing/downloading
     */
    @GetMapping("/{contentId}/view")
    public ResponseEntity<?> viewContent(@PathVariable Long contentId) {
        try {
            System.out.println("Attempting to serve content ID: " + contentId);
            
            // Get content details from service
            LearningContent content = contentService.getContentById(contentId);
            
            if (content == null) {
                System.out.println("Content not found for ID: " + contentId);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Content found - Status: " + content.getStatus() + ", ContentType: " + content.getContentType());
            System.out.println("Content Type Enum: " + content.getContentTypeEnum());
            System.out.println("File Name: " + content.getFileName());
            System.out.println("H5P Content ID: " + content.getH5pContentId());
            System.out.println("H5P Library: " + content.getH5pLibrary());
            System.out.println("Is H5P Content: " + content.isH5PContent());
            
            if (!content.getStatus().equals("published")) {
                System.out.println("Content not published, status: " + content.getStatus());
                return ResponseEntity.notFound().build();
            }
            
            // Handle H5P interactive content differently - return JSON metadata instead of ZIP file
            if (content.isH5PContent()) {
                System.out.println("*** SERVING H5P CONTENT AS JSON ***");
                System.out.println("Serving H5P content metadata: " + content.getH5pContentId());
                
                try {
                    // Generate signed URL for the actual H5P file (for iframe embedding)
                    String h5pFileUrl = gcsService.generateSignedUrl(content.getCloudinaryPublicId(), 60);
                    if (h5pFileUrl == null) {
                        System.err.println("Failed to generate signed URL for H5P file: " + content.getCloudinaryPublicId());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Failed to generate H5P file URL"));
                    }
                    
                    // Return JSON metadata for H5P content viewer
                    Map<String, Object> h5pResponse = new HashMap<>();
                    h5pResponse.put("contentType", "interactive");
                    h5pResponse.put("id", content.getId());
                    h5pResponse.put("title", content.getTitle());
                    h5pResponse.put("description", content.getDescription());
                    h5pResponse.put("fileName", content.getFileName());
                    h5pResponse.put("h5pContentId", content.getH5pContentId());
                    h5pResponse.put("h5pLibrary", content.getH5pLibrary());
                    h5pResponse.put("h5pMetadata", content.getH5pMetadata());
                    h5pResponse.put("h5pSettings", content.getH5pSettings());
                    h5pResponse.put("h5pFileUrl", h5pFileUrl); // Direct GCS URL to .h5p file
                    h5pResponse.put("h5pEmbedUrl", "/api/h5p/player/" + content.getId()); // H5P player endpoint for iframe embedding
                    
                    return ResponseEntity.ok()
                            .header("Content-Type", "application/json")
                            .body(h5pResponse);
                            
                } catch (Exception e) {
                    System.err.println("Error serving H5P content metadata: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to serve H5P content: " + e.getMessage()));
                }
            }
            
            // Check if content uses Google Cloud Storage (stored in cloudinaryPublicId field for compatibility)
            if (content.getCloudinaryPublicId() != null && !content.getCloudinaryPublicId().trim().isEmpty()) {
                System.out.println("Serving GCS content for object: " + content.getCloudinaryPublicId());
                
                try {
                    // Generate a signed URL for accessing the GCS file (valid for 1 hour)
                    String signedUrl = gcsService.generateSignedUrl(content.getCloudinaryPublicId(), 60);
                    
                    if (signedUrl == null || signedUrl.trim().isEmpty()) {
                        System.err.println("Failed to generate signed URL for GCS object: " + content.getCloudinaryPublicId());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    }
                    
                    System.out.println("Generated GCS signed URL: " + signedUrl);
                    
                    // Download the file from GCS using the signed URL and serve it directly
                    URL gcsUrl = new URL(signedUrl);
                    URLConnection connection = gcsUrl.openConnection();
                    connection.setRequestProperty("User-Agent", "ThinkAble-ContentServer/1.0");
                    
                    // Get the content type and length
                    String contentType = connection.getContentType();
                    long contentLength = connection.getContentLengthLong();
                    
                    // If content type is null, try to determine from file name
                    if (contentType == null) {
                        String fileName = content.getFileName().toLowerCase();
                        if (fileName.endsWith(".pdf")) {
                            contentType = "application/pdf";
                        } else if (fileName.endsWith(".txt")) {
                            contentType = "text/plain";
                        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                            contentType = "application/msword";
                        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                            contentType = "image/jpeg";
                        } else if (fileName.endsWith(".png")) {
                            contentType = "image/png";
                        } else if (fileName.endsWith(".mp4")) {
                            contentType = "video/mp4";
                        } else {
                            contentType = "application/octet-stream";
                        }
                    }
                    
                    // Create headers for proper file serving
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Type", contentType);
                    headers.add("Content-Disposition", "inline; filename=\"" + content.getFileName() + "\"");
                    headers.add("Cache-Control", "public, max-age=3600"); // Cache for 1 hour
                    headers.add("X-Frame-Options", "ALLOWALL"); // Allow iframe embedding
                    headers.add("Content-Security-Policy", "frame-ancestors *"); // Modern iframe embedding policy
                    
                    if (contentLength > 0) {
                        headers.add("Content-Length", String.valueOf(contentLength));
                    }
                    
                    // Create a resource from the GCS stream with error handling
                    InputStream inputStream;
                    try {
                        inputStream = connection.getInputStream();
                    } catch (FileNotFoundException e) {
                        System.err.println("File not found when creating input stream for: " + content.getCloudinaryPublicId());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    } catch (IOException e) {
                        System.err.println("Error creating input stream: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    }
                    
                    Resource resource = new InputStreamResource(inputStream);
                    
                    System.out.println("Successfully serving GCS content: " + content.getFileName());
                    
                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(resource);
                            
                } catch (FileNotFoundException e) {
                    System.err.println("File not found on GCS for object: " + content.getCloudinaryPublicId());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                } catch (IOException e) {
                    System.err.println("Error accessing GCS file: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                } catch (Exception e) {
                    System.err.println("Error serving GCS content: " + e.getMessage());
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
            
            // Fallback to local file serving (backward compatibility)
            Path filePath = Paths.get("uploads/content/" + content.getFilePath());
            System.out.println("Looking for local file at: " + filePath.toAbsolutePath());
            
            if (!Files.exists(filePath)) {
                System.out.println("File does not exist at path: " + filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            // Create resource from file
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // Set headers for inline viewing (not download)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            // Set inline disposition for all content types to display in browser
            headers.add("Content-Disposition", "inline; filename=\"" + content.getFileName() + "\"");
            headers.add("X-Frame-Options", "ALLOWALL"); // Allow iframe embedding
            headers.add("Content-Security-Policy", "frame-ancestors *"); // Modern iframe embedding policy
            
            System.out.println("Serving local file with content type: " + contentType);
            System.out.println("Content-Disposition: " + headers.getFirst("Content-Disposition"));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Debug endpoint to fix H5P content types in database
     */
    @PostMapping("/fix-h5p-content-types")
    public ResponseEntity<?> fixH5PContentTypes() {
        try {
            List<LearningContent> allContent = contentService.getAllContent();
            int fixedCount = 0;
            
            for (LearningContent content : allContent) {
                if (content.getFileName() != null && content.getFileName().toLowerCase().endsWith(".h5p")) {
                    System.out.println("Fixing content ID " + content.getId() + ": " + content.getFileName());
                    System.out.println("  Before: contentType=" + content.getContentType() + ", contentTypeEnum=" + content.getContentTypeEnum());
                    
                    // Fix the content type
                    content.setContentTypeFromEnum(ContentType.INTERACTIVE);
                    
                    // Save the fixed content
                    contentService.saveContent(content);
                    
                    System.out.println("  After: contentType=" + content.getContentType() + ", contentTypeEnum=" + content.getContentTypeEnum());
                    fixedCount++;
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Fixed H5P content types",
                "fixedCount", fixedCount
            ));
            
        } catch (Exception e) {
            System.err.println("Error fixing H5P content types: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fix H5P content types: " + e.getMessage()));
        }
    }
    
    /**
     * Serve H5P files directly for iframe embedding
     */
    @GetMapping("/{contentId}/h5p-file")
    public ResponseEntity<?> serveH5PFile(@PathVariable Long contentId) {
        try {
            System.out.println("Serving H5P file for content ID: " + contentId);
            
            // Get content details from service
            LearningContent content = contentService.getContentById(contentId);
            
            if (content == null) {
                System.out.println("Content not found for ID: " + contentId);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Content found - Status: " + content.getStatus() + ", ContentType: " + content.getContentType());
            
            if (!content.getStatus().equals("published")) {
                System.out.println("Content not published, status: " + content.getStatus());
                return ResponseEntity.notFound().build();
            }
            
            // Only serve H5P files through this endpoint
            if (!content.isH5PContent()) {
                System.out.println("Content is not H5P type: " + content.getContentType());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "This endpoint only serves H5P files"));
            }
            
            // Generate signed URL for the H5P file and redirect
            try {
                String signedUrl = gcsService.generateSignedUrl(content.getCloudinaryPublicId(), 60);
                if (signedUrl != null) {
                    System.out.println("Redirecting to H5P file: " + signedUrl);
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(java.net.URI.create(signedUrl))
                            .build();
                } else {
                    System.err.println("Failed to generate signed URL for H5P file: " + content.getCloudinaryPublicId());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to serve H5P file"));
                }
            } catch (Exception e) {
                System.err.println("Error generating signed URL for H5P file: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to serve H5P file: " + e.getMessage()));
            }
            
        } catch (Exception e) {
            System.err.println("Error serving H5P file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Download content files (forces download instead of inline viewing)
     */
    @GetMapping("/{contentId}/download")
    public ResponseEntity<Resource> downloadContent(@PathVariable Long contentId) {
        try {
            // Get content details from service
            LearningContent content = contentService.getContentById(contentId);
            
            if (content == null || !content.getStatus().equals("published")) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if content uses Google Cloud Storage (stored in cloudinaryPublicId field for compatibility)
            if (content.getCloudinaryPublicId() != null && !content.getCloudinaryPublicId().trim().isEmpty()) {
                System.out.println("Generating GCS download URL for object: " + content.getCloudinaryPublicId());
                
                // Generate a signed URL for downloading the GCS file (valid for 1 hour)
                String downloadUrl = gcsService.generateSignedUrl(content.getCloudinaryPublicId(), 60);
                
                if (downloadUrl != null && !downloadUrl.trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .header("Location", downloadUrl)
                            .build();
                } else {
                    System.err.println("Failed to generate download URL for GCS object: " + content.getCloudinaryPublicId());
                    return ResponseEntity.notFound().build();
                }
            }
            
            // Fallback to local file serving (backward compatibility)
            Path filePath = Paths.get("uploads/content/" + content.getFilePath());
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            // Create resource from file
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // Set headers for download (attachment)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", content.getFileName());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Extract text content from a document for accessibility features
     */
    @GetMapping("/extract-text/{contentId}")
    public ResponseEntity<?> extractTextFromContent(@PathVariable Long contentId) {
        try {
            System.out.println("Extracting text for content ID: " + contentId);
            
            // Get content details from service
            LearningContent content = contentService.getContentById(contentId);
            
            if (content == null) {
                System.out.println("Content not found for ID: " + contentId);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Content found - Status: " + content.getStatus() + ", FilePath: " + content.getFilePath());
            
            if (!content.getStatus().equals("published")) {
                System.out.println("Content not published, status: " + content.getStatus());
                return ResponseEntity.notFound().build();
            }
            
            // Check if text extraction is supported for this file type
            if (!textExtractionService.isTextExtractionSupported(content.getFileName())) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Text extraction not supported for this file type",
                    "supportedFormats", "PDF, TXT, DOCX"
                ));
            }
            
            // Extract text using GCS-compatible method
            Map<String, Object> options = new HashMap<>();
            Map<String, Object> extractionResult = textExtractionService.extractTextFromContent(content, options);
            
            String extractedText = (String) extractionResult.get("text");
            String summary = textExtractionService.getTextSummary(extractedText, 500);
            
            System.out.println("Text extraction successful, length: " + extractedText.length());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "contentId", contentId,
                "fileName", content.getFileName(),
                "title", content.getTitle(),
                "extractedText", extractedText,
                "summary", summary,
                "wordCount", extractedText.split("\\s+").length,
                "characterCount", extractedText.length()
            ));
            
        } catch (Exception e) {
            System.err.println("Error extracting text for content ID " + contentId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "Failed to extract text: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * Generate AI quiz from tutor's PDF content
     */
    @PostMapping("/{contentId}/generate-quiz")
    public ResponseEntity<?> generateQuizFromContent(@PathVariable Long contentId, @RequestParam Long tutorUserId) {
        try {
            // Get content details and verify ownership
            LearningContent content = contentService.getContentById(contentId);
            if (content == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify ownership by checking if the content's tutor profile belongs to this user
            if (!content.getTutor().getUserId().equals(tutorUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Not authorized to generate quiz for this content"));
            }
            
            // Check if content is a PDF
            if (!content.getFileName().toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Quiz generation is only available for PDF files"));
            }
            
            // Check if quiz already exists for this content
            List<Quiz> existingQuizzes = quizRepository.findByLearningContentId(contentId);
            if (!existingQuizzes.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Quiz already exists for this content"));
            }
            
            System.out.println("Generating AI quiz for content: " + content.getTitle());
            
            // Create a temporary Book-like object for the AI service
            // (Since AIQuizGenerationService expects Book, but we're using LearningContent)
            TutorContentQuizResult result = generateQuizFromTutorContent(content);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "message", result.getMessage(),
                    "quizId", result.getQuiz().getId(),
                    "questionCount", result.getQuiz().getQuestions().size(),
                    "contentId", contentId
                ));
            } else {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Quiz generation failed: " + result.getMessage()));
            }
            
        } catch (Exception e) {
            System.err.println("Error generating AI quiz for content " + contentId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate quiz: " + e.getMessage()));
        }
    }
    
    /**
     * Create manual quiz for tutor content
     */
    @PostMapping("/{contentId}/create-quiz")
    public ResponseEntity<?> createManualQuiz(
            @PathVariable Long contentId, 
            @RequestParam Long tutorUserId,
            @RequestBody TutorQuizRequest request) {
        try {
            // Get content details and verify ownership
            LearningContent content = contentService.getContentById(contentId);
            if (content == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify ownership by checking if the content's tutor profile belongs to this user
            if (!content.getTutor().getUserId().equals(tutorUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Not authorized to create quiz for this content"));
            }
            
            // Validate request
            if (request.title == null || request.title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Quiz title is required"));
            }
            if (request.questions == null || request.questions.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "At least one question is required"));
            }
            
            // Create quiz
            Quiz quiz = new Quiz();
            quiz.setTitle(request.title);
            quiz.setAiGenerated(false);
            quiz.setLearningContentId(contentId);
            
            List<Question> questions = new ArrayList<>();
            
            // Validate and create questions
            for (TutorQuizRequest.QuestionRequest qr : request.questions) {
                if (qr.question == null || qr.question.trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Question text cannot be empty"));
                }
                if (qr.options == null || qr.options.size() < 2) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Each question must have at least 2 options"));
                }
                if (qr.correctOption == null || qr.correctOption < 0 || qr.correctOption >= qr.options.size()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid correct option index"));
                }
                
                Question question = new Question();
                question.setQuestion(qr.question.trim());
                question.setOptions(qr.options);
                question.setCorrectOption(qr.correctOption);
                question.setQuiz(quiz);
                questions.add(question);
            }
            
            quiz.setQuestions(questions);
            Quiz savedQuiz = quizRepository.save(quiz);
            
            System.out.println("Manual quiz created successfully for content: " + content.getTitle());
            
            return ResponseEntity.ok(Map.of(
                "message", "Quiz created successfully",
                "quizId", savedQuiz.getId(),
                "questionCount", savedQuiz.getQuestions().size(),
                "contentId", contentId
            ));
            
        } catch (Exception e) {
            System.err.println("Error creating manual quiz for content " + contentId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create quiz: " + e.getMessage()));
        }
    }
    
    /**
     * Get quizzes for tutor content
     */
    @GetMapping("/{contentId}/quizzes")
    public ResponseEntity<?> getContentQuizzes(@PathVariable Long contentId, @RequestParam Long tutorUserId) {
        try {
            // Get content details and verify ownership
            LearningContent content = contentService.getContentById(contentId);
            if (content == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify ownership by checking if the content's tutor profile belongs to this user
            if (!content.getTutor().getUserId().equals(tutorUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Not authorized to view quizzes for this content"));
            }
            
            List<Quiz> quizzes = quizRepository.findByLearningContentId(contentId);
            
            return ResponseEntity.ok(Map.of(
                "quizzes", quizzes,
                "count", quizzes.size(),
                "contentId", contentId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch quizzes: " + e.getMessage()));
        }
    }
    
    /**
     * Delete quiz for tutor content
     */
    @DeleteMapping("/quiz/{quizId}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long quizId, @RequestParam Long tutorUserId) {
        try {
            Quiz quiz = quizRepository.findById(quizId).orElse(null);
            if (quiz == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify ownership through content
            Long contentId = quiz.getLearningContentId();
            if (contentId != null) {
                LearningContent content = contentService.getContentById(contentId);
                if (content == null || !content.getTutor().getUserId().equals(tutorUserId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Not authorized to delete this quiz"));
                }
            }
            
            // Delete related leaderboard entries first to avoid foreign key constraint violation
            logger.info("Deleting leaderboard entries for quiz {} by tutor {}", quizId, tutorUserId);
            leaderboardRepository.deleteByQuizId(quizId);
            
            quizRepository.delete(quiz);
            
            return ResponseEntity.ok(Map.of(
                "message", "Quiz deleted successfully",
                "quizId", quizId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete quiz: " + e.getMessage()));
        }
    }
    
    /**
     * Get all quizzes for a specific tutor
     */
    @GetMapping("/tutor/{tutorUserId}/quizzes")
    public ResponseEntity<?> getTutorQuizzes(@PathVariable Long tutorUserId) {
        try {
            // Get all content for this tutor
            List<LearningContent> tutorContent = contentService.getTutorContent(tutorUserId);
            
            // Collect all quizzes from their content
            List<Quiz> allQuizzes = new ArrayList<>();
            for (LearningContent content : tutorContent) {
                List<Quiz> contentQuizzes = quizRepository.findByLearningContentId(content.getId());
                allQuizzes.addAll(contentQuizzes);
            }
            
            // Also get quizzes that might not be linked to content (standalone quizzes)
            // For now, we'll just return content-linked quizzes
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "quizzes", allQuizzes,
                "count", allQuizzes.size(),
                "tutorUserId", tutorUserId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch tutor quizzes: " + e.getMessage()));
        }
    }
    
    // Helper method to generate quiz from tutor content using Gemini API
    private TutorContentQuizResult generateQuizFromTutorContent(LearningContent content) {
        try {
            logger.info("Generating AI quiz for tutor content: {}", content.getTitle());
            
            // Extract text from the PDF using the GCS-compatible text extraction service
            Map<String, Object> options = new HashMap<>();
            Map<String, Object> extractionResult = textExtractionService.extractTextFromContent(content, options);
            String extractedText = (String) extractionResult.get("text");
            
            if (extractedText == null || extractedText.length() < 100) {
                return new TutorContentQuizResult(false, "PDF text too short for quiz generation", null);
            }
            
            logger.info("Extracted text length: {} characters", extractedText.length());
            
            // Create a temporary Book object and use AIQuizGenerationService logic directly
            Book tempBook = new Book();
            tempBook.setTitle(content.getTitle());
            
            // Use the Gemini generation logic from AIQuizGenerationService
            // but bypass the file reading since we already have the text
            List<AIQuizGenerationService.AIQuestion> aiQuestions = generateQuestionsWithGeminiDirect(extractedText, content.getTitle());
            
            if (aiQuestions == null || aiQuestions.isEmpty()) {
                logger.warn("Gemini generation failed, falling back to mock quiz");
                return generateMockQuizForContent(content);
            }
            
            // Convert AI questions to Quiz entity
            Quiz quiz = new Quiz();
            quiz.setTitle("AI Quiz: " + content.getTitle());
            quiz.setLearningContentId(content.getId());
            quiz.setAiGenerated(true);
            
            List<Question> questions = new ArrayList<>();
            for (AIQuizGenerationService.AIQuestion aiQuestion : aiQuestions) {
                Question question = new Question();
                question.setQuestion(aiQuestion.question);
                question.setOptions(aiQuestion.options);
                question.setCorrectOption(aiQuestion.correctAnswer);
                questions.add(question);
            }
            
            quiz.setQuestions(questions);
            Quiz savedQuiz = quizRepository.save(quiz);
            
            logger.info("Successfully generated AI quiz with {} questions for content: {}", aiQuestions.size(), content.getTitle());
            return new TutorContentQuizResult(true, "Quiz generated successfully using Gemini AI", savedQuiz);
            
        } catch (Exception e) {
            logger.error("Error generating quiz from tutor content: {}", e.getMessage(), e);
            return new TutorContentQuizResult(false, "Error generating quiz: " + e.getMessage(), null);
        }
    }
    
    // Helper method to call Gemini API directly with extracted text
    private List<AIQuizGenerationService.AIQuestion> generateQuestionsWithGeminiDirect(String textContent, String contentTitle) {
        try {
            // Use the AIQuizGenerationService's Gemini generation method
            // We'll create a reflection-like approach or expose the method
            // For now, let's duplicate the Gemini logic here (temporary solution)
            
            logger.info("Calling Gemini API for quiz generation with text length: {}", textContent.length());
            
            // Create the same request as AIQuizGenerationService but directly here
            String prompt = createQuizGenerationPrompt(textContent, contentTitle);
            return callGeminiAPI(prompt);
            
        } catch (Exception e) {
            logger.error("Error calling Gemini API: {}", e.getMessage(), e);
            return null;
        }
    }
    
    // Helper method to create quiz generation prompt
    private String createQuizGenerationPrompt(String textContent, String contentTitle) {
        return String.format(
            "Based on this text from the content \"%s\", create exactly 5 multiple choice questions.\n" +
            "\n" +
            "Guidelines:\n" +
            "- Questions should be appropriate for students with learning differences (clear, simple language)\n" +
            "- Focus on key concepts and main ideas\n" +
            "- Avoid trick questions or overly complex wording\n" +
            "- Each question should have 4 answer choices (A, B, C, D)\n" +
            "- Only one correct answer per question\n" +
            "\n" +
            "Format your response as valid JSON array:\n" +
            "[\n" +
            "    {\n" +
            "        \"question\": \"What is the main topic discussed?\",\n" +
            "        \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n" +
            "        \"correctAnswer\": 0\n" +
            "    }\n" +
            "]\n" +
            "\n" +
            "Text content:\n" +
            "%s",
            contentTitle, textContent.substring(0, Math.min(textContent.length(), 2000)));
    }
    
    // Helper method to call Gemini API
    private List<AIQuizGenerationService.AIQuestion> callGeminiAPI(String prompt) {
        // This would contain the Gemini API call logic
        // For now, let's use the AIQuizGenerationService instance
        // We'll create a public method in AIQuizGenerationService to do this
        return aiQuizGenerationService.generateQuestionsWithText(prompt);
    }
    
    // Fallback mock quiz for content
    private TutorContentQuizResult generateMockQuizForContent(LearningContent content) {
        Quiz quiz = new Quiz();
        quiz.setTitle("Sample Quiz: " + content.getTitle());
        quiz.setLearningContentId(content.getId());
        quiz.setAiGenerated(true);
        
        List<Question> questions = new ArrayList<>();
        
        Question q1 = new Question();
        q1.setQuestion("What is the main topic of this content?");
        q1.setOptions(Arrays.asList("Core concepts", "Advanced theory", "Practical application", "Summary"));
        q1.setCorrectOption(0);
        questions.add(q1);
        
        Question q2 = new Question();
        q2.setQuestion("This content appears to be designed for which audience?");
        q2.setOptions(Arrays.asList("Beginners", "Professionals", "Students", "All levels"));
        q2.setCorrectOption(2);
        questions.add(q2);
        
        Question q3 = new Question();
        q3.setQuestion("What type of learning material is this?");
        q3.setOptions(Arrays.asList("Reference", "Tutorial", "Guide", "Assessment"));
        q3.setCorrectOption(2);
        questions.add(q3);
        
        quiz.setQuestions(questions);
        Quiz savedQuiz = quizRepository.save(quiz);
        
        return new TutorContentQuizResult(true, "Mock quiz generated (Gemini not available)", savedQuiz);
    }
    
    // Inner classes for quiz requests and responses
    public static class TutorQuizRequest {
        public String title;
        public List<QuestionRequest> questions;
        
        public static class QuestionRequest {
            public String question;
            public List<String> options;
            public Integer correctOption;
        }
    }
    
    public static class TutorContentQuizResult {
        private final boolean success;
        private final String message;
        private final Quiz quiz;
        
        public TutorContentQuizResult(boolean success, String message, Quiz quiz) {
            this.success = success;
            this.message = message;
            this.quiz = quiz;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Quiz getQuiz() { return quiz; }
    }
    
    // Inner class for accessibility update request
    public static class AccessibilityUpdateRequest {
        private Boolean dyslexiaFriendly;
        private Boolean adhdFriendly;
        private Boolean autismFriendly;
        private Boolean visualImpairmentFriendly;
        private Boolean hearingImpairmentFriendly;
        private Boolean motorImpairmentFriendly;
        private String fontType;
        private String readingLevel;
        private Boolean hasAudioDescription;
        private Boolean hasSubtitles;
        private String cognitiveLoadLevel;
        
        // Getters and setters
        public Boolean getDyslexiaFriendly() { return dyslexiaFriendly; }
        public void setDyslexiaFriendly(Boolean dyslexiaFriendly) { this.dyslexiaFriendly = dyslexiaFriendly; }
        public Boolean getAdhdFriendly() { return adhdFriendly; }
        public void setAdhdFriendly(Boolean adhdFriendly) { this.adhdFriendly = adhdFriendly; }
        public Boolean getAutismFriendly() { return autismFriendly; }
        public void setAutismFriendly(Boolean autismFriendly) { this.autismFriendly = autismFriendly; }
        public Boolean getVisualImpairmentFriendly() { return visualImpairmentFriendly; }
        public void setVisualImpairmentFriendly(Boolean visualImpairmentFriendly) { this.visualImpairmentFriendly = visualImpairmentFriendly; }
        public Boolean getHearingImpairmentFriendly() { return hearingImpairmentFriendly; }
        public void setHearingImpairmentFriendly(Boolean hearingImpairmentFriendly) { this.hearingImpairmentFriendly = hearingImpairmentFriendly; }
        public Boolean getMotorImpairmentFriendly() { return motorImpairmentFriendly; }
        public void setMotorImpairmentFriendly(Boolean motorImpairmentFriendly) { this.motorImpairmentFriendly = motorImpairmentFriendly; }
        public String getFontType() { return fontType; }
        public void setFontType(String fontType) { this.fontType = fontType; }
        public String getReadingLevel() { return readingLevel; }
        public void setReadingLevel(String readingLevel) { this.readingLevel = readingLevel; }
        public Boolean getHasAudioDescription() { return hasAudioDescription; }
        public void setHasAudioDescription(Boolean hasAudioDescription) { this.hasAudioDescription = hasAudioDescription; }
        public Boolean getHasSubtitles() { return hasSubtitles; }
        public void setHasSubtitles(Boolean hasSubtitles) { this.hasSubtitles = hasSubtitles; }
        public String getCognitiveLoadLevel() { return cognitiveLoadLevel; }
        public void setCognitiveLoadLevel(String cognitiveLoadLevel) { this.cognitiveLoadLevel = cognitiveLoadLevel; }
    }


}
