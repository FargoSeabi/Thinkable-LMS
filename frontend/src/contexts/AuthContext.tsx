import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import axios from 'axios';
import config from '../services/config';

interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  recommendedPreset?: string;
  hasCompletedAssessment?: boolean;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<boolean>;
  register: (firstName: string, lastName: string, email: string, password: string, role: string, ageRange?: string) => Promise<boolean>;
  logout: () => void;
  isAuthenticated: boolean;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));

  const API_BASE_URL = config.apiBaseUrl;

  const fetchUserProfile = async () => {
    try {
      const email = localStorage.getItem('userEmail');
      if (!email) {
        logout();
        return;
      }
      const response = await axios.get(`${API_BASE_URL}/api/auth/profile?email=${email}`);
      if (response.data && response.data.id) {
        setUser({
          id: response.data.id,
          email: response.data.email,
          firstName: response.data.username || response.data.email.split('@')[0], // Use username or email prefix
          lastName: '',
          role: response.data.role
        });
      }
    } catch (error) {
      console.error('Failed to fetch user profile:', error);
      logout();
    }
  };

  useEffect(() => {
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      fetchUserProfile();
    }
  }, [token]);

  const login = async (email: string, password: string): Promise<boolean> => {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/auth/login`, {
        email,
        password
      });

      if (response.data.token) {
        const newToken = response.data.token;
        setToken(newToken);
        localStorage.setItem('token', newToken);
        localStorage.setItem('userEmail', email); // Store email for profile fetching
        axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
        
        // Don't set user until profile is fetched to avoid ID 0 issues
        // setUser will be called in fetchUserProfile with correct data
        await fetchUserProfile(); // Ensure user profile is loaded immediately
        return true;
      }
      return false;
    } catch (error) {
      console.error('Login failed:', error);
      return false;
    }
  };

  const register = async (
    firstName: string,
    lastName: string,
    email: string,
    password: string,
    role: string,
    ageRange?: string
  ): Promise<boolean> => {
    try {
      const name = `${firstName} ${lastName}`.trim();
      await axios.post(`${API_BASE_URL}/api/auth/register`, {
        name,
        email,
        password,
        role,
        ageRange
      });
      return true;
    } catch (error) {
      console.error('Registration failed:', error);
      return false;
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
    delete axios.defaults.headers.common['Authorization'];
  };

  const isAuthenticated = !!user && !!token;

  const hasRole = (role: string): boolean => {
    return user?.role === role;
  };

  const value: AuthContextType = {
    user,
    token,
    login,
    register,
    logout,
    isAuthenticated,
    hasRole
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};