package com.thinkable.backend.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String name;

    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "progress")
    private Integer progress;

    @Column(name = "last_active")
    private String lastActive;

    @Column(name = "preferences")
    private String preferences;

    @Column(name = "learning_preferences")
    private String learningPreferences;

    // Assessment-based personalization fields
    @Column(name = "age_range")
    private String ageRange; // 5-8, 9-12, 13-16, 17+

    @Column(name = "grade_level")
    private String gradeLevel;

    @Column(name = "parent_email")
    private String parentEmail; // For users under 13

    @Column(name = "consent_given")
    private Boolean consentGiven;

    // Behavioral assessment results (stored as JSON)
    @Column(name = "learning_profile", columnDefinition = "TEXT")
    private String learningProfile; // JSON: focus_duration, preferred_pace, etc.

    @Column(name = "assessment_scores", columnDefinition = "TEXT")
    private String assessmentScores; // JSON: game results, attention patterns

    @Column(name = "ui_preferences", columnDefinition = "TEXT")
    private String uiPreferences; // JSON: complexity, colors, layout

    @Column(name = "session_preferences", columnDefinition = "TEXT")
    private String sessionPreferences; // JSON: duration, breaks, reminders

    @Column(name = "profile_completed")
    private Boolean profileCompleted;

    @Column(name = "onboarding_completed")
    private Boolean onboardingCompleted;

    // Password security fields
    @Column(name = "password_hashed")
    private Boolean passwordHashed = false; // Track if password is hashed (for migration)

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getLastActive() {
        return lastActive;
    }

    public void setLastActive(String lastActive) {
        this.lastActive = lastActive;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getLearningPreferences() {
        return learningPreferences;
    }

    public void setLearningPreferences(String learningPreferences) {
        this.learningPreferences = learningPreferences;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public Boolean getConsentGiven() {
        return consentGiven;
    }

    public void setConsentGiven(Boolean consentGiven) {
        this.consentGiven = consentGiven;
    }

    public String getLearningProfile() {
        return learningProfile;
    }

    public void setLearningProfile(String learningProfile) {
        this.learningProfile = learningProfile;
    }

    public String getAssessmentScores() {
        return assessmentScores;
    }

    public void setAssessmentScores(String assessmentScores) {
        this.assessmentScores = assessmentScores;
    }

    public String getUiPreferences() {
        return uiPreferences;
    }

    public void setUiPreferences(String uiPreferences) {
        this.uiPreferences = uiPreferences;
    }

    public String getSessionPreferences() {
        return sessionPreferences;
    }

    public void setSessionPreferences(String sessionPreferences) {
        this.sessionPreferences = sessionPreferences;
    }

    public Boolean getProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(Boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public Boolean getOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(Boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public Boolean getPasswordHashed() {
        return passwordHashed;
    }

    public void setPasswordHashed(Boolean passwordHashed) {
        this.passwordHashed = passwordHashed;
    }
}
