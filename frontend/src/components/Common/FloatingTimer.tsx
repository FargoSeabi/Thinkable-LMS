import React from 'react';
import { useTimer } from '../../contexts/TimerContext';
import './FloatingTimer.css';

const FloatingTimer: React.FC = () => {
  const {
    timeLeft,
    isActive,
    isPaused,
    currentMode,
    sessionCount,
    timerSettings,
    showTimer,
    toggleTimer,
    startStudySession,
    startBreakSession,
    pauseTimer,
    resumeTimer,
    stopTimer,
    getProgress,
    formatTime,
    getPresetDisplayName
  } = useTimer();

  // Don't render if timer is not visible
  if (!showTimer) {
    return null;
  }

  // Get timer CSS classes based on preset
  const getTimerClasses = (): string => {
    const baseClass = 'floating-timer';
    const styleClass = `timer-${timerSettings.timerStyle}`;
    const modeClass = `timer-${currentMode}`;
    const stateClass = isActive ? (isPaused ? 'paused' : 'running') : 'stopped';
    
    return `${baseClass} ${styleClass} ${modeClass} ${stateClass}`.trim();
  };

  return (
    <div className={getTimerClasses()}>
      {/* Timer Header */}
      <div className="floating-timer-header">
        <div className="timer-title-section">
          <h4 className="timer-title">
            {currentMode === 'study' ? 'Study Session' : 'Break Time'}
          </h4>
          <div className="preset-info">
            <span className="preset-label">
              {currentMode === 'study' ? timerSettings.studyLength : timerSettings.breakLength}min • {getPresetDisplayName()}
            </span>
          </div>
        </div>
        
        <div className="timer-actions">
          {timerSettings.timerStyle === 'adhd' && sessionCount > 0 && (
            <span className="count-badge">#{sessionCount + 1}</span>
          )}
          <button 
            className="close-btn" 
            onClick={toggleTimer}
            title="Hide timer"
          >
            ×
          </button>
        </div>
      </div>

      {/* Timer Display */}
      <div className="floating-timer-display">
        <div className="time-text">{formatTime(timeLeft)}</div>
        <div className="progress-container">
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ width: `${getProgress()}%` }}
            />
          </div>
          <div className="progress-text">
            {Math.floor(getProgress())}% complete
          </div>
        </div>
      </div>

      {/* Timer Controls */}
      <div className="floating-timer-controls">
        {!isActive ? (
          <div className="start-controls">
            <button 
              className="timer-btn primary-btn" 
              onClick={currentMode === 'study' ? startStudySession : startBreakSession}
            >
              Start {currentMode === 'study' ? 'Study' : 'Break'}
            </button>
          </div>
        ) : (
          <div className="active-controls">
            <button 
              className="timer-btn secondary-btn" 
              onClick={isPaused ? resumeTimer : pauseTimer}
            >
              {isPaused ? 'Resume' : 'Pause'}
            </button>
            <button 
              className="timer-btn danger-btn" 
              onClick={stopTimer}
            >
              Stop
            </button>
          </div>
        )}
      </div>

      {/* Quick Actions for completed session */}
      {timeLeft === 0 && !isActive && (
        <div className="quick-actions">
          {currentMode === 'study' ? (
            <>
              <button className="quick-action-btn" onClick={startBreakSession}>
                Start Break
              </button>
              <button className="quick-action-btn secondary" onClick={startStudySession}>
                Another Session
              </button>
            </>
          ) : (
            <button className="quick-action-btn" onClick={startStudySession}>
              Continue Studying
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default FloatingTimer;