import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useAdaptiveUI } from '../../contexts/AdaptiveUIContext';
import TimerIndicator from './TimerIndicator';
import './StudentNavigation.css';

const StudentNavigation: React.FC = () => {
  const { user, logout } = useAuth();
  const { currentPreset } = useAdaptiveUI();
  const location = useLocation();

  const navItems = [
    { path: '/student', label: 'Dashboard', icon: 'fas fa-home' },
    { path: '/student/content', label: 'Learn', icon: 'fas fa-search' },
    { path: '/student/notes', label: 'My Notes', icon: 'fas fa-sticky-note' },
    { path: '/student/progress', label: 'Progress', icon: 'fas fa-chart-line' },
    { path: '/student/assessment', label: 'Assessment', icon: 'fas fa-brain' },
    { path: '/student/settings', label: 'Settings', icon: 'fas fa-cog' }
  ];

  const getPageTitle = () => {
    const currentItem = navItems.find(item => item.path === location.pathname);
    return currentItem?.label || 'ThinkAble';
  };

  const getPresetName = (preset: string) => {
    const presetNames: Record<string, string> = {
      'STANDARD_ADAPTIVE': 'Standard',
      'FOCUS_ENHANCED': 'Focus Enhanced',
      'FOCUS_CALM': 'Focus Calm',
      'READING_SUPPORT': 'Reading Support',
      'SOCIAL_SIMPLE': 'Social Simple',
      'SENSORY_CALM': 'Sensory Calm'
    };
    return presetNames[preset] || 'Standard';
  };

  return (
    <nav className="student-navigation">
      <div className="nav-header">
        <div className="nav-brand">
          <Link to="/student" className="brand-link">
            <i className="fas fa-graduation-cap"></i>
            <span>ThinkAble</span>
          </Link>
        </div>
        
        <div className="nav-status">
          <span className="current-page">{getPageTitle()}</span>
          <TimerIndicator />
          {currentPreset !== 'STANDARD_ADAPTIVE' && (
            <span className="preset-indicator">
              <i className="fas fa-check-circle"></i>
              {getPresetName(currentPreset)}
            </span>
          )}
        </div>
      </div>

      <div className="nav-items">
        {navItems.map((item) => (
          <Link 
            key={item.path}
            to={item.path}
            className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
          >
            <i className={item.icon}></i>
            <span>{item.label}</span>
          </Link>
        ))}
      </div>

      <div className="nav-user">
        <div className="user-greeting">
          <span>Hi, {user?.firstName}!</span>
        </div>
        <button 
          onClick={() => logout()} 
          className="nav-logout"
          title="Logout"
        >
          <i className="fas fa-sign-out-alt"></i>
        </button>
      </div>
    </nav>
  );
};

export default StudentNavigation;