package com.thinkable.backend.repository;

import com.thinkable.backend.entity.ContentConversation;
import com.thinkable.backend.entity.LearningContent;
import com.thinkable.backend.entity.TutorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentConversationRepository extends JpaRepository<ContentConversation, Long> {
    
    /**
     * Find conversation between specific student and tutor for specific content
     * Returns list to handle potential duplicates, use first result
     */
    @Query("SELECT c FROM ContentConversation c WHERE c.content = :content AND c.studentId = :studentId AND c.tutorProfile = :tutorProfile AND c.isActive = true ORDER BY c.createdAt DESC")
    List<ContentConversation> findByContentAndStudentIdAndTutorProfileList(
            @Param("content") LearningContent content, @Param("studentId") Long studentId, @Param("tutorProfile") TutorProfile tutorProfile, Pageable pageable);
    
    /**
     * Find conversation between specific student and tutor for specific content
     * Helper method that returns the first result
     */
    default Optional<ContentConversation> findByContentAndStudentIdAndTutorProfile(
            LearningContent content, Long studentId, TutorProfile tutorProfile) {
        List<ContentConversation> results = findByContentAndStudentIdAndTutorProfileList(
            content, studentId, tutorProfile, PageRequest.of(0, 1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * Get all conversations for a student
     */
    @Query("SELECT c FROM ContentConversation c WHERE c.studentId = :studentId AND c.isActive = true ORDER BY c.lastMessageAt DESC")
    Page<ContentConversation> findByStudentIdAndIsActiveTrue(@Param("studentId") Long studentId, Pageable pageable);
    
    /**
     * Get all conversations for a tutor
     */
    @Query("SELECT c FROM ContentConversation c WHERE c.tutorProfile = :tutor AND c.isActive = true ORDER BY c.lastMessageAt DESC")
    Page<ContentConversation> findByTutorProfileAndIsActiveTrue(@Param("tutor") TutorProfile tutor, Pageable pageable);
    
    /**
     * Get conversations for specific content
     */
    List<ContentConversation> findByContentAndIsActiveTrue(LearningContent content);
    
    /**
     * Count unread conversations for student
     */
    @Query("SELECT COUNT(c) FROM ContentConversation c WHERE c.studentId = :studentId AND c.unreadCountStudent > 0 AND c.isActive = true")
    Long countUnreadByStudentId(@Param("studentId") Long studentId);
    
    /**
     * Count unread conversations for tutor
     */
    @Query("SELECT COUNT(c) FROM ContentConversation c WHERE c.tutorProfile = :tutor AND c.unreadCountTutor > 0 AND c.isActive = true")
    Long countUnreadByTutorProfile(@Param("tutor") TutorProfile tutor);
    
    /**
     * Find conversations with recent activity
     */
    @Query("SELECT c FROM ContentConversation c WHERE c.lastMessageAt IS NOT NULL AND c.isActive = true ORDER BY c.lastMessageAt DESC")
    Page<ContentConversation> findRecentConversations(Pageable pageable);
    
    /**
     * Delete all conversations for specific content (for content cleanup)
     */
    @Modifying
    @Query("DELETE FROM ContentConversation c WHERE c.content.id = :contentId")
    void deleteByContentId(@Param("contentId") Long contentId);
}
