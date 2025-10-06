package com.thinkable.backend.controller;

import java.util.stream.Collectors;

import com.thinkable.backend.entity.ContentConversation;
import com.thinkable.backend.entity.ContentMessage;
import com.thinkable.backend.service.ContentMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/messaging")
@RequiredArgsConstructor
public class ContentMessagingController {
    
    private final ContentMessagingService messagingService;
    
    /**
     * Start or get conversation for specific content
     */
    @PostMapping("/conversations")
    public ResponseEntity<Map<String, Object>> getOrCreateConversation(@RequestBody Map<String, Object> request) {
        try {
            Long contentId = Long.valueOf(request.get("contentId").toString());
            Long studentId = Long.valueOf(request.get("studentId").toString());
            
            ContentConversation conversation = messagingService.getOrCreateConversation(contentId, studentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conversation", buildConversationResponse(conversation));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Send a message in a conversation
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Object> request) {
        try {
            Long senderId = Long.valueOf(request.get("senderId").toString());
            String senderTypeStr = request.get("senderType").toString();
            String messageContent = request.get("messageContent").toString();
            
            ContentMessage.SenderType senderType = ContentMessage.SenderType.valueOf(senderTypeStr.toUpperCase());
            
            ContentMessage message = messagingService.sendMessage(conversationId, senderId, senderType, messageContent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", buildMessageResponse(message));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Get student's conversations
     */
    @GetMapping("/students/{studentId}/conversations")
    public ResponseEntity<Map<String, Object>> getStudentConversations(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ContentConversation> conversationsPage = messagingService.getStudentConversations(studentId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conversations", conversationsPage.getContent().stream()
                .map(this::buildConversationResponse).collect(Collectors.toList()));
            response.put("totalElements", conversationsPage.getTotalElements());
            response.put("totalPages", conversationsPage.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get tutor's conversations
     */
    @GetMapping("/tutors/{tutorId}/conversations")
    public ResponseEntity<Map<String, Object>> getTutorConversations(
            @PathVariable Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ContentConversation> conversationsPage = messagingService.getTutorConversations(tutorId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conversations", conversationsPage.getContent().stream()
                .map(this::buildConversationResponse).collect(Collectors.toList()));
            response.put("totalElements", conversationsPage.getTotalElements());
            response.put("totalPages", conversationsPage.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get messages in a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ContentMessage> messagesPage = messagingService.getConversationMessages(conversationId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", messagesPage.getContent().stream()
                .map(this::buildMessageResponse).collect(Collectors.toList()));
            response.put("totalElements", messagesPage.getTotalElements());
            response.put("totalPages", messagesPage.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Mark conversation as read
     */
    @PostMapping("/conversations/{conversationId}/mark-read")
    public ResponseEntity<Map<String, Object>> markConversationAsRead(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Boolean isStudent = Boolean.valueOf(request.get("isStudent").toString());
            
            messagingService.markConversationAsRead(conversationId, userId, isStudent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Conversation marked as read");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Get unread message counts for a user
     */
    @GetMapping("/users/{userId}/unread-counts")
    public ResponseEntity<Map<String, Object>> getUnreadCounts(
            @PathVariable Long userId,
            @RequestParam Boolean isStudent) {
        try {
            Map<String, Long> counts = messagingService.getUnreadCounts(userId, isStudent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.putAll(counts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Archive a conversation
     */
    @PostMapping("/conversations/{conversationId}/archive")
    public ResponseEntity<Map<String, Object>> archiveConversation(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Boolean isStudent = Boolean.valueOf(request.get("isStudent").toString());
            
            messagingService.archiveConversation(conversationId, userId, isStudent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Conversation archived");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Helper method to build conversation response
     */
    private Map<String, Object> buildConversationResponse(ContentConversation conversation) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", conversation.getId());
        response.put("subject", conversation.getSubject());
        response.put("isActive", conversation.getIsActive());
        response.put("lastMessageAt", conversation.getLastMessageAt());
        response.put("unreadCountStudent", conversation.getUnreadCountStudent());
        response.put("unreadCountTutor", conversation.getUnreadCountTutor());
        response.put("createdAt", conversation.getCreatedAt());
        
        // Content info
        if (conversation.getContent() != null) {
            Map<String, Object> contentInfo = new HashMap<>();
            contentInfo.put("id", conversation.getContent().getId());
            contentInfo.put("title", conversation.getContent().getTitle());
            contentInfo.put("subjectArea", conversation.getContent().getSubjectArea());
            response.put("content", contentInfo);
        }
        
        // Tutor info
        if (conversation.getTutorProfile() != null) {
            Map<String, Object> tutorInfo = new HashMap<>();
            tutorInfo.put("id", conversation.getTutorProfile().getId());
            tutorInfo.put("displayName", conversation.getTutorProfile().getDisplayName());
            tutorInfo.put("subjectExpertise", conversation.getTutorProfile().getSubjectExpertise());
            response.put("tutor", tutorInfo);
        }
        
        response.put("studentId", conversation.getStudentId());
        
        return response;
    }
    
    /**
     * Helper method to build message response
     */
    private Map<String, Object> buildMessageResponse(ContentMessage message) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", message.getId());
        response.put("senderId", message.getSenderId());
        response.put("senderType", message.getSenderType());
        response.put("messageContent", message.getMessageContent());
        response.put("messageType", message.getMessageType());
        response.put("isRead", message.getIsRead());
        response.put("readAt", message.getReadAt());
        response.put("createdAt", message.getCreatedAt());
        response.put("editedAt", message.getEditedAt());
        response.put("isSystemMessage", message.getIsSystemMessage());
        
        return response;
    }
}
