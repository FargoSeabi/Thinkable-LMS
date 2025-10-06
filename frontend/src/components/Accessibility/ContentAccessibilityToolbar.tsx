import React, { useState, useCallback } from 'react';
import { useTextToSpeech } from '../../contexts/TextToSpeechContext';
import { useAuth } from '../../contexts/AuthContext';
import './ContentAccessibilityToolbar.css';

interface ContentAccessibilityToolbarProps {
  content: any;
  contentType: string;
  onTextModeToggle: () => void;
  isTextModeActive: boolean;
  onFontSizeChange?: (size: number) => void;
  currentFontSize?: number;
}

const ContentAccessibilityToolbar: React.FC<ContentAccessibilityToolbarProps> = ({
  content,
  contentType,
  onTextModeToggle,
  isTextModeActive,
  onFontSizeChange,
  currentFontSize = 16
}) => {
  const { user } = useAuth();
  const { 
    speak, 
    pause, 
    resume, 
    stop, 
    state: ttsState, 
    settings: ttsSettings,
    setRate,
    setVolume,
    speakCurrentContent,
    setCurrentContent
  } = useTextToSpeech();

  const [showTTSControls, setShowTTSControls] = useState(false);
  const [showAccessibilityMenu, setShowAccessibilityMenu] = useState(false);

  // Set current content for TTS
  React.useEffect(() => {
    if (content) {
      setCurrentContent(content);
    }
  }, [content, setCurrentContent]);

  const handleTTSToggle = useCallback(async () => {
    if (ttsState.isPlaying) {
      if (ttsState.isPaused) {
        resume();
      } else {
        pause();
      }
    } else {
      await speakCurrentContent();
    }
  }, [ttsState.isPlaying, ttsState.isPaused, pause, resume, speakCurrentContent]);

  const handleTTSStop = useCallback(() => {
    stop();
  }, [stop]);

  const handleFontSizeAdjust = useCallback((delta: number) => {
    if (onFontSizeChange) {
      const newSize = Math.max(12, Math.min(32, currentFontSize + delta));
      onFontSizeChange(newSize);
    }
  }, [currentFontSize, onFontSizeChange]);

  const getContentTypeIcon = () => {
    switch (contentType.toLowerCase()) {
      case 'pdf':
      case 'document': return 'fa-file-pdf';
      case 'video': return 'fa-video';
      case 'audio': return 'fa-volume-up';
      case 'image': return 'fa-image';
      default: return 'fa-file';
    }
  };

  const getPresetName = () => {
    const preset = user?.recommendedPreset || 'STANDARD';
    return preset.replace('_', ' ').toLowerCase();
  };

  return (
    <div className={`accessibility-toolbar ${user?.recommendedPreset?.toLowerCase() || 'standard'}-preset`}>
      {/* Main toolbar */}
      <div className="toolbar-main">
        {/* Content info */}
        <div className="content-info">
          <i className={`fas ${getContentTypeIcon()}`}></i>
          <span className="content-title">{content?.title || 'Content'}</span>
          {user?.recommendedPreset && (
            <span className="preset-badge">
              <i className="fas fa-universal-access"></i>
              {getPresetName()}
            </span>
          )}
        </div>

        {/* Quick actions */}
        <div className="quick-actions">
          {/* Text-to-Speech */}
          <div className="tts-controls">
            <button
              className={`toolbar-btn primary ${ttsState.isPlaying ? 'playing' : ''}`}
              onClick={handleTTSToggle}
              title={ttsState.isPlaying 
                ? (ttsState.isPaused ? 'Resume reading' : 'Pause reading') 
                : 'Read aloud'}
              disabled={!ttsState.isAvailable}
            >
              <i className={`fas ${
                ttsState.isPlaying 
                  ? (ttsState.isPaused ? 'fa-play' : 'fa-pause') 
                  : 'fa-play'
              }`}></i>
              {ttsState.isPlaying && !ttsState.isPaused ? 'Reading...' : 'Read Aloud'}
            </button>

            {ttsState.isPlaying && (
              <button
                className="toolbar-btn stop-btn"
                onClick={handleTTSStop}
                title="Stop reading"
              >
                <i className="fas fa-stop"></i>
              </button>
            )}

            <button
              className={`toolbar-btn settings-btn ${showTTSControls ? 'active' : ''}`}
              onClick={() => setShowTTSControls(!showTTSControls)}
              title="TTS settings"
            >
              <i className="fas fa-cog"></i>
            </button>
          </div>

          {/* Text Mode */}
          <button
            className={`toolbar-btn text-mode-btn ${isTextModeActive ? 'active' : ''}`}
            onClick={onTextModeToggle}
            title={isTextModeActive ? 'Exit text mode' : 'Enter text mode - Extract and format text with your accessibility settings'}
          >
            <i className={`fas ${isTextModeActive ? 'fa-eye' : 'fa-font'}`}></i>
            {isTextModeActive ? 'View Mode' : 'Text Mode'}
          </button>

          {/* Font Size Controls */}
          {onFontSizeChange && (
            <div className="font-size-controls">
              <button
                className="toolbar-btn"
                onClick={() => handleFontSizeAdjust(-2)}
                title="Decrease font size"
              >
                <i className="fas fa-search-minus"></i>
              </button>
              <span className="font-size-display">{currentFontSize}px</span>
              <button
                className="toolbar-btn"
                onClick={() => handleFontSizeAdjust(2)}
                title="Increase font size"
              >
                <i className="fas fa-search-plus"></i>
              </button>
            </div>
          )}

          {/* Accessibility Menu */}
          <button
            className={`toolbar-btn accessibility-btn ${showAccessibilityMenu ? 'active' : ''}`}
            onClick={() => setShowAccessibilityMenu(!showAccessibilityMenu)}
            title="Accessibility options"
          >
            <i className="fas fa-universal-access"></i>
            <i className="fas fa-chevron-down dropdown-icon"></i>
          </button>
        </div>
      </div>

      {/* TTS Advanced Controls */}
      {showTTSControls && (
        <div className="toolbar-expanded tts-expanded">
          <div className="control-group">
            <label>Reading Speed</label>
            <input
              type="range"
              min="0.5"
              max="2"
              step="0.1"
              value={ttsSettings.rate}
              onChange={(e) => setRate(parseFloat(e.target.value))}
              className="range-slider"
            />
            <span className="value-display">{ttsSettings.rate}x</span>
          </div>

          <div className="control-group">
            <label>Volume</label>
            <input
              type="range"
              min="0.1"
              max="1"
              step="0.1"
              value={ttsSettings.volume}
              onChange={(e) => setVolume(parseFloat(e.target.value))}
              className="range-slider"
            />
            <span className="value-display">{Math.round(ttsSettings.volume * 100)}%</span>
          </div>

          {/* Progress bar if reading */}
          {ttsState.isPlaying && ttsState.totalLength > 0 && (
            <div className="progress-group">
              <label>Reading Progress</label>
              <div className="progress-bar">
                <div 
                  className="progress-fill"
                  style={{ width: `${(ttsState.currentPosition / ttsState.totalLength) * 100}%` }}
                />
              </div>
              <span className="progress-text">
                {Math.round((ttsState.currentPosition / ttsState.totalLength) * 100)}%
              </span>
            </div>
          )}
        </div>
      )}

      {/* Accessibility Menu */}
      {showAccessibilityMenu && (
        <div className="toolbar-expanded accessibility-expanded">
          <div className="accessibility-grid">
            {/* Current preset info */}
            <div className="preset-info-card">
              <h4>
                <i className="fas fa-user-cog"></i>
                Current Accessibility Settings
              </h4>
              <p>Your interface is optimized for <strong>{getPresetName()}</strong> support.</p>
              {user?.recommendedPreset && (
                <div className="preset-features">
                  {getPresetFeatures(user.recommendedPreset).map((feature, index) => (
                    <span key={index} className="feature-tag">
                      <i className="fas fa-check"></i>
                      {feature}
                    </span>
                  ))}
                </div>
              )}
            </div>

            {/* Quick accessibility toggles */}
            <div className="quick-toggles">
              <h4>
                <i className="fas fa-toggle-on"></i>
                Quick Adjustments
              </h4>
              <div className="toggle-grid">
                <button className="toggle-btn" title="High contrast mode">
                  <i className="fas fa-adjust"></i>
                  High Contrast
                </button>
                <button className="toggle-btn" title="Reduce animations">
                  <i className="fas fa-pause-circle"></i>
                  Reduce Motion
                </button>
                <button className="toggle-btn" title="Focus indicators">
                  <i className="fas fa-crosshairs"></i>
                  Enhanced Focus
                </button>
                <button className="toggle-btn" title="Word highlighting">
                  <i className="fas fa-highlighter"></i>
                  Word Highlight
                </button>
              </div>
            </div>

            {/* Content-specific help */}
            <div className="content-help">
              <h4>
                <i className="fas fa-question-circle"></i>
                How to Use This Content
              </h4>
              {getContentHelp(contentType).map((tip, index) => (
                <div key={index} className="help-tip">
                  <i className="fas fa-lightbulb"></i>
                  {tip}
                </div>
              ))}
            </div>
          </div>

          {/* Settings link */}
          <div className="toolbar-footer">
            <a href="/student/settings" className="settings-link">
              <i className="fas fa-cog"></i>
              Customize All Accessibility Settings
            </a>
          </div>
        </div>
      )}

      {/* Keyboard shortcuts hint */}
      <div className="keyboard-shortcuts">
        <span><kbd>Space</kbd> Read/Pause</span>
        <span><kbd>T</kbd> Text Mode</span>
        <span><kbd>Esc</kbd> Close</span>
      </div>
    </div>
  );
};

// Helper functions
function getPresetFeatures(preset: string): string[] {
  switch (preset) {
    case 'DYSLEXIA_SUPPORT':
      return ['Dyslexia-friendly fonts', 'Enhanced spacing', 'Clear structure', 'Word highlighting'];
    case 'ADHD_SUPPORT':
      return ['Focused layout', 'Clear sections', 'Enhanced focus', 'Reduced distractions'];
    case 'AUTISM_SUPPORT':
      return ['Consistent interface', 'Predictable layout', 'Calm colors', 'Clear navigation'];
    case 'SENSORY_CALM':
      return ['Soft colors', 'Reduced stimulation', 'Gentle animations', 'Comfortable viewing'];
    case 'READING_SUPPORT':
      return ['Optimized text', 'Enhanced readability', 'Clear formatting', 'Reading aids'];
    default:
      return ['Standard accessibility', 'Balanced design', 'Clear interface'];
  }
}

function getContentHelp(contentType: string): string[] {
  switch (contentType.toLowerCase()) {
    case 'pdf':
    case 'document':
      return [
        'Use "Text Mode" to extract and format text with your accessibility settings',
        'Use "Read Aloud" to hear the content spoken',
        'Adjust font size using the zoom controls'
      ];
    case 'video':
      return [
        'Turn on captions if available',
        'Use "Text Mode" to see transcript if available',
        'Adjust playback speed if needed'
      ];
    case 'audio':
      return [
        'Use playback controls to pause and replay sections',
        'Check if transcript is available in "Text Mode"',
        'Adjust volume to comfortable level'
      ];
    case 'image':
      return [
        'Use "Text Mode" to see image descriptions',
        'Look for alternative text descriptions',
        'Zoom in to see details better'
      ];
    default:
      return [
        'Use accessibility tools to customize your experience',
        'Try different viewing modes to find what works best',
        'Contact support if you need additional assistance'
      ];
  }
}

export default ContentAccessibilityToolbar;