package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Achievement Entity
 * Represents badges/achievements that students can earn
 */
@Entity
@Table(name = "achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @Column(name = "icon", nullable = false, length = 50)
    private String icon; // Emoji or icon class name
    
    @Column(name = "category", nullable = false, length = 50)
    private String category; // LEARNING, STREAK, MILESTONE, SOCIAL, ACCESSIBILITY
    
    @Column(name = "points_value", nullable = false)
    private Integer pointsValue;
    
    @Column(name = "requirement_type", nullable = false, length = 50)
    private String requirementType; // LESSONS_COMPLETED, DAYS_STREAK, QUIZ_SCORE, etc.
    
    @Column(name = "requirement_value", nullable = false)
    private Integer requirementValue;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false; // Hidden until unlocked
    
    @Column(name = "rarity", nullable = false, length = 20)
    private String rarity; // COMMON, RARE, EPIC, LEGENDARY
    
    @Column(name = "age_group", length = 20)
    private String ageGroup; // KIDS, TEENS, ALL
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if this achievement is unlocked based on user metrics
     */
    public boolean isUnlockedBy(int userValue) {
        return userValue >= this.requirementValue;
    }
}
