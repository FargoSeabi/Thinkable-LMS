import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import config from '../../services/config';
import './FocusHelper.css';

interface FocusSession {
  task: string;
  duration: number;
  startTime: number;
  breaks: number;
  energyLevel: number;
}

interface FocusHelperProps {
  isMinimized?: boolean;
  onMinimize?: () => void;
  onRestore?: () => void;
}

const FocusHelper: React.FC<FocusHelperProps> = ({ 
  isMinimized = false, 
  onMinimize, 
  onRestore 
}) => {
  const { user } = useAuth();
  const [isVisible, setIsVisible] = useState(true);
  const [currentSession, setCurrentSession] = useState<FocusSession | null>(null);
  const [energyLevel, setEnergyLevel] = useState(7);
  const [isBreakTime, setIsBreakTime] = useState(false);
  const [showEnergyCheck, setShowEnergyCheck] = useState(false);
  const [showBreathingExercise, setShowBreathingExercise] = useState(false);
  const [showCalmMode, setShowCalmMode] = useState(false);
  const [achievements, setAchievements] = useState({
    sessionsCompleted: 0,
    breaksTaken: 0,
    selfCarePoints: 0
  });

  const timerRef = useRef<NodeJS.Timeout | null>(null);
  const breakTimerRef = useRef<NodeJS.Timeout | null>(null);

  // Load saved data
  useEffect(() => {
    const saved = localStorage.getItem('focusHelperData');
    if (saved) {
      try {
        const data = JSON.parse(saved);
        setEnergyLevel(data.energyLevel || 7);
        setAchievements(data.achievements || achievements);
      } catch (error) {
        console.warn('Failed to load focus helper data:', error);
      }
    }
  }, []);

  // Save data when it changes
  useEffect(() => {
    const dataToSave = {
      energyLevel,
      achievements,
      timestamp: Date.now()
    };
    localStorage.setItem('focusHelperData', JSON.stringify(dataToSave));
  }, [energyLevel, achievements]);

  // Auto energy check based on user preset
  useEffect(() => {
    if (!user?.recommendedPreset) return;

    let interval: number;
    switch (user.recommendedPreset) {
      case 'ADHD_SUPPORT':
        interval = 20 * 60 * 1000; // 20 minutes for ADHD
        break;
      case 'SENSORY_CALM':
        interval = 15 * 60 * 1000; // 15 minutes for sensory sensitive
        break;
      default:
        interval = 30 * 60 * 1000; // 30 minutes default
    }

    const energyTimer = setInterval(() => {
      if (Math.random() < 0.3) { // 30% chance
        setShowEnergyCheck(true);
      }
    }, interval);

    return () => clearInterval(energyTimer);
  }, [user?.recommendedPreset]);

  const startFocusSession = useCallback((task: string, duration: number) => {
    const session: FocusSession = {
      task,
      duration,
      startTime: Date.now(),
      breaks: 0,
      energyLevel
    };

    setCurrentSession(session);
    setIsBreakTime(false);

    // Set break reminder based on user preset
    const breakInterval = getBreakInterval();
    timerRef.current = setTimeout(() => {
      setIsBreakTime(true);
      playGentleChime();
    }, breakInterval * 60 * 1000);

    recordUsage('focus_session_started', { task, duration, preset: user?.recommendedPreset });
  }, [energyLevel, user?.recommendedPreset]);

  const endFocusSession = useCallback(() => {
    if (currentSession) {
      const sessionDuration = (Date.now() - currentSession.startTime) / (1000 * 60);
      setAchievements(prev => ({
        ...prev,
        sessionsCompleted: prev.sessionsCompleted + 1
      }));

      recordUsage('focus_session_completed', { 
        duration: sessionDuration, 
        breaks: currentSession.breaks 
      });
    }

    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }

    setCurrentSession(null);
    setIsBreakTime(false);
  }, [currentSession]);

  const takeBreak = useCallback((breakType: 'movement' | 'breathing' | 'sensory' | 'hydration') => {
    setIsBreakTime(false);
    
    if (currentSession) {
      setCurrentSession(prev => prev ? { ...prev, breaks: prev.breaks + 1 } : prev);
    }

    setAchievements(prev => ({
      ...prev,
      breaksTaken: prev.breaksTaken + 1,
      selfCarePoints: prev.selfCarePoints + 1
    }));

    // Start break timer (5 minutes)
    breakTimerRef.current = setTimeout(() => {
      playGentleChime();
      // Notification to return to work
    }, 5 * 60 * 1000);

    recordUsage('break_taken', { type: breakType });

    // Show appropriate break content
    switch (breakType) {
      case 'breathing':
        setShowBreathingExercise(true);
        break;
      case 'sensory':
        // Could open fidget tools
        break;
    }
  }, [currentSession]);

  const handleOverwhelm = useCallback(() => {
    setShowCalmMode(true);
    setAchievements(prev => ({
      ...prev,
      selfCarePoints: prev.selfCarePoints + 2
    }));
    recordUsage('overwhelm_escape_used');
  }, []);

  const getBreakInterval = () => {
    if (!user?.recommendedPreset) return 25;
    
    switch (user.recommendedPreset) {
      case 'ADHD_SUPPORT': return 15; // Shorter intervals for ADHD
      case 'AUTISM_SUPPORT': return 30; // Longer, predictable intervals
      case 'SENSORY_CALM': return 20; // Regular breaks for sensory processing
      default: return 25;
    }
  };

  const getSessionOptions = () => {
    if (!user?.recommendedPreset) return [15, 25, 45];
    
    switch (user.recommendedPreset) {
      case 'ADHD_SUPPORT': return [10, 15, 25]; // Shorter sessions
      case 'AUTISM_SUPPORT': return [25, 45, 60]; // Longer, structured sessions
      case 'SENSORY_CALM': return [15, 20, 30]; // Gentle durations
      default: return [15, 25, 45];
    }
  };

  const playGentleChime = () => {
    try {
      const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();
      
      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);
      
      oscillator.frequency.value = 440; // A note
      gainNode.gain.setValueAtTime(0, audioContext.currentTime);
      gainNode.gain.linearRampToValueAtTime(0.1, audioContext.currentTime + 0.1);
      gainNode.gain.linearRampToValueAtTime(0, audioContext.currentTime + 0.5);
      
      oscillator.start(audioContext.currentTime);
      oscillator.stop(audioContext.currentTime + 0.5);
    } catch (error) {
      console.warn('Could not play notification sound:', error);
    }
  };

  const recordUsage = (action: string, details?: any) => {
    try {
      fetch(`${config.apiBaseUrl}/api/neurodivergent/usage`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          action,
          details: {
            ...details,
            userId: user?.id
          },
          energyLevel,
          userPreset: user?.recommendedPreset,
          timestamp: new Date().toISOString()
        })
      }).catch(console.warn);
    } catch (error) {
      console.warn('Failed to record usage:', error);
    }
  };

  const formatSessionTime = (session: FocusSession) => {
    const elapsed = Math.floor((Date.now() - session.startTime) / 60000);
    if (elapsed < 60) return `${elapsed}m`;
    return `${Math.floor(elapsed / 60)}h ${elapsed % 60}m`;
  };

  const getPresetClassName = () => {
    return user?.recommendedPreset?.toLowerCase().replace('_', '-') || 'standard';
  };

  if (isMinimized) {
    return (
      <div className={`focus-helper-minimized ${getPresetClassName()}`}>
        <button onClick={onRestore} className="restore-btn" title="Open Focus Helper">
          🧠
          {currentSession && (
            <span className="session-indicator">
              <i className="fas fa-circle"></i>
            </span>
          )}
        </button>
      </div>
    );
  }

  if (!isVisible) return null;

  return (
    <>
      <div className={`focus-helper-panel ${getPresetClassName()}`}>
        {/* Header */}
        <div className="panel-header">
          <div className="panel-title">
            <i className="fas fa-brain"></i>
            <span>Focus Helper</span>
            {user?.recommendedPreset && (
              <span className="preset-indicator">
                {user.recommendedPreset.replace('_', ' ').toLowerCase()}
              </span>
            )}
          </div>
          <div className="panel-controls">
            <button onClick={onMinimize} className="control-btn" title="Minimize">
              <i className="fas fa-minus"></i>
            </button>
            <button onClick={() => setIsVisible(false)} className="control-btn" title="Hide">
              <i className="fas fa-times"></i>
            </button>
          </div>
        </div>

        {/* Overwhelm Escape Hatch */}
        <button 
          className="escape-hatch"
          onClick={handleOverwhelm}
          title="Need immediate calm and support"
        >
          <i className="fas fa-life-ring"></i>
          <span>I Need Calm Now</span>
        </button>

        {/* Current Session Display */}
        {currentSession && (
          <div className="current-session">
            <div className="session-info">
              <h3>
                <i className="fas fa-tasks"></i>
                {currentSession.task}
              </h3>
              <div className="session-stats">
                <span>Time: {formatSessionTime(currentSession)}</span>
                <span>Breaks: {currentSession.breaks}</span>
              </div>
            </div>
            <button onClick={endFocusSession} className="end-session-btn">
              <i className="fas fa-stop"></i>
              End Session
            </button>
          </div>
        )}

        {/* Energy Level Display */}
        <div className="energy-display">
          <span>Energy Level:</span>
          <div className="energy-hearts">
            {Array.from({ length: 10 }, (_, i) => (
              <span key={i} className={i < energyLevel ? 'heart filled' : 'heart empty'}>
                {i < energyLevel ? '❤️' : '🤍'}
              </span>
            ))}
          </div>
        </div>

        {/* Quick Tools */}
        <div className="quick-tools">
          {!currentSession && (
            <button 
              className="tool-btn primary"
              onClick={() => {
                // Show session start modal
                const task = prompt('What are you working on?') || 'Focus session';
                const duration = user?.recommendedPreset === 'ADHD_SUPPORT' ? 15 : 25;
                startFocusSession(task, duration);
              }}
            >
              <i className="fas fa-play"></i>
              Start Focus
            </button>
          )}
          
          <button 
            className="tool-btn"
            onClick={() => setShowEnergyCheck(true)}
            title="Check your energy level"
          >
            <i className="fas fa-battery-half"></i>
            Energy Check
          </button>

          <button 
            className="tool-btn"
            onClick={() => setShowBreathingExercise(true)}
            title="Breathing exercise for calm"
          >
            <i className="fas fa-lungs"></i>
            Breathe
          </button>

          <button 
            className="tool-btn"
            onClick={() => takeBreak('movement')}
            title="Take a movement break"
          >
            <i className="fas fa-walking"></i>
            Move
          </button>
        </div>

        {/* Achievements */}
        <div className="achievements-display">
          <div className="achievement-item">
            <i className="fas fa-check-circle"></i>
            <span>{achievements.sessionsCompleted} sessions</span>
          </div>
          <div className="achievement-item">
            <i className="fas fa-coffee"></i>
            <span>{achievements.breaksTaken} breaks</span>
          </div>
          <div className="achievement-item">
            <i className="fas fa-heart"></i>
            <span>{achievements.selfCarePoints} self-care</span>
          </div>
        </div>
      </div>

      {/* Break Reminder Modal */}
      {isBreakTime && (
        <div className="break-modal-overlay">
          <div className={`break-modal ${getPresetClassName()}`}>
            <h3><i className="fas fa-pause-circle"></i> Time for a Brain Break!</h3>
            <p>You've been focusing hard! Your brain needs a refresh.</p>
            
            <div className="break-options">
              <button 
                className="break-option movement"
                onClick={() => takeBreak('movement')}
              >
                <i className="fas fa-walking"></i>
                <h4>Movement Break</h4>
                <p>2-3 minutes of gentle movement</p>
              </button>
              
              <button 
                className="break-option breathing"
                onClick={() => takeBreak('breathing')}
              >
                <i className="fas fa-lungs"></i>
                <h4>Breathing Break</h4>
                <p>5 minutes of mindful breathing</p>
              </button>
              
              <button 
                className="break-option sensory"
                onClick={() => takeBreak('sensory')}
              >
                <i className="fas fa-hand-paper"></i>
                <h4>Sensory Break</h4>
                <p>Fidget tools and calming activities</p>
              </button>
              
              <button 
                className="break-option hydrate"
                onClick={() => takeBreak('hydration')}
              >
                <i className="fas fa-tint"></i>
                <h4>Hydration Break</h4>
                <p>Drink water and reset</p>
              </button>
            </div>
            
            <div className="break-actions">
              <button 
                className="skip-break"
                onClick={() => setIsBreakTime(false)}
              >
                I'm in hyperfocus - remind me in 15 min
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Energy Check Modal */}
      {showEnergyCheck && (
        <div className="energy-modal-overlay">
          <div className={`energy-modal ${getPresetClassName()}`}>
            <h3><i className="fas fa-battery-half"></i> How's Your Energy?</h3>
            <p>Understanding your energy helps me support you better!</p>
            
            <div className="energy-scale">
              {Array.from({ length: 10 }, (_, i) => (
                <button
                  key={i}
                  className={`energy-option ${energyLevel === i + 1 ? 'selected' : ''}`}
                  onClick={() => {
                    setEnergyLevel(i + 1);
                    setShowEnergyCheck(false);
                    recordUsage('energy_level_updated', { level: i + 1 });
                  }}
                >
                  <div className="energy-hearts-small">
                    {Array.from({ length: Math.ceil((i + 1) / 2) }, (_, j) => (
                      <span key={j}>❤️</span>
                    ))}
                  </div>
                  <span>{i + 1}</span>
                </button>
              ))}
            </div>
            
            <button 
              className="close-modal"
              onClick={() => setShowEnergyCheck(false)}
            >
              Done
            </button>
          </div>
        </div>
      )}

      {/* Breathing Exercise Modal */}
      {showBreathingExercise && (
        <BreathingExercise 
          isVisible={showBreathingExercise}
          onClose={() => setShowBreathingExercise(false)}
          preset={user?.recommendedPreset}
        />
      )}

      {/* Calm Mode Modal */}
      {showCalmMode && (
        <CalmMode 
          isVisible={showCalmMode}
          onClose={() => setShowCalmMode(false)}
          preset={user?.recommendedPreset}
        />
      )}
    </>
  );
};

// Breathing Exercise Component
const BreathingExercise: React.FC<{
  isVisible: boolean;
  onClose: () => void;
  preset?: string;
}> = ({ isVisible, onClose, preset }) => {
  const [phase, setPhase] = useState<'inhale' | 'hold' | 'exhale' | 'pause'>('inhale');
  const [count, setCount] = useState(0);
  const [isActive, setIsActive] = useState(false);

  useEffect(() => {
    if (!isActive) return;

    const timer = setInterval(() => {
      setCount(prev => {
        const newCount = prev + 1;
        
        switch (phase) {
          case 'inhale':
            if (newCount >= 4) {
              setPhase('hold');
              return 0;
            }
            break;
          case 'hold':
            if (newCount >= 2) {
              setPhase('exhale');
              return 0;
            }
            break;
          case 'exhale':
            if (newCount >= 4) {
              setPhase('pause');
              return 0;
            }
            break;
          case 'pause':
            if (newCount >= 2) {
              setPhase('inhale');
              return 0;
            }
            break;
        }
        
        return newCount;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [isActive, phase]);

  if (!isVisible) return null;

  const getInstruction = () => {
    switch (phase) {
      case 'inhale': return 'Breathe in slowly...';
      case 'hold': return 'Hold your breath...';
      case 'exhale': return 'Breathe out gently...';
      case 'pause': return 'Rest and relax...';
    }
  };

  return (
    <div className="breathing-overlay">
      <div className={`breathing-modal ${preset?.toLowerCase().replace('_', '-') || 'standard'}`}>
        <h3><i className="fas fa-dove"></i> Take a Breath</h3>
        <p>You're safe. Let's reset together.</p>
        
        <div className={`breathing-circle ${phase} ${isActive ? 'active' : ''}`}>
          <div className="circle-inner">
            <span className="breath-instruction">{getInstruction()}</span>
          </div>
        </div>
        
        <div className="breathing-controls">
          <button 
            className={`breathing-btn ${isActive ? 'active' : ''}`}
            onClick={() => setIsActive(!isActive)}
          >
            <i className={`fas ${isActive ? 'fa-pause' : 'fa-play'}`}></i>
            {isActive ? 'Pause' : 'Start Breathing'}
          </button>
          
          <button className="close-breathing" onClick={onClose}>
            <i className="fas fa-arrow-left"></i>
            I'm Ready to Continue
          </button>
        </div>
      </div>
    </div>
  );
};

// Calm Mode Component  
const CalmMode: React.FC<{
  isVisible: boolean;
  onClose: () => void;
  preset?: string;
}> = ({ isVisible, onClose, preset }) => {
  if (!isVisible) return null;

  const affirmations = [
    "Your brain works beautifully in its own unique way.",
    "Taking breaks shows wisdom, not weakness.",
    "You are capable of amazing things.",
    "Every small step forward is worth celebrating.",
    "Your neurodivergent mind is a gift to the world."
  ];

  const randomAffirmation = affirmations[Math.floor(Math.random() * affirmations.length)];

  return (
    <div className="calm-overlay">
      <div className={`calm-modal ${preset?.toLowerCase().replace('_', '-') || 'standard'}`}>
        <h2><i className="fas fa-dove"></i> Take a Breath</h2>
        <p>You're safe. Let's reset together.</p>
        
        <div className="affirmation-card">
          <i className="fas fa-heart"></i>
          <p>{randomAffirmation}</p>
        </div>
        
        <div className="calm-options">
          <button className="calm-btn">
            <i className="fas fa-lungs"></i>
            Guided Breathing
          </button>
          <button className="calm-btn">
            <i className="fas fa-volume-up"></i>
            Calming Sounds
          </button>
          <button className="calm-btn">
            <i className="fas fa-heart"></i>
            Positive Reminders
          </button>
          <button className="calm-btn" onClick={onClose}>
            <i className="fas fa-arrow-left"></i>
            I'm Ready to Continue
          </button>
        </div>
      </div>
    </div>
  );
};

export default FocusHelper;