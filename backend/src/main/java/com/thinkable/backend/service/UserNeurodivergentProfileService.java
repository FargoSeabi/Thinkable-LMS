package com.thinkable.backend.service;

import java.util.stream.Collectors;

import com.thinkable.backend.entity.UserNeurodivergentProfile;
import com.thinkable.backend.repository.UserNeurodivergentProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service class for UserNeurodivergentProfile operations
 * Handles business logic for individual neurodivergent learning profiles
 */
@Service
@Transactional
public class UserNeurodivergentProfileService {
    
    @Autowired
    private UserNeurodivergentProfileRepository profileRepository;
    
    /**
     * Get or create a neurodivergent profile for a user
     */
    public UserNeurodivergentProfile getOrCreateProfile(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
    }
    
    /**
     * Create a default profile for a new user
     */
    private UserNeurodivergentProfile createDefaultProfile(Long userId) {
        UserNeurodivergentProfile profile = new UserNeurodivergentProfile();
        profile.setUserId(userId);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        profile.setVersion("1.0");
        return profileRepository.save(profile);
    }
    
    /**
     * Update a user's neurodivergent profile
     */
    public UserNeurodivergentProfile updateProfile(UserNeurodivergentProfile profile) {
        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(profile);
    }
    
    /**
     * Update profile from assessment results
     */
    public UserNeurodivergentProfile updateFromAssessment(Long userId, Map<String, Object> assessmentData) {
        UserNeurodivergentProfile profile = getOrCreateProfile(userId);
        
        // Update traits based on assessment results
        updateTraitsFromAssessment(profile, assessmentData);
        
        // Update preferences based on responses
        updatePreferencesFromAssessment(profile, assessmentData);
        
        // Update focus characteristics
        updateFocusCharacteristicsFromAssessment(profile, assessmentData);
        
        return updateProfile(profile);
    }
    
    /**
     * Calculate personalized tool priorities for a user
     */
    public Map<String, Integer> calculateToolPriorities(Long userId) {
        UserNeurodivergentProfile profile = getOrCreateProfile(userId);
        
        Map<String, Integer> priorities = new HashMap<>();
        String[] tools = {"escape_hatch", "focus_timer", "fidget_tools", "breathing_tool", "energy_check"};
        
        for (String tool : tools) {
            priorities.put(tool, profile.calculateToolPriority(tool));
        }
        
        return priorities;
    }
    
    /**
     * Generate personalized recommendations based on profile
     */
    public Map<String, Object> generatePersonalizedRecommendations(Long userId) {
        UserNeurodivergentProfile profile = getOrCreateProfile(userId);
        
        Map<String, Object> recommendations = new HashMap<>();
        
        // Focus session recommendations
        recommendations.put("focusOptions", profile.generatePersonalizedFocusOptions());
        
        // Tool priorities
        recommendations.put("toolPriorities", calculateToolPriorities(userId));
        
        // Environment recommendations
        recommendations.put("environment", generateEnvironmentRecommendations(profile));
        
        // Break recommendations
        recommendations.put("breaks", generateBreakRecommendations(profile));
        
        // Learning style recommendations
        recommendations.put("learningStyle", generateLearningStyleRecommendations(profile));
        
        return recommendations;
    }
    
    /**
     * Find users with similar profiles for peer insights
     */
    public List<UserNeurodivergentProfile> findSimilarProfiles(Long userId, int limit) {
        UserNeurodivergentProfile userProfile = getOrCreateProfile(userId);
        
        List<UserNeurodivergentProfile> similarProfiles = profileRepository.findSimilarProfiles(
            userProfile.getHyperfocusIntensity(),
            userProfile.getAttentionFlexibility(),
            userProfile.getSensoryProcessing(),
            userId
        );
        
        return similarProfiles.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get profile statistics for analytics
     */
    public Map<String, Object> getProfileStatistics(Long userId) {
        UserNeurodivergentProfile profile = getOrCreateProfile(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("profileAge", calculateProfileAge(profile));
        stats.put("traitStrengths", identifyTraitStrengths(profile));
        stats.put("supportNeeds", identifySupportNeeds(profile));
        stats.put("adaptationRecommendations", generateAdaptationRecommendations(profile));
        
        return stats;
    }
    
    /**
     * Check if profile needs updating based on usage patterns
     */
    public boolean needsProfileUpdate(Long userId) {
        UserNeurodivergentProfile profile = profileRepository.findByUserId(userId).orElse(null);
        
        if (profile == null) return true;
        
        // Check if profile is older than 3 months
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        if (profile.getUpdatedAt().isBefore(threeMonthsAgo)) {
            return true;
        }
        
        // Check if version is outdated
        return !"1.0".equals(profile.getVersion());
    }
    
    // Private helper methods
    
    private void updateTraitsFromAssessment(UserNeurodivergentProfile profile, Map<String, Object> assessmentData) {
        // Extract trait values from assessment responses
        if (assessmentData.containsKey("hyperfocusIntensity")) {
            profile.setHyperfocusIntensity((Integer) assessmentData.get("hyperfocusIntensity"));
        }
        if (assessmentData.containsKey("attentionFlexibility")) {
            profile.setAttentionFlexibility((Integer) assessmentData.get("attentionFlexibility"));
        }
        if (assessmentData.containsKey("sensoryProcessing")) {
            profile.setSensoryProcessing((Integer) assessmentData.get("sensoryProcessing"));
        }
        if (assessmentData.containsKey("executiveFunction")) {
            profile.setExecutiveFunction((Integer) assessmentData.get("executiveFunction"));
        }
        if (assessmentData.containsKey("emotionalRegulation")) {
            profile.setEmotionalRegulation((Integer) assessmentData.get("emotionalRegulation"));
        }
    }
    
    private void updatePreferencesFromAssessment(UserNeurodivergentProfile profile, Map<String, Object> assessmentData) {
        // Update sensory preferences
        if (assessmentData.containsKey("auditoryPreference")) {
            profile.setAuditoryPreference((String) assessmentData.get("auditoryPreference"));
        }
        if (assessmentData.containsKey("visualPreference")) {
            profile.setVisualPreference((String) assessmentData.get("visualPreference"));
        }
        if (assessmentData.containsKey("learningEnvironment")) {
            profile.setLearningEnvironment((String) assessmentData.get("learningEnvironment"));
        }
    }
    
    private void updateFocusCharacteristicsFromAssessment(UserNeurodivergentProfile profile, Map<String, Object> assessmentData) {
        if (assessmentData.containsKey("optimalSessionLength")) {
            profile.setOptimalSessionLength((Integer) assessmentData.get("optimalSessionLength"));
        }
        if (assessmentData.containsKey("naturalRhythm")) {
            profile.setNaturalRhythm((String) assessmentData.get("naturalRhythm"));
        }
    }
    
    private Map<String, Object> generateEnvironmentRecommendations(UserNeurodivergentProfile profile) {
        Map<String, Object> env = new HashMap<>();
        
        // Lighting recommendations
        if ("high".equals(profile.getLightSensitivity())) {
            env.put("lighting", "Use soft, diffused lighting. Avoid fluorescent lights.");
        } else {
            env.put("lighting", "Natural light preferred when available.");
        }
        
        // Sound recommendations
        if ("quiet".equals(profile.getAuditoryPreference())) {
            env.put("sound", "Minimize background noise. Consider noise-cancelling headphones.");
        } else if ("background".equals(profile.getAuditoryPreference())) {
            env.put("sound", "Light background music or white noise may help focus.");
        }
        
        return env;
    }
    
    private Map<String, Object> generateBreakRecommendations(UserNeurodivergentProfile profile) {
        Map<String, Object> breaks = new HashMap<>();
        
        if (profile.isHyperfocusIntense()) {
            breaks.put("frequency", "Every 60-90 minutes with alerts");
            breaks.put("type", "Forced breaks to prevent hyperfocus exhaustion");
        } else if (profile.isHighlyFlexible()) {
            breaks.put("frequency", "Every 20-25 minutes as needed");
            breaks.put("type", "Flexible micro-breaks");
        } else {
            breaks.put("frequency", "Every 45 minutes");
            breaks.put("type", "Regular movement breaks");
        }
        
        return breaks;
    }
    
    private Map<String, Object> generateLearningStyleRecommendations(UserNeurodivergentProfile profile) {
        Map<String, Object> learning = new HashMap<>();
        
        if ("small".equals(profile.getInformationChunking())) {
            learning.put("content", "Break content into small, digestible chunks");
        }
        
        if ("gentle".equals(profile.getFeedbackPreference())) {
            learning.put("feedback", "Provide supportive, constructive feedback");
        }
        
        return learning;
    }
    
    private long calculateProfileAge(UserNeurodivergentProfile profile) {
        return java.time.Duration.between(profile.getCreatedAt(), LocalDateTime.now()).toDays();
    }
    
    private List<String> identifyTraitStrengths(UserNeurodivergentProfile profile) {
        List<String> strengths = new ArrayList<>();
        
        if (profile.getHyperfocusIntensity() > 7) {
            strengths.add("Deep focus capability");
        }
        if (profile.getCreativityExpression() > 7) {
            strengths.add("Creative thinking");
        }
        if (profile.getInformationProcessing() > 7) {
            strengths.add("Information processing");
        }
        
        return strengths;
    }
    
    private List<String> identifySupportNeeds(UserNeurodivergentProfile profile) {
        List<String> needs = new ArrayList<>();
        
        if (profile.needsExecutiveSupport()) {
            needs.add("Executive function support");
        }
        if (profile.needsEmotionalRegulationSupport()) {
            needs.add("Emotional regulation tools");
        }
        if (profile.isSensoryProcessingHigh()) {
            needs.add("Sensory accommodation");
        }
        
        return needs;
    }
    
    private List<String> generateAdaptationRecommendations(UserNeurodivergentProfile profile) {
        List<String> recommendations = new ArrayList<>();
        
        if ("gradual".equals(profile.getAdaptationSpeed())) {
            recommendations.add("Introduce changes slowly");
            recommendations.add("Provide advance notice of updates");
        }
        
        if (profile.prefersStructure()) {
            recommendations.add("Maintain consistent interface layout");
            recommendations.add("Provide clear navigation patterns");
        }
        
        return recommendations;
    }
}
