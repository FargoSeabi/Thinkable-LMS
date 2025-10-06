import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useAdaptiveUI } from '../../contexts/AdaptiveUIContext';
import { useNotification } from '../../contexts/NotificationContext';
import { adaptiveUIPresets } from '../../utils/adaptiveUIPresets';
import config from '../../services/config';
import './Settings.css';

interface UserPreferences {
  currentPreset: string;
  manualOverride: boolean;
  assessmentResults?: any;
  customSettings: {
    highContrast: boolean;
    dyslexiaFriendly: boolean;
    adhdFriendly: boolean;
    autismFriendly: boolean;
    sensoryFriendly: boolean;
  };
}

interface PresetHistory {
  recentChanges: Array<{
    presetName: string;
    changeReason: string;
    timestamp: string;
    duration: string;
  }>;
  usageStats: Record<string, number>;
  recommendations: {
    mostUsed: string;
    suggested: string;
    reason: string;
  };
  totalSessions: number;
  averageSessionLength: string;
}

const Settings: React.FC = () => {
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const { 
    currentPreset, 
    availablePresets, 
    applyPreset, 
    resetToDefault,
    isHighContrast,
    isDyslexiaFriendly,
    isADHDFriendly,
    isAutismFriendly,
    isSensoryFriendly,
    toggleAccessibilityFeature
  } = useAdaptiveUI();

  const [preferences, setPreferences] = useState<UserPreferences>({
    currentPreset: currentPreset,
    manualOverride: false,
    customSettings: {
      highContrast: isHighContrast,
      dyslexiaFriendly: isDyslexiaFriendly,
      adhdFriendly: isADHDFriendly,
      autismFriendly: isAutismFriendly,
      sensoryFriendly: isSensoryFriendly
    }
  });

  const [showAdvanced, setShowAdvanced] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [presetHistory, setPresetHistory] = useState<PresetHistory | null>(null);
  const [showHistory, setShowHistory] = useState(false);

  useEffect(() => {
    loadUserPreferences();
  }, []);

  useEffect(() => {
    if (showHistory && !presetHistory) {
      loadPresetHistory();
    }
  }, [showHistory, presetHistory]);

  const loadUserPreferences = async () => {
    try {
      // Load from backend API first, fallback to localStorage
      if (user?.email) {
        const response = await fetch(`${config.apiBaseUrl}/api/user-preferences?username=${encodeURIComponent(user.email)}`);
        if (response.ok) {
          const backendPrefs = await response.json();
          setPreferences({
            currentPreset: backendPrefs.currentPreset || currentPreset,
            manualOverride: backendPrefs.manualOverride || false,
            assessmentResults: backendPrefs.assessmentResults || null,
            customSettings: {
              highContrast: backendPrefs.customSettings?.highContrast || isHighContrast,
              dyslexiaFriendly: backendPrefs.customSettings?.dyslexiaFriendly || isDyslexiaFriendly,
              adhdFriendly: backendPrefs.customSettings?.adhdFriendly || isADHDFriendly,
              autismFriendly: backendPrefs.customSettings?.autismFriendly || isAutismFriendly,
              sensoryFriendly: backendPrefs.customSettings?.sensoryFriendly || isSensoryFriendly
            }
          });
          return;
        }
      }
      
      // Fallback to localStorage if backend fails or user not logged in
      const savedPrefs = localStorage.getItem('thinkable-user-preferences');
      const assessmentResults = localStorage.getItem('thinkable-assessment-results');
      
      if (savedPrefs) {
        const parsedPrefs = JSON.parse(savedPrefs);
        setPreferences({
          ...parsedPrefs,
          assessmentResults: assessmentResults ? JSON.parse(assessmentResults) : null
        });
      }
    } catch (error) {
      console.error('Failed to load user preferences:', error);
      // Fallback to localStorage on error
      try {
        const savedPrefs = localStorage.getItem('thinkable-user-preferences');
        if (savedPrefs) {
          const parsedPrefs = JSON.parse(savedPrefs);
          setPreferences(parsedPrefs);
        }
      } catch (localError) {
        console.error('Failed to load from localStorage as well:', localError);
      }
    }
  };

  const loadPresetHistory = async () => {
    try {
      // Try to load from backend API first
      if (user?.email) {
        const response = await fetch(`${config.apiBaseUrl}/api/user-preferences/history?username=${encodeURIComponent(user.email)}`);
        if (response.ok) {
          const backendHistory = await response.json();
          setPresetHistory(backendHistory);
          return;
        }
      }
      
      // Fallback to mock data if backend unavailable
      const mockHistory: PresetHistory = {
        recentChanges: [
          {
            presetName: 'STANDARD_ADAPTIVE',
            changeReason: 'Assessment Recommended',
            timestamp: '2024-01-15T10:00:00',
            duration: '45 min'
          },
          {
            presetName: 'READING_SUPPORT',
            changeReason: 'Manual Selection',
            timestamp: '2024-01-16T14:30:00',
            duration: '1h 20min'
          },
          {
            presetName: 'SENSORY_CALM',
            changeReason: 'Manual Selection',
            timestamp: '2024-01-17T09:15:00',
            duration: '35 min'
          }
        ],
        usageStats: {
          'STANDARD_ADAPTIVE': 45,
          'READING_SUPPORT': 120,
          'SENSORY_CALM': 85,
          'FOCUS_ENHANCED': 30,
          'FOCUS_CALM': 15,
          'SOCIAL_SIMPLE': 8
        },
        recommendations: {
          mostUsed: 'READING_SUPPORT',
          suggested: 'FOCUS_ENHANCED',
          reason: 'Based on your usage patterns, you might benefit from enhanced focus features during study sessions'
        },
        totalSessions: 303,
        averageSessionLength: '28 minutes'
      };
      
      setPresetHistory(mockHistory);
    } catch (error) {
      console.error('Failed to load preset history:', error);
    }
  };

  const saveUserPreferences = async () => {
    setIsSaving(true);
    try {
      // Save to backend API first
      if (user?.email) {
        const params = new URLSearchParams({ username: user.email });
        const response = await fetch(`${config.apiBaseUrl}/api/user-preferences?${params}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(preferences),
        });

        if (!response.ok) {
          throw new Error('Failed to save to backend');
        }
      }
      
      // Also save to localStorage as backup
      localStorage.setItem('thinkable-user-preferences', JSON.stringify(preferences));
      
      // Apply the changes
      if (preferences.manualOverride) {
        applyPreset(preferences.currentPreset);
      }

      showNotification('Settings saved successfully!', 'success');
    } catch (error) {
      console.error('Failed to save user preferences:', error);
      // Try to save to localStorage at least
      try {
        localStorage.setItem('thinkable-user-preferences', JSON.stringify(preferences));
        showNotification('Settings saved locally (backend unavailable)', 'warning');
      } catch (localError) {
        showNotification('Failed to save settings. Please try again.', 'error');
      }
    } finally {
      setIsSaving(false);
    }
  };

  const handlePresetChange = (presetName: string) => {
    setPreferences(prev => ({
      ...prev,
      currentPreset: presetName,
      manualOverride: true
    }));
    applyPreset(presetName);
    
    // Log preset usage to backend
    if (user?.email) {
      const params = new URLSearchParams({ username: user.email });
      fetch(`${config.apiBaseUrl}/api/user-preferences/log-preset-usage?${params}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ presetName }),
      })
      .catch(error => console.error('Failed to log preset usage:', error));
    }
  };

  const handleToggleFeature = (feature: keyof UserPreferences['customSettings']) => {
    const newValue = !preferences.customSettings[feature];
    setPreferences(prev => ({
      ...prev,
      customSettings: {
        ...prev.customSettings,
        [feature]: newValue
      }
    }));
    
    // Apply the toggle immediately
    toggleAccessibilityFeature(feature as any);
  };

  const handleResetToAssessment = () => {
    setPreferences(prev => ({
      ...prev,
      manualOverride: false
    }));
    
    if (preferences.assessmentResults) {
      applyPreset(preferences.assessmentResults.recommendedPreset);
      showNotification('Reset to assessment-recommended preset', 'success');
    } else {
      resetToDefault();
      showNotification('Reset to default preset', 'success');
    }
  };

  const retakeAssessment = () => {
    window.location.href = '/student/assessment';
  };

  return (
    <div className="settings-container">
      <header className="settings-header">
        <h1>Settings & Preferences</h1>
        <p>Customize your ThinkAble learning experience</p>
      </header>

      <div className="settings-content">
        {/* Current Status Card */}
        <div className="settings-card adaptive-card">
          <h2>Current Adaptive Interface</h2>
          <div className="current-preset-info">
            <div className="preset-display">
              <h3>{adaptiveUIPresets[currentPreset]?.name || 'Standard'}</h3>
              <p>{adaptiveUIPresets[currentPreset]?.description || 'Standard adaptive interface'}</p>
              
              {preferences.manualOverride && (
                <div className="override-badge">
                  <i className="fas fa-user-edit"></i>
                  Manually Selected
                </div>
              )}
              
              {preferences.assessmentResults && !preferences.manualOverride && (
                <div className="assessment-badge">
                  <i className="fas fa-brain"></i>
                  Assessment Recommended
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Interface Preset Selection */}
        <div className="settings-card adaptive-card">
          <h2>Interface Preset Selection</h2>
          <p>Choose the interface style that works best for you:</p>
          
          <div className="preset-grid">
            {availablePresets.map(presetName => {
              const preset = adaptiveUIPresets[presetName];
              return (
                <div 
                  key={presetName}
                  className={`preset-option ${currentPreset === presetName ? 'active' : ''}`}
                  onClick={() => handlePresetChange(presetName)}
                >
                  <div className="preset-header">
                    <h4>{preset.name}</h4>
                    {currentPreset === presetName && (
                      <i className="fas fa-check-circle active-icon"></i>
                    )}
                  </div>
                  <p>{preset.description}</p>
                  <div className="preset-details">
                    <small>Font: {preset.fontFamily.split(',')[0]}</small>
                    <small>Size: {preset.fontSize}</small>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Individual Accessibility Features */}
        <div className="settings-card adaptive-card">
          <h2>Individual Accessibility Features</h2>
          <p>Toggle specific accessibility features independently:</p>
          
          <div className="feature-toggles">
            <div className="feature-toggle">
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.customSettings.highContrast}
                  onChange={() => handleToggleFeature('highContrast')}
                />
                <span className="slider"></span>
              </label>
              <div className="feature-info">
                <h4>High Contrast</h4>
                <p>Enhanced contrast for better visibility</p>
              </div>
            </div>

            <div className="feature-toggle">
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.customSettings.dyslexiaFriendly}
                  onChange={() => handleToggleFeature('dyslexiaFriendly')}
                />
                <span className="slider"></span>
              </label>
              <div className="feature-info">
                <h4>Dyslexia-Friendly</h4>
                <p>Enhanced fonts and spacing for reading support</p>
              </div>
            </div>

            <div className="feature-toggle">
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.customSettings.adhdFriendly}
                  onChange={() => handleToggleFeature('adhdFriendly')}
                />
                <span className="slider"></span>
              </label>
              <div className="feature-info">
                <h4>ADHD-Friendly</h4>
                <p>Reduced distractions and enhanced focus</p>
              </div>
            </div>

            <div className="feature-toggle">
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.customSettings.autismFriendly}
                  onChange={() => handleToggleFeature('autismFriendly')}
                />
                <span className="slider"></span>
              </label>
              <div className="feature-info">
                <h4>Autism-Friendly</h4>
                <p>Clear navigation and predictable interface</p>
              </div>
            </div>

            <div className="feature-toggle">
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.customSettings.sensoryFriendly}
                  onChange={() => handleToggleFeature('sensoryFriendly')}
                />
                <span className="slider"></span>
              </label>
              <div className="feature-info">
                <h4>Sensory-Friendly</h4>
                <p>Reduced sensory overload with soft colors</p>
              </div>
            </div>
          </div>
        </div>

        {/* Assessment Information */}
        {preferences.assessmentResults && (
          <div className="settings-card adaptive-card">
            <h2>Assessment Results</h2>
            <p>Based on your latest assessment:</p>
            
            <div className="assessment-scores">
              <div className="score-item">
                <span>Attention Support:</span>
                <div className="score-bar">
                  <div 
                    className="score-fill" 
                    style={{ width: `${(preferences.assessmentResults.attentionScore / 10) * 100}%` }}
                  ></div>
                </div>
                <span>{preferences.assessmentResults.attentionScore}/10</span>
              </div>
              
              <div className="score-item">
                <span>Reading Support:</span>
                <div className="score-bar">
                  <div 
                    className="score-fill" 
                    style={{ width: `${(preferences.assessmentResults.readingDifficultyScore / 10) * 100}%` }}
                  ></div>
                </div>
                <span>{preferences.assessmentResults.readingDifficultyScore}/10</span>
              </div>
              
              <div className="score-item">
                <span>Social Communication:</span>
                <div className="score-bar">
                  <div 
                    className="score-fill" 
                    style={{ width: `${(preferences.assessmentResults.socialCommunicationScore / 10) * 100}%` }}
                  ></div>
                </div>
                <span>{preferences.assessmentResults.socialCommunicationScore}/10</span>
              </div>
              
              <div className="score-item">
                <span>Sensory Processing:</span>
                <div className="score-bar">
                  <div 
                    className="score-fill" 
                    style={{ width: `${(preferences.assessmentResults.sensoryProcessingScore / 10) * 100}%` }}
                  ></div>
                </div>
                <span>{preferences.assessmentResults.sensoryProcessingScore}/10</span>
              </div>
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="settings-actions">
          <div className="action-group">
            <button 
              className="adaptive-button primary" 
              onClick={saveUserPreferences}
              disabled={isSaving}
            >
              {isSaving ? (
                <>
                  <i className="fas fa-spinner fa-spin"></i>
                  Saving...
                </>
              ) : (
                <>
                  <i className="fas fa-save"></i>
                  Save Settings
                </>
              )}
            </button>
            
            <button 
              className="adaptive-button secondary" 
              onClick={handleResetToAssessment}
            >
              <i className="fas fa-undo"></i>
              Reset to Assessment
            </button>
          </div>
          
          <div className="action-group">
            <button 
              className="adaptive-button" 
              onClick={retakeAssessment}
            >
              <i className="fas fa-brain"></i>
              Retake Assessment
            </button>
            
            <button 
              className="adaptive-button" 
              onClick={() => setShowAdvanced(!showAdvanced)}
            >
              <i className="fas fa-cog"></i>
              {showAdvanced ? 'Hide' : 'Show'} Advanced
            </button>
            
            <button 
              className="adaptive-button" 
              onClick={() => setShowHistory(!showHistory)}
            >
              <i className="fas fa-chart-line"></i>
              {showHistory ? 'Hide' : 'Show'} Usage History
            </button>
          </div>
        </div>

        {/* Usage History & Analytics */}
        {showHistory && presetHistory && (
          <div className="settings-card adaptive-card">
            <h2>Usage History & Analytics</h2>
            
            {/* Usage Overview */}
            <div className="usage-overview">
              <div className="stats-grid">
                <div className="stat-item">
                  <h3>{presetHistory.totalSessions}</h3>
                  <p>Total Sessions</p>
                </div>
                <div className="stat-item">
                  <h3>{presetHistory.averageSessionLength}</h3>
                  <p>Average Session</p>
                </div>
                <div className="stat-item">
                  <h3>{adaptiveUIPresets[presetHistory.recommendations.mostUsed]?.name}</h3>
                  <p>Most Used Preset</p>
                </div>
                <div className="stat-item">
                  <h3>{presetHistory.recentChanges.length}</h3>
                  <p>Recent Changes</p>
                </div>
              </div>
            </div>

            {/* Usage Statistics Chart */}
            <div className="usage-stats">
              <h3>Preset Usage Statistics</h3>
              <div className="usage-bars">
                {Object.entries(presetHistory.usageStats)
                  .sort(([,a], [,b]) => b - a)
                  .map(([presetName, minutes]) => (
                    <div key={presetName} className="usage-bar-item">
                      <span className="preset-name">
                        {adaptiveUIPresets[presetName]?.name || presetName}
                      </span>
                      <div className="usage-bar">
                        <div 
                          className="usage-fill" 
                          style={{ 
                            width: `${(minutes / Math.max(...Object.values(presetHistory.usageStats))) * 100}%` 
                          }}
                        ></div>
                      </div>
                      <span className="usage-time">{minutes}min</span>
                    </div>
                  ))}
              </div>
            </div>

            {/* Recent Changes */}
            <div className="recent-changes">
              <h3>Recent Preset Changes</h3>
              <div className="changes-list">
                {presetHistory.recentChanges.map((change, index) => (
                  <div key={index} className="change-item">
                    <div className="change-info">
                      <h4>{adaptiveUIPresets[change.presetName]?.name}</h4>
                      <p>{change.changeReason}</p>
                      <small>{new Date(change.timestamp).toLocaleDateString()} • {change.duration}</small>
                    </div>
                    <div className="change-icon">
                      <i className={change.changeReason.includes('Manual') ? 'fas fa-user-edit' : 'fas fa-brain'}></i>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Recommendations */}
            <div className="recommendations">
              <h3>Personalized Recommendations</h3>
              <div className="recommendation-card">
                <div className="recommendation-header">
                  <i className="fas fa-lightbulb"></i>
                  <h4>Suggested: {adaptiveUIPresets[presetHistory.recommendations.suggested]?.name}</h4>
                </div>
                <p>{presetHistory.recommendations.reason}</p>
                <button 
                  className="adaptive-button"
                  onClick={() => handlePresetChange(presetHistory.recommendations.suggested)}
                >
                  <i className="fas fa-magic"></i>
                  Try Suggested Preset
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Advanced Settings */}
        {showAdvanced && (
          <div className="settings-card adaptive-card advanced-settings">
            <h2>Advanced Settings</h2>
            <div className="advanced-options">
              <div className="option-group">
                <h4>Reset Options</h4>
                <button 
                  className="adaptive-button danger" 
                  onClick={() => {
                    if (window.confirm('This will reset all your settings to default. Are you sure?')) {
                      resetToDefault();
                      localStorage.removeItem('thinkable-user-preferences');
                      showNotification('All settings reset to default', 'success');
                    }
                  }}
                >
                  <i className="fas fa-trash"></i>
                  Reset All Settings
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Settings;