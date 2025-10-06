package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

/**
 * Content Review Entity
 * Student reviews and feedback on learning content
 */
@Entity
@Table(name = "content_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    @JsonBackReference("content-reviews")
    private LearningContent content;
    
    @Column(name = "overall_rating", nullable = false) // 1-5 scale
    private Integer overallRating;
    
    @Column(name = "content_quality_rating") // 1-5 scale
    private Integer contentQualityRating;
    
    @Column(name = "accessibility_rating") // 1-5 scale
    private Integer accessibilityRating;
    
    @Column(name = "engagement_rating") // 1-5 scale
    private Integer engagementRating;
    
    @Column(name = "difficulty_rating") // 1-5 scale (1=too easy, 3=just right, 5=too hard)
    private Integer difficultyRating;
    
    @Column(name = "review_title", length = 100)
    private String reviewTitle;
    
    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;
    
    @Column(name = "pros", columnDefinition = "TEXT")
    private String pros; // What worked well
    
    @Column(name = "cons", columnDefinition = "TEXT")
    private String cons; // What could be improved
    
    @Column(name = "accessibility_feedback", columnDefinition = "TEXT")
    private String accessibilityFeedback; // Specific accessibility comments
    
    @Column(name = "would_recommend")
    private Boolean wouldRecommend;
    
    @Column(name = "helped_with_goals")
    private Boolean helpedWithGoals;
    
    @Column(name = "neurodivergent_friendly_rating") // 1-5 scale
    private Integer neurodivergentFriendlyRating;
    
    @Column(name = "tags", length = 300)
    private String tags; // JSON array of descriptive tags
    
    @Column(name = "is_verified")
    private Boolean isVerified = false; // Verified as genuine review
    
    @Column(name = "is_public")
    private Boolean isPublic = true;
    
    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean isPositive() {
        return overallRating != null && overallRating >= 4;
    }
    
    public boolean isNegative() {
        return overallRating != null && overallRating <= 2;
    }
    
    public boolean hasAccessibilityFeedback() {
        return accessibilityFeedback != null && !accessibilityFeedback.trim().isEmpty();
    }
    
    public boolean isDetailed() {
        return (reviewText != null && reviewText.length() > 50) ||
               (pros != null && pros.length() > 20) ||
               (cons != null && cons.length() > 20);
    }
    
    public boolean isHelpful() {
        return helpfulCount != null && helpfulCount >= 3;
    }
    
    public boolean recommendsContent() {
        return Boolean.TRUE.equals(wouldRecommend);
    }
    
    public boolean foundAccessible() {
        return accessibilityRating != null && accessibilityRating >= 4;
    }
    
    public boolean foundEngaging() {
        return engagementRating != null && engagementRating >= 4;
    }
    
    public boolean foundNeurodivergentFriendly() {
        return neurodivergentFriendlyRating != null && neurodivergentFriendlyRating >= 4;
    }
    
    public void incrementHelpfulCount() {
        this.helpfulCount = (helpfulCount != null ? helpfulCount : 0) + 1;
    }
}
