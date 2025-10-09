import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import '../Student/Dashboard.css';
import '../Dashboard/Dashboard.css';

const AdminDashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    console.log('AdminDashboard: Logout clicked');
    console.log('AdminDashboard: Calling logout() with navigation callback');
    logout(() => {
      console.log('AdminDashboard: Navigation callback executing');
      navigate('/');
    });
    console.log('AdminDashboard: Logout initiated');
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="header-content">
          <h1>ThinkAble - Admin Dashboard</h1>
          <div className="user-info">
            <span>Welcome, {user?.firstName} {user?.lastName}</span>
            <button onClick={handleLogout} className="logout-btn">Logout</button>
          </div>
        </div>
      </header>

      <main className="dashboard-main">
        <div className="dashboard-grid">
          <div className="dashboard-card">
            <div className="card-icon">
              <i className="fas fa-users-cog"></i>
            </div>
            <h3>User Management</h3>
            <p>Manage students, tutors, and their permissions across the platform.</p>
            <button className="card-button" disabled>
              Coming Soon
            </button>
          </div>

          <div className="dashboard-card">
            <div className="card-icon">
              <i className="fas fa-content-copy"></i>
            </div>
            <h3>Content Moderation</h3>
            <p>Review and approve educational content uploaded by tutors.</p>
            <button className="card-button" disabled>
              Coming Soon
            </button>
          </div>

          <div className="dashboard-card">
            <div className="card-icon">
              <i className="fas fa-chart-pie"></i>
            </div>
            <h3>Platform Analytics</h3>
            <p>View comprehensive analytics and usage statistics for the platform.</p>
            <button className="card-button" disabled>
              Coming Soon
            </button>
          </div>

          <div className="dashboard-card">
            <div className="card-icon">
              <i className="fas fa-cogs"></i>
            </div>
            <h3>System Settings</h3>
            <p>Configure platform settings, features, and system parameters.</p>
            <button className="card-button" disabled>
              Coming Soon
            </button>
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;