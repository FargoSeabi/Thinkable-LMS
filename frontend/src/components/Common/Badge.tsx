import React from 'react';
import Icon from './Icon';
import './Badge.css';

interface BadgeProps {
  achievement: {
    id: number;
    name: string;
    description: string;
    icon: string;
    category: string;
    pointsValue: number;
    rarity: string;
    earnedAt?: string;
  };
  isEarned: boolean;
  isNew?: boolean;
  progress?: number;
  currentValue?: number;
  requiredValue?: number;
  onClick?: () => void;
  size?: 'small' | 'medium' | 'large';
  showDetails?: boolean;
}

const Badge: React.FC<BadgeProps> = ({
  achievement,
  isEarned,
  isNew = false,
  progress = 0,
  currentValue,
  requiredValue,
  onClick,
  size = 'medium',
  showDetails = true
}) => {

  const getRarityClass = (rarity: string) => {
    return `badge-${rarity.toLowerCase()}`;
  };

  const getIconName = (achievementIcon: string, category: string) => {
    // Map common emojis to icon names
    const emojiToIcon: { [key: string]: string } = {
      '📚': 'books',
      '🔥': 'fire',
      '🎯': 'bullseye',
      '🏆': 'trophy',
      '⭐': 'star',
      '🌟': 'star',
      '🔧': 'accessibility',
      '🎉': 'celebration',
      '👍': 'awesome',
      '🕐': 'timer',
      '🔍': 'search',
      '🥚': 'egghead',
      '🍪': 'cookie',
      '📖': 'books',
      '📝': 'books',
      '🎓': 'egghead',
      '💡': 'star',
      '🚀': 'fire',
      '⚡': 'fire',
      '🎮': 'awesome',
      '🎊': 'celebration',
      '⏰': 'timer',
      '🧠': 'egghead',
      '👑': 'trophy',
      '💎': 'star',
      '💪': 'fire',
      '🦸': 'awesome'
    };

    // If it's an emoji, convert it
    if (emojiToIcon[achievementIcon]) {
      return emojiToIcon[achievementIcon];
    }

    // If it's already an icon name, use it
    if (typeof achievementIcon === 'string' && !achievementIcon.match(/[\u{1F600}-\u{1F64F}]|[\u{1F300}-\u{1F5FF}]|[\u{1F680}-\u{1F6FF}]|[\u{1F1E0}-\u{1F1FF}]|[\u{2600}-\u{26FF}]|[\u{2700}-\u{27BF}]/u)) {
      return achievementIcon;
    }

    // Fallback based on category
    const categoryIcons: { [key: string]: string } = {
      'LEARNING': 'books',
      'STREAK': 'fire',
      'MILESTONE': 'bullseye',
      'ACCESSIBILITY': 'accessibility',
      'SOCIAL': 'awesome'
    };

    return categoryIcons[category] || 'star';
  };

  const getCategoryColor = (category: string) => {
    const colors = {
      LEARNING: '#4facfe',
      STREAK: '#ff6b6b',
      MILESTONE: '#feca57',
      ACCESSIBILITY: '#48dbfb',
      SOCIAL: '#ff9ff3'
    };
    return colors[category as keyof typeof colors] || '#6c5ce7';
  };

  const formatEarnedDate = (earnedAt: string) => {
    const date = new Date(earnedAt);
    const now = new Date();
    const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) return 'Today!';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    return date.toLocaleDateString();
  };

  return (
    <div 
      className={`badge ${size} ${getRarityClass(achievement.rarity)} ${isEarned ? 'earned' : 'locked'} ${onClick ? 'clickable' : ''}`}
      onClick={onClick}
      style={!isEarned ? { '--category-color': getCategoryColor(achievement.category) } as React.CSSProperties : {}}
    >
      {/* New Badge Indicator */}
      {isNew && (
        <div className="badge-new-indicator">
          <span>NEW!</span>
        </div>
      )}
      
      {/* Badge Icon */}
      <div className="badge-icon">
        <div className={`icon ${isEarned ? '' : 'locked-icon'}`}>
          {isEarned ? (
            <Icon
              name={getIconName(achievement.icon, achievement.category)}
              size={size === 'small' ? 24 : size === 'large' ? 48 : 32}
              color={isEarned ? '#ffd700' : getCategoryColor(achievement.category)}
            />
          ) : (
            <Icon
              name={getIconName(achievement.icon, achievement.category)}
              size={size === 'small' ? 24 : size === 'large' ? 48 : 32}
              color="#999"
              style={{ opacity: 0.3, filter: 'grayscale(100%)' }}
            />
          )}
        </div>
        {!isEarned && progress > 0 && (
          <div className="progress-ring">
            <svg width="60" height="60" className="progress-ring-svg">
              <circle
                className="progress-ring-circle-bg"
                stroke="#e0e0e0"
                strokeWidth="4"
                fill="transparent"
                r="26"
                cx="30"
                cy="30"
              />
              <circle
                className="progress-ring-circle"
                stroke={getCategoryColor(achievement.category)}
                strokeWidth="4"
                fill="transparent"
                r="26"
                cx="30"
                cy="30"
                style={{
                  strokeDasharray: `${2 * Math.PI * 26}`,
                  strokeDashoffset: `${2 * Math.PI * 26 * (1 - progress / 100)}`
                }}
              />
            </svg>
          </div>
        )}
      </div>

      {/* Badge Content */}
      {showDetails && (
        <div className="badge-content">
          <h3 className="badge-name">{achievement.name}</h3>
          <p className="badge-description">{achievement.description}</p>
          
          {isEarned ? (
            <div className="badge-earned-info">
              <div className="badge-points">+{achievement.pointsValue} points</div>
              {achievement.earnedAt && (
                <div className="badge-earned-date">
                  {formatEarnedDate(achievement.earnedAt)}
                </div>
              )}
            </div>
          ) : (
            <div className="badge-progress-info">
              {currentValue !== undefined && requiredValue !== undefined && (
                <div className="badge-progress-text">
                  Progress: {currentValue} / {requiredValue}
                </div>
              )}
              <div className="badge-progress-bar">
                <div 
                  className="badge-progress-fill"
                  style={{ 
                    width: `${progress}%`,
                    backgroundColor: getCategoryColor(achievement.category)
                  }}
                />
              </div>
              <div className="badge-progress-percentage">{progress}%</div>
            </div>
          )}
        </div>
      )}

      {/* Rarity Indicator */}
      <div className={`badge-rarity ${achievement.rarity.toLowerCase()}`}>
        {achievement.rarity}
      </div>

      {/* Shine Effect for Earned Badges */}
      {isEarned && <div className="badge-shine"></div>}
    </div>
  );
};

export default Badge;