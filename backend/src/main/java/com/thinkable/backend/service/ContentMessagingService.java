package com.thinkable.backend.service;

import com.thinkable.backend.entity.ContentConversation;
import com.thinkable.backend.entity.ContentMessage;
import com.thinkable.backend.entity.LearningContent;
import com.thinkable.backend.entity.TutorProfile;
import com.thinkable.backend.repository.ContentConversationRepository;
import com.thinkable.backend.repository.ContentMessageRepository;
import com.thinkable.backend.repository.LearningContentRepository;
import com.thinkable.backend.repository.TutorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentMessagingService {
    
    private final ContentConversationRepository conversationRepository;
    private final ContentMessageRepository messageRepository;
    private final LearningContentRepository contentRepository;
    private final TutorProfileRepository tutorProfileRepository;
    
    /**
     * Start or get existing conversation between student and tutor for specific content
     * Uses synchronized method to prevent race conditions that create duplicate conversations
     */
    @Transactional
    public synchronized ContentConversation getOrCreateConversation(Long contentId, Long studentId) {
        LearningContent content = contentRepository.findById(contentId)
            .orElseThrow(() -> new RuntimeException("Content not found: " + contentId));
        
        TutorProfile tutor = content.getTutor();
        if (tutor == null) {
            throw new RuntimeException("Content has no assigned tutor");
        }
        
        Optional<ContentConversation> existing = conversationRepository
            .findByContentAndStudentIdAndTutorProfile(content, studentId, tutor);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new conversation with error handling for duplicates
        try {
            ContentConversation conversation = new ContentConversation();
            conversation.setContent(content);
            conversation.setStudentId(studentId);
            conversation.setTutorProfile(tutor);
            conversation.setSubject("Question about: " + content.getTitle());
            conversation.setIsActive(true);
            conversation.setCreatedAt(LocalDateTime.now());
            
            return conversationRepository.save(conversation);
        } catch (Exception e) {
            // If save fails due to constraint violation, try to find existing conversation again
            // This handles the race condition where another thread created the conversation
            Optional<ContentConversation> retryExisting = conversationRepository
                .findByContentAndStudentIdAndTutorProfile(content, studentId, tutor);
            
            if (retryExisting.isPresent()) {
                return retryExisting.get();
            }
            
            // If still no conversation found, rethrow the original exception
            throw new RuntimeException("Failed to create or find conversation: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a message in a conversation
     */
    public ContentMessage sendMessage(Long conversationId, Long senderId, 
                                    ContentMessage.SenderType senderType, String messageContent) {
        ContentConversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
        
        // Validate sender has permission to send message in this conversation
        boolean canSend = false;
        if (senderType == ContentMessage.SenderType.STUDENT && 
            conversation.getStudentId().equals(senderId)) {
            canSend = true;
        } else if (senderType == ContentMessage.SenderType.TUTOR) {
            // For tutors, we need to check by user ID, not tutor profile ID
            TutorProfile tutor = tutorProfileRepository.findByUserId(senderId).orElse(null);
            if (tutor != null && conversation.getTutorProfile().getId().equals(tutor.getId())) {
                canSend = true;
            }
        }
        
        if (!canSend) {
            throw new RuntimeException("User not authorized to send message in this conversation");
        }
        
        // Create message
        ContentMessage message = new ContentMessage();
        message.setConversation(conversation);
        message.setSenderId(senderId);
        message.setSenderType(senderType);
        message.setMessageContent(messageContent);
        message.setMessageType(ContentMessage.MessageType.TEXT);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        
        ContentMessage savedMessage = messageRepository.save(message);
        
        // Update conversation unread counts and last message time
        conversation.incrementUnreadCount(senderType == ContentMessage.SenderType.STUDENT);
        conversationRepository.save(conversation);
        
        return savedMessage;
    }
    
    /**
     * Get conversations for a student
     */
    @Transactional(readOnly = true)
    public Page<ContentConversation> getStudentConversations(Long studentId, Pageable pageable) {
        return conversationRepository.findByStudentIdAndIsActiveTrue(studentId, pageable);
    }
    
    /**
     * Get conversations for a tutor by user ID
     */
    @Transactional(readOnly = true)
    public Page<ContentConversation> getTutorConversations(Long userId, Pageable pageable) {
        TutorProfile tutor = tutorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Tutor profile not found for user: " + userId));
        
        return conversationRepository.findByTutorProfileAndIsActiveTrue(tutor, pageable);
    }
    
    /**
     * Get messages in a conversation with pagination
     */
    @Transactional(readOnly = true)
    public Page<ContentMessage> getConversationMessages(Long conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
    }
    
    /**
     * Mark conversation as read for a user
     */
    public void markConversationAsRead(Long conversationId, Long userId, boolean isStudent) {
        ContentConversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
        
        // Validate user has permission
        boolean canMarkRead = false;
        if (isStudent && conversation.getStudentId().equals(userId)) {
            canMarkRead = true;
        } else if (!isStudent) {
            // For tutors, we need to check by user ID, not tutor profile ID
            TutorProfile tutor = tutorProfileRepository.findByUserId(userId).orElse(null);
            if (tutor != null && conversation.getTutorProfile().getId().equals(tutor.getId())) {
                canMarkRead = true;
            }
        }
        
        if (!canMarkRead) {
            throw new RuntimeException("User not authorized to mark this conversation as read");
        }
        
        conversation.markAsRead(isStudent);
        conversationRepository.save(conversation);
        
        // Mark individual messages as read
        List<ContentMessage> unreadMessages;
        if (isStudent) {
            unreadMessages = messageRepository.findUnreadMessagesForStudent(conversationId);
        } else {
            unreadMessages = messageRepository.findUnreadMessagesForTutor(conversationId);
        }
        
        for (ContentMessage message : unreadMessages) {
            message.markAsRead();
        }
        
        if (!unreadMessages.isEmpty()) {
            messageRepository.saveAll(unreadMessages);
        }
    }
    
    /**
     * Get unread message counts for a user
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUnreadCounts(Long userId, boolean isStudent) {
        Map<String, Long> counts = new HashMap<>();
        
        if (isStudent) {
            Long unreadConversations = conversationRepository.countUnreadByStudentId(userId);
            counts.put("unreadConversations", unreadConversations);
        } else {
            TutorProfile tutor = tutorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Tutor profile not found for user: " + userId));
            Long unreadConversations = conversationRepository.countUnreadByTutorProfile(tutor);
            counts.put("unreadConversations", unreadConversations);
        }
        
        return counts;
    }
    
    /**
     * Archive/deactivate a conversation
     */
    public void archiveConversation(Long conversationId, Long userId, boolean isStudent) {
        ContentConversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
        
        // Validate user has permission
        boolean canArchive = false;
        if (isStudent && conversation.getStudentId().equals(userId)) {
            canArchive = true;
        } else if (!isStudent) {
            // For tutors, we need to check by user ID, not tutor profile ID
            TutorProfile tutor = tutorProfileRepository.findByUserId(userId).orElse(null);
            if (tutor != null && conversation.getTutorProfile().getId().equals(tutor.getId())) {
                canArchive = true;
            }
        }
        
        if (!canArchive) {
            throw new RuntimeException("User not authorized to archive this conversation");
        }
        
        conversation.setIsActive(false);
        conversationRepository.save(conversation);
    }
}
