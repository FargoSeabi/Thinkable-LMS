package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.thinkable.backend.model.User;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Content Conversation Entity
 * Represents a conversation thread between a student and tutor about specific content
 */
@Entity
@Table(name = "content_conversations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"content_id", "student_id", "tutor_profile_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentConversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private LearningContent content;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId; // Links to User entity
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_profile_id", nullable = false)
    private TutorProfile tutorProfile;
    
    @Column(name = "subject", nullable = false, length = 200)
    private String subject; // Generated subject based on content
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
    
    @Column(name = "unread_count_student")
    private Integer unreadCountStudent = 0;
    
    @Column(name = "unread_count_tutor")
    private Integer unreadCountTutor = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContentMessage> messages;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark messages as read for a specific user type
     */
    public void markAsRead(boolean isStudent) {
        if (isStudent) {
            this.unreadCountStudent = 0;
        } else {
            this.unreadCountTutor = 0;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Increment unread count for the recipient
     */
    public void incrementUnreadCount(boolean messageFromStudent) {
        if (messageFromStudent) {
            this.unreadCountTutor++;
        } else {
            this.unreadCountStudent++;
        }
        this.lastMessageAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
