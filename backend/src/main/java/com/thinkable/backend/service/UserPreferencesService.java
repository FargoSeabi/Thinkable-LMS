package com.thinkable.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkable.backend.model.User;
import com.thinkable.backend.model.UserAssessment;
import com.thinkable.backend.model.AdaptiveUISettings;
import com.thinkable.backend.repository.UserRepository;
import com.thinkable.backend.repository.UserAssessmentRepository;
import com.thinkable.backend.repository.AdaptiveUISettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserPreferencesService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAssessmentRepository userAssessmentRepository;

    @Autowired
    private AdaptiveUISettingsRepository adaptiveUISettingsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getUserPreferences(String username) {
        try {
            User user = userRepository.findByEmail(username);
            if (user == null) {
                return getDefaultPreferences(username);
            }

            // FIRST: Check AdaptiveUISettings for user's manual choices
            Optional<AdaptiveUISettings> settingsOpt = adaptiveUISettingsRepository.findByUserId(user.getId());
            if (settingsOpt.isPresent()) {
                AdaptiveUISettings settings = settingsOpt.get();
                // If user has made manual choices (autoApplied = false), honor them
                if (!settings.getAutoApplied()) {
                    String backendPreset = settings.getUiPreset();
                    String frontendPreset = mapBackendToFrontendPreset(backendPreset);
                    System.out.println("DEBUG: Manual override for " + username);
                    System.out.println("DEBUG: Backend preset from DB: '" + backendPreset + "'");
                    System.out.println("DEBUG: Mapped to frontend preset: '" + frontendPreset + "'");
                    System.out.println("DEBUG: autoApplied = " + settings.getAutoApplied());

                    Map<String, Object> preferences = new HashMap<>();
                    preferences.put("username", username);
                    preferences.put("currentPreset", frontendPreset);
                    preferences.put("manualOverride", true); // User's manual choice
                    preferences.put("customSettings", getCustomSettingsFromUISettings(settings));
                    preferences.put("lastUpdated", settings.getLastUpdated());
                    preferences.put("source", "manual_override"); // Debug info
                    System.out.println("Loading manual preferences from AdaptiveUISettings for " + username + ": " + backendPreset + " -> " + frontendPreset);
                    return preferences;
                }
            }

            // SECOND: Fallback to assessment recommendations (if no manual override)
            Optional<UserAssessment> assessmentOpt = userAssessmentRepository.findTopByUserIdOrderByAssessmentDateDesc(user.getId());
            if (assessmentOpt.isPresent()) {
                UserAssessment assessment = assessmentOpt.get();
                if (assessment.getAssessmentCompleted() && assessment.getRecommendedPreset() != null) {
                    Map<String, Object> preferences = new HashMap<>();
                    preferences.put("username", username);
                    preferences.put("currentPreset", mapBackendToFrontendPreset(assessment.getRecommendedPreset()));
                    preferences.put("manualOverride", false); // Assessment-based is automatic
                    preferences.put("customSettings", getCustomSettingsFromAssessment(assessment));
                    preferences.put("lastUpdated", assessment.getAssessmentDate());
                    preferences.put("source", "assessment_recommendation"); // Debug info
                    System.out.println("Loading assessment preferences from UserAssessment for " + username + ": " + assessment.getRecommendedPreset());
                    return preferences;
                }
            }

            // THIRD: Check if there are any AdaptiveUISettings at all (even auto-applied ones)
            if (settingsOpt.isPresent()) {
                AdaptiveUISettings settings = settingsOpt.get();
                Map<String, Object> preferences = new HashMap<>();
                preferences.put("username", username);
                preferences.put("currentPreset", mapBackendToFrontendPreset(settings.getUiPreset()));
                preferences.put("manualOverride", false); // Auto-applied setting
                preferences.put("customSettings", getCustomSettingsFromUISettings(settings));
                preferences.put("lastUpdated", settings.getLastUpdated());
                preferences.put("source", "auto_applied_settings"); // Debug info
                System.out.println("Loading auto-applied preferences from AdaptiveUISettings for " + username + ": " + settings.getUiPreset());
                return preferences;
            }

            // FINAL FALLBACK: Default preferences
            System.out.println("No preferences found for " + username + ", using defaults");
            return getDefaultPreferences(username);

        } catch (Exception e) {
            System.err.println("Error loading preferences for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            return getDefaultPreferences(username);
        }
    }
    
    private Map<String, Object> getDefaultPreferences(String username) {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("username", username);
        preferences.put("currentPreset", "STANDARD_ADAPTIVE");
        preferences.put("manualOverride", false);
        preferences.put("customSettings", getDefaultCustomSettings());
        preferences.put("lastUpdated", LocalDateTime.now());
        return preferences;
    }
    
    private Map<String, Boolean> getCustomSettingsFromUISettings(AdaptiveUISettings settings) {
        Map<String, Boolean> customSettings = new HashMap<>();
        customSettings.put("highContrast", settings.isHighContrast());
        customSettings.put("dyslexiaFriendly", settings.needsDyslexiaSupport());
        customSettings.put("adhdFriendly", settings.needsSimplifiedUI());
        customSettings.put("autismFriendly", settings.needsReducedMotion());
        customSettings.put("sensoryFriendly", settings.needsReducedMotion() && !settings.isHighContrast());
        return customSettings;
    }

    private Map<String, Boolean> getCustomSettingsFromAssessment(UserAssessment assessment) {
        Map<String, Boolean> customSettings = new HashMap<>();

        // Derive settings from assessment scores and recommended preset
        String preset = assessment.getRecommendedPreset();

        // Set accessibility features based on preset and assessment scores
        customSettings.put("dyslexiaFriendly",
            "READING_SUPPORT".equals(preset) || assessment.getReadingDifficultyScore() >= 15);

        customSettings.put("adhdFriendly",
            "FOCUS_ENHANCED".equals(preset) || assessment.getAttentionScore() >= 18);

        customSettings.put("autismFriendly",
            "SOCIAL_SIMPLE".equals(preset) || assessment.getSocialCommunicationScore() >= 16);

        customSettings.put("sensoryFriendly",
            "SENSORY_CALM".equals(preset) || assessment.getSensoryProcessingScore() >= 14);

        customSettings.put("highContrast",
            assessment.getReadingDifficultyScore() >= 20 || assessment.getSensoryProcessingScore() >= 20);

        return customSettings;
    }

    public void saveUserPreferences(String username, Map<String, Object> preferences) {
        try {
            System.out.println("Starting to save preferences for user: " + username);
            User user = userRepository.findByEmail(username);
            if (user == null) {
                System.err.println("User not found for preferences save: " + username);
                return;
            }
            System.out.println("Found user with ID: " + user.getId());
            
            // Get or create AdaptiveUISettings
            AdaptiveUISettings settings = adaptiveUISettingsRepository.findByUserId(user.getId())
                    .orElse(new AdaptiveUISettings(user.getId()));
            
            // Update settings from preferences
            String currentPreset = (String) preferences.get("currentPreset");
            Boolean manualOverride = (Boolean) preferences.get("manualOverride");
            @SuppressWarnings("unchecked")
            Map<String, Boolean> customSettings = (Map<String, Boolean>) preferences.get("customSettings");
            
            if (currentPreset != null) {
                settings.setUiPreset(mapFrontendToBackendPreset(currentPreset));
                settings.applyPresetDefaults(settings.getUiPreset());
            }
            
            if (manualOverride != null) {
                settings.setAutoApplied(!manualOverride);
            }
            
            // Apply custom settings
            if (customSettings != null) {
                applyCustomSettingsToUISettings(settings, customSettings);
            }
            
            settings.setLastUpdated(LocalDateTime.now());
            System.out.println("About to save settings for user ID: " + settings.getUserId());
            AdaptiveUISettings savedSettings = adaptiveUISettingsRepository.save(settings);
            System.out.println("Successfully saved settings with ID: " + savedSettings.getId());
            
            // Log this preference change
            logPresetChange(username, preferences);
            
        } catch (Exception e) {
            System.err.println("Error saving preferences for user " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String mapFrontendToBackendPreset(String frontendPreset) {
        // Map frontend preset names to backend preset names
        switch (frontendPreset) {
            case "STANDARD_ADAPTIVE": return "standard";
            case "READING_SUPPORT": return "dyslexia";
            case "FOCUS_ENHANCED": return "adhd";
            case "FOCUS_CALM": return "dyslexia-adhd";
            case "SENSORY_CALM": return "sensory";
            case "SOCIAL_SIMPLE": return "autism";
            default: return "standard";
        }
    }
    
    private String mapBackendToFrontendPreset(String backendPreset) {
        // Map backend preset names to frontend preset names
        switch (backendPreset) {
            case "standard": return "STANDARD_ADAPTIVE";
            case "dyslexia": return "READING_SUPPORT";
            case "adhd": return "FOCUS_ENHANCED";
            case "dyslexia-adhd": return "FOCUS_CALM";
            case "sensory": return "SENSORY_CALM";
            case "autism": return "SOCIAL_SIMPLE";
            default: return "STANDARD_ADAPTIVE";
        }
    }
    
    private void applyCustomSettingsToUISettings(AdaptiveUISettings settings, Map<String, Boolean> customSettings) {
        Boolean highContrast = customSettings.get("highContrast");
        Boolean dyslexiaFriendly = customSettings.get("dyslexiaFriendly");
        Boolean adhdFriendly = customSettings.get("adhdFriendly");
        Boolean autismFriendly = customSettings.get("autismFriendly");
        Boolean sensoryFriendly = customSettings.get("sensoryFriendly");
        
        if (highContrast != null && highContrast) {
            settings.setContrastLevel("high");
            settings.setTextColor("#000000");
            settings.setBackgroundColor("#ffffff");
        }
        
        if (dyslexiaFriendly != null && dyslexiaFriendly) {
            settings.setFontFamily("Comic Neue, OpenDyslexic, cursive");
            settings.setFontSize(18);
        }
        
        if (adhdFriendly != null && adhdFriendly) {
            settings.setBreakIntervals(15);
            settings.setTimerStyle("adhd");
            settings.setAnimationsEnabled(true);
        }
        
        if (autismFriendly != null && autismFriendly) {
            settings.setAnimationsEnabled(false);
            settings.setTimerStyle("quiet");
        }
        
        if (sensoryFriendly != null && sensoryFriendly) {
            settings.setBackgroundColor("#f8f9fa");
            settings.setTextColor("#495057");
            settings.setAnimationsEnabled(false);
        }
    }

    public Map<String, Object> getPresetHistory(String username) {
        Map<String, Object> history = new HashMap<>();
        
        // Mock data for now - this would come from actual usage tracking
        List<Map<String, Object>> usageHistory = Arrays.asList(
            createHistoryEntry("STANDARD_ADAPTIVE", "Assessment Recommended", "2024-01-15T10:00:00"),
            createHistoryEntry("READING_SUPPORT", "Manual Selection", "2024-01-16T14:30:00"),
            createHistoryEntry("SENSORY_CALM", "Manual Selection", "2024-01-17T09:15:00")
        );
        
        Map<String, Integer> usageStats = new HashMap<>();
        usageStats.put("STANDARD_ADAPTIVE", 45);
        usageStats.put("READING_SUPPORT", 120);
        usageStats.put("SENSORY_CALM", 85);
        usageStats.put("FOCUS_ENHANCED", 30);
        
        Map<String, Object> recommendations = new HashMap<>();
        recommendations.put("mostUsed", "READING_SUPPORT");
        recommendations.put("suggested", "FOCUS_ENHANCED");
        recommendations.put("reason", "Based on your usage patterns and assessment scores");
        
        history.put("recentChanges", usageHistory);
        history.put("usageStats", usageStats);
        history.put("recommendations", recommendations);
        history.put("totalSessions", 280);
        history.put("averageSessionLength", "25 minutes");
        
        return history;
    }

    public void logPresetUsage(String username, String presetName) {
        // Find user to validate but don't require for logging
        User user = userRepository.findByEmail(username);
        if (user == null) {
            System.out.println("Warning: User not found for logging: " + username);
        }

        // Log preset usage - this would be stored in a usage tracking table
        System.out.println("Logging preset usage - User: " + username + ", Preset: " + presetName + ", Time: " + LocalDateTime.now());
        
        // In a real implementation, you'd save this to a database table:
        // INSERT INTO user_preset_usage (user_id, preset_name, used_at) VALUES (?, ?, ?)
    }

    public void resetUserPreferences(String username) {
        // Find user to validate but don't require for reset
        User user = userRepository.findByEmail(username);
        if (user == null) {
            System.out.println("Warning: User not found for reset: " + username);
        }

        // Reset user preferences to defaults
        // This would clear stored preferences and usage history
        System.out.println("Resetting preferences for user: " + username);
    }

    private Map<String, Object> createHistoryEntry(String presetName, String changeReason, String timestamp) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("presetName", presetName);
        entry.put("changeReason", changeReason);
        entry.put("timestamp", timestamp);
        entry.put("duration", generateRandomDuration());
        return entry;
    }

    private String generateRandomDuration() {
        String[] durations = {"15 min", "32 min", "45 min", "1h 20min", "25 min", "50 min"};
        return durations[new Random().nextInt(durations.length)];
    }

    private void logPresetChange(String username, Map<String, Object> preferences) {
        System.out.println("Preference change logged for " + username + " at " + LocalDateTime.now());
        System.out.println("New preferences: " + preferences);
    }


    private Map<String, Boolean> getDefaultCustomSettings() {
        Map<String, Boolean> settings = new HashMap<>();
        settings.put("highContrast", false);
        settings.put("dyslexiaFriendly", false);
        settings.put("adhdFriendly", false);
        settings.put("autismFriendly", false);
        settings.put("sensoryFriendly", false);
        return settings;
    }
}
