package com.thinkable.backend.controller;

import com.thinkable.backend.entity.StudySession;
import com.thinkable.backend.service.ActivityTrackingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {
    
    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);
    
    private final ActivityTrackingService activityTrackingService;
    
    /**
     * Record a lesson completion
     */
    @PostMapping("/lesson-completed")
    public ResponseEntity<Map<String, Object>> recordLessonCompletion(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String lessonId = (String) request.get("lessonId");
            Integer durationMinutes = (Integer) request.getOrDefault("durationMinutes", 15);
            List<String> accessibilityTools = (List<String>) request.get("accessibilityTools");
            
            StudySession session = activityTrackingService.recordLessonCompletion(userId, lessonId, durationMinutes, accessibilityTools);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lesson completion recorded successfully!");
            response.put("sessionId", session.getId());
            response.put("currentStreak", activityTrackingService.calculateCurrentStreak(userId));
            
            logger.info("Lesson completion recorded for user {}, lesson {}", userId, lessonId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error recording lesson completion: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to record lesson completion: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Record a quiz completion
     */
    @PostMapping("/quiz-completed")
    public ResponseEntity<Map<String, Object>> recordQuizCompletion(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String quizId = (String) request.get("quizId");
            Integer score = (Integer) request.get("score");
            Integer maxScore = (Integer) request.getOrDefault("maxScore", 100);
            Integer durationMinutes = (Integer) request.getOrDefault("durationMinutes", 10);
            List<String> accessibilityTools = (List<String>) request.get("accessibilityTools");
            
            StudySession session = activityTrackingService.recordQuizCompletion(userId, quizId, score, maxScore, durationMinutes, accessibilityTools);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quiz completion recorded successfully!");
            response.put("sessionId", session.getId());
            response.put("scorePercentage", session.getScorePercentage());
            response.put("currentStreak", activityTrackingService.calculateCurrentStreak(userId));
            
            logger.info("Quiz completion recorded for user {}, quiz {}, score {}/{}", userId, quizId, score, maxScore);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error recording quiz completion: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to record quiz completion: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Record a general study session
     */
    @PostMapping("/study-session")
    public ResponseEntity<Map<String, Object>> recordStudySession(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String activityTypeStr = (String) request.getOrDefault("activityType", "PRACTICE_SESSION");
            StudySession.ActivityType activityType = StudySession.ActivityType.valueOf(activityTypeStr);
            Integer durationMinutes = (Integer) request.getOrDefault("durationMinutes", 5);
            List<String> accessibilityTools = (List<String>) request.get("accessibilityTools");
            
            StudySession session = activityTrackingService.recordStudySession(userId, activityType, durationMinutes, accessibilityTools);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Study session recorded successfully!");
            response.put("sessionId", session.getId());
            response.put("currentStreak", activityTrackingService.calculateCurrentStreak(userId));
            
            logger.info("Study session recorded for user {}, type {}, duration {} min", userId, activityType, durationMinutes);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error recording study session: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to record study session: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get user's current streak
     */
    @GetMapping("/user/{userId}/streak")
    public ResponseEntity<Map<String, Object>> getCurrentStreak(@PathVariable Long userId) {
        try {
            Integer currentStreak = activityTrackingService.calculateCurrentStreak(userId);
            Map<String, Object> todaysSummary = activityTrackingService.getTodaysSummary(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("currentStreak", currentStreak);
            response.put("todaysSummary", todaysSummary);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting streak for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get streak: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get user's activity summary
     */
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getActivitySummary(@PathVariable Long userId,
                                                                @RequestParam(defaultValue = "7") int days) {
        try {
            Map<String, Object> summary = activityTrackingService.getRecentActivitySummary(userId, days);
            Map<String, Integer> userMetrics = activityTrackingService.getUserMetrics(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recentSummary", summary);
            response.put("overallMetrics", userMetrics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting activity summary for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get activity summary: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Quick activity tracker - for simple interactions
     */
    @PostMapping("/quick-activity")
    public ResponseEntity<Map<String, Object>> recordQuickActivity(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String action = (String) request.get("action"); // "read", "interact", "explore", etc.
            Integer durationMinutes = (Integer) request.getOrDefault("durationMinutes", 2);
            
            StudySession.ActivityType activityType;
            switch (action.toLowerCase()) {
                case "read":
                    activityType = StudySession.ActivityType.READING_SESSION;
                    break;
                case "watch":
                    activityType = StudySession.ActivityType.VIDEO_WATCHED;
                    break;
                case "explore":
                    activityType = StudySession.ActivityType.CONTENT_DISCOVERY;
                    break;
                default:
                    activityType = StudySession.ActivityType.INTERACTION_COMPLETED;
            }
            
            StudySession session = activityTrackingService.recordStudySession(userId, activityType, durationMinutes, null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Activity recorded!");
            response.put("currentStreak", activityTrackingService.calculateCurrentStreak(userId));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error recording quick activity: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to record activity");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
