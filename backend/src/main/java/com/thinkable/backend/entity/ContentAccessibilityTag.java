package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Content Accessibility Tag Entity
 * Detailed accessibility features and requirements for learning content
 */
@Entity
@Table(name = "content_accessibility_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentAccessibilityTag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    @JsonBackReference("content-tags")
    private LearningContent content;
    
    @Column(name = "tag_category", nullable = false, length = 50)
    private String tagCategory; // neurodivergent_type, sensory, cognitive, motor, language
    
    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;
    
    @Column(name = "tag_value", length = 200)
    private String tagValue;
    
    @Column(name = "compatibility_score", precision = 3, scale = 2)
    private BigDecimal compatibilityScore; // 0.0 to 1.0
    
    @Column(name = "is_required")
    private Boolean isRequired = false; // Must have this feature
    
    @Column(name = "is_beneficial")
    private Boolean isBeneficial = true; // Nice to have this feature
    
    @Column(name = "evidence_based")
    private Boolean evidenceBased = false; // Based on research/proven methods
    
    @Column(name = "description", length = 300)
    private String description;
    
    @Column(name = "created_by_tutor")
    private Boolean createdByTutor = true;
    
    @Column(name = "verified_by_expert")
    private Boolean verifiedByExpert = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Helper methods
    public boolean isHighCompatibility() {
        return compatibilityScore != null && compatibilityScore.compareTo(BigDecimal.valueOf(0.8)) >= 0;
    }
    
    public boolean isEssential() {
        return Boolean.TRUE.equals(isRequired);
    }
    
    public boolean isTrusted() {
        return Boolean.TRUE.equals(verifiedByExpert) || Boolean.TRUE.equals(evidenceBased);
    }
    
    // Static methods for common accessibility tags
    public static ContentAccessibilityTag createDyslexiaTag(LearningContent content, String feature, BigDecimal score) {
        ContentAccessibilityTag tag = new ContentAccessibilityTag();
        tag.setContent(content);
        tag.setTagCategory("dyslexia");
        tag.setTagName(feature);
        tag.setCompatibilityScore(score);
        return tag;
    }
    
    public static ContentAccessibilityTag createADHDTag(LearningContent content, String feature, BigDecimal score) {
        ContentAccessibilityTag tag = new ContentAccessibilityTag();
        tag.setContent(content);
        tag.setTagCategory("adhd");
        tag.setTagName(feature);
        tag.setCompatibilityScore(score);
        return tag;
    }
    
    public static ContentAccessibilityTag createAutismTag(LearningContent content, String feature, BigDecimal score) {
        ContentAccessibilityTag tag = new ContentAccessibilityTag();
        tag.setContent(content);
        tag.setTagCategory("autism");
        tag.setTagName(feature);
        tag.setCompatibilityScore(score);
        return tag;
    }
}
