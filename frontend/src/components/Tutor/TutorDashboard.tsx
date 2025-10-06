import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import config from '../../services/config';
import '../Student/Dashboard.css';

const TutorDashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState<number>(0);
  
  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    if (user) {
      fetchUnreadCount();
      // Poll for unread messages every 30 seconds
      const interval = setInterval(fetchUnreadCount, 30000);
      return () => clearInterval(interval);
    }
  }, [user]);

  const fetchUnreadCount = async () => {
    if (!user || !user.id || user.id === 0) {
      console.log('TutorDashboard: User not loaded or invalid ID:', user);
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/messaging/users/${user.id}/unread-counts?isStudent=false`);
      const data = await response.json();
      
      if (data.success) {
        setUnreadCount(data.unreadConversations || 0);
      }
    } catch (err) {
      console.error('Error fetching unread count:', err);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      <main className="dashboard-main">
        <div className="dashboard-grid">
          <div className="dashboard-card">
            <div className="card-icon">
              <i className="fas fa-upload"></i>
            </div>
            <h3>Content Upload</h3>
            <p>Upload new educational content, videos, and documents.</p>
            <Link to="/tutor/upload" className="card-button">
              Upload Content
            </Link>
          </div>

          <div className="dashboard-card">
            <div className="card-icon">
              <i className="fas fa-folder-open"></i>
            </div>
            <h3>Content Management</h3>
            <p>Manage your uploaded content, publish drafts, and view analytics.</p>
            <Link to="/tutor/content" className="card-button">
              Manage Content
            </Link>
          </div>

          <div className="dashboard-card">
            <div className="card-icon">
              <i className="fas fa-question-circle"></i>
            </div>
            <h3>Quiz Creator</h3>
            <p>Create interactive quizzes and assessments for your students.</p>
            <Link to="/tutor/quizzes" className="card-button">
              Manage Quizzes
            </Link>
          </div>

          <div className="dashboard-card">
            <div className="card-icon">
              <i className="fas fa-comments"></i>
              {unreadCount > 0 && (
                <span className="notification-badge">{unreadCount}</span>
              )}
            </div>
            <h3>Messages</h3>
            <p>Respond to student questions and provide support about your content.</p>
            <Link to="/tutor/messages" className="card-button">
              {unreadCount > 0 ? `View Messages (${unreadCount} new)` : 'View Messages'}
            </Link>
          </div>
        </div>
      </main>
    </div>
  );
};

export default TutorDashboard;