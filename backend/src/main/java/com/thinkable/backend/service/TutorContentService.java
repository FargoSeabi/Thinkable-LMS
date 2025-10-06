package com.thinkable.backend.service;

import com.thinkable.backend.entity.*;
import com.thinkable.backend.repository.*;
import com.thinkable.backend.model.User;
import com.thinkable.backend.dto.H5PContentRequest;
import com.thinkable.backend.enums.ContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Service for managing tutor content uploads and accessibility features
 */
@Service
@Transactional
public class TutorContentService {
    
    @Autowired
    private LearningContentRepository contentRepository;
    
    @Autowired
    private TutorProfileRepository tutorRepository;
    
    @Autowired
    private ContentAccessibilityTagRepository accessibilityTagRepository;
    
    @Autowired
    private StudentContentInteractionRepository interactionRepository;
    
    @Autowired
    private ContentConversationRepository conversationRepository;
    
    @Autowired
    private ContentReviewRepository reviewRepository;
    
    @Autowired
    private ContentMessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GoogleCloudStorageService gcsService;

    @Autowired
    private H5PExtractionService h5pExtractionService;

    @Autowired
    private ActivityTrackingService activityTrackingService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String UPLOAD_DIR = "uploads/content/"; // Kept for backward compatibility with existing files
    
    /**
     * Upload new learning content with accessibility features
     */
    public LearningContent uploadContent(Long tutorUserId, MultipartFile file, ContentUploadRequest request) throws IOException {
        // Check if this is an H5P file and route to H5P processing
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".h5p")) {
            // Convert ContentUploadRequest to H5PContentRequest and process as H5P
            H5PContentRequest h5pRequest = new H5PContentRequest();
            h5pRequest.setTitle(request.getTitle());
            h5pRequest.setDescription(request.getDescription());
            h5pRequest.setSubjectArea(request.getSubjectArea());
            h5pRequest.setDifficultyLevel(request.getDifficultyLevel());
            h5pRequest.setTargetAgeMin(request.getTargetAgeMin());
            h5pRequest.setTargetAgeMax(request.getTargetAgeMax());
            h5pRequest.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
            h5pRequest.setDyslexiaFriendly(request.getDyslexiaFriendly());
            h5pRequest.setAdhdFriendly(request.getAdhdFriendly());
            h5pRequest.setAutismFriendly(request.getAutismFriendly());
            h5pRequest.setVisualImpairmentFriendly(request.getVisualImpairmentFriendly());
            h5pRequest.setHearingImpairmentFriendly(request.getHearingImpairmentFriendly());
            h5pRequest.setMotorImpairmentFriendly(request.getMotorImpairmentFriendly());
            h5pRequest.setFontType(request.getFontType());
            h5pRequest.setReadingLevel(request.getReadingLevel());
            h5pRequest.setHasAudioDescription(request.getHasAudioDescription());
            h5pRequest.setHasSubtitles(request.getHasSubtitles());
            h5pRequest.setCognitiveLoadLevel(request.getCognitiveLoadLevel());
            
            // Route to H5P processing method
            return uploadH5PContent(tutorUserId, file, h5pRequest);
        }
        
        TutorProfile tutor = tutorRepository.findByUserId(tutorUserId)
                .orElseGet(() -> createBasicTutorProfile(tutorUserId));
        
        // Upload file to Google Cloud Storage
        GoogleCloudStorageService.GCSUploadResult uploadResult = gcsService.uploadFile(file, "learning-content");
        
        // Create content entity
        LearningContent content = new LearningContent();
        content.setTutor(tutor);
        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setContentType(determineContentType(file.getContentType()));
        content.setSubjectArea(request.getSubjectArea());
        content.setDifficultyLevel(request.getDifficultyLevel());
        content.setTargetAgeMin(request.getTargetAgeMin());
        content.setTargetAgeMax(request.getTargetAgeMax());
        content.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        
        // File information - now using Cloudinary
        content.setFileName(uploadResult.getOriginalFilename());
        content.setFilePath(uploadResult.getObjectName()); // Store object name for reference
        content.setFileSizeBytes(uploadResult.getSize());
        content.setMimeType(file.getContentType());
        
        // Google Cloud Storage fields (reusing Cloudinary fields for compatibility)
        content.setCloudinaryPublicId(uploadResult.getObjectName());  // GCS object name
        content.setCloudinaryUrl(uploadResult.getMediaLink());        // GCS media link
        content.setCloudinarySecureUrl(uploadResult.getMediaLink());  // GCS media link (same as URL, always secure)
        
        // Accessibility features
        setAccessibilityFeatures(content, request);
        
        // Topic tags
        if (request.getTopicTags() != null) {
            content.setTopicTags(objectMapper.writeValueAsString(request.getTopicTags()));
        }
        
        // Learning styles
        if (request.getLearningStyles() != null) {
            content.setLearningStyles(objectMapper.writeValueAsString(request.getLearningStyles()));
        }
        
        content.setStatus("draft");
        content = contentRepository.save(content);
        
        // Create accessibility tags
        createAccessibilityTags(content, request);
        
        // Update tutor content count
        tutor.setContentCount(tutor.getContentCount() + 1);
        tutorRepository.save(tutor);
        
        return content;
    }
    
    /**
     * Upload H5P interactive content file (.h5p)
     */
    public LearningContent uploadH5PContent(Long tutorUserId, MultipartFile file, H5PContentRequest request) throws IOException {
        TutorProfile tutor = tutorRepository.findByUserId(tutorUserId)
                .orElseGet(() -> createBasicTutorProfile(tutorUserId));
        
        // Extract H5P metadata from the ZIP file
        H5PMetadata h5pMetadata = extractH5PMetadata(file);
        
        // Upload original H5P file to Google Cloud Storage
        GoogleCloudStorageService.GCSUploadResult uploadResult = gcsService.uploadFile(file, "h5p-content");
        
        // Extract H5P content files and upload individually
        String extractionContentId = uploadResult.getObjectName().replace("h5p-content/", "").replace(".h5p", "");
        H5PExtractionService.H5PExtractionResult extractionResult = 
            h5pExtractionService.extractAndUploadH5PContent(file, extractionContentId);
        
        // Create H5P content entity
        LearningContent content = new LearningContent();
        content.setTutor(tutor);
        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setContentTypeFromEnum(ContentType.INTERACTIVE);
        content.setSubjectArea(request.getSubjectArea());
        content.setDifficultyLevel(request.getDifficultyLevel());
        content.setTargetAgeMin(request.getTargetAgeMin());
        content.setTargetAgeMax(request.getTargetAgeMax());
        content.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        
        // File information from GCS
        content.setFileName(uploadResult.getOriginalFilename());
        content.setFilePath(uploadResult.getObjectName());
        content.setFileSizeBytes(uploadResult.getSize());
        content.setMimeType("application/x-h5p");
        
        // Google Cloud Storage fields (reusing Cloudinary fields for compatibility)
        content.setCloudinaryPublicId(uploadResult.getObjectName());  // GCS object name
        content.setCloudinaryUrl(uploadResult.getMediaLink());        // GCS media link
        content.setCloudinarySecureUrl(uploadResult.getMediaLink());  // GCS media link (same as URL, always secure)
        
        // H5P-specific fields from extracted metadata
        content.setH5pContentId(generateH5PContentId(uploadResult.getObjectName()));
        content.setH5pLibrary(h5pMetadata.getMainLibrary());
        content.setH5pMetadata(objectMapper.writeValueAsString(h5pMetadata));
        // We'll set the embed URL after saving to get the content ID
        content.setH5pSettings(generateH5PSettings(h5pMetadata));
        
        // Set accessibility features from request
        setH5PAccessibilityFeatures(content, request);
        
        // Default settings for H5P
        content.setInteractionType("interactive");
        content.setCognitiveLoadLevel(request.getCognitiveLoadLevel());
        
        // Learning styles for H5P content
        if (request.getLearningStyles() != null) {
            content.setLearningStyles(request.getLearningStyles());
        }
        
        content.setStatus("draft");
        content = contentRepository.save(content);
        
        // Set H5P embed URL now that we have the content ID
        content.setH5pEmbedUrl("/api/h5p/player/" + content.getId());
        content = contentRepository.save(content);
        
        // Update tutor content count
        tutor.setContentCount(tutor.getContentCount() + 1);
        tutorRepository.save(tutor);
        
        return content;
    }
    
    /**
     * Set accessibility features for H5P content
     */
    private void setH5PAccessibilityFeatures(LearningContent content, H5PContentRequest request) {
        content.setDyslexiaFriendly(request.getDyslexiaFriendly());
        content.setAdhdFriendly(request.getAdhdFriendly());
        content.setAutismFriendly(request.getAutismFriendly());
        content.setVisualImpairmentFriendly(request.getVisualImpairmentFriendly());
        content.setHearingImpairmentFriendly(request.getHearingImpairmentFriendly());
        content.setMotorImpairmentFriendly(request.getMotorImpairmentFriendly());
        content.setFontType(request.getFontType());
        content.setReadingLevel(request.getReadingLevel());
        content.setHasAudioDescription(request.getHasAudioDescription());
        content.setHasSubtitles(request.getHasSubtitles());
        content.setCognitiveLoadLevel(request.getCognitiveLoadLevel());
        content.setInteractionType(request.getInteractionType());
    }
    
    /**
     * Extract H5P metadata from .h5p file (ZIP format)
     */
    private H5PMetadata extractH5PMetadata(MultipartFile file) throws IOException {
        // Create a temporary file to work with ZipFile (more robust than ZipInputStream)
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("h5p-", ".zip");
            Files.write(tempFile, file.getBytes());
            
            try (ZipFile zipFile = new ZipFile(tempFile.toFile())) {
                ZipEntry h5pJsonEntry = zipFile.getEntry("h5p.json");
                
                if (h5pJsonEntry == null) {
                    throw new IOException("h5p.json not found in H5P file");
                }
                
                // Read h5p.json content
                try (InputStream inputStream = zipFile.getInputStream(h5pJsonEntry);
                     InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    
                    StringBuilder content = new StringBuilder();
                    char[] buffer = new char[1024];
                    int length;
                    
                    while ((length = reader.read(buffer)) != -1) {
                        content.append(buffer, 0, length);
                    }
                    
                    // Parse JSON and create H5PMetadata
                    return parseH5PJson(content.toString());
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to extract H5P metadata: " + e.getMessage(), e);
        } finally {
            // Clean up temporary file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    // Log but don't fail the operation
                    System.err.println("Warning: Could not delete temporary file: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Parse h5p.json content into H5PMetadata
     */
    private H5PMetadata parseH5PJson(String h5pJson) throws IOException {
        try {
            Map<String, Object> h5pData = objectMapper.readValue(h5pJson, Map.class);
            
            H5PMetadata metadata = new H5PMetadata();
            metadata.setTitle((String) h5pData.get("title"));
            metadata.setLanguage((String) h5pData.get("language"));
            metadata.setDescription((String) h5pData.get("description"));
            // Handle authors - can be List of Maps or other formats
            Object authorsObj = h5pData.get("authors");
            if (authorsObj instanceof List) {
                try {
                    metadata.setAuthors((List<Map<String, Object>>) authorsObj);
                } catch (ClassCastException e) {
                    // If authors is a List but not of Maps, set to null or empty
                    metadata.setAuthors(null);
                }
            } else {
                metadata.setAuthors(null);
            }
            metadata.setSource((String) h5pData.get("source"));
            metadata.setLicense((String) h5pData.get("license"));
            metadata.setLicenseVersion((String) h5pData.get("licenseVersion"));
            metadata.setYearFrom((Integer) h5pData.get("yearFrom"));
            metadata.setYearTo((Integer) h5pData.get("yearTo"));
            metadata.setDefaultLanguage((String) h5pData.get("defaultLanguage"));
            
            // Extract main library information - handle both Map and List formats
            Object preloadedDependenciesObj = h5pData.get("preloadedDependencies");
            if (preloadedDependenciesObj instanceof Map) {
                Map<String, Object> preloadedDependencies = (Map<String, Object>) preloadedDependenciesObj;
                if (!preloadedDependencies.isEmpty()) {
                    // Get the first preloaded dependency as main library
                    Object firstDep = preloadedDependencies.values().iterator().next();
                    if (firstDep instanceof Map) {
                        Map<String, Object> mainLib = (Map<String, Object>) firstDep;
                        metadata.setMainLibrary((String) mainLib.get("machineName"));
                    }
                }
            } else if (preloadedDependenciesObj instanceof List) {
                List<?> preloadedList = (List<?>) preloadedDependenciesObj;
                if (!preloadedList.isEmpty() && preloadedList.get(0) instanceof Map) {
                    Map<String, Object> mainLib = (Map<String, Object>) preloadedList.get(0);
                    metadata.setMainLibrary((String) mainLib.get("machineName"));
                }
            }
            
            // Store raw JSON for complete metadata
            metadata.setRawJson(h5pJson);
            
            return metadata;
        } catch (Exception e) {
            throw new IOException("Failed to parse h5p.json: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate unique H5P content ID
     */
    private String generateH5PContentId(String objectName) {
        // Create a unique ID based on the GCS object name and current timestamp
        return "h5p-" + objectName.replaceAll("[^a-zA-Z0-9]", "-") + "-" + System.currentTimeMillis();
    }
    
    /**
     * Generate H5P embed URL for viewing the content
     */
    private String generateH5PEmbedUrl(String objectName) {
        // This would be the URL where your H5P content can be viewed
        // For now, we'll use a placeholder that points to the GCS file
        return "/api/tutor/content/h5p/view/" + objectName;
    }
    
    /**
     * Generate H5P player settings
     */
    private String generateH5PSettings(H5PMetadata metadata) throws JsonProcessingException {
        Map<String, Object> settings = new HashMap<>();
        settings.put("frame", true);
        settings.put("export", false);
        settings.put("embed", false);
        settings.put("copyright", true);
        settings.put("icon", true);
        settings.put("fullscreen", true);
        settings.put("language", metadata.getLanguage() != null ? metadata.getLanguage() : "en");
        
        return objectMapper.writeValueAsString(settings);
    }
    
    /**
     * H5P Metadata class for storing extracted information
     */
    public static class H5PMetadata {
        private String title;
        private String language;
        private String description;
        private List<Map<String, Object>> authors;
        private String source;
        private String license;
        private String licenseVersion;
        private Integer yearFrom;
        private Integer yearTo;
        private String defaultLanguage;
        private String mainLibrary;
        private String rawJson;
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<Map<String, Object>> getAuthors() { return authors; }
        public void setAuthors(List<Map<String, Object>> authors) { this.authors = authors; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getLicense() { return license; }
        public void setLicense(String license) { this.license = license; }
        
        public String getLicenseVersion() { return licenseVersion; }
        public void setLicenseVersion(String licenseVersion) { this.licenseVersion = licenseVersion; }
        
        public Integer getYearFrom() { return yearFrom; }
        public void setYearFrom(Integer yearFrom) { this.yearFrom = yearFrom; }
        
        public Integer getYearTo() { return yearTo; }
        public void setYearTo(Integer yearTo) { this.yearTo = yearTo; }
        
        public String getDefaultLanguage() { return defaultLanguage; }
        public void setDefaultLanguage(String defaultLanguage) { this.defaultLanguage = defaultLanguage; }
        
        public String getMainLibrary() { return mainLibrary; }
        public void setMainLibrary(String mainLibrary) { this.mainLibrary = mainLibrary; }
        
        public String getRawJson() { return rawJson; }
        public void setRawJson(String rawJson) { this.rawJson = rawJson; }
    }
    
    /**
     * Publish content (make it public and discoverable)
     */
    public LearningContent publishContent(Long contentId, Long tutorUserId) {
        LearningContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        
        // Verify ownership
        if (!content.getTutor().getUserId().equals(tutorUserId)) {
            throw new SecurityException("Not authorized to publish this content");
        }
        
        content.publish();
        return contentRepository.save(content);
    }
    
    /**
     * Get content for a specific tutor (all statuses for content management)
     */
    public List<LearningContent> getTutorContent(Long tutorUserId) {
        TutorProfile tutor = tutorRepository.findByUserId(tutorUserId)
                .orElseGet(() -> createBasicTutorProfile(tutorUserId));
        
        // For tutor dashboard, show all content regardless of status so they can manage drafts and published content
        return contentRepository.findByTutor(tutor);
    }
    
    /**
     * Get published content for a specific tutor (for student discovery)
     */
    public List<LearningContent> getTutorPublishedContent(Long tutorUserId) {
        TutorProfile tutor = tutorRepository.findByUserId(tutorUserId)
                .orElseGet(() -> createBasicTutorProfile(tutorUserId));
        
        // For student discovery, show only published content
        return contentRepository.findByTutorAndStatus(tutor, "published");
    }
    
    /**
     * Get content by ID for viewing
     */
    public LearningContent getContentById(Long contentId) {
        return contentRepository.findById(contentId).orElse(null);
    }
    
    /**
     * Search and filter content for students
     */
    public Page<LearningContent> searchContent(ContentSearchRequest searchRequest, Pageable pageable) {
        if (searchRequest.getQuery() != null && !searchRequest.getQuery().trim().isEmpty()) {
            return contentRepository.searchContent(searchRequest.getQuery().trim(), pageable);
        }
        
        if (searchRequest.getSubjectArea() != null) {
            return contentRepository.findContentByAccessibilityFilters(
                searchRequest.getSubjectArea(),
                searchRequest.getDyslexiaFriendly(),
                searchRequest.getAdhdFriendly(),
                searchRequest.getAutismFriendly(),
                pageable
            );
        }
        
        return contentRepository.findByStatusAndIsPublicTrue("published", pageable);
    }
    
    /**
     * Get personalized content recommendations for a student
     */
    public List<LearningContent> getPersonalizedContent(Long studentId) {
        UserNeurodivergentProfile profile = getUserProfile(studentId);
        if (profile == null) {
            return contentRepository.findHighRatedContent(BigDecimal.valueOf(4.0));
        }
        
        List<LearningContent> recommendations = new ArrayList<>();
        
        // Get content based on neurodivergent traits
        if (profile.isHyperfocusIntense()) {
            recommendations.addAll(contentRepository.findDyslexiaFriendlyContent(true));
        }
        
        if (profile.isSensoryProcessingHigh()) {
            recommendations.addAll(contentRepository.findAutismFriendlyContent(true));
        }
        
        if (profile.needsExecutiveSupport()) {
            recommendations.addAll(contentRepository.findADHDFriendlyContent(true));
        }
        
        // Remove duplicates and limit results
        return recommendations.stream()
                .distinct()
                .limit(20)
                .collect(Collectors.toList());
    }
    
    /**
     * Record student interaction with content
     */
    public void recordInteraction(Long studentId, Long contentId, InteractionRequest request) {
        LearningContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        
        Optional<StudentContentInteraction> existingInteraction = 
                interactionRepository.findByStudentIdAndContentId(studentId, contentId);
        
        StudentContentInteraction interaction = existingInteraction.orElse(new StudentContentInteraction());
        
        if (existingInteraction.isEmpty()) {
            interaction.setStudentId(studentId);
            interaction.setContent(content);
            interaction.setStartedAt(LocalDateTime.now());
        }
        
        // Update interaction data
        updateInteractionFromRequest(interaction, request);
        interaction.updateLastAccessed();
        
        interactionRepository.save(interaction);
        
        // Update content metrics
        content.incrementViewCount();
        if ("download".equals(request.getInteractionType())) {
            content.incrementDownloadCount();
        }

        contentRepository.save(content);

        // Auto-record study session for meaningful content engagement
        if ("view_end".equals(request.getInteractionType()) &&
            request.getDuration() != null && request.getDuration() >= 300) { // 5+ minutes in seconds
            try {
                // Convert duration from seconds to minutes
                int durationMinutes = request.getDuration() / 60;

                // Ensure minimum 1 minute for recording
                if (durationMinutes < 1) durationMinutes = 1;

                // Record as content discovery activity
                activityTrackingService.recordActivity(
                    studentId,
                    com.thinkable.backend.entity.StudySession.ActivityType.CONTENT_DISCOVERY,
                    "content-" + contentId,
                    durationMinutes,
                    null, null, null
                );

                System.out.println("Auto-recorded study session: user " + studentId +
                                 " studied content " + contentId + " for " + durationMinutes + " minutes (" +
                                 request.getDuration() + " seconds)");

            } catch (Exception e) {
                System.err.println("Failed to auto-record study session: " + e.getMessage());
                // Don't fail the interaction recording if study session recording fails
            }
        }
    }
    
    /**
     * Get content analytics for tutors
     */
    public ContentAnalytics getContentAnalytics(Long contentId, Long tutorUserId) {
        LearningContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        
        // Verify ownership
        if (!content.getTutor().getUserId().equals(tutorUserId)) {
            throw new SecurityException("Not authorized to view analytics for this content");
        }
        
        ContentAnalytics analytics = new ContentAnalytics();
        analytics.setContentId(contentId);
        analytics.setViewCount(content.getViewCount());
        analytics.setDownloadCount(content.getDownloadCount());
        
        // Calculate engagement metrics
        Long uniqueStudents = interactionRepository.countUniqueStudentsForContent(contentId);
        Long completedInteractions = interactionRepository.countCompletedInteractions(contentId);
        Long helpfulInteractions = interactionRepository.countHelpfulInteractions(contentId);
        
        analytics.setUniqueStudents(uniqueStudents);
        analytics.setCompletionRate(calculatePercentage(completedInteractions, uniqueStudents));
        analytics.setHelpfulRate(calculatePercentage(helpfulInteractions, uniqueStudents));
        
        // Average ratings
        Double avgUsefulness = interactionRepository.getAverageUsefulnessRating(contentId);
        Double avgAccessibility = interactionRepository.getAverageAccessibilityRating(contentId);
        
        analytics.setAverageUsefulnessRating(avgUsefulness);
        analytics.setAverageAccessibilityRating(avgAccessibility);
        
        return analytics;
    }
    
    /**
     * Delete content (and associated file) by ID
     */
    @Transactional
    public void deleteContent(Long contentId, Long tutorUserId) throws IOException {
        LearningContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        
        // Verify ownership
        if (!content.getTutor().getUserId().equals(tutorUserId)) {
            throw new SecurityException("Not authorized to delete this content");
        }
        
        // Delete associated file from Cloudinary or local storage
        if (content.getCloudinaryPublicId() != null && !content.getCloudinaryPublicId().trim().isEmpty()) {
            // Delete from Google Cloud Storage
            try {
                boolean deleted = gcsService.deleteFile(content.getCloudinaryPublicId());
                if (!deleted) {
                    System.err.println("Warning: Could not delete GCS file with object name: " + content.getCloudinaryPublicId());
                }
            } catch (Exception e) {
                System.err.println("Warning: Error deleting GCS file " + content.getCloudinaryPublicId() + ": " + e.getMessage());
            }
        } else if (content.getFilePath() != null && !content.getFilePath().trim().isEmpty()) {
            // Delete from local storage (backward compatibility)
            try {
                Path filePath = Paths.get(UPLOAD_DIR).resolve(content.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not delete local file " + content.getFilePath() + ": " + e.getMessage());
            }
        }
        
        // Delete associated accessibility tags
        accessibilityTagRepository.deleteByContentId(contentId);
        
        // Delete associated interactions
        interactionRepository.deleteByContentId(contentId);
        
        // Delete associated messages first (before conversations due to foreign key constraints)
        try {
            messageRepository.deleteByContentId(contentId);
        } catch (Exception e) {
            System.err.println("Warning: Could not delete messages for content " + contentId + ": " + e.getMessage());
        }
        
        // Delete associated conversations
        try {
            conversationRepository.deleteByContentId(contentId);
        } catch (Exception e) {
            System.err.println("Warning: Could not delete conversations for content " + contentId + ": " + e.getMessage());
        }
        
        // Delete associated reviews
        try {
            reviewRepository.deleteByContentId(contentId);
        } catch (Exception e) {
            System.err.println("Warning: Could not delete reviews for content " + contentId + ": " + e.getMessage());
        }
        
        // Delete the content record from database
        contentRepository.delete(content);
        
        // Update tutor content count
        TutorProfile tutor = content.getTutor();
        tutor.setContentCount(Math.max(0, tutor.getContentCount() - 1));
        tutorRepository.save(tutor);
    }
    
    // Private helper methods
    
    private String saveFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        return fileName;
    }
    
    private String determineContentType(String mimeType) {
        if (mimeType == null) return "document";
        
        if (mimeType.startsWith("video/")) return "video";
        if (mimeType.startsWith("audio/")) return "audio";
        if (mimeType.startsWith("image/")) return "image";
        if (mimeType.contains("pdf") || mimeType.contains("document")) return "document";
        
        return "document";
    }
    
    private void setAccessibilityFeatures(LearningContent content, ContentUploadRequest request) {
        content.setDyslexiaFriendly(request.getDyslexiaFriendly());
        content.setAdhdFriendly(request.getAdhdFriendly());
        content.setAutismFriendly(request.getAutismFriendly());
        content.setVisualImpairmentFriendly(request.getVisualImpairmentFriendly());
        content.setHearingImpairmentFriendly(request.getHearingImpairmentFriendly());
        content.setMotorImpairmentFriendly(request.getMotorImpairmentFriendly());
        
        content.setFontType(request.getFontType());
        content.setColorContrastRatio(request.getColorContrastRatio());
        content.setReadingLevel(request.getReadingLevel());
        content.setHasAudioDescription(request.getHasAudioDescription());
        content.setHasSubtitles(request.getHasSubtitles());
        content.setHasSignLanguage(request.getHasSignLanguage());
        content.setCognitiveLoadLevel(request.getCognitiveLoadLevel());
        content.setInteractionType(request.getInteractionType());
    }
    
    private void createAccessibilityTags(LearningContent content, ContentUploadRequest request) {
        List<ContentAccessibilityTag> tags = new ArrayList<>();
        
        if (Boolean.TRUE.equals(request.getDyslexiaFriendly())) {
            tags.add(ContentAccessibilityTag.createDyslexiaTag(content, "dyslexia_friendly_font", BigDecimal.valueOf(0.9)));
        }
        
        if (Boolean.TRUE.equals(request.getAdhdFriendly())) {
            tags.add(ContentAccessibilityTag.createADHDTag(content, "structured_content", BigDecimal.valueOf(0.85)));
        }
        
        if (Boolean.TRUE.equals(request.getAutismFriendly())) {
            tags.add(ContentAccessibilityTag.createAutismTag(content, "clear_navigation", BigDecimal.valueOf(0.8)));
        }
        
        accessibilityTagRepository.saveAll(tags);
    }
    
    private UserNeurodivergentProfile getUserProfile(Long studentId) {
        // This would integrate with the existing profile service
        return null; // Placeholder - integrate with UserNeurodivergentProfileService
    }
    
    private void updateInteractionFromRequest(StudentContentInteraction interaction, InteractionRequest request) {
        interaction.setInteractionType(request.getInteractionType());
        interaction.setTimeSpentMinutes(request.getTimeSpentMinutes());
        interaction.setCompletionPercentage(request.getCompletionPercentage());
        interaction.setEngagementScore(request.getEngagementScore());
        interaction.setComprehensionScore(request.getComprehensionScore());
        interaction.setDifficultyRating(request.getDifficultyRating());
        interaction.setUsefulnessRating(request.getUsefulnessRating());
        interaction.setAccessibilityRating(request.getAccessibilityRating());
        interaction.setEnergyLevelBefore(request.getEnergyLevelBefore());
        interaction.setEnergyLevelAfter(request.getEnergyLevelAfter());
        interaction.setFocusLevel(request.getFocusLevel());
        interaction.setStressLevel(request.getStressLevel());
        interaction.setDeviceType(request.getDeviceType());
        interaction.setNotes(request.getNotes());
        interaction.setWasHelpful(request.getWasHelpful());
        interaction.setWouldRecommend(request.getWouldRecommend());
        
        try {
            if (request.getContextTags() != null) {
                interaction.setContextTags(objectMapper.writeValueAsString(request.getContextTags()));
            }
            if (request.getAccessibilityBarriers() != null) {
                interaction.setAccessibilityBarriers(objectMapper.writeValueAsString(request.getAccessibilityBarriers()));
            }
        } catch (JsonProcessingException e) {
            // Log error but continue
        }
        
        if ("complete".equals(request.getInteractionType())) {
            interaction.markCompleted();
        }
    }
    
    private Double calculatePercentage(Long numerator, Long denominator) {
        if (denominator == null || denominator == 0) return 0.0;
        return (numerator != null ? numerator.doubleValue() : 0.0) / denominator.doubleValue() * 100.0;
    }
    
    /**
     * Create a basic tutor profile for users who don't have one yet
     */
    private TutorProfile createBasicTutorProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        TutorProfile tutorProfile = new TutorProfile();
        tutorProfile.setUserId(userId);
        tutorProfile.setDisplayName(user.getUsername() != null ? user.getUsername() : user.getEmail().split("@")[0]);
        tutorProfile.setBio("New tutor - profile setup in progress");
        tutorProfile.setQualifications("To be updated");
        tutorProfile.setTeachingExperienceYears(0);
        tutorProfile.setSubjectExpertise("[]");
        tutorProfile.setNeurodivergentSpecialization("[]");
        tutorProfile.setTeachingStyles("[]");
        tutorProfile.setAccessibilityExpertise("[]");
        tutorProfile.setVerificationStatus("pending");
        tutorProfile.setIsActive(true);
        tutorProfile.setContentCount(0);
        
        return tutorRepository.save(tutorProfile);
    }
    
    // Inner classes for request/response DTOs
    public static class ContentUploadRequest {
        private String title;
        private String description;
        private String subjectArea;
        private String difficultyLevel;
        private Integer targetAgeMin;
        private Integer targetAgeMax;
        private Integer estimatedDurationMinutes;
        private List<String> topicTags;
        private List<String> learningStyles;
        
        // Accessibility features
        private Boolean dyslexiaFriendly = false;
        private Boolean adhdFriendly = false;
        private Boolean autismFriendly = false;
        private Boolean visualImpairmentFriendly = false;
        private Boolean hearingImpairmentFriendly = false;
        private Boolean motorImpairmentFriendly = false;
        private String fontType;
        private BigDecimal colorContrastRatio;
        private String readingLevel;
        private Boolean hasAudioDescription = false;
        private Boolean hasSubtitles = false;
        private Boolean hasSignLanguage = false;
        private String cognitiveLoadLevel;
        private String interactionType;
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSubjectArea() { return subjectArea; }
        public void setSubjectArea(String subjectArea) { this.subjectArea = subjectArea; }
        public String getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        public Integer getTargetAgeMin() { return targetAgeMin; }
        public void setTargetAgeMin(Integer targetAgeMin) { this.targetAgeMin = targetAgeMin; }
        public Integer getTargetAgeMax() { return targetAgeMax; }
        public void setTargetAgeMax(Integer targetAgeMax) { this.targetAgeMax = targetAgeMax; }
        public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
        public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
        public List<String> getTopicTags() { return topicTags; }
        public void setTopicTags(List<String> topicTags) { this.topicTags = topicTags; }
        public List<String> getLearningStyles() { return learningStyles; }
        public void setLearningStyles(List<String> learningStyles) { this.learningStyles = learningStyles; }
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
        public BigDecimal getColorContrastRatio() { return colorContrastRatio; }
        public void setColorContrastRatio(BigDecimal colorContrastRatio) { this.colorContrastRatio = colorContrastRatio; }
        public String getReadingLevel() { return readingLevel; }
        public void setReadingLevel(String readingLevel) { this.readingLevel = readingLevel; }
        public Boolean getHasAudioDescription() { return hasAudioDescription; }
        public void setHasAudioDescription(Boolean hasAudioDescription) { this.hasAudioDescription = hasAudioDescription; }
        public Boolean getHasSubtitles() { return hasSubtitles; }
        public void setHasSubtitles(Boolean hasSubtitles) { this.hasSubtitles = hasSubtitles; }
        public Boolean getHasSignLanguage() { return hasSignLanguage; }
        public void setHasSignLanguage(Boolean hasSignLanguage) { this.hasSignLanguage = hasSignLanguage; }
        public String getCognitiveLoadLevel() { return cognitiveLoadLevel; }
        public void setCognitiveLoadLevel(String cognitiveLoadLevel) { this.cognitiveLoadLevel = cognitiveLoadLevel; }
        public String getInteractionType() { return interactionType; }
        public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
    }
    
    public static class ContentSearchRequest {
        private String query;
        private String subjectArea;
        private Boolean dyslexiaFriendly = false;
        private Boolean adhdFriendly = false;
        private Boolean autismFriendly = false;
        
        // Getters and setters
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getSubjectArea() { return subjectArea; }
        public void setSubjectArea(String subjectArea) { this.subjectArea = subjectArea; }
        public Boolean getDyslexiaFriendly() { return dyslexiaFriendly; }
        public void setDyslexiaFriendly(Boolean dyslexiaFriendly) { this.dyslexiaFriendly = dyslexiaFriendly; }
        public Boolean getAdhdFriendly() { return adhdFriendly; }
        public void setAdhdFriendly(Boolean adhdFriendly) { this.adhdFriendly = adhdFriendly; }
        public Boolean getAutismFriendly() { return autismFriendly; }
        public void setAutismFriendly(Boolean autismFriendly) { this.autismFriendly = autismFriendly; }
    }
    
    public static class InteractionRequest {
        private String interactionType;
        private Integer timeSpentMinutes;
        private Integer completionPercentage;
        private BigDecimal engagementScore;
        private BigDecimal comprehensionScore;
        private Integer difficultyRating;
        private Integer usefulnessRating;
        private Integer accessibilityRating;
        private Integer energyLevelBefore;
        private Integer energyLevelAfter;
        private Integer focusLevel;
        private Integer stressLevel;
        private String deviceType;
        private List<String> contextTags;
        private String notes;
        private Boolean wasHelpful;
        private Boolean wouldRecommend;
        private List<String> accessibilityBarriers;
        private Integer duration; // Duration in seconds from frontend
        
        // Getters and setters
        public String getInteractionType() { return interactionType; }
        public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
        public Integer getTimeSpentMinutes() { return timeSpentMinutes; }
        public void setTimeSpentMinutes(Integer timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }
        public Integer getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }
        public BigDecimal getEngagementScore() { return engagementScore; }
        public void setEngagementScore(BigDecimal engagementScore) { this.engagementScore = engagementScore; }
        public BigDecimal getComprehensionScore() { return comprehensionScore; }
        public void setComprehensionScore(BigDecimal comprehensionScore) { this.comprehensionScore = comprehensionScore; }
        public Integer getDifficultyRating() { return difficultyRating; }
        public void setDifficultyRating(Integer difficultyRating) { this.difficultyRating = difficultyRating; }
        public Integer getUsefulnessRating() { return usefulnessRating; }
        public void setUsefulnessRating(Integer usefulnessRating) { this.usefulnessRating = usefulnessRating; }
        public Integer getAccessibilityRating() { return accessibilityRating; }
        public void setAccessibilityRating(Integer accessibilityRating) { this.accessibilityRating = accessibilityRating; }
        public Integer getEnergyLevelBefore() { return energyLevelBefore; }
        public void setEnergyLevelBefore(Integer energyLevelBefore) { this.energyLevelBefore = energyLevelBefore; }
        public Integer getEnergyLevelAfter() { return energyLevelAfter; }
        public void setEnergyLevelAfter(Integer energyLevelAfter) { this.energyLevelAfter = energyLevelAfter; }
        public Integer getFocusLevel() { return focusLevel; }
        public void setFocusLevel(Integer focusLevel) { this.focusLevel = focusLevel; }
        public Integer getStressLevel() { return stressLevel; }
        public void setStressLevel(Integer stressLevel) { this.stressLevel = stressLevel; }
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public List<String> getContextTags() { return contextTags; }
        public void setContextTags(List<String> contextTags) { this.contextTags = contextTags; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Boolean getWasHelpful() { return wasHelpful; }
        public void setWasHelpful(Boolean wasHelpful) { this.wasHelpful = wasHelpful; }
        public Boolean getWouldRecommend() { return wouldRecommend; }
        public void setWouldRecommend(Boolean wouldRecommend) { this.wouldRecommend = wouldRecommend; }
        public List<String> getAccessibilityBarriers() { return accessibilityBarriers; }
        public void setAccessibilityBarriers(List<String> accessibilityBarriers) { this.accessibilityBarriers = accessibilityBarriers; }
        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
    }
    
    public static class ContentAnalytics {
        private Long contentId;
        private Integer viewCount;
        private Integer downloadCount;
        private Long uniqueStudents;
        private Double completionRate;
        private Double helpfulRate;
        private Double averageUsefulnessRating;
        private Double averageAccessibilityRating;
        
        // Getters and setters
        public Long getContentId() { return contentId; }
        public void setContentId(Long contentId) { this.contentId = contentId; }
        public Integer getViewCount() { return viewCount; }
        public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
        public Integer getDownloadCount() { return downloadCount; }
        public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
        public Long getUniqueStudents() { return uniqueStudents; }
        public void setUniqueStudents(Long uniqueStudents) { this.uniqueStudents = uniqueStudents; }
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
        public Double getHelpfulRate() { return helpfulRate; }
        public void setHelpfulRate(Double helpfulRate) { this.helpfulRate = helpfulRate; }
        public Double getAverageUsefulnessRating() { return averageUsefulnessRating; }
        public void setAverageUsefulnessRating(Double averageUsefulnessRating) { this.averageUsefulnessRating = averageUsefulnessRating; }
        public Double getAverageAccessibilityRating() { return averageAccessibilityRating; }
        public void setAverageAccessibilityRating(Double averageAccessibilityRating) { this.averageAccessibilityRating = averageAccessibilityRating; }
    }
    
    /**
     * Get bookmarked content for a student
     */
    public List<StudentContentInteraction> getBookmarkedContent(Long studentId) {
        return interactionRepository.findBookmarkedContent(studentId);
    }
    
    /**
     * Toggle bookmark status for content
     * Returns true if bookmarked, false if bookmark removed
     */
    public boolean toggleBookmark(Long studentId, Long contentId) {
        Optional<StudentContentInteraction> existingInteraction = 
                interactionRepository.findByStudentIdAndContentId(studentId, contentId);
        
        if (existingInteraction.isPresent() && "bookmark".equals(existingInteraction.get().getInteractionType())) {
            // Remove bookmark
            interactionRepository.delete(existingInteraction.get());
            return false;
        } else {
            // Add bookmark
            InteractionRequest bookmarkRequest = new InteractionRequest();
            bookmarkRequest.setInteractionType("bookmark");
            recordInteraction(studentId, contentId, bookmarkRequest);
            return true;
        }
    }
    
    /**
     * Check if content is bookmarked by student
     */
    public boolean isBookmarked(Long studentId, Long contentId) {
        Optional<StudentContentInteraction> interaction = 
                interactionRepository.findByStudentIdAndContentId(studentId, contentId);
        return interaction.isPresent() && "bookmark".equals(interaction.get().getInteractionType());
    }
    
    /**
     * Get student interaction with specific content
     */
    public Optional<StudentContentInteraction> getStudentInteraction(Long studentId, Long contentId) {
        return interactionRepository.findByStudentIdAndContentId(studentId, contentId);
    }
    
    /**
     * Get all content (for debugging/admin purposes)
     */
    public List<LearningContent> getAllContent() {
        return contentRepository.findAll();
    }
    
    /**
     * Save content (for debugging/admin purposes)
     */
    @Transactional
    public LearningContent saveContent(LearningContent content) {
        return contentRepository.save(content);
    }
}
