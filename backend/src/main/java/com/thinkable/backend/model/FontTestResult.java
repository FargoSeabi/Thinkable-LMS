package com.thinkable.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "font_test_results")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FontTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "font_name", nullable = false, length = 100)
    private String fontName;

    @Column(name = "readability_rating")
    private Integer readabilityRating; // 1-5 scale

    @Column(name = "reading_time_ms")
    private Integer readingTimeMs;

    @Column(name = "difficulty_reported", length = 20)
    private String difficultyReported; // 'easy', 'medium', 'hard'

    @Column(name = "symptoms_reported", columnDefinition = "TEXT")
    private String symptomsReported; // letters_move, eye_strain, slow_reading, etc.

    @Column(name = "test_date")
    private LocalDateTime testDate;

    // Constructors
    public FontTestResult() {
        this.testDate = LocalDateTime.now();
    }

    public FontTestResult(Long userId, String fontName, Integer readabilityRating, String difficultyReported) {
        this();
        this.userId = userId;
        this.fontName = fontName;
        this.readabilityRating = readabilityRating;
        this.difficultyReported = difficultyReported;
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

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public Integer getReadabilityRating() {
        return readabilityRating;
    }

    public void setReadabilityRating(Integer readabilityRating) {
        this.readabilityRating = readabilityRating;
    }

    public Integer getReadingTimeMs() {
        return readingTimeMs;
    }

    public void setReadingTimeMs(Integer readingTimeMs) {
        this.readingTimeMs = readingTimeMs;
    }

    public String getDifficultyReported() {
        return difficultyReported;
    }

    public void setDifficultyReported(String difficultyReported) {
        this.difficultyReported = difficultyReported;
    }

    public String getSymptomsReported() {
        return symptomsReported;
    }

    public void setSymptomsReported(String symptomsReported) {
        this.symptomsReported = symptomsReported;
    }

    public LocalDateTime getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDateTime testDate) {
        this.testDate = testDate;
    }

    // Helper methods
    public boolean indicatesDyslexia() {
        // Basic heuristic: serif fonts rated as difficult OR dyslexia-friendly fonts rated as easy
        boolean serifDifficulty = ("Times New Roman".equals(fontName) || "Georgia".equals(fontName)) 
                                 && "hard".equals(difficultyReported);
        
        boolean dyslexiaFontPreference = ("Comic Neue".equals(fontName) || "OpenDyslexic".equals(fontName)) 
                                       && "easy".equals(difficultyReported);
        
        return serifDifficulty || dyslexiaFontPreference;
    }

    public boolean hasMovementSymptoms() {
        if (symptomsReported == null) return false;
        return symptomsReported.contains("lettersMove");
    }

    public boolean hasEyeStrain() {
        if (symptomsReported == null) return false;
        return symptomsReported.contains("eyeStrain");
    }

    public boolean hasSlowReading() {
        if (symptomsReported == null) return false;
        return symptomsReported.contains("slowReading");
    }

    @PrePersist
    protected void onCreate() {
        if (testDate == null) {
            testDate = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "FontTestResult{" +
                "id=" + id +
                ", userId=" + userId +
                ", fontName='" + fontName + '\'' +
                ", readabilityRating=" + readabilityRating +
                ", difficultyReported='" + difficultyReported + '\'' +
                ", testDate=" + testDate +
                '}';
    }
}
