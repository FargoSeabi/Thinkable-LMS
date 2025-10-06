package com.thinkable.backend.controller;

import com.thinkable.backend.entity.UserToolUsage;
import com.thinkable.backend.entity.UserAdaptiveInsight;
import com.thinkable.backend.repository.UserToolUsageRepository;
import com.thinkable.backend.service.PersonalPatternRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/neurodivergent")
public class NeurodivergentUsageController {

    private static final Logger logger = LoggerFactory.getLogger(NeurodivergentUsageController.class);

    @Autowired
    private UserToolUsageRepository toolUsageRepository;

    @Autowired
    private PersonalPatternRecognitionService patternService;

    @PostMapping("/usage")
    public ResponseEntity<Map<String, Object>> trackNeurodivergentUsage(@RequestBody Map<String, Object> usageData) {
        try {
            // Extract data from frontend payload
            String action = (String) usageData.get("action");
            Map<String, Object> details = (Map<String, Object>) usageData.get("details");
            Integer energyLevel = (Integer) usageData.get("energyLevel");
            String userPreset = (String) usageData.get("userPreset");
            String timestamp = (String) usageData.get("timestamp");

            // Extract userId from details or use a default approach
            Long userId = null;
            if (details != null && details.containsKey("userId")) {
                userId = Long.parseLong(details.get("userId").toString());
            }

            // Log for debugging
            logger.info("Tracking neurodivergent usage - Action: {}, EnergyLevel: {}, Preset: {}",
                       action, energyLevel, userPreset);

            // Create UserToolUsage entity
            UserToolUsage usage = new UserToolUsage();
            usage.setUserId(userId);
            usage.setToolName(mapActionToToolName(action));
            usage.setToolContext(action);
            usage.setUsageTimestamp(LocalDateTime.now());
            usage.setUserEnergyLevel(energyLevel);
            usage.setActivityContext(userPreset);

            // Set time-based data
            LocalDateTime now = LocalDateTime.now();
            usage.setTimeOfDay(getTimeOfDay(now.getHour()));
            usage.setDayOfWeek(now.getDayOfWeek().getValue() % 7);

            // Extract additional details
            if (details != null) {
                if (details.containsKey("duration")) {
                    Object durationObj = details.get("duration");
                    if (durationObj instanceof Number) {
                        usage.setSessionDurationMinutes(((Number) durationObj).intValue());
                    }
                }

                // Store additional data as JSON string
                usage.setAdditionalData(details.toString());
            }

            // Save to database
            UserToolUsage savedUsage = toolUsageRepository.save(usage);

            // Generate new insights based on updated usage patterns if userId is available
            if (userId != null) {
                try {
                    patternService.analyzeAndGenerateInsights(userId);
                } catch (Exception e) {
                    logger.warn("Failed to analyze patterns for user {}: {}", userId, e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Neurodivergent usage tracked successfully");
            response.put("usageId", savedUsage.getId());
            response.put("timestamp", savedUsage.getUsageTimestamp());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error tracking neurodivergent usage: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to track usage: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/usage-insights/{userId}")
    public ResponseEntity<Map<String, Object>> getNeurodivergentUsageInsights(@PathVariable Long userId) {
        try {
            // Get personalized recommendations from pattern recognition service
            Map<String, Object> recommendations = patternService.getPersonalizedRecommendations(userId);

            // Get presentation-ready insights
            List<UserAdaptiveInsight> insights = patternService.getInsightsForPresentation(userId);

            // Build response with insights and recommendations
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("lastUpdated", LocalDateTime.now());
            response.put("recommendations", recommendations);
            response.put("insights", insights);
            response.put("insightCount", insights.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating insights for user {}: {}", userId, e.getMessage());

            // Fallback to basic insights
            Map<String, Object> basicInsights = new HashMap<>();
            basicInsights.put("userId", userId);
            basicInsights.put("attentionPatterns", "Analysis in progress");
            basicInsights.put("recommendations", new HashMap<>());
            basicInsights.put("insights", new ArrayList<>());
            basicInsights.put("error", "Unable to generate detailed insights at this time");

            return ResponseEntity.ok(basicInsights);
        }
    }

    /**
     * Helper method to map frontend actions to tool names
     */
    private String mapActionToToolName(String action) {
        if (action == null) return "unknown";

        switch (action) {
            case "focus_session_started":
            case "focus_session_completed":
                return "focus_timer";
            case "break_taken":
                return "break_manager";
            case "overwhelm_escape_used":
                return "overwhelm_escape";
            case "energy_level_updated":
                return "energy_tracker";
            default:
                return action.replaceAll("_", "-");
        }
    }

    /**
     * Helper method to determine time of day
     */
    private String getTimeOfDay(int hour) {
        if (hour < 6) return "late_night";
        if (hour < 9) return "early_morning";
        if (hour < 12) return "late_morning";
        if (hour < 15) return "early_afternoon";
        if (hour < 18) return "late_afternoon";
        if (hour < 21) return "early_evening";
        return "late_evening";
    }
}
