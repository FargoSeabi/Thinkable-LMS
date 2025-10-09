import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const LogoutTest: React.FC = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    console.log('LogoutTest: Starting logout process');
    console.log('LogoutTest: Current user:', user);
    console.log('LogoutTest: Is authenticated:', isAuthenticated);
    
    console.log('LogoutTest: Navigating to /');
    navigate('/');
    
    console.log('LogoutTest: Calling logout()');
    logout();
    
    console.log('LogoutTest: Logout process completed');
  };

  return (
    <div style={{ padding: '20px', border: '1px solid #ccc', margin: '20px' }}>
      <h3>Logout Test Component</h3>
      <p>Current User: {user ? `${user.firstName} ${user.lastName} (${user.role})` : 'Not logged in'}</p>
      <p>Is Authenticated: {isAuthenticated ? 'Yes' : 'No'}</p>
      <button onClick={handleLogout} style={{ padding: '10px 20px', fontSize: '16px' }}>
        Test Logout
      </button>
    </div>
  );
};

export default LogoutTest;