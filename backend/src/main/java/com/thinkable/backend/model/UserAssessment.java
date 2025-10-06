package com.thinkable.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_assessments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "assessment_date")
    private LocalDateTime assessmentDate;

    @Column(name = "attention_score")
    private Integer attentionScore = 0;

    @Column(name = "social_communication_score")
    private Integer socialCommunicationScore = 0;

    @Column(name = "sensory_processing_score")
    private Integer sensoryProcessingScore = 0;

    @Column(name = "reading_difficulty_score")
    private Integer readingDifficultyScore = 0;

    @Column(name = "motor_skills_score")
    private Integer motorSkillsScore = 0;

    @Column(name = "font_preferences", columnDefinition = "TEXT")
    private String fontPreferences;

    @Column(name = "ui_adaptations", columnDefinition = "TEXT")
    private String uiAdaptations;

    @Column(name = "recommended_preset", length = 50)
    private String recommendedPreset = "standard";

    @Column(name = "assessment_completed")
    private Boolean assessmentCompleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public UserAssessment() {
        this.assessmentDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserAssessment(Long userId) {
        this();
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDateTime assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public Integer getAttentionScore() {
        return attentionScore;
    }

    public void setAttentionScore(Integer attentionScore) {
        this.attentionScore = attentionScore;
    }

    public Integer getSocialCommunicationScore() {
        return socialCommunicationScore;
    }

    public void setSocialCommunicationScore(Integer socialCommunicationScore) {
        this.socialCommunicationScore = socialCommunicationScore;
    }

    public Integer getSensoryProcessingScore() {
        return sensoryProcessingScore;
    }

    public void setSensoryProcessingScore(Integer sensoryProcessingScore) {
        this.sensoryProcessingScore = sensoryProcessingScore;
    }

    public Integer getReadingDifficultyScore() {
        return readingDifficultyScore;
    }

    public void setReadingDifficultyScore(Integer readingDifficultyScore) {
        this.readingDifficultyScore = readingDifficultyScore;
    }

    public Integer getMotorSkillsScore() {
        return motorSkillsScore;
    }

    public void setMotorSkillsScore(Integer motorSkillsScore) {
        this.motorSkillsScore = motorSkillsScore;
    }

    public String getFontPreferences() {
        return fontPreferences;
    }

    public void setFontPreferences(String fontPreferences) {
        this.fontPreferences = fontPreferences;
    }

    public String getUiAdaptations() {
        return uiAdaptations;
    }

    public void setUiAdaptations(String uiAdaptations) {
        this.uiAdaptations = uiAdaptations;
    }

    public String getRecommendedPreset() {
        return recommendedPreset;
    }

    public void setRecommendedPreset(String recommendedPreset) {
        this.recommendedPreset = recommendedPreset;
    }

    public Boolean getAssessmentCompleted() {
        return assessmentCompleted;
    }

    public void setAssessmentCompleted(Boolean assessmentCompleted) {
        this.assessmentCompleted = assessmentCompleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public int getTotalScore() {
        return (attentionScore != null ? attentionScore : 0) +
               (socialCommunicationScore != null ? socialCommunicationScore : 0) +
               (sensoryProcessingScore != null ? sensoryProcessingScore : 0) +
               (readingDifficultyScore != null ? readingDifficultyScore : 0) +
               (motorSkillsScore != null ? motorSkillsScore : 0);
    }

    public boolean hasSignificantAttentionNeeds() {
        return attentionScore != null && attentionScore >= 18;
    }

    public boolean hasSignificantReadingNeeds() {
        return readingDifficultyScore != null && readingDifficultyScore >= 15;
    }

    public boolean hasSignificantSocialNeeds() {
        return socialCommunicationScore != null && socialCommunicationScore >= 16;
    }

    public boolean hasSignificantSensoryNeeds() {
        return sensoryProcessingScore != null && sensoryProcessingScore >= 14;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "UserAssessment{" +
                "id=" + id +
                ", userId=" + userId +
                ", attentionScore=" + attentionScore +
                ", socialCommunicationScore=" + socialCommunicationScore +
                ", sensoryProcessingScore=" + sensoryProcessingScore +
                ", readingDifficultyScore=" + readingDifficultyScore +
                ", motorSkillsScore=" + motorSkillsScore +
                ", recommendedPreset='" + recommendedPreset + '\'' +
                ", assessmentCompleted=" + assessmentCompleted +
                '}';
    }
}
