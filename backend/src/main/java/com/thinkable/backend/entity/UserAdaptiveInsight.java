package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * User Adaptive Insights Entity
 * Stores personalized insights discovered through pattern recognition
 */
@Entity
@Table(name = "user_adaptive_insights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAdaptiveInsight {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "insight_type", nullable = false, length = 50)
    private String insightType;
    
    @Column(name = "insight_title", nullable = false, length = 200)
    private String insightTitle;
    
    @Column(name = "insight_description", nullable = false, columnDefinition = "TEXT")
    private String insightDescription;
    
    @Column(name = "insight_data", columnDefinition = "TEXT")
    private String insightData;
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore = BigDecimal.valueOf(0.50);
    
    @Column(name = "priority_level", length = 20)
    private String priorityLevel = "medium";
    
    @Column(name = "presented_to_user")
    private Boolean presentedToUser = false;
    
    @Column(name = "user_response", length = 20)
    private String userResponse; // 'accepted', 'rejected', 'pending'
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    // Helper methods
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore.compareTo(BigDecimal.valueOf(0.8)) >= 0;
    }
    
    public boolean isHighPriority() {
        return "high".equals(priorityLevel);
    }
    
    public boolean isPending() {
        return userResponse == null || "pending".equals(userResponse);
    }
    
    public boolean isAccepted() {
        return "accepted".equals(userResponse);
    }
    
    public boolean isRejected() {
        return "rejected".equals(userResponse);
    }
    
    public boolean shouldPresent() {
        return !presentedToUser && isHighConfidence() && isPending();
    }
    
    public void markPresented() {
        this.presentedToUser = true;
    }
    
    public void acceptInsight() {
        this.userResponse = "accepted";
        this.respondedAt = LocalDateTime.now();
    }
    
    public void rejectInsight() {
        this.userResponse = "rejected";
        this.respondedAt = LocalDateTime.now();
    }
}
