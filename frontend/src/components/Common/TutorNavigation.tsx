import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './TutorNavigation.css';

const TutorNavigation: React.FC = () => {
  const { user, logout } = useAuth();
  const location = useLocation();

  const navItems = [
    { path: '/tutor', label: 'Dashboard', icon: 'fas fa-home' },
    { path: '/tutor/upload', label: 'Upload', icon: 'fas fa-upload' },
    { path: '/tutor/content', label: 'Content', icon: 'fas fa-folder-open' },
    { path: '/tutor/quizzes', label: 'Quizzes', icon: 'fas fa-question-circle' },
    { path: '/tutor/messages', label: 'Messages', icon: 'fas fa-comments' }
  ];

  const getPageTitle = () => {
    const currentItem = navItems.find(item => item.path === location.pathname);
    return currentItem?.label || 'ThinkAble';
  };

  return (
    <nav className="tutor-navigation">
      <div className="nav-header">
        <div className="nav-brand">
          <Link to="/tutor" className="brand-link">
            <i className="fas fa-chalkboard-teacher"></i>
            <span>ThinkAble</span>
          </Link>
        </div>

        <div className="nav-status">
          <span className="current-page">{getPageTitle()}</span>
          <span className="role-indicator">
            <i className="fas fa-user-tie"></i>
            Tutor
          </span>
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

export default TutorNavigation;