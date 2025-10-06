package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Content Recommendation Entity
 * AI-generated personalized content suggestions for students
 */
@Entity
@Table(name = "content_recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private LearningContent content;
    
    @Column(name = "recommendation_type", length = 30)
    private String recommendationType; // personalized, trending, similar_users, continuation
    
    @Column(name = "confidence_score", precision = 4, scale = 3)
    private BigDecimal confidenceScore; // 0.000 to 1.000
    
    @Column(name = "relevance_score", precision = 4, scale = 3)
    private BigDecimal relevanceScore;
    
    @Column(name = "accessibility_match_score", precision = 4, scale = 3)
    private BigDecimal accessibilityMatchScore;
    
    @Column(name = "learning_style_match_score", precision = 4, scale = 3)
    private BigDecimal learningStyleMatchScore;
    
    @Column(name = "difficulty_match_score", precision = 4, scale = 3)
    private BigDecimal difficultyMatchScore;
    
    @Column(name = "success_prediction_score", precision = 4, scale = 3)
    private BigDecimal successPredictionScore;
    
    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning; // Why this content was recommended
    
    @Column(name = "matching_factors", length = 500)
    private String matchingFactors; // JSON array of factors that led to recommendation
    
    @Column(name = "optimal_timing", length = 50)
    private String optimalTiming; // When student should consume this content
    
    @Column(name = "expected_outcomes", length = 300)
    private String expectedOutcomes; // JSON array of predicted learning outcomes
    
    @Column(name = "priority_level", length = 20)
    private String priorityLevel = "medium"; // low, medium, high, urgent
    
    @Column(name = "context_tags", length = 300)
    private String contextTags; // JSON array of contextual factors
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "presented_to_student")
    private Boolean presentedToStudent = false;
    
    @Column(name = "student_response", length = 20)
    private String studentResponse; // viewed, ignored, bookmarked, started, completed
    
    @Column(name = "feedback_rating") // 1-5 scale
    private Integer feedbackRating;
    
    @Column(name = "was_helpful")
    private Boolean wasHelpful;
    
    // Algorithm metadata
    @Column(name = "algorithm_version", length = 20)
    private String algorithmVersion = "1.0";
    
    @Column(name = "model_features", columnDefinition = "TEXT")
    private String modelFeatures; // JSON object of features used in recommendation
    
    @Column(name = "recommendation_source", length = 50)
    private String recommendationSource; // collaborative_filtering, content_based, hybrid, rule_based
    
    // Timestamps
    @Column(name = "generated_at")
    private LocalDateTime generatedAt = LocalDateTime.now();
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "presented_at")
    private LocalDateTime presentedAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    // Helper methods
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore.compareTo(BigDecimal.valueOf(0.8)) >= 0;
    }
    
    public boolean isHighPriority() {
        return "high".equals(priorityLevel) || "urgent".equals(priorityLevel);
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean wasWellReceived() {
        return Boolean.TRUE.equals(wasHelpful) && 
               (feedbackRating == null || feedbackRating >= 4);
    }
    
    public boolean wasIgnored() {
        return presentedToStudent == true && 
               (studentResponse == null || "ignored".equals(studentResponse));
    }
    
    public BigDecimal calculateOverallScore() {
        if (confidenceScore == null || relevanceScore == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = confidenceScore.multiply(BigDecimal.valueOf(0.3))
                          .add(relevanceScore.multiply(BigDecimal.valueOf(0.2)));
        
        if (accessibilityMatchScore != null) {
            total = total.add(accessibilityMatchScore.multiply(BigDecimal.valueOf(0.25)));
        }
        
        if (successPredictionScore != null) {
            total = total.add(successPredictionScore.multiply(BigDecimal.valueOf(0.25)));
        }
        
        return total;
    }
    
    public void markPresented() {
        this.presentedToStudent = true;
        this.presentedAt = LocalDateTime.now();
    }
    
    public void recordResponse(String response) {
        this.studentResponse = response;
        this.respondedAt = LocalDateTime.now();
    }
    
    public void recordFeedback(Integer rating, Boolean helpful) {
        this.feedbackRating = rating;
        this.wasHelpful = helpful;
    }
    
    public void expire() {
        this.isActive = false;
        this.expiresAt = LocalDateTime.now();
    }
}
