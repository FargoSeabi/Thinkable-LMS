package com.thinkable.backend.repository;

import com.thinkable.backend.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    
    // Basic user session queries
    List<StudySession> findByUserIdOrderByStartedAtDesc(Long userId);
    
    List<StudySession> findByUserIdAndStudyDateOrderByStartedAtDesc(Long userId, LocalDate studyDate);
    
    // Activity type specific queries
    List<StudySession> findByUserIdAndActivityType(Long userId, StudySession.ActivityType activityType);
    
    @Query("SELECT COUNT(s) FROM StudySession s WHERE s.userId = :userId AND s.activityType = :activityType AND s.completed = true")
    Long countCompletedByUserIdAndActivityType(@Param("userId") Long userId, @Param("activityType") StudySession.ActivityType activityType);
    
    // Lesson completion tracking
    @Query("SELECT COUNT(s) FROM StudySession s WHERE s.userId = :userId AND s.activityType = 'LESSON_COMPLETED' AND s.completed = true")
    Long countLessonsCompleted(@Param("userId") Long userId);
    
    // Quiz performance tracking
    @Query("SELECT COUNT(s) FROM StudySession s WHERE s.userId = :userId AND s.activityType = 'QUIZ_TAKEN' AND s.completed = true")
    Long countQuizzesCompleted(@Param("userId") Long userId);
    
    @Query("SELECT MAX(s.score * 100 / s.maxScore) FROM StudySession s WHERE s.userId = :userId AND s.activityType = 'QUIZ_TAKEN' AND s.score IS NOT NULL AND s.maxScore > 0")
    Optional<Integer> getHighestQuizScore(@Param("userId") Long userId);
    
    @Query("SELECT AVG(s.score * 100.0 / s.maxScore) FROM StudySession s WHERE s.userId = :userId AND s.activityType = 'QUIZ_TAKEN' AND s.score IS NOT NULL AND s.maxScore > 0")
    Optional<Double> getAverageQuizScore(@Param("userId") Long userId);
    
    // Study time tracking
    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s WHERE s.userId = :userId AND s.completed = true")
    Long getTotalStudyTimeMinutes(@Param("userId") Long userId);
    
    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s WHERE s.userId = :userId AND s.studyDate = :date AND s.completed = true")
    Long getStudyTimeForDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    // Streak calculation queries
    @Query("SELECT DISTINCT s.studyDate FROM StudySession s WHERE s.userId = :userId AND s.completed = true ORDER BY s.studyDate DESC")
    List<LocalDate> getDistinctStudyDates(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(DISTINCT s.studyDate) FROM StudySession s WHERE s.userId = :userId AND s.studyDate >= :fromDate AND s.completed = true")
    Long countStudyDaysInPeriod(@Param("userId") Long userId, @Param("fromDate") LocalDate fromDate);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM StudySession s WHERE s.userId = :userId AND s.studyDate = :date AND s.completed = true")
    Boolean hasStudyActivityOnDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    // Recent activity tracking
    @Query("SELECT s FROM StudySession s WHERE s.userId = :userId AND s.startedAt >= :since ORDER BY s.startedAt DESC")
    List<StudySession> findRecentSessions(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    // Accessibility tools tracking
    @Query("SELECT COUNT(DISTINCT s.accessibilityToolsUsed) FROM StudySession s WHERE s.userId = :userId AND s.accessibilityToolsUsed IS NOT NULL AND s.accessibilityToolsUsed != ''")
    Long countUniqueAccessibilityToolsUsed(@Param("userId") Long userId);
    
    @Query("SELECT s.accessibilityToolsUsed FROM StudySession s WHERE s.userId = :userId AND s.accessibilityToolsUsed IS NOT NULL AND s.accessibilityToolsUsed != '' GROUP BY s.accessibilityToolsUsed")
    List<String> getUniqueAccessibilityToolsUsed(@Param("userId") Long userId);
    
    // Performance analytics
    @Query("SELECT s FROM StudySession s WHERE s.userId = :userId AND s.activityType = 'QUIZ_TAKEN' AND s.score IS NOT NULL AND s.maxScore > 0 AND (s.score * 100 / s.maxScore) >= :minPercentage")
    List<StudySession> findQuizzesWithMinScore(@Param("userId") Long userId, @Param("minPercentage") Integer minPercentage);
    
    // Today's activity
    @Query("SELECT COUNT(s) FROM StudySession s WHERE s.userId = :userId AND s.studyDate = CURRENT_DATE AND s.completed = true")
    Long countTodaysActivities(@Param("userId") Long userId);
    
    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s WHERE s.userId = :userId AND s.studyDate = CURRENT_DATE AND s.completed = true")
    Long getTodaysStudyTime(@Param("userId") Long userId);
    
    // Week and month statistics
    @Query("SELECT COUNT(DISTINCT s.studyDate) FROM StudySession s WHERE s.userId = :userId AND s.studyDate >= :weekStart AND s.completed = true")
    Long countStudyDaysThisWeek(@Param("userId") Long userId, @Param("weekStart") LocalDate weekStart);
    
    @Query("SELECT COUNT(DISTINCT s.studyDate) FROM StudySession s WHERE s.userId = :userId AND s.studyDate >= :monthStart AND s.completed = true")
    Long countStudyDaysThisMonth(@Param("userId") Long userId, @Param("monthStart") LocalDate monthStart);
}
