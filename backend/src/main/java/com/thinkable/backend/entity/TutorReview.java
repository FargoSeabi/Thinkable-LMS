package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

/**
 * Tutor Review Entity
 * Student reviews and ratings for tutors
 */
@Entity
@Table(name = "tutor_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    @JsonBackReference("tutor-reviews")
    private TutorProfile tutor;
    
    @Column(name = "overall_rating", nullable = false) // 1-5 scale
    private Integer overallRating;
    
    @Column(name = "content_quality_rating") // 1-5 scale
    private Integer contentQualityRating;
    
    @Column(name = "accessibility_expertise_rating") // 1-5 scale
    private Integer accessibilityExpertiseRating;
    
    @Column(name = "communication_rating") // 1-5 scale
    private Integer communicationRating;
    
    @Column(name = "responsiveness_rating") // 1-5 scale
    private Integer responsivenessRating;
    
    @Column(name = "neurodivergent_understanding_rating") // 1-5 scale
    private Integer neurodivergentUnderstandingRating;
    
    @Column(name = "review_title", length = 100)
    private String reviewTitle;
    
    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;
    
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths; // What tutor does well
    
    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    private String areasForImprovement;
    
    @Column(name = "would_recommend")
    private Boolean wouldRecommend;
    
    @Column(name = "helped_achieve_goals")
    private Boolean helpedAchieveGoals;
    
    @Column(name = "created_accessible_content")
    private Boolean createdAccessibleContent;
    
    @Column(name = "understood_my_needs")
    private Boolean understoodMyNeeds;
    
    @Column(name = "tags", length = 300)
    private String tags; // JSON array of descriptive tags
    
    @Column(name = "interaction_context", length = 50)
    private String interactionContext; // content_only, one_on_one, group, workshop
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
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
    
    public boolean recommendsTutor() {
        return Boolean.TRUE.equals(wouldRecommend);
    }
    
    public boolean foundTutorHelpful() {
        return Boolean.TRUE.equals(helpedAchieveGoals);
    }
    
    public boolean foundAccessibilityExpert() {
        return accessibilityExpertiseRating != null && accessibilityExpertiseRating >= 4;
    }
    
    public boolean foundNeurodivergentAware() {
        return neurodivergentUnderstandingRating != null && neurodivergentUnderstandingRating >= 4;
    }
    
    public boolean isDetailed() {
        return (reviewText != null && reviewText.length() > 50) ||
               (strengths != null && strengths.length() > 20) ||
               (areasForImprovement != null && areasForImprovement.length() > 20);
    }
    
    public void incrementHelpfulCount() {
        this.helpfulCount = (helpfulCount != null ? helpfulCount : 0) + 1;
    }
}
