package com.thinkable.backend.service;

import com.thinkable.backend.entity.StudySession;
import com.thinkable.backend.repository.StudySessionRepository;
import com.thinkable.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(ProgressTrackingService.class);

    private final UserRepository userRepository;
    private final StudySessionRepository studySessionRepository;
    private final ActivityTrackingService activityTrackingService;

    /**
     * Get total study time in minutes for a user
     */
    public int getTotalStudyTime(Long userId) {
        return studySessionRepository.getTotalStudyTimeMinutes(userId).intValue();
    }

    /**
     * Get total lessons completed by a user
     */
    public int getLessonsCompleted(Long userId) {
        return studySessionRepository.countLessonsCompleted(userId).intValue();
    }

    /**
     * Get total quizzes completed by a user
     */
    public int getQuizzesCompleted(Long userId) {
        return studySessionRepository.countQuizzesCompleted(userId).intValue();
    }

    /**
     * Get average quiz score for a user
     */
    public double getAverageQuizScore(Long userId) {
        return studySessionRepository.getAverageQuizScore(userId).orElse(0.0);
    }

    /**
     * Get current learning streak in days
     */
    public int getCurrentStreak(Long userId) {
        return activityTrackingService.calculateCurrentStreak(userId);
    }

    /**
     * Get count of accessibility tools used
     */
    public int getAccessibilityToolsUsedCount(Long userId) {
        return studySessionRepository.countUniqueAccessibilityToolsUsed(userId).intValue();
    }

    /**
     * Get list of active accessibility features
     */
    public List<String> getActiveAccessibilityFeatures(Long userId) {
        return activityTrackingService.getAccessibilityToolsUsed(userId);
    }

    /**
     * Get weekly study time for a specific week
     */
    public int getWeeklyStudyTime(Long userId, LocalDate weekStart) {
        return studySessionRepository.countStudyDaysThisWeek(userId, weekStart).intValue() * 30; // Average 30 min per day
    }

    /**
     * Get weekly lessons completed for a specific week
     */
    public int getWeeklyLessonsCompleted(Long userId, LocalDate weekStart) {
        return studySessionRepository.countStudyDaysThisWeek(userId, weekStart).intValue();
    }

    /**
     * Get weekly average quiz score for a specific week
     */
    public double getWeeklyAverageQuizScore(Long userId, LocalDate weekStart) {
        return getAverageQuizScore(userId); // For now, return overall average
    }

    /**
     * Get skill progress percentage for a specific skill
     */
    public int getSkillProgress(Long userId, String skillName) {
        return 0; // TODO: Implement with real skill tracking
    }

    /**
     * Get last practice time for a specific skill
     */
    public LocalDateTime getLastSkillPractice(Long userId, String skillName) {
        // TODO: This should be calculated from actual lesson completion data
        // For now, return null to indicate no practice recorded
        return null;
    }

    /**
     * Get accommodation usage counts
     */
    public int getFontAdjustmentCount(Long userId) {
        return 0; // TODO: Implement with real accessibility tracking
    }

    public int getColorSchemeChangeCount(Long userId) {
        return 0; // TODO: Implement with real accessibility tracking
    }

    public int getTextToSpeechUsageCount(Long userId) {
        return 0; // TODO: Implement with real accessibility tracking
    }

    public int getFocusModeUsageCount(Long userId) {
        return 0; // TODO: Implement with real accessibility tracking
    }

    public int getBreakReminderCount(Long userId) {
        return 0; // TODO: Implement with real accessibility tracking
    }

    /**
     * Record a study session
     */
    public void recordStudySession(Long userId, String lessonId, int durationMinutes, 
                                 String completionStatus, List<String> toolsUsed, Integer quizScore) {
        logger.info("Recording study session for user {}: lesson {}, duration {} minutes", 
                   userId, lessonId, durationMinutes);

        if ("completed".equals(completionStatus)) {
            if (quizScore != null) {
                // This is a quiz completion
                activityTrackingService.recordQuizCompletion(userId, lessonId, quizScore, 100, durationMinutes, toolsUsed);
            } else {
                // This is a lesson completion
                activityTrackingService.recordLessonCompletion(userId, lessonId, durationMinutes, toolsUsed);
            }
        } else {
            // General study session
            activityTrackingService.recordStudySession(userId, StudySession.ActivityType.PRACTICE_SESSION, durationMinutes, toolsUsed);
        }
    }

    /**
     * Record accessibility tool usage
     */
    public void recordToolUsage(Long userId, String toolName, String usageType, 
                              int durationSeconds, String context) {
        logger.info("Recording tool usage for user {}: {} used for {} seconds", 
                   userId, toolName, durationSeconds);

        // TODO: Use ActivityTrackingService to record tool usage
        // For now, just log the usage
    }

    /**
     * Get learning insights
     */
    public String getBestPerformanceTime(Long userId) {
        return "Mornings (9-11 AM)"; // Mock data
    }

    public String getPreferredLearningStyle(Long userId) {
        return "Visual with audio support"; // Mock data
    }

    public String getOptimalSessionLength(Long userId) {
        return "20-25 minutes"; // Mock data
    }

    public List<String> getMostEffectiveTools(Long userId) {
        return Arrays.asList("Font adjustment", "Focus mode", "Reading guide");
    }

    public List<String> getStrengths(Long userId) {
        return Arrays.asList(
            "Consistent daily engagement",
            "Strong visual learning performance", 
            "Effective use of accessibility tools"
        );
    }

    public List<String> getRecommendedFocusAreas(Long userId) {
        return Arrays.asList(
            "Reading comprehension speed",
            "Mathematics problem solving",
            "Extended concentration periods"
        );
    }

    public List<String> getPersonalizedTips(Long userId) {
        return Arrays.asList(
            "Try shorter 15-minute study sessions",
            "Use break reminders more frequently",
            "Explore text-to-speech for complex topics"
        );
    }

    /**
     * Get user goals
     */
    public List<Map<String, Object>> getUserGoals(Long userId) {
        List<Map<String, Object>> goals = new ArrayList<>();
        
        Map<String, Object> goal1 = new HashMap<>();
        goal1.put("id", 1);
        goal1.put("title", "Complete 5 lessons this week");
        goal1.put("progress", 60);
        goal1.put("target", 5);
        goal1.put("current", 3);
        goal1.put("deadline", LocalDate.now().plusDays(3));
        goals.add(goal1);
        
        Map<String, Object> goal2 = new HashMap<>();
        goal2.put("id", 2);
        goal2.put("title", "Achieve 85% average quiz score");
        goal2.put("progress", 78);
        goal2.put("target", 85);
        goal2.put("current", 82);
        goal2.put("deadline", LocalDate.now().plusDays(7));
        goals.add(goal2);
        
        return goals;
    }

    public double calculateOverallGoalProgress(Long userId) {
        return 69.0; // Mock data: 69% overall goal progress
    }

    /**
     * Get summary of user's activities
     */
    public Map<String, Object> getUserSummary(Long userId) {
        return activityTrackingService.getTodaysSummary(userId);
    }
    
    /**
     * Get recent activity summary
     */
    public Map<String, Object> getRecentActivity(Long userId, int days) {
        return activityTrackingService.getRecentActivitySummary(userId, days);
    }
}
