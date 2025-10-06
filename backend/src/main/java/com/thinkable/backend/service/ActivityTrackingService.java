package com.thinkable.backend.service;

import com.thinkable.backend.entity.StudySession;
import com.thinkable.backend.entity.UserAchievement;
import com.thinkable.backend.repository.StudySessionRepository;
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
public class ActivityTrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ActivityTrackingService.class);
    
    private final StudySessionRepository studySessionRepository;
    private final AchievementService achievementService;
    
    /**
     * Record a new study session and check for achievements
     */
    public StudySession recordActivity(Long userId, StudySession.ActivityType activityType, 
                                     String details, Integer durationMinutes, 
                                     Integer score, Integer maxScore, 
                                     List<String> accessibilityTools) {
        
        StudySession session = new StudySession();
        session.setUserId(userId);
        session.setActivityType(activityType);
        session.setActivityDetails(details);
        session.setDurationMinutes(durationMinutes);
        session.setScore(score);
        session.setMaxScore(maxScore);
        session.setCompleted(true);
        session.setCompletedAt(LocalDateTime.now());
        
        // Convert accessibility tools to JSON string
        if (accessibilityTools != null && !accessibilityTools.isEmpty()) {
            session.setAccessibilityToolsUsed(String.join(",", accessibilityTools));
        }
        
        StudySession saved = studySessionRepository.save(session);
        
        // Check for new achievements after recording activity
        checkAndAwardAchievements(userId);
        
        logger.info("Recorded {} activity for user {}, duration: {} min", 
                   activityType, userId, durationMinutes);
        
        return saved;
    }
    
    /**
     * Record lesson completion
     */
    public StudySession recordLessonCompletion(Long userId, String lessonId, Integer durationMinutes, List<String> accessibilityTools) {
        return recordActivity(userId, StudySession.ActivityType.LESSON_COMPLETED, 
                            "lesson_id:" + lessonId, durationMinutes, null, null, accessibilityTools);
    }
    
    /**
     * Record quiz completion
     */
    public StudySession recordQuizCompletion(Long userId, String quizId, Integer score, Integer maxScore, 
                                           Integer durationMinutes, List<String> accessibilityTools) {
        return recordActivity(userId, StudySession.ActivityType.QUIZ_TAKEN, 
                            "quiz_id:" + quizId, durationMinutes, score, maxScore, accessibilityTools);
    }
    
    /**
     * Record general study session
     */
    public StudySession recordStudySession(Long userId, StudySession.ActivityType activityType, 
                                         Integer durationMinutes, List<String> accessibilityTools) {
        return recordActivity(userId, activityType, null, durationMinutes, null, null, accessibilityTools);
    }
    
    /**
     * Calculate current study streak for user
     */
    public Integer calculateCurrentStreak(Long userId) {
        List<LocalDate> studyDates = studySessionRepository.getDistinctStudyDates(userId);
        
        if (studyDates.isEmpty()) {
            return 0;
        }
        
        // Sort dates in descending order (most recent first)
        studyDates.sort(Collections.reverseOrder());
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // Check if user studied today or yesterday to continue streak
        if (!studyDates.contains(today) && !studyDates.contains(yesterday)) {
            return 0; // Streak is broken
        }
        
        int streak = 0;
        LocalDate checkDate = studyDates.contains(today) ? today : yesterday;
        
        // Count consecutive days backwards from most recent study day
        for (LocalDate studyDate : studyDates) {
            if (studyDate.equals(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else if (studyDate.isBefore(checkDate)) {
                break; // Gap found, streak ends
            }
        }
        
        return streak;
    }
    
    /**
     * Get comprehensive user metrics for achievement calculations
     */
    public Map<String, Integer> getUserMetrics(Long userId) {
        Map<String, Integer> metrics = new HashMap<>();
        
        // Lessons completed
        Long lessonsCompleted = studySessionRepository.countLessonsCompleted(userId);
        metrics.put("LESSONS_COMPLETED", lessonsCompleted.intValue());
        
        // Quizzes completed
        Long quizzesCompleted = studySessionRepository.countQuizzesCompleted(userId);
        metrics.put("QUIZZES_COMPLETED", quizzesCompleted.intValue());
        
        // Current streak
        Integer currentStreak = calculateCurrentStreak(userId);
        metrics.put("DAYS_STREAK", currentStreak);
        
        // Total study time
        Long totalStudyTime = studySessionRepository.getTotalStudyTimeMinutes(userId);
        metrics.put("TOTAL_STUDY_TIME", totalStudyTime.intValue());
        
        // Highest quiz score
        Optional<Integer> highestScore = studySessionRepository.getHighestQuizScore(userId);
        metrics.put("QUIZ_SCORE", highestScore.orElse(0));
        
        // Accessibility tools count
        Long toolsCount = studySessionRepository.countUniqueAccessibilityToolsUsed(userId);
        metrics.put("TOOLS_USED_COUNT", toolsCount.intValue());
        
        // Study days this week
        LocalDate weekStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        Long studyDaysThisWeek = studySessionRepository.countStudyDaysThisWeek(userId, weekStart);
        metrics.put("WEEKLY_STUDY_DAYS", studyDaysThisWeek.intValue());
        
        // Study days this month
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        Long studyDaysThisMonth = studySessionRepository.countStudyDaysThisMonth(userId, monthStart);
        metrics.put("MONTHLY_STUDY_DAYS", studyDaysThisMonth.intValue());
        
        logger.debug("User {} metrics: {}", userId, metrics);
        return metrics;
    }
    
    /**
     * Check and award achievements based on current user metrics
     */
    public void checkAndAwardAchievements(Long userId) {
        try {
            logger.info("Checking achievements for user {}", userId);
            Map<String, Integer> userMetrics = getUserMetrics(userId);
            logger.debug("User {} metrics: {}", userId, userMetrics);
            List<UserAchievement> newAchievements = achievementService.checkAndAwardAchievements(userId, userMetrics);
            
            if (!newAchievements.isEmpty()) {
                logger.info("User {} earned {} new achievements!", userId, newAchievements.size());
                for (UserAchievement ua : newAchievements) {
                    logger.info("  - {}: {}", ua.getAchievement().getName(), ua.getAchievement().getDescription());
                }
            } else {
                logger.debug("No new achievements for user {}", userId);
            }
        } catch (Exception e) {
            logger.error("Failed to check achievements for user {}: {}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * Get user's recent activity summary
     */
    public Map<String, Object> getRecentActivitySummary(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<StudySession> recentSessions = studySessionRepository.findRecentSessions(userId, since);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSessions", recentSessions.size());
        summary.put("totalStudyTime", recentSessions.stream().mapToInt(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 0).sum());
        summary.put("uniqueStudyDays", recentSessions.stream().map(StudySession::getStudyDate).distinct().count());
        summary.put("lessonsCompleted", recentSessions.stream().filter(s -> s.getActivityType() == StudySession.ActivityType.LESSON_COMPLETED).count());
        summary.put("quizzesTaken", recentSessions.stream().filter(s -> s.getActivityType() == StudySession.ActivityType.QUIZ_TAKEN).count());
        
        return summary;
    }
    
    /**
     * Get today's activity summary
     */
    public Map<String, Object> getTodaysSummary(Long userId) {
        Long todaysActivities = studySessionRepository.countTodaysActivities(userId);
        Long todaysStudyTime = studySessionRepository.getTodaysStudyTime(userId);
        Boolean hasStudiedToday = studySessionRepository.hasStudyActivityOnDate(userId, LocalDate.now());
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("activitiesCompleted", todaysActivities.intValue());
        summary.put("studyTimeMinutes", todaysStudyTime.intValue());
        summary.put("hasStudiedToday", hasStudiedToday);
        summary.put("currentStreak", calculateCurrentStreak(userId));
        
        return summary;
    }
    
    /**
     * Get user's accessibility tools usage
     */
    public List<String> getAccessibilityToolsUsed(Long userId) {
        return studySessionRepository.getUniqueAccessibilityToolsUsed(userId);
    }
}
