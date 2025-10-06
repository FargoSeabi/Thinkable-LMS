import React from 'react';
import { useTimer } from '../../contexts/TimerContext';
import './StudyTimer.css';

interface StudyTimerProps {
  className?: string;
}

const StudyTimer: React.FC<StudyTimerProps> = ({
  className = ''
}) => {
  const {
    timeLeft,
    isActive,
    isPaused,
    currentMode,
    sessionCount,
    timerSettings,
    startStudySession,
    startBreakSession,
    pauseTimer,
    resumeTimer,
    stopTimer,
    getProgress,
    formatTime,
    getPresetDisplayName
  } = useTimer();

  // Get timer CSS classes based on preset
  const getTimerClasses = (): string => {
    const baseClass = 'study-timer';
    const styleClass = `timer-${timerSettings.timerStyle}`;
    const modeClass = `timer-${currentMode}`;
    const stateClass = isActive ? (isPaused ? 'paused' : 'running') : 'stopped';
    
    return `${baseClass} ${styleClass} ${modeClass} ${stateClass} ${className}`.trim();
  };

  // Get encouragement message based on preset
  const getEncouragementMessage = (): string => {
    const style = timerSettings.timerStyle;
    
    if (currentMode === 'study') {
      switch (style) {
        case 'adhd':
          return sessionCount === 0 ? 'Time to focus! You\'ve got this! 💪' : `Great job! Session ${sessionCount + 1} starting! 🎯`;
        case 'quiet':
          return 'Focus time started.';
        case 'calm':
          return 'Begin your focused study session.';
        default:
          return 'Study session in progress. Stay focused! 📚';
      }
    } else {
      switch (style) {
        case 'adhd':
          return 'Break time! Move around and recharge! ☕';
        case 'quiet':
          return 'Break time.';
        case 'calm':
          return 'Take your break.';
        default:
          return 'Break time! Step away and refresh. ☕';
      }
    }
  };

  // Get completion message based on preset
  const getCompletionMessage = (): string => {
    const style = timerSettings.timerStyle;
    
    if (currentMode === 'study') {
      switch (style) {
        case 'adhd':
          return `Amazing focus! 🎉 You completed ${timerSettings.studyLength} minutes of study!`;
        case 'quiet':
          return 'Study session complete.';
        case 'calm':
          return 'Study session finished.';
        default:
          return `Great work! You've completed a ${timerSettings.studyLength}-minute study session!`;
      }
    } else {
      switch (style) {
        case 'adhd':
          return 'Break complete! Ready to focus again? 🚀';
        case 'quiet':
          return 'Break complete.';
        case 'calm':
          return 'Break finished.';
        default:
          return 'Break time over! Ready to continue learning?';
      }
    }
  };

  return (
    <div className={getTimerClasses()}>

      {/* Timer Header */}
      <div className="timer-header">
        <div className="timer-title-section">
          <h3 className="timer-title">
            {currentMode === 'study' ? 'Study Session' : 'Break Time'}
          </h3>
          <div className="preset-info">
            <span className="preset-label">
              {timerSettings.studyLength}min • {getPresetDisplayName()}
            </span>
          </div>
        </div>
        
        {timerSettings.timerStyle === 'adhd' && sessionCount > 0 && (
          <div className="session-count">
            <span className="count-badge">Session {sessionCount + 1}</span>
          </div>
        )}
      </div>

      {/* Simple Timer Display */}
      <div className="timer-display">
        <div className="time-text">{formatTime(timeLeft)}</div>
        <div className="progress-container">
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ width: `${getProgress()}%` }}
            />
          </div>
          <div className="progress-text">
            {Math.floor((getProgress()))}% complete
          </div>
        </div>
      </div>

      {/* Encouragement Message */}
      <div className="timer-message">
        {isActive ? getEncouragementMessage() : getCompletionMessage()}
      </div>

      {/* Timer Controls */}
      <div className="timer-controls">
        {!isActive ? (
          <button 
            className="timer-btn primary-btn" 
            onClick={startStudySession}
          >
            Start {currentMode === 'study' ? 'Study' : 'Break'}
          </button>
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

export default StudyTimer;