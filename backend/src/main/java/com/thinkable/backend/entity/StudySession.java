package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "study_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudySession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;
    
    @Column(name = "activity_details")
    private String activityDetails; // JSON or simple string with details
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes; // How long they studied
    
    @Column(name = "score")
    private Integer score; // For quizzes, assessments
    
    @Column(name = "max_score")
    private Integer maxScore; // For calculating percentage
    
    @Column(name = "completed", nullable = false)
    private Boolean completed = true;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate; // For streak calculations
    
    @Column(name = "accessibility_tools_used")
    private String accessibilityToolsUsed; // JSON array of tools used
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public enum ActivityType {
        LESSON_COMPLETED,
        QUIZ_TAKEN,
        ASSESSMENT_COMPLETED,
        CONTENT_DISCOVERY,
        PRACTICE_SESSION,
        READING_SESSION,
        VIDEO_WATCHED,
        INTERACTION_COMPLETED
    }
    
    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.startedAt = LocalDateTime.now();
        this.studyDate = LocalDate.now();
    }
    
    // Helper methods
    public Integer getScorePercentage() {
        if (maxScore == null || maxScore == 0 || score == null) {
            return null;
        }
        return (score * 100) / maxScore;
    }
    
    public boolean isCompletedToday() {
        return studyDate.equals(LocalDate.now());
    }
    
    public boolean hasAccessibilityTools() {
        return accessibilityToolsUsed != null && !accessibilityToolsUsed.isEmpty();
    }
}
