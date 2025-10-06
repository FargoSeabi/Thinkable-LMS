package com.thinkable.backend.repository;

import com.thinkable.backend.entity.UserAdaptiveInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for UserAdaptiveInsight entity
 * Handles database operations for personalized insights and pattern recognition
 */
@Repository
public interface UserAdaptiveInsightRepository extends JpaRepository<UserAdaptiveInsight, Long> {
    
    /**
     * Find all insights for a specific user
     */
    List<UserAdaptiveInsight> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Find insights by user and type
     */
    List<UserAdaptiveInsight> findByUserIdAndInsightTypeOrderByCreatedAtDesc(Long userId, String insightType);
    
    /**
     * Find pending insights that should be presented to user
     */
    @Query("SELECT i FROM UserAdaptiveInsight i WHERE i.userId = :userId AND i.presentedToUser = false AND i.userResponse IS NULL")
    List<UserAdaptiveInsight> findPendingInsightsByUserId(@Param("userId") Long userId);
    
    /**
     * Find high-confidence insights ready for presentation
     */
    @Query("SELECT i FROM UserAdaptiveInsight i WHERE i.userId = :userId AND i.confidenceScore >= :minConfidence AND i.presentedToUser = false")
    List<UserAdaptiveInsight> findHighConfidenceInsightsByUserId(@Param("userId") Long userId, @Param("minConfidence") java.math.BigDecimal minConfidence);
    
    /**
     * Find high-priority insights
     */
    List<UserAdaptiveInsight> findByUserIdAndPriorityLevelOrderByCreatedAtDesc(Long userId, String priorityLevel);
    
    /**
     * Find insights by user response
     */
    List<UserAdaptiveInsight> findByUserIdAndUserResponseOrderByRespondedAtDesc(Long userId, String userResponse);
    
    /**
     * Find accepted insights for effectiveness tracking
     */
    @Query("SELECT i FROM UserAdaptiveInsight i WHERE i.userId = :userId AND i.userResponse = 'accepted'")
    List<UserAdaptiveInsight> findAcceptedInsightsByUserId(@Param("userId") Long userId);
    
    /**
     * Find rejected insights to avoid similar recommendations
     */
    @Query("SELECT i FROM UserAdaptiveInsight i WHERE i.userId = :userId AND i.userResponse = 'rejected'")
    List<UserAdaptiveInsight> findRejectedInsightsByUserId(@Param("userId") Long userId);
    
    /**
     * Count insights by type for analytics
     */
    @Query("SELECT i.insightType, COUNT(i) FROM UserAdaptiveInsight i WHERE i.userId = :userId GROUP BY i.insightType")
    List<Object[]> countInsightsByTypeForUser(@Param("userId") Long userId);
    
    /**
     * Find recent insights within time period
     */
    @Query("SELECT i FROM UserAdaptiveInsight i WHERE i.userId = :userId AND i.createdAt >= :since")
    List<UserAdaptiveInsight> findRecentInsightsByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    /**
     * Find insights awaiting user response for follow-up
     */
    @Query("SELECT i FROM UserAdaptiveInsight i WHERE i.userId = :userId AND i.presentedToUser = true AND i.userResponse IS NULL")
    List<UserAdaptiveInsight> findInsightsAwaitingResponseByUserId(@Param("userId") Long userId);
    
    /**
     * Get average confidence score by insight type
     */
    @Query("SELECT AVG(i.confidenceScore) FROM UserAdaptiveInsight i WHERE i.userId = :userId AND i.insightType = :insightType")
    java.math.BigDecimal getAverageConfidenceByTypeForUser(@Param("userId") Long userId, @Param("insightType") String insightType);
    
    /**
     * Find insights with low confidence for improvement
     */
    @Query("SELECT i FROM UserAdaptiveInsight i WHERE i.userId = :userId AND i.confidenceScore < :maxConfidence")
    List<UserAdaptiveInsight> findLowConfidenceInsightsByUserId(@Param("userId") Long userId, @Param("maxConfidence") java.math.BigDecimal maxConfidence);
    
    /**
     * Check if similar insight already exists
     */
    @Query("SELECT COUNT(i) > 0 FROM UserAdaptiveInsight i WHERE " +
           "i.userId = :userId AND i.insightType = :insightType AND i.insightTitle = :title AND " +
           "i.createdAt >= :recentThreshold")
    boolean existsSimilarRecentInsight(
        @Param("userId") Long userId,
        @Param("insightType") String insightType,
        @Param("title") String title,
        @Param("recentThreshold") LocalDateTime recentThreshold
    );
    
    /**
     * Find insights by confidence range
     */
    @Query("SELECT i FROM UserAdaptiveInsight i WHERE i.userId = :userId AND " +
           "i.confidenceScore BETWEEN :minConfidence AND :maxConfidence")
    List<UserAdaptiveInsight> findInsightsByConfidenceRange(
        @Param("userId") Long userId,
        @Param("minConfidence") java.math.BigDecimal minConfidence,
        @Param("maxConfidence") java.math.BigDecimal maxConfidence
    );
    
    /**
     * Get acceptance rate by insight type
     */
    @Query("SELECT " +
           "i.insightType, " +
           "SUM(CASE WHEN i.userResponse = 'accepted' THEN 1 ELSE 0 END) * 100.0 / COUNT(i) as acceptance_rate " +
           "FROM UserAdaptiveInsight i WHERE i.userId = :userId AND i.userResponse IS NOT NULL " +
           "GROUP BY i.insightType")
    List<Object[]> getAcceptanceRateByTypeForUser(@Param("userId") Long userId);
    
    /**
     * Find insights for cleanup (old, rejected, or ignored)
     */
    @Query("SELECT i FROM UserAdaptiveInsight i WHERE " +
           "i.userId = :userId AND " +
           "(i.userResponse = 'rejected' AND i.respondedAt < :cleanupThreshold) OR " +
           "(i.presentedToUser = true AND i.userResponse IS NULL AND i.createdAt < :ignoreThreshold)")
    List<UserAdaptiveInsight> findInsightsForCleanup(
        @Param("userId") Long userId,
        @Param("cleanupThreshold") LocalDateTime cleanupThreshold,
        @Param("ignoreThreshold") LocalDateTime ignoreThreshold
    );
    
    /**
     * Delete old insights for data maintenance
     */
    @Query("DELETE FROM UserAdaptiveInsight i WHERE i.createdAt < :cutoffDate AND i.userResponse = 'rejected'")
    void deleteOldRejectedInsights(@Param("cutoffDate") LocalDateTime cutoffDate);
}
