package com.thinkable.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for H5P content upload requests
 * Used when tutors upload or link H5P interactive content
 */
public class H5PContentRequest {
    
    private String title;
    
    private String description;
    
    private String subjectArea;
    
    private String difficultyLevel;
    
    private Integer targetAgeMin;
    private Integer targetAgeMax;
    private Integer estimatedDurationMinutes;
    
    // H5P-specific fields will be extracted from the uploaded .h5p file
    // No need to require these fields in the request
    
    // Accessibility features
    private Boolean dyslexiaFriendly = false;
    private Boolean adhdFriendly = false;
    private Boolean autismFriendly = false;
    private Boolean visualImpairmentFriendly = false;
    private Boolean hearingImpairmentFriendly = false;
    private Boolean motorImpairmentFriendly = false;
    
    private String fontType;
    private String readingLevel;
    private Boolean hasAudioDescription = false;
    private Boolean hasSubtitles = false;
    private String cognitiveLoadLevel;
    private String interactionType;
    private String learningStyles; // JSON string
    
    // Constructors
    public H5PContentRequest() {}
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSubjectArea() { return subjectArea; }
    public void setSubjectArea(String subjectArea) { this.subjectArea = subjectArea; }
    
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    
    public Integer getTargetAgeMin() { return targetAgeMin; }
    public void setTargetAgeMin(Integer targetAgeMin) { this.targetAgeMin = targetAgeMin; }
    
    public Integer getTargetAgeMax() { return targetAgeMax; }
    public void setTargetAgeMax(Integer targetAgeMax) { this.targetAgeMax = targetAgeMax; }
    
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    
    // Accessibility Getters and Setters
    public Boolean getDyslexiaFriendly() { return dyslexiaFriendly; }
    public void setDyslexiaFriendly(Boolean dyslexiaFriendly) { this.dyslexiaFriendly = dyslexiaFriendly; }
    
    public Boolean getAdhdFriendly() { return adhdFriendly; }
    public void setAdhdFriendly(Boolean adhdFriendly) { this.adhdFriendly = adhdFriendly; }
    
    public Boolean getAutismFriendly() { return autismFriendly; }
    public void setAutismFriendly(Boolean autismFriendly) { this.autismFriendly = autismFriendly; }
    
    public Boolean getVisualImpairmentFriendly() { return visualImpairmentFriendly; }
    public void setVisualImpairmentFriendly(Boolean visualImpairmentFriendly) { this.visualImpairmentFriendly = visualImpairmentFriendly; }
    
    public Boolean getHearingImpairmentFriendly() { return hearingImpairmentFriendly; }
    public void setHearingImpairmentFriendly(Boolean hearingImpairmentFriendly) { this.hearingImpairmentFriendly = hearingImpairmentFriendly; }
    
    public Boolean getMotorImpairmentFriendly() { return motorImpairmentFriendly; }
    public void setMotorImpairmentFriendly(Boolean motorImpairmentFriendly) { this.motorImpairmentFriendly = motorImpairmentFriendly; }
    
    public String getFontType() { return fontType; }
    public void setFontType(String fontType) { this.fontType = fontType; }
    
    public String getReadingLevel() { return readingLevel; }
    public void setReadingLevel(String readingLevel) { this.readingLevel = readingLevel; }
    
    public Boolean getHasAudioDescription() { return hasAudioDescription; }
    public void setHasAudioDescription(Boolean hasAudioDescription) { this.hasAudioDescription = hasAudioDescription; }
    
    public Boolean getHasSubtitles() { return hasSubtitles; }
    public void setHasSubtitles(Boolean hasSubtitles) { this.hasSubtitles = hasSubtitles; }
    
    public String getCognitiveLoadLevel() { return cognitiveLoadLevel; }
    public void setCognitiveLoadLevel(String cognitiveLoadLevel) { this.cognitiveLoadLevel = cognitiveLoadLevel; }
    
    public String getInteractionType() { return interactionType; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
    
    public String getLearningStyles() { return learningStyles; }
    public void setLearningStyles(String learningStyles) { this.learningStyles = learningStyles; }
}
