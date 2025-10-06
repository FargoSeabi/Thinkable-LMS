package com.thinkable.backend.controller;

import com.thinkable.backend.entity.UserAchievement;
import com.thinkable.backend.service.AchievementService;
import com.thinkable.backend.service.ProgressTrackingService;
import com.thinkable.backend.service.ActivityTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {
    
    private final AchievementService achievementService;
    private final ProgressTrackingService progressTrackingService;
    private final ActivityTrackingService activityTrackingService;
    
    /**
     * Get all achievements earned by a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAchievements(@PathVariable Long userId) {
        try {
            List<UserAchievement> achievements = achievementService.getUserAchievements(userId);
            Map<String, Object> stats = achievementService.getAchievementStats(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("achievements", achievements);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get new achievements for a user (not yet viewed)
     */
    @GetMapping("/user/{userId}/new")
    public ResponseEntity<Map<String, Object>> getNewAchievements(@PathVariable Long userId) {
        try {
            List<UserAchievement> newAchievements = achievementService.getNewAchievements(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newAchievements", newAchievements);
            response.put("count", newAchievements.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get achievement progress for a user (including locked achievements)
     */
    @GetMapping("/user/{userId}/progress")
    public ResponseEntity<Map<String, Object>> getAchievementProgress(@PathVariable Long userId) {
        try {
            // Get current user metrics from activity tracking service
            Map<String, Integer> userMetrics = activityTrackingService.getUserMetrics(userId);
            
            List<Map<String, Object>> progress = achievementService.getAchievementProgress(userId, userMetrics);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("progress", progress);
            response.put("userMetrics", userMetrics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Mark achievements as viewed
     */
    @PostMapping("/user/{userId}/mark-viewed")
    public ResponseEntity<Map<String, Object>> markAchievementsAsViewed(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> achievementIds = (List<Long>) request.get("achievementIds");
            
            if (achievementIds != null) {
                achievementService.markAchievementsAsViewed(userId, achievementIds);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Achievements marked as viewed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Initialize default achievements (admin endpoint)
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeDefaultAchievements() {
        try {
            achievementService.initializeDefaultAchievements();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Default achievements initialized");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Manually check and award achievements for a user (admin endpoint)
     */
    @PostMapping("/user/{userId}/check")
    public ResponseEntity<Map<String, Object>> checkAchievements(@PathVariable Long userId) {
        try {
            // Use the activity tracking service to check achievements
            activityTrackingService.checkAndAwardAchievements(userId);
            
            // Get the updated user metrics and achievements
            Map<String, Integer> userMetrics = activityTrackingService.getUserMetrics(userId);
            List<UserAchievement> allAchievements = achievementService.getUserAchievements(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("allAchievements", allAchievements);
            response.put("userMetrics", userMetrics);
            response.put("message", "Achievement check completed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
