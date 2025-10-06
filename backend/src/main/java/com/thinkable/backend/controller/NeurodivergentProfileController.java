package com.thinkable.backend.controller;

import java.util.stream.Collectors;

import com.thinkable.backend.entity.UserAdaptiveInsight;
import com.thinkable.backend.entity.UserNeurodivergentProfile;
import com.thinkable.backend.entity.UserToolUsage;
import com.thinkable.backend.repository.UserToolUsageRepository;
import com.thinkable.backend.service.PersonalPatternRecognitionService;
import com.thinkable.backend.service.UserNeurodivergentProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Neurodivergent Profile Management
 * Handles API endpoints for individual learning companion functionality
 */
@RestController
@RequestMapping("/api/neurodivergent")
public class NeurodivergentProfileController {
    
    @Autowired
    private UserNeurodivergentProfileService profileService;
    
    @Autowired
    private PersonalPatternRecognitionService patternService;
    
    @Autowired
    private UserToolUsageRepository toolUsageRepository;
    
    /**
     * Get or create user's neurodivergent profile
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserNeurodivergentProfile> getProfile(@PathVariable Long userId) {
        try {
            UserNeurodivergentProfile profile = profileService.getOrCreateProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update user's neurodivergent profile
     */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserNeurodivergentProfile> updateProfile(
            @PathVariable Long userId,
            @RequestBody UserNeurodivergentProfile profileData) {
        try {
            // Ensure the profile belongs to the correct user
            profileData.setUserId(userId);
            UserNeurodivergentProfile updatedProfile = profileService.updateProfile(profileData);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update profile from assessment results
     */
    @PostMapping("/profile/{userId}/from-assessment")
    public ResponseEntity<UserNeurodivergentProfile> updateFromAssessment(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> assessmentData) {
        try {
            UserNeurodivergentProfile updatedProfile = profileService.updateFromAssessment(userId, assessmentData);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get personalized tool priorities
     */
    @GetMapping("/profile/{userId}/tool-priorities")
    public ResponseEntity<Map<String, Integer>> getToolPriorities(@PathVariable Long userId) {
        try {
            Map<String, Integer> priorities = profileService.calculateToolPriorities(userId);
            return ResponseEntity.ok(priorities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get personalized recommendations
     */
    @GetMapping("/profile/{userId}/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations(@PathVariable Long userId) {
        try {
            Map<String, Object> recommendations = profileService.generatePersonalizedRecommendations(userId);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Record tool usage for pattern analysis
     */
    @PostMapping("/usage/{userId}")
    public ResponseEntity<UserToolUsage> recordToolUsage(
            @PathVariable Long userId,
            @RequestBody UserToolUsage usageData) {
        try {
            // Set user ID and timestamp
            usageData.setUserId(userId);
            usageData.setUsageTimestamp(LocalDateTime.now());
            
            // Set time-based data
            LocalDateTime now = LocalDateTime.now();
            usageData.setTimeOfDay(getTimeOfDay(now.getHour()));
            usageData.setDayOfWeek(now.getDayOfWeek().getValue() % 7); // Convert to 0-6 format
            
            UserToolUsage savedUsage = toolUsageRepository.save(usageData);
            return ResponseEntity.ok(savedUsage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get user's tool usage history
     */
    @GetMapping("/usage/{userId}")
    public ResponseEntity<List<UserToolUsage>> getToolUsageHistory(@PathVariable Long userId) {
        try {
            List<UserToolUsage> usageHistory = toolUsageRepository.findByUserIdOrderByUsageTimestampDesc(userId);
            return ResponseEntity.ok(usageHistory);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get personalized insights
     */
    @GetMapping("/insights/{userId}")
    public ResponseEntity<List<UserAdaptiveInsight>> getInsights(@PathVariable Long userId) {
        try {
            List<UserAdaptiveInsight> insights = patternService.getInsightsForPresentation(userId);
            return ResponseEntity.ok(insights);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Respond to an insight
     */
    @PostMapping("/insights/{insightId}/respond")
    public ResponseEntity<Void> respondToInsight(
            @PathVariable Long insightId,
            @RequestBody Map<String, String> response) {
        try {
            String userResponse = response.get("response");
            patternService.recordInsightResponse(insightId, userResponse);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Trigger pattern analysis for user
     */
    @PostMapping("/analyze/{userId}")
    public ResponseEntity<List<UserAdaptiveInsight>> analyzePatterns(@PathVariable Long userId) {
        try {
            List<UserAdaptiveInsight> newInsights = patternService.analyzeAndGenerateInsights(userId);
            return ResponseEntity.ok(newInsights);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get profile statistics and analytics
     */
    @GetMapping("/profile/{userId}/statistics")
    public ResponseEntity<Map<String, Object>> getProfileStatistics(@PathVariable Long userId) {
        try {
            Map<String, Object> stats = profileService.getProfileStatistics(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Check if profile needs updating
     */
    @GetMapping("/profile/{userId}/needs-update")
    public ResponseEntity<Map<String, Boolean>> checkProfileUpdate(@PathVariable Long userId) {
        try {
            boolean needsUpdate = profileService.needsProfileUpdate(userId);
            return ResponseEntity.ok(Map.of("needsUpdate", needsUpdate));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get similar profiles for peer insights (anonymized)
     */
    @GetMapping("/profile/{userId}/similar")
    public ResponseEntity<List<Map<String, Object>>> getSimilarProfiles(@PathVariable Long userId) {
        try {
            List<UserNeurodivergentProfile> similarProfiles = profileService.findSimilarProfiles(userId, 5);
            
            // Anonymize the data - remove personal identifiers
            List<Map<String, Object>> anonymizedProfiles = similarProfiles.stream()
                    .map(this::anonymizeProfile)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(anonymizedProfiles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get tool effectiveness analytics
     */
    @GetMapping("/usage/{userId}/effectiveness")
    public ResponseEntity<Map<String, Object>> getToolEffectiveness(@PathVariable Long userId) {
        try {
            Map<String, Object> effectiveness = Map.of(
                "mostUsedTools", toolUsageRepository.findMostUsedToolsByUserId(userId),
                "highEnergyTools", toolUsageRepository.findHighEnergyToolsByUserId(userId),
                "lowEnergyTools", toolUsageRepository.findLowEnergyToolsByUserId(userId),
                "peakHours", toolUsageRepository.findPeakUsageHoursByUserId(userId)
            );
            return ResponseEntity.ok(effectiveness);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Helper methods
    
    private String getTimeOfDay(int hour) {
        if (hour < 6) return "late_night";
        if (hour < 9) return "early_morning";
        if (hour < 12) return "late_morning";
        if (hour < 15) return "early_afternoon";
        if (hour < 18) return "late_afternoon";
        if (hour < 21) return "early_evening";
        return "late_evening";
    }
    
    private Map<String, Object> anonymizeProfile(UserNeurodivergentProfile profile) {
        return Map.of(
            "traits", Map.of(
                "hyperfocusIntensity", profile.getHyperfocusIntensity(),
                "attentionFlexibility", profile.getAttentionFlexibility(),
                "sensoryProcessing", profile.getSensoryProcessing(),
                "executiveFunction", profile.getExecutiveFunction()
            ),
            "preferences", Map.of(
                "optimalSessionLength", profile.getOptimalSessionLength(),
                "naturalRhythm", profile.getNaturalRhythm(),
                "learningEnvironment", profile.getLearningEnvironment()
            ),
            "supportNeeds", Map.of(
                "needsExecutiveSupport", profile.needsExecutiveSupport(),
                "needsEmotionalSupport", profile.needsEmotionalRegulationSupport(),
                "highSensoryProcessing", profile.isSensoryProcessingHigh()
            )
        );
    }
}
