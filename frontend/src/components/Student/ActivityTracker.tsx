import React, { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import activityService from '../../services/activityService';
import './ActivityTracker.css';

const ActivityTracker: React.FC = () => {
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const [loading, setLoading] = useState(false);
  const [currentStreak, setCurrentStreak] = useState<number>(0);

  const trackActivity = async (activityType: string, description: string) => {
    if (!user?.id) return;
    
    try {
      setLoading(true);
      let response;
      
      switch (activityType) {
        case 'lesson':
          response = await activityService.recordLessonCompletion({
            userId: user.id,
            lessonId: `demo-lesson-${Date.now()}`,
            durationMinutes: Math.floor(Math.random() * 20) + 10, // 10-30 minutes
            accessibilityTools: ['reading guide', 'focus mode']
          });
          break;
          
        case 'quiz':
          const score = Math.floor(Math.random() * 40) + 60; // 60-100%
          response = await activityService.recordQuizCompletion({
            userId: user.id,
            quizId: `demo-quiz-${Date.now()}`,
            score: score,
            maxScore: 100,
            durationMinutes: Math.floor(Math.random() * 10) + 5, // 5-15 minutes
            accessibilityTools: ['high contrast', 'text size']
          });
          showNotification(`🎯 Quiz completed! You scored ${score}%`, 'success');
          break;
          
        case 'reading':
          response = await activityService.trackReading(user.id, Math.floor(Math.random() * 10) + 3);
          break;
          
        case 'video':
          response = await activityService.trackVideoWatching(user.id, Math.floor(Math.random() * 15) + 5);
          break;
          
        case 'explore':
          response = await activityService.trackContentExploration(user.id, Math.floor(Math.random() * 5) + 2);
          break;
          
        default:
          response = await activityService.trackInteraction(user.id, 1);
      }
      
      if (response.success) {
        setCurrentStreak(response.currentStreak || 0);
        showNotification(`✅ ${description} tracked! Current streak: ${response.currentStreak} days`, 'success');
      }
      
    } catch (error) {
      console.error('Error tracking activity:', error);
      showNotification('❌ Failed to track activity', 'error');
    } finally {
      setLoading(false);
    }
  };

  const activities = [
    {
      type: 'lesson',
      title: 'Complete Lesson',
      description: 'Finish a learning lesson',
      icon: '📚',
      color: '#4facfe'
    },
    {
      type: 'quiz',
      title: 'Take Quiz',
      description: 'Test your knowledge',
      icon: '🎯',
      color: '#ff6b6b'
    },
    {
      type: 'reading',
      title: 'Read Content',
      description: 'Spend time reading',
      icon: '📖',
      color: '#feca57'
    },
    {
      type: 'video',
      title: 'Watch Video',
      description: 'View educational content',
      icon: '🎥',
      color: '#48dbfb'
    },
    {
      type: 'explore',
      title: 'Explore',
      description: 'Discover new content',
      icon: '🔍',
      color: '#ff9ff3'
    }
  ];

  return (
    <div className="activity-tracker">
      <div className="tracker-header">
        <h2>🎮 Activity Tracker</h2>
        <p>Track your learning activities to earn badges!</p>
        
        {currentStreak > 0 && (
          <div className="current-streak">
            <span className="streak-icon">🔥</span>
            <span className="streak-text">{currentStreak} day streak!</span>
          </div>
        )}
      </div>
      
      <div className="activity-grid">
        {activities.map((activity) => (
          <button
            key={activity.type}
            className="activity-button"
            onClick={() => trackActivity(activity.type, activity.title)}
            disabled={loading}
            style={{ '--activity-color': activity.color } as React.CSSProperties}
          >
            <div className="activity-icon">{activity.icon}</div>
            <h3>{activity.title}</h3>
            <p>{activity.description}</p>
            {loading && <div className="loading-spinner">⏳</div>}
          </button>
        ))}
      </div>
      
      <div className="tracker-info">
        <h3>How Badge Tracking Works:</h3>
        <ul>
          <li>🌟 <strong>Day One:</strong> Complete any activity today</li>
          <li>🔥 <strong>Streak Badges:</strong> Study for consecutive days</li>
          <li>📚 <strong>Learning Badges:</strong> Complete lessons (1, 5, 10, 25, 50)</li>
          <li>🎯 <strong>Quiz Badges:</strong> Take quizzes and score well</li>
          <li>⏰ <strong>Study Time:</strong> Accumulate study minutes</li>
          <li>🔧 <strong>Accessibility:</strong> Use different tools</li>
        </ul>
      </div>
    </div>
  );
};

export default ActivityTracker;