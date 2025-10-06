package com.thinkable.backend.repository;

import com.thinkable.backend.entity.ContentMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentMessageRepository extends JpaRepository<ContentMessage, Long> {
    
    /**
     * Get messages for a conversation, ordered by creation time (newest first)
     */
    Page<ContentMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
    
    /**
     * Get messages for a conversation, ordered by creation time (oldest first)
     */
    List<ContentMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    
    /**
     * Count total messages in a conversation
     */
    Long countByConversationId(Long conversationId);
    
    /**
     * Find unread messages for student in a conversation
     */
    @Query("SELECT m FROM ContentMessage m WHERE m.conversation.id = :conversationId " +
           "AND m.isRead = false AND m.senderType = 'TUTOR'")
    List<ContentMessage> findUnreadMessagesForStudent(@Param("conversationId") Long conversationId);
    
    /**
     * Find unread messages for tutor in a conversation
     */
    @Query("SELECT m FROM ContentMessage m WHERE m.conversation.id = :conversationId " +
           "AND m.isRead = false AND m.senderType = 'STUDENT'")
    List<ContentMessage> findUnreadMessagesForTutor(@Param("conversationId") Long conversationId);
    
    /**
     * Count unread messages for student in a conversation
     */
    @Query("SELECT COUNT(m) FROM ContentMessage m WHERE m.conversation.id = :conversationId " +
           "AND m.isRead = false AND m.senderType = 'TUTOR'")
    Long countUnreadMessagesForStudent(@Param("conversationId") Long conversationId);
    
    /**
     * Count unread messages for tutor in a conversation
     */
    @Query("SELECT COUNT(m) FROM ContentMessage m WHERE m.conversation.id = :conversationId " +
           "AND m.isRead = false AND m.senderType = 'STUDENT'")
    Long countUnreadMessagesForTutor(@Param("conversationId") Long conversationId);
    
    /**
     * Find latest message in a conversation
     */
    ContentMessage findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);
    
    /**
     * Find messages by sender in a conversation
     */
    List<ContentMessage> findByConversationIdAndSenderIdOrderByCreatedAtAsc(Long conversationId, Long senderId);
    
    /**
     * Find system messages in a conversation
     */
    List<ContentMessage> findByConversationIdAndIsSystemMessageTrueOrderByCreatedAtAsc(Long conversationId);
    
    /**
     * Delete all messages for conversations related to specific content (for content cleanup)
     */
    @Modifying
    @Query("DELETE FROM ContentMessage m WHERE m.conversation.content.id = :contentId")
    void deleteByContentId(@Param("contentId") Long contentId);
}
