package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.thinkable.backend.enums.ContentType;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

/**
 * Learning Content Entity
 * Stores educational materials uploaded by tutors with accessibility metadata
 */
@Entity
@Table(name = "learning_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningContent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    @JsonBackReference("tutor-contents")
    private TutorProfile tutor;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "content_type", nullable = false, length = 20)
    private String contentType; // document, video, audio, interactive, image
    
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type_enum")
    private ContentType contentTypeEnum;
    
    @Column(name = "subject_area", nullable = false, length = 50)
    private String subjectArea; // math, science, language, history, etc.
    
    @Column(name = "topic_tags", length = 500)
    private String topicTags; // JSON array of specific topics
    
    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel; // beginner, intermediate, advanced
    
    @Column(name = "target_age_min")
    private Integer targetAgeMin;
    
    @Column(name = "target_age_max")
    private Integer targetAgeMax;
    
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    // File storage information
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "file_name", length = 200)
    private String fileName;
    
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;
    
    // Cloudinary storage information  
    @Column(name = "cloudinary_public_id", length = 200)
    private String cloudinaryPublicId;
    
    @Column(name = "cloudinary_url", length = 500)
    private String cloudinaryUrl;
    
    @Column(name = "cloudinary_secure_url", length = 500)
    private String cloudinarySecureUrl;
    
    // H5P Interactive Content fields
    @Column(name = "h5p_content_id", length = 100)
    private String h5pContentId;
    
    @Column(name = "h5p_library", length = 100)
    private String h5pLibrary;
    
    @Column(name = "h5p_metadata", columnDefinition = "TEXT")
    private String h5pMetadata; // JSON containing H5P-specific metadata
    
    @Column(name = "h5p_embed_url", length = 1000)
    private String h5pEmbedUrl;
    
    @Column(name = "h5p_settings", columnDefinition = "TEXT")
    private String h5pSettings; // JSON containing H5P player settings
    
    // Accessibility features
    @Column(name = "dyslexia_friendly")
    private Boolean dyslexiaFriendly = false;
    
    @Column(name = "adhd_friendly")
    private Boolean adhdFriendly = false;
    
    @Column(name = "autism_friendly")
    private Boolean autismFriendly = false;
    
    @Column(name = "visual_impairment_friendly")
    private Boolean visualImpairmentFriendly = false;
    
    @Column(name = "hearing_impairment_friendly")
    private Boolean hearingImpairmentFriendly = false;
    
    @Column(name = "motor_impairment_friendly")
    private Boolean motorImpairmentFriendly = false;
    
    @Column(name = "font_type", length = 50)
    private String fontType;
    
    @Column(name = "color_contrast_ratio", precision = 4, scale = 2)
    private BigDecimal colorContrastRatio;
    
    @Column(name = "reading_level", length = 20)
    private String readingLevel;
    
    @Column(name = "has_audio_description")
    private Boolean hasAudioDescription = false;
    
    @Column(name = "has_subtitles")
    private Boolean hasSubtitles = false;
    
    @Column(name = "has_sign_language")
    private Boolean hasSignLanguage = false;
    
    @Column(name = "sensory_considerations", length = 300)
    private String sensoryConsiderations; // JSON array
    
    @Column(name = "cognitive_load_level", length = 20)
    private String cognitiveLoadLevel; // low, medium, high
    
    @Column(name = "interaction_type", length = 100)
    private String interactionType; // passive, interactive, collaborative
    
    @Column(name = "learning_styles", length = 200)
    private String learningStyles; // JSON array: visual, auditory, kinesthetic, reading
    
    // Content metrics
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "download_count")
    private Integer downloadCount = 0;
    
    @Column(name = "rating_average", precision = 3, scale = 2)
    private BigDecimal ratingAverage = BigDecimal.ZERO;
    
    @Column(name = "rating_count")
    private Integer ratingCount = 0;
    
    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate = BigDecimal.ZERO;
    
    // Status and moderation
    @Column(name = "status", length = 20)
    private String status = "draft"; // draft, published, under_review, rejected
    
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    @Column(name = "is_premium")
    private Boolean isPremium = false;
    
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;
    
    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    // Relationships
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("content-tags")
    private List<ContentAccessibilityTag> accessibilityTags;
    
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("content-interactions")
    private List<StudentContentInteraction> interactions;
    
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("content-reviews")
    private List<ContentReview> reviews;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean isPublished() {
        return "published".equals(status);
    }
    
    public boolean isAccessibleFor(String neurodivergentType) {
        switch (neurodivergentType.toLowerCase()) {
            case "dyslexia":
                return Boolean.TRUE.equals(dyslexiaFriendly);
            case "adhd":
                return Boolean.TRUE.equals(adhdFriendly);
            case "autism":
                return Boolean.TRUE.equals(autismFriendly);
            case "visual_impairment":
                return Boolean.TRUE.equals(visualImpairmentFriendly);
            case "hearing_impairment":
                return Boolean.TRUE.equals(hearingImpairmentFriendly);
            case "motor_impairment":
                return Boolean.TRUE.equals(motorImpairmentFriendly);
            default:
                return false;
        }
    }
    
    public boolean isHighlyRated() {
        return ratingAverage != null && ratingAverage.compareTo(BigDecimal.valueOf(4.0)) >= 0;
    }
    
    public boolean isPopular() {
        return viewCount != null && viewCount > 100;
    }
    
    public boolean isEffective() {
        return successRate != null && successRate.compareTo(BigDecimal.valueOf(75.0)) >= 0;
    }
    
    public void incrementViewCount() {
        this.viewCount = (viewCount != null ? viewCount : 0) + 1;
    }
    
    public void incrementDownloadCount() {
        this.downloadCount = (downloadCount != null ? downloadCount : 0) + 1;
    }
    
    public void publish() {
        this.status = "published";
        this.isPublic = true;
        this.publishedAt = LocalDateTime.now();
    }
    
    // H5P Helper methods
    public boolean isH5PContent() {
        return ContentType.INTERACTIVE.equals(contentTypeEnum) || 
               "interactive".equals(contentType);
    }
    
    public boolean isFileBasedContent() {
        return !isH5PContent();
    }
    
    public void setContentTypeFromEnum(ContentType type) {
        this.contentTypeEnum = type;
        this.contentType = type.getValue();
    }
    
    public ContentType getContentTypeEnum() {
        if (contentTypeEnum == null && contentType != null) {
            contentTypeEnum = ContentType.fromString(contentType);
        }
        return contentTypeEnum;
    }
}
