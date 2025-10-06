package com.thinkable.backend.repository;

import com.thinkable.backend.model.UserAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAssessmentRepository extends JpaRepository<UserAssessment, Long> {

    /**
     * Find the most recent assessment for a user
     */
    Optional<UserAssessment> findTopByUserIdOrderByAssessmentDateDesc(Long userId);

    /**
     * Find all assessments for a user, ordered by date
     */
    List<UserAssessment> findByUserIdOrderByAssessmentDateDesc(Long userId);

    /**
     * Find users who have completed assessments
     */
    @Query("SELECT ua FROM UserAssessment ua WHERE ua.assessmentCompleted = true")
    List<UserAssessment> findCompletedAssessments();

    /**
     * Find users by recommended preset
     */
    List<UserAssessment> findByRecommendedPreset(String preset);

    /**
     * Count completed assessments
     */
    @Query("SELECT COUNT(ua) FROM UserAssessment ua WHERE ua.assessmentCompleted = true")
    long countCompletedAssessments();

    /**
     * Find users with specific trait scores above threshold
     */
    @Query("SELECT ua FROM UserAssessment ua WHERE ua.attentionScore >= :threshold")
    List<UserAssessment> findUsersWithHighAttentionScores(@Param("threshold") Integer threshold);

    @Query("SELECT ua FROM UserAssessment ua WHERE ua.readingDifficultyScore >= :threshold")
    List<UserAssessment> findUsersWithHighReadingScores(@Param("threshold") Integer threshold);

    @Query("SELECT ua FROM UserAssessment ua WHERE ua.socialCommunicationScore >= :threshold")
    List<UserAssessment> findUsersWithHighSocialScores(@Param("threshold") Integer threshold);

    @Query("SELECT ua FROM UserAssessment ua WHERE ua.sensoryProcessingScore >= :threshold")
    List<UserAssessment> findUsersWithHighSensoryScores(@Param("threshold") Integer threshold);

    /**
     * Find assessments that need follow-up (incomplete after certain time)
     */
    @Query("SELECT ua FROM UserAssessment ua WHERE ua.assessmentCompleted = false AND ua.createdAt < :cutoffDate")
    List<UserAssessment> findIncompleteAssessmentsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Check if user has completed assessment
     */
    @Query("SELECT COUNT(ua) > 0 FROM UserAssessment ua WHERE ua.userId = :userId AND ua.assessmentCompleted = true")
    boolean hasCompletedAssessment(@Param("userId") Long userId);

    /**
     * Get preset distribution statistics
     */
    @Query("SELECT ua.recommendedPreset, COUNT(ua) FROM UserAssessment ua WHERE ua.assessmentCompleted = true GROUP BY ua.recommendedPreset")
    List<Object[]> getPresetDistribution();

    /**
     * Find users with multiple significant needs (complex profiles)
     */
    @Query("SELECT ua FROM UserAssessment ua WHERE " +
           "(ua.attentionScore >= 18 AND ua.readingDifficultyScore >= 15) OR " +
           "(ua.socialCommunicationScore >= 16 AND ua.sensoryProcessingScore >= 14) OR " +
           "(ua.attentionScore >= 18 AND ua.sensoryProcessingScore >= 14)")
    List<UserAssessment> findComplexNeedsProfiles();

    /**
     * Find recent assessments for dashboard
     */
    @Query("SELECT ua FROM UserAssessment ua WHERE ua.assessmentCompleted = true AND ua.assessmentDate >= :since ORDER BY ua.assessmentDate DESC")
    List<UserAssessment> findRecentCompletedAssessments(@Param("since") LocalDateTime since);

    /**
     * Get average scores by category
     */
    @Query("SELECT AVG(ua.attentionScore), AVG(ua.socialCommunicationScore), AVG(ua.sensoryProcessingScore), " +
           "AVG(ua.readingDifficultyScore), AVG(ua.motorSkillsScore) FROM UserAssessment ua WHERE ua.assessmentCompleted = true")
    Object[] getAverageScores();

    /**
     * Delete old incomplete assessments (cleanup)
     */
    @Query("DELETE FROM UserAssessment ua WHERE ua.assessmentCompleted = false AND ua.createdAt < :cutoffDate")
    void deleteOldIncompleteAssessments(@Param("cutoffDate") LocalDateTime cutoffDate);
}
