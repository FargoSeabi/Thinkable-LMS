package com.thinkable.backend.repository;

import com.thinkable.backend.entity.UserToolUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for UserToolUsage entity
 * Handles database operations for neurodivergent tool usage tracking
 */
@Repository
public interface UserToolUsageRepository extends JpaRepository<UserToolUsage, Long> {
    
    /**
     * Find all tool usage records for a specific user
     */
    List<UserToolUsage> findByUserIdOrderByUsageTimestampDesc(Long userId);
    
    /**
     * Find tool usage by user and tool name
     */
    List<UserToolUsage> findByUserIdAndToolNameOrderByUsageTimestampDesc(Long userId, String toolName);
    
    /**
     * Find recent tool usage within a time period
     */
    @Query("SELECT u FROM UserToolUsage u WHERE u.userId = :userId AND u.usageTimestamp >= :since")
    List<UserToolUsage> findRecentUsageByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    /**
     * Find successful tool usage sessions (rating >= 7)
     */
    @Query("SELECT u FROM UserToolUsage u WHERE u.userId = :userId AND u.successRating >= 7")
    List<UserToolUsage> findSuccessfulUsageByUserId(@Param("userId") Long userId);
    
    /**
     * Find tool usage by energy level
     */
    List<UserToolUsage> findByUserIdAndUserEnergyLevel(Long userId, Integer energyLevel);
    
    /**
     * Find tool usage by time of day for pattern analysis
     */
    List<UserToolUsage> findByUserIdAndTimeOfDay(Long userId, String timeOfDay);
    
    /**
     * Find tool usage by day of week
     */
    List<UserToolUsage> findByUserIdAndDayOfWeek(Long userId, Integer dayOfWeek);
    
    /**
     * Find tool usage by activity context
     */
    List<UserToolUsage> findByUserIdAndActivityContext(Long userId, String activityContext);
    
    /**
     * Get average success rating for a tool by user
     */
    @Query("SELECT AVG(u.successRating) FROM UserToolUsage u WHERE u.userId = :userId AND u.toolName = :toolName AND u.successRating IS NOT NULL")
    Double getAverageSuccessRatingByUserAndTool(@Param("userId") Long userId, @Param("toolName") String toolName);
    
    /**
     * Get tool usage frequency (count) by user and tool
     */
    @Query("SELECT COUNT(u) FROM UserToolUsage u WHERE u.userId = :userId AND u.toolName = :toolName")
    Long getUsageCountByUserAndTool(@Param("userId") Long userId, @Param("toolName") String toolName);
    
    /**
     * Find most used tools by user
     */
    @Query("SELECT u.toolName, COUNT(u) as usage_count FROM UserToolUsage u WHERE u.userId = :userId GROUP BY u.toolName ORDER BY usage_count DESC")
    List<Object[]> findMostUsedToolsByUserId(@Param("userId") Long userId);
    
    /**
     * Find peak usage hours for a user
     */
    @Query("SELECT HOUR(u.usageTimestamp) as hour, COUNT(u) as usage_count " +
           "FROM UserToolUsage u WHERE u.userId = :userId " +
           "GROUP BY HOUR(u.usageTimestamp) ORDER BY usage_count DESC")
    List<Object[]> findPeakUsageHoursByUserId(@Param("userId") Long userId);
    
    /**
     * Find optimal session duration by tool and user
     */
    @Query("SELECT AVG(u.sessionDurationMinutes) FROM UserToolUsage u WHERE " +
           "u.userId = :userId AND u.toolName = :toolName AND u.successRating >= 7 AND u.sessionDurationMinutes IS NOT NULL")
    Double findOptimalSessionDurationByUserAndTool(@Param("userId") Long userId, @Param("toolName") String toolName);
    
    /**
     * Find tools used during high energy periods
     */
    @Query("SELECT u.toolName, COUNT(u) as usage_count FROM UserToolUsage u WHERE " +
           "u.userId = :userId AND u.userEnergyLevel >= 8 " +
           "GROUP BY u.toolName ORDER BY usage_count DESC")
    List<Object[]> findHighEnergyToolsByUserId(@Param("userId") Long userId);
    
    /**
     * Find tools used during low energy periods
     */
    @Query("SELECT u.toolName, COUNT(u) as usage_count FROM UserToolUsage u WHERE " +
           "u.userId = :userId AND u.userEnergyLevel <= 3 " +
           "GROUP BY u.toolName ORDER BY usage_count DESC")
    List<Object[]> findLowEnergyToolsByUserId(@Param("userId") Long userId);
    
    /**
     * Get tool effectiveness by context
     */
    @Query("SELECT u.toolContext, AVG(u.successRating) as avg_rating FROM UserToolUsage u WHERE " +
           "u.userId = :userId AND u.toolName = :toolName AND u.successRating IS NOT NULL " +
           "GROUP BY u.toolContext ORDER BY avg_rating DESC")
    List<Object[]> getToolEffectivenessByContext(@Param("userId") Long userId, @Param("toolName") String toolName);
    
    /**
     * Find usage patterns by week day vs weekend
     */
    @Query("SELECT " +
           "CASE WHEN u.dayOfWeek IN (0, 6) THEN 'weekend' ELSE 'weekday' END as day_type, " +
           "u.toolName, COUNT(u) as usage_count " +
           "FROM UserToolUsage u WHERE u.userId = :userId " +
           "GROUP BY day_type, u.toolName ORDER BY usage_count DESC")
    List<Object[]> findUsagePatternsByDayType(@Param("userId") Long userId);
    
    /**
     * Delete old usage records (for data cleanup)
     */
    @Query("DELETE FROM UserToolUsage u WHERE u.usageTimestamp < :cutoffDate")
    void deleteOldUsageRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
}
