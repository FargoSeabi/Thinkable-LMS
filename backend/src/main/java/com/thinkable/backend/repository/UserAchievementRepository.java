package com.thinkable.backend.repository;

import com.thinkable.backend.entity.UserAchievement;
import com.thinkable.backend.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    
    /**
     * Find all achievements earned by a user
     */
    List<UserAchievement> findByUserIdOrderByEarnedAtDesc(Long userId);
    
    /**
     * Find new achievements for a user (not yet viewed)
     */
    List<UserAchievement> findByUserIdAndIsNewTrueOrderByEarnedAtDesc(Long userId);
    
    /**
     * Check if user has already earned a specific achievement
     */
    Optional<UserAchievement> findByUserIdAndAchievement(Long userId, Achievement achievement);
    
    /**
     * Count total achievements earned by user
     */
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    /**
     * Get total points earned by user
     */
    @Query("SELECT SUM(ua.achievement.pointsValue) FROM UserAchievement ua WHERE ua.userId = :userId")
    Long getTotalPointsByUserId(@Param("userId") Long userId);
    
    /**
     * Find achievements earned by category
     */
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.userId = :userId AND ua.achievement.category = :category ORDER BY ua.earnedAt DESC")
    List<UserAchievement> findByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);
    
    /**
     * Find recent achievements (last 7 days)
     */
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.userId = :userId AND ua.earnedAt >= :sevenDaysAgo ORDER BY ua.earnedAt DESC")
    List<UserAchievement> findRecentAchievements(@Param("userId") Long userId, @Param("sevenDaysAgo") java.time.LocalDateTime sevenDaysAgo);
}
