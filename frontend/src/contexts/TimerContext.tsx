import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { useAdaptiveUI } from './AdaptiveUIContext';
import { apiService } from '../services/api';

interface TimerSettings {
  studyLength: number; // minutes
  breakLength: number; // minutes
  timerStyle: 'standard' | 'adhd' | 'quiet' | 'calm';
  enableNotifications: boolean;
  enableSounds: boolean;
}

interface SessionData {
  duration: number;
  completed: boolean;
  type: 'study' | 'break';
  timestamp: Date;
}

interface TimerContextType {
  // Timer state
  timeLeft: number; // seconds
  isActive: boolean;
  isPaused: boolean;
  currentMode: 'study' | 'break';
  sessionCount: number;
  timerSettings: TimerSettings;
  
  // Timer controls
  startStudySession: () => void;
  startBreakSession: () => void;
  pauseTimer: () => void;
  resumeTimer: () => void;
  stopTimer: () => void;
  
  // UI state
  showTimer: boolean;
  toggleTimer: () => void;
  
  // Progress info
  getProgress: () => number;
  formatTime: (seconds: number) => string;
  getPresetDisplayName: () => string;
  
  // Event handlers
  onSessionComplete?: (sessionData: SessionData) => void;
  onBreakComplete?: () => void;
}

const TimerContext = createContext<TimerContextType | undefined>(undefined);

export const useTimer = () => {
  const context = useContext(TimerContext);
  if (context === undefined) {
    throw new Error('useTimer must be used within a TimerProvider');
  }
  return context;
};

interface TimerProviderProps {
  children: ReactNode;
}

export const TimerProvider: React.FC<TimerProviderProps> = ({ children }) => {
  const { user } = useAuth();
  const { isADHDFriendly, isAutismFriendly, isSensoryFriendly, currentPreset } = useAdaptiveUI();
  
  // Timer state
  const [timeLeft, setTimeLeft] = useState(0);
  const [isActive, setIsActive] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [currentMode, setCurrentMode] = useState<'study' | 'break'>('study');
  const [sessionCount, setSessionCount] = useState(0);
  const [showTimer, setShowTimer] = useState(false);
  const [userSettings, setUserSettings] = useState<any>(null);

  // Load user settings from backend
  useEffect(() => {
    const loadUserSettings = async () => {
      if (!user?.id) return;
      
      try {
        const profile = await apiService.get(`/api/assessment/profile/${user.id}`);
        if (profile) {
          if (profile.uiSettings) {
            setUserSettings(profile.uiSettings);
            return;
          }
        }
      } catch (error) {
        console.warn('Could not load user settings:', error);
      }
      
      setUserSettings({ breakIntervals: 25, uiPreset: currentPreset.toLowerCase() });
    };
    
    loadUserSettings();
  }, [user, currentPreset]);

  // Get timer settings from user's assessment results
  const getTimerSettings = useCallback((): TimerSettings => {
    let studyLength = 25;
    
    if (userSettings?.breakIntervals) {
      studyLength = userSettings.breakIntervals;
    } else {
      if (isADHDFriendly) studyLength = 15;
      else if (isSensoryFriendly) studyLength = 20;
      else studyLength = 25;
    }
    
    const breakLength = Math.max(5, Math.floor(studyLength * 0.2));
    
    let timerStyle: 'standard' | 'adhd' | 'quiet' | 'calm' = 'standard';
    let enableNotifications = true;
    let enableSounds = true;
    
    if (isADHDFriendly) {
      timerStyle = 'adhd';
      enableNotifications = true;
      enableSounds = true;
    } else if (isAutismFriendly) {
      timerStyle = 'quiet';
      enableNotifications = true;
      enableSounds = false;
    } else if (isSensoryFriendly) {
      timerStyle = 'calm';
      enableNotifications = false;
      enableSounds = false;
    }
    
    return {
      studyLength,
      breakLength,
      timerStyle,
      enableNotifications,
      enableSounds
    };
  }, [userSettings, isADHDFriendly, isAutismFriendly, isSensoryFriendly]);

  const timerSettings = getTimerSettings();

  // Initialize timer with study session length
  useEffect(() => {
    if (!isActive && timeLeft === 0) {
      setTimeLeft(timerSettings.studyLength * 60);
    }
  }, [timerSettings.studyLength, isActive, timeLeft]);

  // Show notification
  const showNotification = useCallback((message: string) => {
    if (timerSettings.enableNotifications && 'Notification' in window) {
      if (Notification.permission === 'granted') {
        new Notification('ThinkAble Study Timer', {
          body: message,
          icon: '/favicon.ico'
        });
      } else if (Notification.permission === 'default') {
        Notification.requestPermission();
      }
    }
  }, [timerSettings.enableNotifications]);

  // Handle timer completion
  const handleTimerComplete = useCallback(() => {
    setIsActive(false);
    setIsPaused(false);

    const sessionData: SessionData = {
      duration: currentMode === 'study' ? timerSettings.studyLength : timerSettings.breakLength,
      completed: true,
      type: currentMode,
      timestamp: new Date()
    };

    if (currentMode === 'study') {
      setSessionCount(prev => prev + 1);
      showNotification('Study session complete! 🎉 Ready for a break?');
      
      // Auto-suggest break mode but don't start automatically
      setCurrentMode('break');
      setTimeLeft(timerSettings.breakLength * 60);
      
    } else {
      showNotification('Break complete! Ready to get back to studying?');
      setCurrentMode('study');
      setTimeLeft(timerSettings.studyLength * 60);
    }
  }, [currentMode, timerSettings, showNotification]);

  // Countdown timer effect
  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;

    if (isActive && !isPaused && timeLeft > 0) {
      interval = setInterval(() => {
        setTimeLeft((prevTime) => prevTime - 1);
      }, 1000);
    }

    if (timeLeft === 0 && isActive) {
      handleTimerComplete();
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isActive, isPaused, timeLeft, handleTimerComplete]);

  // Timer controls
  const startStudySession = useCallback(() => {
    setCurrentMode('study');
    setTimeLeft(timerSettings.studyLength * 60);
    setIsActive(true);
    setIsPaused(false);
    setShowTimer(true);
  }, [timerSettings.studyLength]);

  const startBreakSession = useCallback(() => {
    setCurrentMode('break');
    setTimeLeft(timerSettings.breakLength * 60);
    setIsActive(true);
    setIsPaused(false);
    setShowTimer(true);
  }, [timerSettings.breakLength]);

  const pauseTimer = useCallback(() => {
    setIsPaused(true);
  }, []);

  const resumeTimer = useCallback(() => {
    setIsPaused(false);
  }, []);

  const stopTimer = useCallback(() => {
    setIsActive(false);
    setIsPaused(false);
    setCurrentMode('study');
    setTimeLeft(timerSettings.studyLength * 60);
  }, [timerSettings.studyLength]);

  const toggleTimer = useCallback(() => {
    setShowTimer(!showTimer);
  }, [showTimer]);

  // Utility functions
  const formatTime = useCallback((seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }, []);

  const getProgress = useCallback((): number => {
    const totalTime = currentMode === 'study' 
      ? timerSettings.studyLength * 60 
      : timerSettings.breakLength * 60;
    return ((totalTime - timeLeft) / totalTime) * 100;
  }, [currentMode, timerSettings, timeLeft]);

  const getPresetDisplayName = useCallback((): string => {
    if (isADHDFriendly) return 'ADHD Focus';
    if (isAutismFriendly) return 'Autism Support';
    if (isSensoryFriendly) return 'Sensory Friendly';
    return 'Standard';
  }, [isADHDFriendly, isAutismFriendly, isSensoryFriendly]);

  // Auto-hide timer on session completion after 10 seconds
  useEffect(() => {
    if (!isActive && timeLeft > 0 && timeLeft < (currentMode === 'study' ? timerSettings.studyLength * 60 : timerSettings.breakLength * 60)) {
      const autoHideTimer = setTimeout(() => {
        if (!isActive) {
          setShowTimer(false);
        }
      }, 10000);

      return () => clearTimeout(autoHideTimer);
    }
  }, [isActive, timeLeft, currentMode, timerSettings]);

  const contextValue: TimerContextType = {
    // State
    timeLeft,
    isActive,
    isPaused,
    currentMode,
    sessionCount,
    timerSettings,
    
    // Controls
    startStudySession,
    startBreakSession,
    pauseTimer,
    resumeTimer,
    stopTimer,
    
    // UI
    showTimer,
    toggleTimer,
    
    // Utils
    getProgress,
    formatTime,
    getPresetDisplayName
  };

  return (
    <TimerContext.Provider value={contextValue}>
      {children}
    </TimerContext.Provider>
  );
};