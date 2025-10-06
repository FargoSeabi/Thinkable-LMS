package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Content Message Entity
 * Represents individual messages within a content conversation
 */
@Entity
@Table(name = "content_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ContentConversation conversation;
    
    @Column(name = "sender_id", nullable = false)
    private Long senderId; // User ID of the sender
    
    @Column(name = "sender_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SenderType senderType;
    
    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;
    
    @Column(name = "message_type", length = 20)
    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.TEXT;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "edited_at")
    private LocalDateTime editedAt;
    
    @Column(name = "is_system_message", nullable = false)
    private Boolean isSystemMessage = false;
    
    /**
     * Message sender types
     */
    public enum SenderType {
        STUDENT, TUTOR, SYSTEM
    }
    
    /**
     * Message content types
     */
    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM_NOTIFICATION
    }
    
    /**
     * Mark message as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * Check if message was sent by student
     */
    public boolean isFromStudent() {
        return SenderType.STUDENT.equals(this.senderType);
    }
    
    /**
     * Check if message was sent by tutor
     */
    public boolean isFromTutor() {
        return SenderType.TUTOR.equals(this.senderType);
    }
}
