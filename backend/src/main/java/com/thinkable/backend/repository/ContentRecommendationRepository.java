package com.thinkable.backend.repository;

import com.thinkable.backend.entity.ContentRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface ContentRecommendationRepository extends JpaRepository<ContentRecommendation, Long> {
    
    List<ContentRecommendation> findByStudentIdAndIsActiveTrueOrderByConfidenceScoreDesc(Long studentId);
    
    List<ContentRecommendation> findByStudentIdAndIsActiveTrueAndPresentedToStudentFalseOrderByConfidenceScoreDesc(Long studentId);
    
    @Query("SELECT r FROM ContentRecommendation r WHERE r.studentId = :studentId AND r.isActive = true AND " +
           "r.presentedToStudent = false AND r.confidenceScore >= :minConfidence " +
           "ORDER BY r.confidenceScore DESC, r.priorityLevel DESC")
    List<ContentRecommendation> findHighConfidenceRecommendations(@Param("studentId") Long studentId, 
                                                                  @Param("minConfidence") BigDecimal minConfidence);
    
    List<ContentRecommendation> findByStudentIdAndRecommendationTypeAndIsActiveTrue(Long studentId, String recommendationType);
    
    @Query("SELECT r FROM ContentRecommendation r WHERE r.studentId = :studentId AND r.isActive = true AND " +
           "r.priorityLevel IN ('high', 'urgent') ORDER BY r.confidenceScore DESC")
    List<ContentRecommendation> findHighPriorityRecommendations(@Param("studentId") Long studentId);
    
    @Query("SELECT r FROM ContentRecommendation r WHERE r.isActive = true AND " +
           "(r.expiresAt IS NULL OR r.expiresAt > :now)")
    List<ContentRecommendation> findActiveNonExpiredRecommendations(@Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM ContentRecommendation r WHERE r.isActive = true AND r.expiresAt <= :now")
    List<ContentRecommendation> findExpiredRecommendations(@Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM ContentRecommendation r WHERE r.studentId = :studentId AND r.presentedToStudent = true AND " +
           "r.studentResponse IS NOT NULL ORDER BY r.presentedAt DESC")
    List<ContentRecommendation> findRespondedRecommendations(@Param("studentId") Long studentId);
    
    @Query("SELECT AVG(r.feedbackRating) FROM ContentRecommendation r WHERE r.feedbackRating IS NOT NULL AND " +
           "r.algorithmVersion = :version")
    Double getAverageFeedbackRatingForAlgorithm(@Param("version") String algorithmVersion);
    
    @Query("SELECT r.recommendationType, COUNT(r), AVG(r.confidenceScore) FROM ContentRecommendation r " +
           "WHERE r.studentId = :studentId GROUP BY r.recommendationType")
    List<Object[]> getRecommendationTypeStats(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(r) FROM ContentRecommendation r WHERE r.studentId = :studentId AND " +
           "r.presentedToStudent = true AND r.studentResponse = 'completed'")
    Long countCompletedRecommendations(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(r) FROM ContentRecommendation r WHERE r.studentId = :studentId AND " +
           "r.presentedToStudent = true AND (r.studentResponse IS NULL OR r.studentResponse = 'ignored')")
    Long countIgnoredRecommendations(@Param("studentId") Long studentId);
}
