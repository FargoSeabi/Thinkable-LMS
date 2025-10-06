package com.thinkable.backend.repository;

import com.thinkable.backend.entity.StudentContentInteraction;
import com.thinkable.backend.entity.LearningContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentContentInteractionRepository extends JpaRepository<StudentContentInteraction, Long> {
    
    List<StudentContentInteraction> findByStudentIdOrderByLastAccessedAtDesc(Long studentId);
    
    List<StudentContentInteraction> findByStudentIdAndInteractionType(Long studentId, String interactionType);
    
    Optional<StudentContentInteraction> findByStudentIdAndContentId(Long studentId, Long contentId);
    
    @Query("SELECT i FROM StudentContentInteraction i WHERE i.studentId = :studentId AND " +
           "i.lastAccessedAt >= :since ORDER BY i.lastAccessedAt DESC")
    List<StudentContentInteraction> findRecentInteractions(@Param("studentId") Long studentId, 
                                                          @Param("since") LocalDateTime since);
    
    @Query("SELECT i FROM StudentContentInteraction i WHERE i.studentId = :studentId AND " +
           "i.interactionType = 'complete' AND i.completedAt IS NOT NULL")
    List<StudentContentInteraction> findCompletedContent(@Param("studentId") Long studentId);
    
    @Query("SELECT i FROM StudentContentInteraction i WHERE i.studentId = :studentId AND " +
           "i.wasHelpful = true ORDER BY i.lastAccessedAt DESC")
    List<StudentContentInteraction> findHelpfulContent(@Param("studentId") Long studentId);
    
    @Query("SELECT i FROM StudentContentInteraction i WHERE i.studentId = :studentId AND " +
           "i.engagementScore >= :minScore ORDER BY i.engagementScore DESC")
    List<StudentContentInteraction> findHighEngagementContent(@Param("studentId") Long studentId, 
                                                             @Param("minScore") Double minScore);
    
    @Query("SELECT AVG(i.usefulnessRating) FROM StudentContentInteraction i WHERE i.content.id = :contentId AND " +
           "i.usefulnessRating IS NOT NULL")
    Double getAverageUsefulnessRating(@Param("contentId") Long contentId);
    
    @Query("SELECT AVG(i.accessibilityRating) FROM StudentContentInteraction i WHERE i.content.id = :contentId AND " +
           "i.accessibilityRating IS NOT NULL")
    Double getAverageAccessibilityRating(@Param("contentId") Long contentId);
    
    @Query("SELECT COUNT(i) FROM StudentContentInteraction i WHERE i.content.id = :contentId AND i.wasHelpful = true")
    Long countHelpfulInteractions(@Param("contentId") Long contentId);
    
    @Query("SELECT COUNT(i) FROM StudentContentInteraction i WHERE i.content.id = :contentId AND " +
           "i.completionPercentage >= 80")
    Long countCompletedInteractions(@Param("contentId") Long contentId);
    
    @Query("SELECT i.content.subjectArea, AVG(i.comprehensionScore) FROM StudentContentInteraction i " +
           "WHERE i.studentId = :studentId AND i.comprehensionScore IS NOT NULL " +
           "GROUP BY i.content.subjectArea")
    List<Object[]> getComprehensionScoresBySubject(@Param("studentId") Long studentId);
    
    @Query("SELECT i FROM StudentContentInteraction i WHERE i.studentId = :studentId AND " +
           "i.accessibilityBarriers IS NOT NULL AND i.accessibilityBarriers != '[]'")
    List<StudentContentInteraction> findInteractionsWithAccessibilityBarriers(@Param("studentId") Long studentId);
    
    @Query("SELECT i FROM StudentContentInteraction i WHERE i.studentId = :studentId AND " +
           "i.interactionType = 'bookmark'")
    List<StudentContentInteraction> findBookmarkedContent(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(DISTINCT i.studentId) FROM StudentContentInteraction i WHERE i.content.id = :contentId")
    Long countUniqueStudentsForContent(@Param("contentId") Long contentId);
    
    @Query("SELECT i.deviceType, COUNT(i) FROM StudentContentInteraction i WHERE i.studentId = :studentId AND " +
           "i.deviceType IS NOT NULL GROUP BY i.deviceType")
    List<Object[]> getDeviceUsageStats(@Param("studentId") Long studentId);
    
    @Modifying
    @Query("DELETE FROM StudentContentInteraction i WHERE i.content.id = :contentId")
    void deleteByContentId(@Param("contentId") Long contentId);
}
