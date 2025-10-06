import React from 'react';
import { useTimer } from '../../contexts/TimerContext';
import './TimerIndicator.css';

const TimerIndicator: React.FC = () => {
  const {
    timeLeft,
    isActive,
    isPaused,
    currentMode,
    showTimer,
    toggleTimer,
    formatTime,
    getProgress,
    timerSettings
  } = useTimer();

  // Only show if timer is active or has been used
  if (!isActive && timeLeft === (currentMode === 'study' ? timerSettings.studyLength * 60 : timerSettings.breakLength * 60)) {
    return null;
  }

  const handleClick = () => {
    toggleTimer();
  };

  const getIndicatorClasses = (): string => {
    const baseClass = 'timer-indicator';
    const modeClass = `indicator-${currentMode}`;
    const stateClass = isActive ? (isPaused ? 'paused' : 'running') : 'completed';
    
    return `${baseClass} ${modeClass} ${stateClass}`.trim();
  };

  return (
    <button 
      className={getIndicatorClasses()}
      onClick={handleClick}
      title={`${currentMode === 'study' ? 'Study' : 'Break'} timer: ${formatTime(timeLeft)} ${isActive ? (isPaused ? '(Paused)' : '(Running)') : '(Completed)'}`}
    >
      <div className="timer-indicator-content">
        <div className="timer-icon">
          <i className="fas fa-clock"></i>
          {isActive && (
            <div className="progress-ring">
              <svg viewBox="0 0 20 20">
                <circle
                  cx="10"
                  cy="10"
                  r="8"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  opacity="0.3"
                />
                <circle
                  cx="10"
                  cy="10"
                  r="8"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  style={{
                    strokeDasharray: 50.27,
                    strokeDashoffset: 50.27 - (50.27 * getProgress()) / 100,
                    transform: 'rotate(-90deg)',
                    transformOrigin: '50% 50%'
                  }}
                />
              </svg>
            </div>
          )}
        </div>
        <div className="timer-info">
          <div className="timer-time">{formatTime(timeLeft)}</div>
          <div className="timer-mode">{currentMode}</div>
        </div>
      </div>
      
      {!showTimer && (
        <div className="expand-hint">
          <i className="fas fa-chevron-up"></i>
        </div>
      )}
    </button>
  );
};

export default TimerIndicator;