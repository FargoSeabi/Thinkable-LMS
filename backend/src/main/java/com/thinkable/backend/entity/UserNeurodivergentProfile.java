package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Individual Neurodivergent Profile Entity
 * Stores comprehensive, personalized learning companion data for each user
 */
@Entity
@Table(name = "user_neurodivergent_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNeurodivergentProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    // Personal identity
    @Column(name = "preferred_name", length = 100)
    private String preferredName;
    
    @Column(name = "pronouns", length = 50)
    private String pronouns;
    
    // Multi-dimensional traits (0-10 scales)
    @Column(name = "hyperfocus_intensity")
    private Integer hyperfocusIntensity = 5;
    
    @Column(name = "attention_flexibility")
    private Integer attentionFlexibility = 5;
    
    @Column(name = "sensory_processing")
    private Integer sensoryProcessing = 5;
    
    @Column(name = "executive_function")
    private Integer executiveFunction = 5;
    
    @Column(name = "social_battery")
    private Integer socialBattery = 5;
    
    @Column(name = "change_adaptability")
    private Integer changeAdaptability = 5;
    
    @Column(name = "emotional_regulation")
    private Integer emotionalRegulation = 5;
    
    @Column(name = "information_processing")
    private Integer informationProcessing = 5;
    
    @Column(name = "creativity_expression")
    private Integer creativityExpression = 5;
    
    @Column(name = "structure_preference")
    private Integer structurePreference = 5;
    
    // Focus characteristics
    @Column(name = "optimal_session_length")
    private Integer optimalSessionLength = 25;
    
    @Column(name = "natural_rhythm", length = 20)
    private String naturalRhythm = "morning";
    
    @Column(name = "hyperfocus_warning_time")
    private Integer hyperfocusWarningTime = 90;
    
    @Column(name = "transition_time")
    private Integer transitionTime = 5;
    
    @Column(name = "deep_work_capacity")
    private Double deepWorkCapacity = 2.0;
    
    // Sensory preferences
    @Column(name = "auditory_preference", length = 20)
    private String auditoryPreference = "moderate";
    
    @Column(name = "visual_preference", length = 20)
    private String visualPreference = "calm";
    
    @Column(name = "tactile_comfort", length = 20)
    private String tactileComfort = "smooth";
    
    @Column(name = "movement_need", length = 20)
    private String movementNeed = "moderate";
    
    @Column(name = "light_sensitivity", length = 20)
    private String lightSensitivity = "medium";
    
    @Column(name = "temperature_preference", length = 20)
    private String temperaturePreference = "cool";
    
    // Energy and patterns
    @Column(name = "daily_pattern", length = 20)
    private String dailyPattern = "variable";
    
    // Learning preferences
    @Column(name = "primary_learning_style", length = 20)
    private String primaryLearningStyle = "mixed";
    
    @Column(name = "information_chunking", length = 20)
    private String informationChunking = "small";
    
    @Column(name = "feedback_preference", length = 20)
    private String feedbackPreference = "gentle";
    
    @Column(name = "mistake_handling", length = 20)
    private String mistakeHandling = "supportive";
    
    @Column(name = "motivation_style", length = 20)
    private String motivationStyle = "internal";
    
    @Column(name = "challenge_level", length = 20)
    private String challengeLevel = "adaptive";
    
    @Column(name = "learning_environment", length = 20)
    private String learningEnvironment = "quiet";
    
    @Column(name = "time_preference", length = 20)
    private String timePreference = "flexible";
    
    // Support preferences
    @Column(name = "celebration_style", length = 20)
    private String celebrationStyle = "quiet";
    
    @Column(name = "communication_style", length = 20)
    private String communicationStyle = "direct";
    
    @Column(name = "autonomy_level", length = 20)
    private String autonomyLevel = "high";
    
    @Column(name = "privacy_needs", length = 20)
    private String privacyNeeds = "respected";
    
    // Goals and growth
    @Column(name = "adaptation_speed", length = 20)
    private String adaptationSpeed = "gradual";
    
    // Metadata
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "version", length = 10)
    private String version = "1.0";
    
    // Note: Relationships to supporting entities will be added in future iterations
    // when those entities are created (UserDistractionTrigger, UserFocusEnhancer, etc.)
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods for business logic
    public boolean isHyperfocusIntense() {
        return hyperfocusIntensity != null && hyperfocusIntensity > 7;
    }
    
    public boolean isHighlyFlexible() {
        return attentionFlexibility != null && attentionFlexibility > 7;
    }
    
    public boolean isSensoryProcessingHigh() {
        return sensoryProcessing != null && sensoryProcessing > 7;
    }
    
    public boolean needsExecutiveSupport() {
        return executiveFunction != null && executiveFunction < 5;
    }
    
    public boolean needsEmotionalRegulationSupport() {
        return emotionalRegulation != null && emotionalRegulation < 5;
    }
    
    public boolean prefersStructure() {
        return structurePreference != null && structurePreference > 7;
    }
    
    // Calculate tool priorities based on individual traits
    public int calculateToolPriority(String toolName) {
        int basePriority = 5;
        
        switch (toolName.toLowerCase()) {
            case "escape_hatch":
                if (isSensoryProcessingHigh()) return 9;
                if (needsEmotionalRegulationSupport()) return 8;
                return basePriority;
                
            case "focus_timer":
                if (isHyperfocusIntense()) return 9;
                if (needsExecutiveSupport()) return 8;
                if (isHighlyFlexible()) return 3;
                return basePriority;
                
            case "fidget_tools":
                if (isHighlyFlexible()) return 8;
                if (isSensoryProcessingHigh()) return 7;
                if ("textured".equals(tactileComfort)) return 7;
                return basePriority;
                
            case "breathing_tool":
                if (needsEmotionalRegulationSupport()) return 9;
                if (isSensoryProcessingHigh()) return 8;
                return basePriority;
                
            case "energy_check":
                if (isHighlyFlexible()) return 7;
                if (needsExecutiveSupport()) return 7;
                return basePriority;
                
            default:
                return basePriority;
        }
    }
    
    // Generate personalized focus session options
    public java.util.Map<String, Object> generatePersonalizedFocusOptions() {
        java.util.Map<String, Object> options = new java.util.HashMap<>();
        
        // Primary recommendation
        options.put("optimal", java.util.Map.of(
            "duration", optimalSessionLength,
            "name", "Your Sweet Spot",
            "description", "Perfect for your focus style",
            "recommended", true
        ));
        
        // Additional options based on traits
        if (isHighlyFlexible()) {
            options.put("micro", java.util.Map.of(
                "duration", Math.max(10, optimalSessionLength - 10),
                "name", "Quick Burst",
                "description", "For when you need flexibility",
                "recommended", false
            ));
        }
        
        if (isHyperfocusIntense()) {
            options.put("extended", java.util.Map.of(
                "duration", Math.min(90, optimalSessionLength + 20),
                "name", "Deep Dive",
                "description", "For deep, sustained work",
                "recommended", false
            ));
        }
        
        if (needsExecutiveSupport()) {
            options.put("structured", java.util.Map.of(
                "duration", 25,
                "name", "Structured Flow",
                "description", "With built-in check-ins",
                "recommended", false
            ));
        }
        
        return options;
    }
}
