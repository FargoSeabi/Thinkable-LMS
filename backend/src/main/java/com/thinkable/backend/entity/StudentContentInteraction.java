package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Student Content Interaction Entity
 * Tracks how students engage with learning content for analytics and recommendations
 */
@Entity
@Table(name = "student_content_interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentContentInteraction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    @JsonBackReference("content-interactions")
    private LearningContent content;
    
    @Column(name = "interaction_type", nullable = false, length = 20)
    private String interactionType; // view, download, bookmark, complete, skip
    
    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;
    
    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;
    
    @Column(name = "engagement_score", precision = 3, scale = 2)
    private BigDecimal engagementScore; // 0.0 to 1.0
    
    @Column(name = "comprehension_score", precision = 3, scale = 2)
    private BigDecimal comprehensionScore; // 0.0 to 1.0 (based on quizzes/assessments)
    
    @Column(name = "difficulty_rating") // 1-5 scale
    private Integer difficultyRating;
    
    @Column(name = "usefulness_rating") // 1-5 scale
    private Integer usefulnessRating;
    
    @Column(name = "accessibility_rating") // 1-5 scale
    private Integer accessibilityRating;
    
    @Column(name = "energy_level_before") // 1-10 scale
    private Integer energyLevelBefore;
    
    @Column(name = "energy_level_after") // 1-10 scale
    private Integer energyLevelAfter;
    
    @Column(name = "focus_level") // 1-10 scale
    private Integer focusLevel;
    
    @Column(name = "stress_level") // 1-10 scale
    private Integer stressLevel;
    
    @Column(name = "device_type", length = 20)
    private String deviceType; // desktop, tablet, mobile
    
    @Column(name = "context_tags", length = 300)
    private String contextTags; // JSON array of situational context
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Student's personal notes
    
    @Column(name = "was_helpful")
    private Boolean wasHelpful;
    
    @Column(name = "would_recommend")
    private Boolean wouldRecommend;
    
    @Column(name = "accessibility_barriers", length = 500)
    private String accessibilityBarriers; // JSON array of encountered barriers
    
    @Column(name = "learning_outcome", length = 100)
    private String learningOutcome; // achieved, partially_achieved, not_achieved
    
    // Timestamps
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt = LocalDateTime.now();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Helper methods
    public boolean isCompleted() {
        return completedAt != null && completionPercentage != null && completionPercentage >= 80;
    }
    
    public boolean isHighEngagement() {
        return engagementScore != null && engagementScore.compareTo(BigDecimal.valueOf(0.7)) >= 0;
    }
    
    public boolean hadPositiveOutcome() {
        return Boolean.TRUE.equals(wasHelpful) && 
               (usefulnessRating == null || usefulnessRating >= 4) &&
               (comprehensionScore == null || comprehensionScore.compareTo(BigDecimal.valueOf(0.7)) >= 0);
    }
    
    public boolean hadAccessibilityIssues() {
        return accessibilityBarriers != null && !accessibilityBarriers.trim().isEmpty() &&
               !accessibilityBarriers.equals("[]");
    }
    
    public boolean improvedEnergyLevel() {
        return energyLevelBefore != null && energyLevelAfter != null && 
               energyLevelAfter > energyLevelBefore;
    }
    
    public boolean hadGoodFocus() {
        return focusLevel != null && focusLevel >= 7;
    }
    
    public boolean hadLowStress() {
        return stressLevel != null && stressLevel <= 4;
    }
    
    public void markCompleted() {
        this.completedAt = LocalDateTime.now();
        this.completionPercentage = 100;
    }
    
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    public BigDecimal calculateOverallSuccess() {
        BigDecimal total = BigDecimal.ZERO;
        int factors = 0;
        
        if (engagementScore != null) {
            total = total.add(engagementScore);
            factors++;
        }
        
        if (comprehensionScore != null) {
            total = total.add(comprehensionScore);
            factors++;
        }
        
        if (usefulnessRating != null) {
            total = total.add(BigDecimal.valueOf(usefulnessRating / 5.0));
            factors++;
        }
        
        if (factors == 0) return BigDecimal.ZERO;
        
        return total.divide(BigDecimal.valueOf(factors), 2, BigDecimal.ROUND_HALF_UP);
    }
}
