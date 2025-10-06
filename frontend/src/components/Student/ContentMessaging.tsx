import React, { useState, useEffect, useRef } from 'react';
import config from '../../services/config';
import './ContentMessaging.css';

interface Message {
  id: number;
  senderId: number;
  senderType: 'STUDENT' | 'TUTOR' | 'SYSTEM';
  messageContent: string;
  messageType: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM_NOTIFICATION';
  isRead: boolean;
  readAt?: string;
  createdAt: string;
  editedAt?: string;
  isSystemMessage: boolean;
}

interface Conversation {
  id: number;
  subject: string;
  isActive: boolean;
  lastMessageAt?: string;
  unreadCountStudent: number;
  unreadCountTutor: number;
  createdAt: string;
  content: {
    id: number;
    title: string;
    subjectArea: string;
  };
  tutor: {
    id: number;
    displayName: string;
    subjectExpertise: string;
  };
  studentId: number;
}

interface ContentMessagingProps {
  contentId: number;
  studentId: number;
  onClose: () => void;
}

const ContentMessaging: React.FC<ContentMessagingProps> = ({
  contentId,
  studentId,
  onClose
}) => {
  const [conversation, setConversation] = useState<Conversation | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messageInputRef = useRef<HTMLTextAreaElement>(null);
  
  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    initializeConversation();
  }, [contentId, studentId]);

  useEffect(() => {
    if (conversation) {
      loadMessages();
      markConversationAsRead();
    }
  }, [conversation]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const initializeConversation = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/messaging/conversations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          contentId,
          studentId
        })
      });

      const data = await response.json();
      
      if (data.success) {
        setConversation(data.conversation);
      } else {
        setError(data.error || 'Failed to initialize conversation');
      }
    } catch (err) {
      setError('Failed to connect to messaging service');
      console.error('Error initializing conversation:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadMessages = async () => {
    if (!conversation) return;

    try {
      const response = await fetch(`${API_BASE_URL}/api/messaging/conversations/${conversation.id}/messages?page=0&size=100`);
      const data = await response.json();
      
      if (data.success) {
        setMessages(data.messages.reverse()); // Reverse to show oldest first
      } else {
        setError(data.error || 'Failed to load messages');
      }
    } catch (err) {
      setError('Failed to load messages');
      console.error('Error loading messages:', err);
    }
  };

  const markConversationAsRead = async () => {
    if (!conversation) return;

    try {
      await fetch(`${API_BASE_URL}/api/messaging/conversations/${conversation.id}/mark-read`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: studentId,
          isStudent: true
        })
      });
    } catch (err) {
      console.error('Error marking conversation as read:', err);
    }
  };

  const sendMessage = async () => {
    if (!conversation || !newMessage.trim() || sending) return;

    setSending(true);
    
    try {
      const response = await fetch(`${API_BASE_URL}/api/messaging/conversations/${conversation.id}/messages`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          senderId: studentId,
          senderType: 'STUDENT',
          messageContent: newMessage.trim()
        })
      });

      const data = await response.json();
      
      if (data.success) {
        setMessages(prev => [...prev, data.message]);
        setNewMessage('');
        messageInputRef.current?.focus();
      } else {
        setError(data.error || 'Failed to send message');
      }
    } catch (err) {
      setError('Failed to send message');
      console.error('Error sending message:', err);
    } finally {
      setSending(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const formatMessageTime = (timestamp: string) => {
    const date = new Date(timestamp);
    return date.toLocaleString();
  };

  if (loading) {
    return (
      <div className="messaging-modal">
        <div className="messaging-content">
          <div className="messaging-loading">
            <i className="fas fa-spinner fa-spin"></i>
            <span>Starting conversation...</span>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="messaging-modal">
        <div className="messaging-content">
          <div className="messaging-error">
            <i className="fas fa-exclamation-triangle"></i>
            <p>{error}</p>
            <button onClick={onClose} className="btn-close">Close</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="messaging-modal">
      <div className="messaging-content">
        <div className="messaging-header">
          <div className="conversation-info">
            <i className="fas fa-comments"></i>
            <div>
              <h3>Chat with {conversation?.tutor.displayName}</h3>
              <p>About: {conversation?.content.title}</p>
            </div>
          </div>
          <button onClick={onClose} className="btn-close">
            <i className="fas fa-times"></i>
          </button>
        </div>

        <div className="messages-container">
          <div className="messages-list">
            {messages.length === 0 ? (
              <div className="no-messages">
                <i className="fas fa-comment-dots"></i>
                <p>Start a conversation! Ask your tutor anything about this content.</p>
              </div>
            ) : (
              messages.map((message) => (
                <div
                  key={message.id}
                  className={`message ${message.senderType.toLowerCase()}`}
                >
                  <div className="message-bubble">
                    <p>{message.messageContent}</p>
                    <div className="message-time">
                      {formatMessageTime(message.createdAt)}
                      {message.senderType === 'STUDENT' && (
                        <span className={`read-status ${message.isRead ? 'read' : 'unread'}`}>
                          <i className={`fas ${message.isRead ? 'fa-check-double' : 'fa-check'}`}></i>
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              ))
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="message-input-container">
            <div className="message-input-wrapper">
              <textarea
                ref={messageInputRef}
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Type your message here... Press Enter to send!"
                className="message-input"
                rows={3}
                disabled={sending}
              />
              <button
                onClick={sendMessage}
                disabled={!newMessage.trim() || sending}
                className="btn-send"
              >
                {sending ? (
                  <i className="fas fa-spinner fa-spin"></i>
                ) : (
                  <i className="fas fa-paper-plane"></i>
                )}
              </button>
            </div>
            <div className="input-hint">
              <i className="fas fa-lightbulb"></i>
              Ask questions, share thoughts, or get help with this content!
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContentMessaging;