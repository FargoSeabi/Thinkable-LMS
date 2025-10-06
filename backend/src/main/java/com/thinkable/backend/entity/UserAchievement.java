package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * UserAchievement Entity
 * Tracks which achievements/badges users have earned
 */
@Entity
@Table(name = "user_achievements", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // Links to User entity
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    
    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt = LocalDateTime.now();
    
    @Column(name = "progress_value", nullable = false)
    private Integer progressValue; // The value that triggered this achievement
    
    @Column(name = "is_new", nullable = false)
    private Boolean isNew = true; // Flag to show "NEW!" badge in UI
    
    @Column(name = "notification_sent", nullable = false)
    private Boolean notificationSent = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Mark this achievement as viewed by the user
     */
    public void markAsViewed() {
        this.isNew = false;
    }
    
    /**
     * Mark notification as sent
     */
    public void markNotificationSent() {
        this.notificationSent = true;
    }
}
