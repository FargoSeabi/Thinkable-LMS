package com.thinkable.backend.repository;

import com.thinkable.backend.entity.ContentReview;
import com.thinkable.backend.entity.LearningContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentReviewRepository extends JpaRepository<ContentReview, Long> {
    
    /**
     * Find reviews by content
     */
    List<ContentReview> findByContent(LearningContent content);
    
    /**
     * Find reviews by content with pagination
     */
    Page<ContentReview> findByContentOrderByCreatedAtDesc(LearningContent content, Pageable pageable);
    
    /**
     * Find reviews by student
     */
    List<ContentReview> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    
    /**
     * Find public reviews by content
     */
    @Query("SELECT r FROM ContentReview r WHERE r.content = :content AND r.isPublic = true ORDER BY r.createdAt DESC")
    List<ContentReview> findPublicReviewsByContent(@Param("content") LearningContent content);
    
    /**
     * Find verified reviews
     */
    @Query("SELECT r FROM ContentReview r WHERE r.isVerified = true ORDER BY r.createdAt DESC")
    List<ContentReview> findVerifiedReviews();
    
    /**
     * Calculate average rating for content
     */
    @Query("SELECT AVG(r.overallRating) FROM ContentReview r WHERE r.content = :content")
    Double getAverageRatingByContent(@Param("content") LearningContent content);
    
    /**
     * Count reviews by content
     */
    Long countByContent(LearningContent content);
    
    /**
     * Delete all reviews for specific content (for content cleanup)
     */
    @Modifying
    @Query("DELETE FROM ContentReview r WHERE r.content.id = :contentId")
    void deleteByContentId(@Param("contentId") Long contentId);
}
