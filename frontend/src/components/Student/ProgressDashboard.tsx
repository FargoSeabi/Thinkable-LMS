import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import LoadingSpinner from '../Common/LoadingSpinner';
import Badge from '../Common/Badge';
import Icon from '../Common/Icon';
import config from '../../services/config';
import './ProgressDashboard.css';

interface Achievement {
  id: number;
  name: string;
  description: string;
  icon: string;
  category: string;
  pointsValue: number;
  rarity: string;
  earnedAt?: string;
}

interface AchievementProgress {
  achievement: Achievement;
  isEarned: boolean;
  progress: number;
  currentValue?: number;
  requiredValue?: number;
}

interface AchievementStats {
  totalEarned: number;
  totalPoints: number;
  totalAvailable: number;
  completionPercentage: number;
  recentAchievements: Array<{
    id: number;
    achievement: Achievement;
    earnedAt: string;
  }>;
}

const ProgressDashboard: React.FC = () => {
  const { user } = useAuth();
  const { showNotification } = useNotification();
  
  const [loading, setLoading] = useState(true);
  const [achievements, setAchievements] = useState<AchievementProgress[]>([]);
  const [stats, setStats] = useState<AchievementStats | null>(null);
  const [newAchievements, setNewAchievements] = useState<any[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  
  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    if (user?.id) {
      fetchAchievementData();
    }
  }, [user]);

  const fetchAchievementData = async () => {
    try {
      setLoading(true);
      
      // Fetch achievement progress
      const progressResponse = await fetch(`${API_BASE_URL}/api/achievements/user/${user?.id}/progress`);
      const progressData = await progressResponse.json();
      
      if (progressData.success) {
        setAchievements(progressData.progress || []);
      }
      
      // Fetch achievement stats
      const statsResponse = await fetch(`${API_BASE_URL}/api/achievements/user/${user?.id}`);
      const statsData = await statsResponse.json();
      
      if (statsData.success) {
        setStats(statsData.stats);
      }
      
      // Fetch new achievements
      const newResponse = await fetch(`${API_BASE_URL}/api/achievements/user/${user?.id}/new`);
      const newData = await newResponse.json();
      
      if (newData.success) {
        setNewAchievements(newData.newAchievements || []);
        
        // Show notification for new achievements
        if (newData.newAchievements?.length > 0) {
          showNotification(
            `You earned ${newData.newAchievements.length} new badge${newData.newAchievements.length > 1 ? 's' : ''}!`,
            'success'
          );
        }
      }
      
    } catch (error) {
      console.error('Failed to fetch achievement data:', error);
      showNotification('Failed to load your badges. Please try again!', 'error');
    } finally {
      setLoading(false);
    }
  };

  const getFilteredAchievements = () => {
    if (selectedCategory === 'all') return achievements;
    return achievements.filter(item => item.achievement.category === selectedCategory);
  };

  const categories = [
    { key: 'all', label: 'All Badges', icon: 'star', color: '#667eea' },
    { key: 'LEARNING', label: 'Learning', icon: 'books', color: '#4facfe' },
    { key: 'STREAK', label: 'Streaks', icon: 'fire', color: '#ff6b6b' },
    { key: 'MILESTONE', label: 'Milestones', icon: 'bullseye', color: '#feca57' },
    { key: 'ACCESSIBILITY', label: 'Accessibility', icon: 'accessibility', color: '#48dbfb' }
  ];

  const handleBadgeClick = (achievement: AchievementProgress) => {
    // Show detailed achievement info
    const message = achievement.isEarned
      ? `${achievement.achievement.name} - You earned this ${achievement.achievement.earnedAt ? 'on ' + new Date(achievement.achievement.earnedAt).toLocaleDateString() : 'recently'}!`
      : `${achievement.achievement.name} - ${achievement.progress}% complete! ${achievement.currentValue}/${achievement.requiredValue}`;

    showNotification(message, achievement.isEarned ? 'success' : 'info');
  };

  const markNewAchievementsAsViewed = async () => {
    if (newAchievements.length === 0) return;
    
    try {
      await fetch(`${API_BASE_URL}/api/achievements/user/${user?.id}/mark-viewed`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          achievementIds: newAchievements.map(ua => ua.id)
        })
      });
      
      setNewAchievements([]);
    } catch (error) {
      console.error('Failed to mark achievements as viewed:', error);
    }
  };

  if (loading) {
    return (
      <div className="progress-dashboard">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="progress-dashboard">
      {/* Header Section */}
      <div className="badge-collection-header">
        <h1 className="badge-collection-title">
          <Icon name="trophy" size={32} style={{ marginRight: '12px' }} />
          Your Badge Collection
        </h1>
        <p>Collect badges by learning, practicing, and exploring!</p>
        
        {stats && (
          <div className="badge-collection-stats">
            <div className="badge-stat">
              <span className="badge-stat-number">{stats.totalEarned}</span>
              <span className="badge-stat-label">Badges Earned</span>
            </div>
            <div className="badge-stat">
              <span className="badge-stat-number">{stats.totalPoints}</span>
              <span className="badge-stat-label">Total Points</span>
            </div>
            <div className="badge-stat">
              <span className="badge-stat-number">{stats.completionPercentage}%</span>
              <span className="badge-stat-label">Collection Complete</span>
            </div>
          </div>
        )}
      </div>

      {/* New Achievements Banner */}
      {newAchievements.length > 0 && (
        <div className="new-achievements-banner">
          <div className="new-achievements-content">
            <h3>
              <Icon name="celebration" size={24} style={{ marginRight: '8px' }} />
              New Badges Earned!
            </h3>
            <p>You've earned {newAchievements.length} new badge{newAchievements.length > 1 ? 's' : ''}!</p>
            <div className="new-achievements-list">
              {newAchievements.map((userAchievement) => (
                <Badge
                  key={userAchievement.id}
                  achievement={userAchievement.achievement}
                  isEarned={true}
                  isNew={true}
                  size="small"
                  showDetails={false}
                />
              ))}
            </div>
            <button
              className="mark-viewed-btn"
              onClick={markNewAchievementsAsViewed}
            >
              <Icon name="awesome" size={16} style={{ marginRight: '6px' }} />
              Awesome!
            </button>
          </div>
        </div>
      )}

      {/* Category Filter */}
      <div className="category-filter">
        {categories.map((category) => (
          <button
            key={category.key}
            className={`category-btn ${selectedCategory === category.key ? 'active' : ''}`}
            onClick={() => setSelectedCategory(category.key)}
            style={{
              '--category-color': category.color
            } as React.CSSProperties}
          >
            <Icon name={category.icon} size={18} style={{ marginRight: '8px' }} />
            {category.label}
          </button>
        ))}
      </div>

      {/* Recent Achievements */}
      {stats?.recentAchievements && stats.recentAchievements.length > 0 && (
        <div className="recent-achievements-section">
          <h2>
            <Icon name="timer" size={20} style={{ marginRight: '8px' }} />
            Recently Earned
          </h2>
          <div className="recent-achievements-list">
            {stats.recentAchievements.slice(0, 5).map((userAchievement) => (
              <Badge
                key={userAchievement.id}
                achievement={userAchievement.achievement}
                isEarned={true}
                size="small"
                showDetails={false}
                onClick={() => handleBadgeClick({
                  achievement: userAchievement.achievement,
                  isEarned: true,
                  progress: 100
                })}
              />
            ))}
          </div>
        </div>
      )}

      {/* Achievement Grid */}
      <div className="achievements-section">
        <h2>
          {selectedCategory === 'all' ? (
            <>
              <Icon name="bullseye" size={20} style={{ marginRight: '8px' }} />
              All Badges ({getFilteredAchievements().length})
            </>
          ) : (
            <>
              <Icon name={categories.find(c => c.key === selectedCategory)?.icon || 'star'} size={20} style={{ marginRight: '8px' }} />
              {categories.find(c => c.key === selectedCategory)?.label} ({getFilteredAchievements().length})
            </>
          )}
        </h2>
        
        <div className="badge-grid">
          {getFilteredAchievements().map((achievementProgress) => (
            <Badge
              key={achievementProgress.achievement.id}
              achievement={achievementProgress.achievement}
              isEarned={achievementProgress.isEarned}
              progress={achievementProgress.progress}
              currentValue={achievementProgress.currentValue}
              requiredValue={achievementProgress.requiredValue}
              onClick={() => handleBadgeClick(achievementProgress)}
              size="medium"
            />
          ))}
        </div>
        
        {getFilteredAchievements().length === 0 && (
          <div className="no-badges">
            <Icon name="search" size={32} style={{ marginBottom: '12px' }} />
            <p>No badges found in this category yet!</p>
            <p>Keep learning to unlock your first badges!</p>
          </div>
        )}
      </div>

      {/* Motivational Footer */}
      <div className="motivational-footer">
        <h3>
          <Icon name="star" size={24} style={{ marginRight: '8px' }} />
          Keep Going!
        </h3>
        <p>
          {stats?.totalEarned === 0
            ? "Start your learning journey to earn your first badge!"
            : stats?.completionPercentage === 100
            ? (
                <>
                  <Icon name="celebration" size={20} style={{ marginRight: '6px' }} />
                  Congratulations! You've collected all badges!
                </>
              )
            : `You're doing great! ${(stats?.totalAvailable || 0) - (stats?.totalEarned || 0)} more badges to collect!`
          }
        </p>
      </div>
    </div>
  );
};

export default ProgressDashboard;