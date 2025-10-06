import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import config from '../../services/config';
import './TutorMessages.css';

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

interface Student {
  id: number;
  name: string;
  email: string;
}

const TutorMessages: React.FC = () => {
  const { user } = useAuth();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [student, setStudent] = useState<Student | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messageInputRef = useRef<HTMLTextAreaElement>(null);

  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    loadConversations();
  }, []);

  useEffect(() => {
    if (selectedConversation) {
      loadMessages();
      loadStudentInfo();
      markConversationAsRead();
    }
  }, [selectedConversation]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadConversations = async () => {
    if (!user) return;

    try {
      const response = await fetch(`${API_BASE_URL}/api/messaging/tutors/${user.id}/conversations?page=0&size=50`);
      const data = await response.json();
      
      if (data.success) {
        setConversations(data.conversations);
        if (data.conversations.length > 0) {
          setSelectedConversation(data.conversations[0]);
        }
      } else {
        setError(data.error || 'Failed to load conversations');
      }
    } catch (err) {
      setError('Failed to connect to messaging service');
      console.error('Error loading conversations:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadMessages = async () => {
    if (!selectedConversation) return;

    try {
      const response = await fetch(`${API_BASE_URL}/api/messaging/conversations/${selectedConversation.id}/messages?page=0&size=100`);
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

  const loadStudentInfo = async () => {
    if (!selectedConversation) return;

    try {
      const response = await fetch(`${API_BASE_URL}/api/users/${selectedConversation.studentId}`);
      const data = await response.json();
      
      if (data.success) {
        setStudent(data.user);
      }
    } catch (err) {
      console.error('Error loading student info:', err);
    }
  };

  const markConversationAsRead = async () => {
    if (!selectedConversation || !user) return;

    try {
      await fetch(`${API_BASE_URL}/api/messaging/conversations/${selectedConversation.id}/mark-read`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: user.id,
          isStudent: false
        })
      });

      // Update local conversation state
      setConversations(prev => prev.map(conv => 
        conv.id === selectedConversation.id 
          ? { ...conv, unreadCountTutor: 0 }
          : conv
      ));
    } catch (err) {
      console.error('Error marking conversation as read:', err);
    }
  };

  const sendMessage = async () => {
    if (!selectedConversation || !newMessage.trim() || sending || !user) return;

    setSending(true);
    
    try {
      const response = await fetch(`${API_BASE_URL}/api/messaging/conversations/${selectedConversation.id}/messages`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          senderId: user.id,
          senderType: 'TUTOR',
          messageContent: newMessage.trim()
        })
      });

      const data = await response.json();
      
      if (data.success) {
        setMessages(prev => [...prev, data.message]);
        setNewMessage('');
        messageInputRef.current?.focus();
        
        // Update conversation list to reflect new message
        setConversations(prev => prev.map(conv => 
          conv.id === selectedConversation.id 
            ? { ...conv, lastMessageAt: new Date().toISOString() }
            : conv
        ));
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

  const formatConversationTime = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffHours = Math.abs(now.getTime() - date.getTime()) / 36e5;

    if (diffHours < 1) {
      return 'Just now';
    } else if (diffHours < 24) {
      return `${Math.floor(diffHours)}h ago`;
    } else {
      return date.toLocaleDateString();
    }
  };

  if (loading) {
    return (
      <div className="tutor-messages-container">
        <div className="messages-loading">
          <i className="fas fa-spinner fa-spin"></i>
          <span>Loading conversations...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="tutor-messages-container">
        <div className="messages-error">
          <i className="fas fa-exclamation-triangle"></i>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="tutor-messages-container">
      <div className="messages-header">
        <h1>Messages</h1>
        <p>Respond to student questions about your content</p>
      </div>

      <div className="messages-layout">
        {/* Conversations Sidebar */}
        <div className="conversations-sidebar">
          <div className="conversations-header">
            <h3>Conversations</h3>
            <span className="conversation-count">{conversations.length}</span>
          </div>
          
          <div className="conversations-list">
            {conversations.length === 0 ? (
              <div className="no-conversations">
                <i className="fas fa-comment-slash"></i>
                <p>No messages yet</p>
                <small>Students will reach out when they have questions about your content</small>
              </div>
            ) : (
              conversations.map((conversation) => (
                <div
                  key={conversation.id}
                  className={`conversation-item ${selectedConversation?.id === conversation.id ? 'active' : ''}`}
                  onClick={() => setSelectedConversation(conversation)}
                >
                  <div className="conversation-avatar">
                    <i className="fas fa-user-graduate"></i>
                  </div>
                  <div className="conversation-info">
                    <div className="conversation-header">
                      <h4>Student Question</h4>
                      {conversation.unreadCountTutor > 0 && (
                        <span className="unread-badge">{conversation.unreadCountTutor}</span>
                      )}
                    </div>
                    <p className="conversation-content">{conversation.content.title}</p>
                    <small className="conversation-subject">{conversation.content.subjectArea}</small>
                    <small className="conversation-time">
                      {conversation.lastMessageAt ? formatConversationTime(conversation.lastMessageAt) : 'New'}
                    </small>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Messages Area */}
        <div className="messages-area">
          {selectedConversation ? (
            <>
              <div className="messages-chat-header">
                <div className="chat-info">
                  <div className="student-avatar">
                    <i className="fas fa-user-graduate"></i>
                  </div>
                  <div>
                    <h3>{student?.name || 'Student'}</h3>
                    <p>About: <strong>{selectedConversation.content.title}</strong></p>
                    <small>{selectedConversation.content.subjectArea}</small>
                  </div>
                </div>
              </div>

              <div className="messages-container">
                <div className="messages-list">
                  {messages.length === 0 ? (
                    <div className="no-messages">
                      <i className="fas fa-comment-dots"></i>
                      <p>This conversation just started!</p>
                      <small>The student has a question about your content.</small>
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
                            {message.senderType === 'TUTOR' && (
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
                      placeholder="Type your response here... Press Enter to send!"
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
                    Provide helpful, encouraging responses to support your student's learning
                  </div>
                </div>
              </div>
            </>
          ) : (
            <div className="no-conversation-selected">
              <i className="fas fa-comments"></i>
              <h3>Select a conversation</h3>
              <p>Choose a conversation from the sidebar to start responding to student messages</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default TutorMessages;