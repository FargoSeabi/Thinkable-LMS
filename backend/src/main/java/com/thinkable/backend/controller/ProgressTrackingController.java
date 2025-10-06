package com.thinkable.backend.controller;

import com.thinkable.backend.service.ProgressTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressTrackingController {

    private static final Logger logger = LoggerFactory.getLogger(ProgressTrackingController.class);

    @Autowired
    private ProgressTrackingService progressService;

    /**
     * Get comprehensive learning metrics for a student
     */
    @GetMapping("/metrics/{userId}")
    public ResponseEntity<?> getLearningMetrics(@PathVariable Long userId) {
        try {
            logger.info("Fetching learning metrics for user: {}", userId);

            Map<String, Object> metrics = new HashMap<>();
            
            // Basic learning metrics
            metrics.put("totalStudyTime", progressService.getTotalStudyTime(userId));
            metrics.put("lessonsCompleted", progressService.getLessonsCompleted(userId));
            metrics.put("quizzesCompleted", progressService.getQuizzesCompleted(userId));
            metrics.put("averageQuizScore", progressService.getAverageQuizScore(userId));
            metrics.put("streakDays", progressService.getCurrentStreak(userId));
            metrics.put("toolsUsedCount", progressService.getAccessibilityToolsUsedCount(userId));
            
            // Weekly progress data
            metrics.put("weeklyProgress", getWeeklyProgressData(userId));
            
            // Skill progress tracking
            metrics.put("skillProgress", getSkillProgressData(userId));
            
            // Accessibility features used
            metrics.put("accessibilityFeaturesUsed", progressService.getActiveAccessibilityFeatures(userId));

            return ResponseEntity.ok(metrics);

        } catch (Exception e) {
            logger.error("Error fetching learning metrics for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch learning metrics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get accommodation usage statistics
     */
    @GetMapping("/accommodations/{userId}")
    public ResponseEntity<?> getAccommodationUsage(@PathVariable Long userId) {
        try {
            logger.info("Fetching accommodation usage for user: {}", userId);

            Map<String, Object> accommodations = new HashMap<>();
            accommodations.put("fontAdjustments", progressService.getFontAdjustmentCount(userId));
            accommodations.put("colorSchemeChanges", progressService.getColorSchemeChangeCount(userId));
            accommodations.put("textToSpeechUsage", progressService.getTextToSpeechUsageCount(userId));
            accommodations.put("focusModeUsage", progressService.getFocusModeUsageCount(userId));
            accommodations.put("breakReminders", progressService.getBreakReminderCount(userId));

            return ResponseEntity.ok(accommodations);

        } catch (Exception e) {
            logger.error("Error fetching accommodation usage for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch accommodation usage: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Record study session activity
     */
    @PostMapping("/record-session/{userId}")
    public ResponseEntity<?> recordStudySession(
            @PathVariable Long userId, 
            @RequestBody StudySessionRequest request) {
        try {
            logger.info("Recording study session for user: {}", userId);

            progressService.recordStudySession(
                userId,
                request.getLessonId(),
                request.getDurationMinutes(),
                request.getCompletionStatus(),
                request.getToolsUsed(),
                request.getQuizScore()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Study session recorded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error recording study session for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record study session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Record accessibility tool usage
     */
    @PostMapping("/record-tool-usage/{userId}")
    public ResponseEntity<?> recordToolUsage(
            @PathVariable Long userId,
            @RequestBody ToolUsageRequest request) {
        try {
            logger.info("Recording tool usage for user: {}", userId);

            progressService.recordToolUsage(
                userId,
                request.getToolName(),
                request.getUsageType(),
                request.getDurationSeconds(),
                request.getContext()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tool usage recorded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error recording tool usage for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record tool usage: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get personalized learning insights
     */
    @GetMapping("/insights/{userId}")
    public ResponseEntity<?> getLearningInsights(@PathVariable Long userId) {
        try {
            logger.info("Generating learning insights for user: {}", userId);

            Map<String, Object> insights = new HashMap<>();
            
            // Learning patterns analysis
            insights.put("bestPerformanceTime", progressService.getBestPerformanceTime(userId));
            insights.put("preferredLearningStyle", progressService.getPreferredLearningStyle(userId));
            insights.put("optimalSessionLength", progressService.getOptimalSessionLength(userId));
            insights.put("mostEffectiveTools", progressService.getMostEffectiveTools(userId));
            
            // Strengths and areas for improvement
            insights.put("strengths", progressService.getStrengths(userId));
            insights.put("recommendedFocus", progressService.getRecommendedFocusAreas(userId));
            insights.put("personalizedTips", progressService.getPersonalizedTips(userId));

            return ResponseEntity.ok(insights);

        } catch (Exception e) {
            logger.error("Error generating learning insights for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate learning insights: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get learning goals and progress
     */
    @GetMapping("/goals/{userId}")
    public ResponseEntity<?> getLearningGoals(@PathVariable Long userId) {
        try {
            logger.info("Fetching learning goals for user: {}", userId);

            List<Map<String, Object>> goals = progressService.getUserGoals(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("goals", goals);
            response.put("overallProgress", progressService.calculateOverallGoalProgress(userId));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching learning goals for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch learning goals: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Helper methods
    private List<Map<String, Object>> getWeeklyProgressData(Long userId) {
        List<Map<String, Object>> weeklyData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate weekStart = currentDate.minusWeeks(i);
            
            Map<String, Object> weekData = new HashMap<>();
            weekData.put("week", formatWeekLabel(weekStart));
            weekData.put("studyTime", progressService.getWeeklyStudyTime(userId, weekStart));
            weekData.put("lessonsCompleted", progressService.getWeeklyLessonsCompleted(userId, weekStart));
            weekData.put("quizScore", progressService.getWeeklyAverageQuizScore(userId, weekStart));
            
            weeklyData.add(weekData);
        }
        
        return weeklyData;
    }

    private List<Map<String, Object>> getSkillProgressData(Long userId) {
        List<Map<String, Object>> skillsData = new ArrayList<>();
        
        // Mathematics
        Map<String, Object> math = new HashMap<>();
        math.put("skillName", "Mathematics");
        math.put("progress", progressService.getSkillProgress(userId, "mathematics"));
        math.put("level", determineSkillLevel(progressService.getSkillProgress(userId, "mathematics")));
        math.put("lastPracticed", formatLastPracticed(progressService.getLastSkillPractice(userId, "mathematics")));
        skillsData.add(math);
        
        // Reading Comprehension
        Map<String, Object> reading = new HashMap<>();
        reading.put("skillName", "Reading Comprehension");
        reading.put("progress", progressService.getSkillProgress(userId, "reading"));
        reading.put("level", determineSkillLevel(progressService.getSkillProgress(userId, "reading")));
        reading.put("lastPracticed", formatLastPracticed(progressService.getLastSkillPractice(userId, "reading")));
        skillsData.add(reading);
        
        // Writing Skills
        Map<String, Object> writing = new HashMap<>();
        writing.put("skillName", "Writing Skills");
        writing.put("progress", progressService.getSkillProgress(userId, "writing"));
        writing.put("level", determineSkillLevel(progressService.getSkillProgress(userId, "writing")));
        writing.put("lastPracticed", formatLastPracticed(progressService.getLastSkillPractice(userId, "writing")));
        skillsData.add(writing);
        
        // Critical Thinking
        Map<String, Object> critical = new HashMap<>();
        critical.put("skillName", "Critical Thinking");
        critical.put("progress", progressService.getSkillProgress(userId, "critical_thinking"));
        critical.put("level", determineSkillLevel(progressService.getSkillProgress(userId, "critical_thinking")));
        critical.put("lastPracticed", formatLastPracticed(progressService.getLastSkillPractice(userId, "critical_thinking")));
        skillsData.add(critical);
        
        return skillsData;
    }

    private String formatWeekLabel(LocalDate weekStart) {
        return String.format("%d/%d", weekStart.getMonthValue(), weekStart.getDayOfMonth());
    }

    private String determineSkillLevel(int progress) {
        if (progress >= 90) return "Expert";
        if (progress >= 70) return "Advanced";
        if (progress >= 40) return "Intermediate";
        return "Beginner";
    }

    private String formatLastPracticed(LocalDateTime lastPracticed) {
        if (lastPracticed == null) return "Never";
        
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(lastPracticed, now).toDays();
        
        if (days == 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        if (days < 30) return (days / 7) + " weeks ago";
        return (days / 30) + " months ago";
    }

    // Request DTOs
    public static class StudySessionRequest {
        private String lessonId;
        private int durationMinutes;
        private String completionStatus;
        private List<String> toolsUsed;
        private Integer quizScore;

        // Getters and setters
        public String getLessonId() { return lessonId; }
        public void setLessonId(String lessonId) { this.lessonId = lessonId; }
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
        public String getCompletionStatus() { return completionStatus; }
        public void setCompletionStatus(String completionStatus) { this.completionStatus = completionStatus; }
        public List<String> getToolsUsed() { return toolsUsed; }
        public void setToolsUsed(List<String> toolsUsed) { this.toolsUsed = toolsUsed; }
        public Integer getQuizScore() { return quizScore; }
        public void setQuizScore(Integer quizScore) { this.quizScore = quizScore; }
    }

    public static class ToolUsageRequest {
        private String toolName;
        private String usageType;
        private int durationSeconds;
        private String context;

        // Getters and setters
        public String getToolName() { return toolName; }
        public void setToolName(String toolName) { this.toolName = toolName; }
        public String getUsageType() { return usageType; }
        public void setUsageType(String usageType) { this.usageType = usageType; }
        public int getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }
}
