package com.thinkable.backend.service;

import com.thinkable.backend.entity.UserAdaptiveInsight;
import com.thinkable.backend.entity.UserNeurodivergentProfile;
import com.thinkable.backend.entity.UserToolUsage;
import com.thinkable.backend.repository.UserAdaptiveInsightRepository;
import com.thinkable.backend.repository.UserToolUsageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * Service for personal pattern recognition and adaptive insights
 * Analyzes user behavior to generate personalized learning insights
 */
@Service
@Transactional
public class PersonalPatternRecognitionService {
    
    @Autowired
    private UserToolUsageRepository toolUsageRepository;
    
    @Autowired
    private UserAdaptiveInsightRepository insightRepository;
    
    @Autowired
    private UserNeurodivergentProfileService profileService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Analyze user patterns and generate new insights
     */
    public List<UserAdaptiveInsight> analyzeAndGenerateInsights(Long userId) {
        List<UserAdaptiveInsight> newInsights = new ArrayList<>();
        
        // Basic pattern analysis
        newInsights.addAll(analyzeFocusPatterns(userId));
        newInsights.addAll(analyzeEnergyPatterns(userId));
        newInsights.addAll(analyzeToolEffectiveness(userId));
        newInsights.addAll(analyzeTemporalPatterns(userId));
        
        // Advanced pattern analysis
        newInsights.addAll(analyzeProductivityRhythms(userId));
        newInsights.addAll(analyzeStressPatterns(userId));
        newInsights.addAll(analyzeFlowStateDetection(userId));
        newInsights.addAll(analyzeLearningVelocity(userId));
        newInsights.addAll(analyzeContextualEffectiveness(userId));
        newInsights.addAll(analyzeAdaptationPatterns(userId));
        
        // Predictive insights
        newInsights.addAll(generatePredictiveRecommendations(userId));
        
        // Save insights that don't already exist
        return saveUniqueInsights(userId, newInsights);
    }
    
    /**
     * Get insights ready for presentation to user
     */
    public List<UserAdaptiveInsight> getInsightsForPresentation(Long userId) {
        return insightRepository.findHighConfidenceInsightsByUserId(userId, BigDecimal.valueOf(0.75))
                .stream()
                .limit(3) // Limit to top 3 insights to avoid overwhelming
                .collect(Collectors.toList());
    }
    
    /**
     * Record user response to an insight
     */
    public void recordInsightResponse(Long insightId, String response) {
        UserAdaptiveInsight insight = insightRepository.findById(insightId).orElse(null);
        if (insight != null) {
            switch (response.toLowerCase()) {
                case "accepted":
                    insight.acceptInsight();
                    break;
                case "rejected":
                    insight.rejectInsight();
                    break;
                default:
                    insight.setUserResponse(response);
                    insight.setRespondedAt(LocalDateTime.now());
            }
            insightRepository.save(insight);
        }
    }
    
    /**
     * Get personalized recommendations based on insights
     */
    public Map<String, Object> getPersonalizedRecommendations(Long userId) {
        List<UserAdaptiveInsight> acceptedInsights = insightRepository.findAcceptedInsightsByUserId(userId);
        Map<String, Object> recommendations = new HashMap<>();
        
        for (UserAdaptiveInsight insight : acceptedInsights) {
            try {
                Map<String, Object> insightData = objectMapper.readValue(insight.getInsightData(), Map.class);
                
                switch (insight.getInsightType()) {
                    case "focus_timing":
                        recommendations.put("optimalFocusTime", insightData.get("discovered"));
                        break;
                    case "tool_effectiveness":
                        recommendations.put("recommendedTools", insightData.get("effectiveTools"));
                        break;
                    case "energy_pattern":
                        recommendations.put("energyOptimization", insightData.get("recommendations"));
                        break;
                }
            } catch (JsonProcessingException e) {
                // Log error but continue processing other insights
                System.err.println("Error parsing insight data for insight " + insight.getId());
            }
        }
        
        return recommendations;
    }
    
    // Private analysis methods
    
    private List<UserAdaptiveInsight> analyzeFocusPatterns(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        
        List<UserToolUsage> recentUsage = toolUsageRepository.findRecentUsageByUserId(userId, oneWeekAgo);
        
        if (recentUsage.size() < 10) return insights; // Need sufficient data
        
        // Analyze optimal focus times
        Map<String, Long> timeSlotSuccess = recentUsage.stream()
                .filter(usage -> usage.isSuccessful())
                .collect(Collectors.groupingBy(UserToolUsage::getTimeSlot, Collectors.counting()));
        
        if (!timeSlotSuccess.isEmpty()) {
            String optimalTime = Collections.max(timeSlotSuccess.entrySet(), Map.Entry.comparingByValue()).getKey();
            UserNeurodivergentProfile profile = profileService.getOrCreateProfile(userId);
            
            if (!optimalTime.equals(profile.getNaturalRhythm())) {
                Map<String, Object> insightData = Map.of(
                    "discovered", optimalTime,
                    "current", profile.getNaturalRhythm(),
                    "successRate", calculateSuccessRate(recentUsage, optimalTime)
                );
                
                UserAdaptiveInsight insight = createInsight(
                    userId,
                    "focus_timing",
                    "Optimal Focus Time Discovered",
                    String.format("You seem to focus best during %s sessions", optimalTime),
                    insightData,
                    BigDecimal.valueOf(0.8),
                    "high"
                );
                
                insights.add(insight);
            }
        }
        
        return insights;
    }
    
    private List<UserAdaptiveInsight> analyzeEnergyPatterns(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        
        List<UserToolUsage> recentUsage = toolUsageRepository.findRecentUsageByUserId(userId, oneWeekAgo);
        
        // Analyze energy level patterns
        Map<Integer, Double> energySuccessRate = recentUsage.stream()
                .filter(usage -> usage.getUserEnergyLevel() != null)
                .collect(Collectors.groupingBy(
                    UserToolUsage::getUserEnergyLevel,
                    Collectors.averagingInt(usage -> usage.isSuccessful() ? 1 : 0)
                ));
        
        if (energySuccessRate.size() >= 3) {
            OptionalInt bestEnergyLevel = energySuccessRate.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .map(OptionalInt::of)
                    .orElse(OptionalInt.empty());
            
            if (bestEnergyLevel.isPresent()) {
                Map<String, Object> insightData = Map.of(
                    "optimalEnergyLevel", bestEnergyLevel.getAsInt(),
                    "successRateAtOptimal", energySuccessRate.get(bestEnergyLevel.getAsInt()),
                    "recommendations", generateEnergyRecommendations(bestEnergyLevel.getAsInt())
                );
                
                UserAdaptiveInsight insight = createInsight(
                    userId,
                    "energy_pattern",
                    "Energy Level Pattern Identified",
                    String.format("You perform best at energy level %d", bestEnergyLevel.getAsInt()),
                    insightData,
                    BigDecimal.valueOf(0.75),
                    "medium"
                );
                
                insights.add(insight);
            }
        }
        
        return insights;
    }
    
    private List<UserAdaptiveInsight> analyzeToolEffectiveness(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        
        // Get tool effectiveness data
        List<Object[]> toolRatings = toolUsageRepository.findMostUsedToolsByUserId(userId);
        
        Map<String, Double> toolEffectiveness = new HashMap<>();
        for (Object[] row : toolRatings) {
            String toolName = (String) row[0];
            Double avgRating = toolUsageRepository.getAverageSuccessRatingByUserAndTool(userId, toolName);
            if (avgRating != null) {
                toolEffectiveness.put(toolName, avgRating);
            }
        }
        
        // Find highly effective tools
        List<String> effectiveTools = toolEffectiveness.entrySet().stream()
                .filter(entry -> entry.getValue() >= 8.0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        if (effectiveTools.size() >= 2) {
            Map<String, Object> insightData = Map.of(
                "effectiveTools", effectiveTools,
                "effectiveness", toolEffectiveness,
                "recommendation", "Consider using these tools more frequently"
            );
            
            UserAdaptiveInsight insight = createInsight(
                userId,
                "tool_effectiveness",
                "High-Performing Tools Identified",
                "Some tools work particularly well for you",
                insightData,
                BigDecimal.valueOf(0.85),
                "high"
            );
            
            insights.add(insight);
        }
        
        return insights;
    }
    
    private List<UserAdaptiveInsight> analyzeTemporalPatterns(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        
        // Analyze day-of-week patterns
        List<Object[]> dayTypePatterns = toolUsageRepository.findUsagePatternsByDayType(userId);
        
        Map<String, Long> dayTypeUsage = new HashMap<>();
        for (Object[] row : dayTypePatterns) {
            String dayType = (String) row[0];
            Long usageCount = (Long) row[2];
            dayTypeUsage.merge(dayType, usageCount, Long::sum);
        }
        
        if (dayTypeUsage.containsKey("weekday") && dayTypeUsage.containsKey("weekend")) {
            long weekdayUsage = dayTypeUsage.get("weekday");
            long weekendUsage = dayTypeUsage.get("weekend");
            
            if (weekendUsage > weekdayUsage * 0.3) { // Significant weekend usage
                Map<String, Object> insightData = Map.of(
                    "weekdayUsage", weekdayUsage,
                    "weekendUsage", weekendUsage,
                    "pattern", "consistent_learner",
                    "recommendation", "You show consistent learning patterns across weekdays and weekends"
                );
                
                UserAdaptiveInsight insight = createInsight(
                    userId,
                    "temporal_pattern",
                    "Consistent Learning Pattern",
                    "You maintain learning consistency throughout the week",
                    insightData,
                    BigDecimal.valueOf(0.7),
                    "medium"
                );
                
                insights.add(insight);
            }
        }
        
        return insights;
    }
    
    private List<UserAdaptiveInsight> saveUniqueInsights(Long userId, List<UserAdaptiveInsight> newInsights) {
        List<UserAdaptiveInsight> savedInsights = new ArrayList<>();
        LocalDateTime recentThreshold = LocalDateTime.now().minusDays(7);
        
        for (UserAdaptiveInsight insight : newInsights) {
            // Check if similar insight already exists recently
            boolean exists = insightRepository.existsSimilarRecentInsight(
                userId,
                insight.getInsightType(),
                insight.getInsightTitle(),
                recentThreshold
            );
            
            if (!exists) {
                savedInsights.add(insightRepository.save(insight));
            }
        }
        
        return savedInsights;
    }
    
    private UserAdaptiveInsight createInsight(Long userId, String type, String title, String description,
                                             Map<String, Object> data, BigDecimal confidence, String priority) {
        UserAdaptiveInsight insight = new UserAdaptiveInsight();
        insight.setUserId(userId);
        insight.setInsightType(type);
        insight.setInsightTitle(title);
        insight.setInsightDescription(description);
        insight.setConfidenceScore(confidence);
        insight.setPriorityLevel(priority);
        insight.setCreatedAt(LocalDateTime.now());
        
        try {
            insight.setInsightData(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            insight.setInsightData("{}");
        }
        
        return insight;
    }
    
    private double calculateSuccessRate(List<UserToolUsage> usage, String timeSlot) {
        List<UserToolUsage> timeSlotUsage = usage.stream()
                .filter(u -> timeSlot.equals(u.getTimeSlot()))
                .collect(Collectors.toList());
        
        if (timeSlotUsage.isEmpty()) return 0.0;
        
        long successCount = timeSlotUsage.stream()
                .mapToLong(u -> u.isSuccessful() ? 1 : 0)
                .sum();
        
        return (double) successCount / timeSlotUsage.size();
    }
    
    private List<String> generateEnergyRecommendations(int optimalEnergyLevel) {
        List<String> recommendations = new ArrayList<>();
        
        if (optimalEnergyLevel >= 8) {
            recommendations.add("Schedule challenging tasks during high-energy periods");
            recommendations.add("Use energy tracking to identify peak performance times");
        } else if (optimalEnergyLevel <= 4) {
            recommendations.add("Focus on gentle, restorative activities during low-energy times");
            recommendations.add("Consider energy-building techniques before learning sessions");
        } else {
            recommendations.add("Moderate energy levels work well for you");
            recommendations.add("Maintain consistent energy through regular breaks");
        }
        
        return recommendations;
    }
    
    // Advanced Pattern Analysis Methods
    
    /**
     * Analyze productivity rhythms - when user is most productive
     */
    private List<UserAdaptiveInsight> analyzeProductivityRhythms(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        
        List<UserToolUsage> recentUsage = toolUsageRepository.findRecentUsageByUserId(userId, oneWeekAgo);
        
        if (recentUsage.size() < 20) return insights; // Need more data
        
        // Group by hour and calculate productivity score
        Map<Integer, List<UserToolUsage>> hourlyUsage = recentUsage.stream()
                .collect(Collectors.groupingBy(usage -> usage.getUsageTimestamp().getHour()));
        
        Map<Integer, Double> productivityByHour = new HashMap<>();
        for (Map.Entry<Integer, List<UserToolUsage>> entry : hourlyUsage.entrySet()) {
            List<UserToolUsage> hourUsage = entry.getValue();
            double avgSuccess = hourUsage.stream()
                    .filter(u -> u.getSuccessRating() != null)
                    .mapToInt(UserToolUsage::getSuccessRating)
                    .average()
                    .orElse(0.0);
            double avgDuration = hourUsage.stream()
                    .filter(u -> u.getSessionDurationMinutes() != null)
                    .mapToInt(UserToolUsage::getSessionDurationMinutes)
                    .average()
                    .orElse(0.0);
            
            // Productivity score combines success and sustained focus
            double productivityScore = (avgSuccess * 0.7) + (Math.min(avgDuration / 60.0, 1.0) * 0.3) * 10;
            productivityByHour.put(entry.getKey(), productivityScore);
        }
        
        // Find peak productivity hours
        List<Integer> peakHours = productivityByHour.entrySet().stream()
                .filter(entry -> entry.getValue() > 7.5)
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        if (!peakHours.isEmpty()) {
            Map<String, Object> insightData = Map.of(
                "peakHours", peakHours,
                "productivityScores", productivityByHour,
                "recommendation", String.format("Schedule important tasks during hours %s for optimal performance", peakHours)
            );
            
            UserAdaptiveInsight insight = createInsight(
                userId,
                "productivity_rhythm",
                "Peak Productivity Hours Identified", 
                String.format("Your most productive hours are %s", formatHourList(peakHours)),
                insightData,
                BigDecimal.valueOf(0.82),
                "high"
            );
            
            insights.add(insight);
        }
        
        return insights;
    }
    
    /**
     * Detect stress patterns and overwhelm indicators
     */
    private List<UserAdaptiveInsight> analyzeStressPatterns(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        
        List<UserToolUsage> recentUsage = toolUsageRepository.findRecentUsageByUserId(userId, oneWeekAgo);
        
        // Look for stress indicators: escape hatch usage, low success ratings, short sessions
        long escapeHatchUsage = recentUsage.stream()
                .filter(u -> "escape_hatch".equals(u.getToolName()))
                .count();
        
        double avgSuccessRate = recentUsage.stream()
                .filter(u -> u.getSuccessRating() != null)
                .mapToInt(UserToolUsage::getSuccessRating)
                .average()
                .orElse(7.0);
        
        // Detect stress patterns
        boolean highEscapeUsage = escapeHatchUsage > recentUsage.size() * 0.3;
        boolean lowSuccessRate = avgSuccessRate < 6.0;
        
        if (highEscapeUsage || lowSuccessRate) {
            List<String> stressIndicators = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();
            
            if (highEscapeUsage) {
                stressIndicators.add("frequent_overwhelm_tool_usage");
                recommendations.add("Consider shorter focus sessions to reduce overwhelm");
                recommendations.add("Practice grounding techniques during breaks");
            }
            
            if (lowSuccessRate) {
                stressIndicators.add("declining_success_ratings");
                recommendations.add("Take more frequent breaks to maintain performance");
                recommendations.add("Review current workload and adjust expectations");
            }
            
            Map<String, Object> insightData = Map.of(
                "stressIndicators", stressIndicators,
                "escapeHatchUsage", escapeHatchUsage,
                "avgSuccessRate", avgSuccessRate,
                "recommendations", recommendations
            );
            
            UserAdaptiveInsight insight = createInsight(
                userId,
                "stress_pattern",
                "Stress Pattern Detected",
                "Your usage patterns suggest you may be experiencing increased stress",
                insightData,
                BigDecimal.valueOf(0.78),
                "high"
            );
            
            insights.add(insight);
        }
        
        return insights;
    }
    
    /**
     * Detect flow state patterns - when user enters deep focus
     */
    private List<UserAdaptiveInsight> analyzeFlowStateDetection(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        
        List<UserToolUsage> focusUsage = toolUsageRepository.findRecentUsageByUserId(userId, oneWeekAgo)
                .stream()
                .filter(u -> "focus_timer".equals(u.getToolName()))
                .filter(u -> u.getSessionDurationMinutes() != null && u.getSuccessRating() != null)
                .collect(Collectors.toList());
        
        if (focusUsage.size() < 10) return insights;
        
        // Detect flow state: sessions > 45 minutes with high success (8+)
        List<UserToolUsage> flowSessions = focusUsage.stream()
                .filter(u -> u.getSessionDurationMinutes() > 45 && u.getSuccessRating() >= 8)
                .collect(Collectors.toList());
        
        if (flowSessions.size() >= 3) {
            // Analyze conditions that lead to flow
            Map<String, Long> energyLevelsInFlow = flowSessions.stream()
                    .filter(u -> u.getUserEnergyLevel() != null)
                    .collect(Collectors.groupingBy(
                        u -> u.getUserEnergyLevel().toString(),
                        Collectors.counting()
                    ));
            
            Map<String, Long> timeSlotsInFlow = flowSessions.stream()
                    .collect(Collectors.groupingBy(
                        UserToolUsage::getTimeSlot,
                        Collectors.counting()
                    ));
            
            String optimalEnergyForFlow = energyLevelsInFlow.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("unknown");
            
            String optimalTimeForFlow = timeSlotsInFlow.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("unknown");
            
            Map<String, Object> insightData = Map.of(
                "flowSessionCount", flowSessions.size(),
                "optimalEnergyForFlow", optimalEnergyForFlow,
                "optimalTimeForFlow", optimalTimeForFlow,
                "avgFlowDuration", flowSessions.stream().mapToInt(UserToolUsage::getSessionDurationMinutes).average().orElse(0),
                "flowTriggers", Map.of(
                    "energyLevels", energyLevelsInFlow,
                    "timeSlots", timeSlotsInFlow
                )
            );
            
            UserAdaptiveInsight insight = createInsight(
                userId,
                "flow_state",
                "Flow State Pattern Discovered",
                String.format("You enter flow state best at energy level %s during %s", optimalEnergyForFlow, optimalTimeForFlow),
                insightData,
                BigDecimal.valueOf(0.88),
                "high"
            );
            
            insights.add(insight);
        }
        
        return insights;
    }
    
    /**
     * Analyze learning velocity - how quickly user adapts and improves
     */
    private List<UserAdaptiveInsight> analyzeLearningVelocity(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);
        
        List<UserToolUsage> allUsage = toolUsageRepository.findRecentUsageByUserId(userId, twoWeeksAgo)
                .stream()
                .filter(u -> u.getSuccessRating() != null)
                .sorted(Comparator.comparing(UserToolUsage::getUsageTimestamp))
                .collect(Collectors.toList());
        
        if (allUsage.size() < 20) return insights;
        
        // Calculate rolling average success rate
        List<Double> rollingAverages = new ArrayList<>();
        int windowSize = 5;
        
        for (int i = windowSize; i <= allUsage.size(); i++) {
            double avg = allUsage.subList(i - windowSize, i).stream()
                    .mapToInt(UserToolUsage::getSuccessRating)
                    .average()
                    .orElse(0.0);
            rollingAverages.add(avg);
        }
        
        // Calculate trend
        double firstHalfAvg = rollingAverages.subList(0, rollingAverages.size() / 2).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        double secondHalfAvg = rollingAverages.subList(rollingAverages.size() / 2, rollingAverages.size()).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        double improvement = secondHalfAvg - firstHalfAvg;
        
        if (Math.abs(improvement) > 0.5) {
            String trendType = improvement > 0 ? "improving" : "declining";
            String description = improvement > 0 
                ? "Your learning effectiveness is steadily improving"
                : "Your learning effectiveness may be declining - consider adjusting your approach";
            
            Map<String, Object> insightData = Map.of(
                "improvement", improvement,
                "trend", trendType,
                "firstHalfAvg", firstHalfAvg,
                "secondHalfAvg", secondHalfAvg,
                "recommendations", improvement > 0 
                    ? List.of("Keep up the great work!", "Consider sharing your successful strategies")
                    : List.of("Try varying your learning approach", "Consider shorter sessions", "Focus on stress management")
            );
            
            UserAdaptiveInsight insight = createInsight(
                userId,
                "learning_velocity",
                "Learning Trend Detected",
                description,
                insightData,
                BigDecimal.valueOf(0.75),
                improvement > 0 ? "medium" : "high"
            );
            
            insights.add(insight);
        }
        
        return insights;
    }
    
    /**
     * Generate predictive recommendations based on patterns
     */
    private List<UserAdaptiveInsight> generatePredictiveRecommendations(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        
        UserNeurodivergentProfile profile = profileService.getOrCreateProfile(userId);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<UserToolUsage> recentUsage = toolUsageRepository.findRecentUsageByUserId(userId, oneWeekAgo);
        
        if (recentUsage.size() < 20) return insights;
        
        // Predict optimal next actions based on current patterns
        LocalDateTime now = LocalDateTime.now();
        String currentTimeSlot = getTimeSlotFromHour(now.getHour());
        int currentDayOfWeek = now.getDayOfWeek().getValue() % 7;
        
        // Find similar historical contexts
        List<UserToolUsage> similarContextUsage = recentUsage.stream()
                .filter(u -> currentTimeSlot.equals(u.getTimeSlot()))
                .filter(u -> u.getDayOfWeek() != null && u.getDayOfWeek().equals(currentDayOfWeek))
                .collect(Collectors.toList());
        
        if (!similarContextUsage.isEmpty()) {
            // Find most effective tool in this context
            Map<String, Double> toolEffectiveness = similarContextUsage.stream()
                    .filter(u -> u.getSuccessRating() != null)
                    .collect(Collectors.groupingBy(
                        UserToolUsage::getToolName,
                        Collectors.averagingInt(UserToolUsage::getSuccessRating)
                    ));
            
            String recommendedTool = toolEffectiveness.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("focus_timer");
            
            double effectiveness = toolEffectiveness.get(recommendedTool);
            
            if (effectiveness > 7.5) {
                Map<String, Object> insightData = Map.of(
                    "recommendedTool", recommendedTool,
                    "effectiveness", effectiveness,
                    "context", Map.of(
                        "timeSlot", currentTimeSlot,
                        "dayOfWeek", currentDayOfWeek
                    ),
                    "prediction", String.format("Based on your patterns, %s would be most effective right now", recommendedTool)
                );
                
                UserAdaptiveInsight insight = createInsight(
                    userId,
                    "predictive_recommendation",
                    "Optimal Tool Recommendation",
                    String.format("Right now would be a great time to use the %s", recommendedTool.replace("_", " ")),
                    insightData,
                    BigDecimal.valueOf(0.85),
                    "medium"
                );
                
                insights.add(insight);
            }
        }
        
        return insights;
    }
    
    /**
     * Analyze how environmental context affects performance
     */
    private List<UserAdaptiveInsight> analyzeContextualEffectiveness(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        
        List<UserToolUsage> recentUsage = toolUsageRepository.findRecentUsageByUserId(userId, oneWeekAgo);
        
        if (recentUsage.size() < 15) return insights;
        
        // Analyze effectiveness by activity context
        Map<String, Double> contextEffectiveness = recentUsage.stream()
                .filter(u -> u.getActivityContext() != null && u.getSuccessRating() != null)
                .collect(Collectors.groupingBy(
                    UserToolUsage::getActivityContext,
                    Collectors.averagingInt(UserToolUsage::getSuccessRating)
                ));
        
        if (contextEffectiveness.size() >= 2) {
            String mostEffectiveContext = contextEffectiveness.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("unknown");
            
            String leastEffectiveContext = contextEffectiveness.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("unknown");
            
            double contextDifference = contextEffectiveness.get(mostEffectiveContext) - 
                                     contextEffectiveness.get(leastEffectiveContext);
            
            if (contextDifference > 1.5) {
                Map<String, Object> insightData = Map.of(
                    "mostEffective", mostEffectiveContext,
                    "leastEffective", leastEffectiveContext,
                    "effectivenessDifference", contextDifference,
                    "contextScores", contextEffectiveness,
                    "recommendation", String.format("Try to work more in %s contexts and avoid %s when possible", 
                                                  mostEffectiveContext, leastEffectiveContext)
                );
                
                UserAdaptiveInsight insight = createInsight(
                    userId,
                    "contextual_effectiveness",
                    "Environmental Context Pattern",
                    String.format("You perform %.1f points better in %s contexts", contextDifference, mostEffectiveContext),
                    insightData,
                    BigDecimal.valueOf(0.76),
                    "medium"
                );
                
                insights.add(insight);
            }
        }
        
        return insights;
    }
    
    /**
     * Analyze how well user adapts to recommendations over time
     */
    private List<UserAdaptiveInsight> analyzeAdaptationPatterns(Long userId) {
        List<UserAdaptiveInsight> insights = new ArrayList<>();
        
        // Get accepted insights from the past
        List<UserAdaptiveInsight> acceptedInsights = insightRepository.findAcceptedInsightsByUserId(userId);
        
        if (acceptedInsights.size() < 3) return insights;
        
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<UserToolUsage> recentUsage = toolUsageRepository.findRecentUsageByUserId(userId, oneWeekAgo);
        
        // Analyze if user is following through on accepted recommendations
        int implementedRecommendations = 0;
        for (UserAdaptiveInsight acceptedInsight : acceptedInsights) {
            if (acceptedInsight.getInsightType().equals("focus_timing") || 
                acceptedInsight.getInsightType().equals("productivity_rhythm")) {
                
                // Check if user is actually using optimal times suggested
                try {
                    Map<String, Object> insightData = objectMapper.readValue(acceptedInsight.getInsightData(), Map.class);
                    Object discoveredTime = insightData.get("discovered");
                    
                    if (discoveredTime != null) {
                        long optimalUsage = recentUsage.stream()
                                .filter(u -> discoveredTime.toString().equals(u.getTimeSlot()))
                                .count();
                        
                        if (optimalUsage > recentUsage.size() * 0.3) {
                            implementedRecommendations++;
                        }
                    }
                } catch (JsonProcessingException e) {
                    // Skip this insight
                }
            }
        }
        
        double adaptationRate = (double) implementedRecommendations / acceptedInsights.size();
        
        if (adaptationRate > 0.6) {
            Map<String, Object> insightData = Map.of(
                "adaptationRate", adaptationRate,
                "implementedCount", implementedRecommendations,
                "totalAccepted", acceptedInsights.size(),
                "adaptationType", "high_adaptation",
                "recommendation", "You're excellent at implementing insights! Keep up the great work."
            );
            
            UserAdaptiveInsight insight = createInsight(
                userId,
                "adaptation_pattern",
                "High Adaptation Rate",
                String.format("You successfully implement %.0f%% of accepted recommendations", adaptationRate * 100),
                insightData,
                BigDecimal.valueOf(0.85),
                "medium"
            );
            
            insights.add(insight);
        } else if (adaptationRate < 0.3) {
            Map<String, Object> insightData = Map.of(
                "adaptationRate", adaptationRate,
                "implementedCount", implementedRecommendations,
                "totalAccepted", acceptedInsights.size(),
                "adaptationType", "low_adaptation",
                "recommendations", List.of(
                    "Try implementing one recommendation at a time",
                    "Set reminders to help with new habits",
                    "Start with smaller, easier changes"
                )
            );
            
            UserAdaptiveInsight insight = createInsight(
                userId,
                "adaptation_pattern",
                "Adaptation Support Needed",
                "It looks like implementing recommendations might be challenging",
                insightData,
                BigDecimal.valueOf(0.72),
                "medium"
            );
            
            insights.add(insight);
        }
        
        return insights;
    }
    
    // Helper methods
    
    private String formatHourList(List<Integer> hours) {
        return hours.stream()
                .map(h -> String.format("%d:00", h))
                .collect(Collectors.joining(", "));
    }
    
    private String getTimeSlotFromHour(int hour) {
        if (hour < 6) return "late_night";
        if (hour < 9) return "early_morning";
        if (hour < 12) return "late_morning";
        if (hour < 15) return "early_afternoon";
        if (hour < 18) return "late_afternoon";
        if (hour < 21) return "early_evening";
        return "late_evening";
    }
}
