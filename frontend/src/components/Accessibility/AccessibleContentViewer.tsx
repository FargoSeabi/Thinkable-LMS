import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useTextToSpeech } from '../../contexts/TextToSpeechContext';
import ContentAccessibilityToolbar from './ContentAccessibilityToolbar';
import TextModeOverlay from './TextModeOverlay';
import config from '../../services/config';
import './AccessibleContentViewer.css';

interface AccessibleContentViewerProps {
  content: any;
  contentType: string;
  children: React.ReactNode;
  className?: string;
}

const AccessibleContentViewer: React.FC<AccessibleContentViewerProps> = ({
  content,
  contentType,
  children,
  className = ''
}) => {
  const { user } = useAuth();
  const { setCurrentContent } = useTextToSpeech();
  const [isTextModeVisible, setIsTextModeVisible] = useState(false);
  const [fontSize, setFontSize] = useState(16);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [viewerSettings, setViewerSettings] = useState({
    highContrast: false,
    reduceMotion: false,
    enhancedFocus: false,
    wordHighlight: true
  });

  // Set content for accessibility services
  useEffect(() => {
    if (content) {
      setCurrentContent(content);
    }
  }, [content, setCurrentContent]);

  // Load saved settings
  useEffect(() => {
    const savedSettings = localStorage.getItem('accessibleViewerSettings');
    if (savedSettings) {
      try {
        const parsed = JSON.parse(savedSettings);
        setViewerSettings(prev => ({ ...prev, ...parsed }));
        if (parsed.fontSize) setFontSize(parsed.fontSize);
      } catch (error) {
        console.warn('Failed to load viewer settings:', error);
      }
    }
  }, []);

  // Save settings when they change
  useEffect(() => {
    const settingsToSave = { ...viewerSettings, fontSize };
    localStorage.setItem('accessibleViewerSettings', JSON.stringify(settingsToSave));
  }, [viewerSettings, fontSize]);

  // Apply user's accessibility preset on mount
  useEffect(() => {
    if (user?.recommendedPreset) {
      applyPresetOptimizations(user.recommendedPreset);
    }
  }, [user?.recommendedPreset]);

  const applyPresetOptimizations = useCallback((preset: string) => {
    switch (preset) {
      case 'DYSLEXIA_SUPPORT':
        setFontSize(19);
        setViewerSettings(prev => ({
          ...prev,
          wordHighlight: true,
          enhancedFocus: true
        }));
        break;
      case 'ADHD_SUPPORT':
        setFontSize(20);
        setViewerSettings(prev => ({
          ...prev,
          enhancedFocus: true,
          reduceMotion: false
        }));
        break;
      case 'AUTISM_SUPPORT':
        setViewerSettings(prev => ({
          ...prev,
          reduceMotion: true,
          enhancedFocus: false
        }));
        break;
      case 'SENSORY_CALM':
        setViewerSettings(prev => ({
          ...prev,
          highContrast: false,
          reduceMotion: true,
          enhancedFocus: false
        }));
        break;
      case 'READING_SUPPORT':
        setFontSize(18);
        setViewerSettings(prev => ({
          ...prev,
          wordHighlight: true,
          enhancedFocus: true
        }));
        break;
    }
  }, []);

  const handleTextModeToggle = useCallback(() => {
    setIsTextModeVisible(!isTextModeVisible);
  }, [isTextModeVisible]);

  const handleFontSizeChange = useCallback((newSize: number) => {
    setFontSize(newSize);
  }, []);

  const toggleFullscreen = useCallback(() => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen()
        .then(() => setIsFullscreen(true))
        .catch(err => console.warn('Fullscreen not supported:', err));
    } else {
      document.exitFullscreen()
        .then(() => setIsFullscreen(false))
        .catch(err => console.warn('Exit fullscreen failed:', err));
    }
  }, []);

  const updateViewerSetting = useCallback((setting: string, value: boolean) => {
    setViewerSettings(prev => ({
      ...prev,
      [setting]: value
    }));
  }, []);

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      // Don't trigger if user is typing in an input
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) {
        return;
      }

      switch (e.key.toLowerCase()) {
        case 't':
          if (!e.ctrlKey && !e.altKey) {
            e.preventDefault();
            handleTextModeToggle();
          }
          break;
        case 'f11':
          e.preventDefault();
          toggleFullscreen();
          break;
        case 'escape':
          if (isTextModeVisible) {
            setIsTextModeVisible(false);
          }
          break;
        case '=':
        case '+':
          if (e.ctrlKey) {
            e.preventDefault();
            handleFontSizeChange(fontSize + 2);
          }
          break;
        case '-':
          if (e.ctrlKey) {
            e.preventDefault();
            handleFontSizeChange(fontSize - 2);
          }
          break;
        case '0':
          if (e.ctrlKey) {
            e.preventDefault();
            handleFontSizeChange(16);
          }
          break;
      }
    };

    document.addEventListener('keydown', handleKeyPress);
    return () => document.removeEventListener('keydown', handleKeyPress);
  }, [handleTextModeToggle, toggleFullscreen, isTextModeVisible, fontSize, handleFontSizeChange]);

  // Generate accessibility classes
  const getAccessibilityClasses = () => {
    const classes = ['accessible-content-viewer'];
    
    // Add preset class
    if (user?.recommendedPreset) {
      classes.push(`preset-${user.recommendedPreset.toLowerCase().replace('_', '-')}`);
    }
    
    // Add setting classes
    if (viewerSettings.highContrast) classes.push('high-contrast');
    if (viewerSettings.reduceMotion) classes.push('reduce-motion');
    if (viewerSettings.enhancedFocus) classes.push('enhanced-focus');
    if (viewerSettings.wordHighlight) classes.push('word-highlight');
    if (isFullscreen) classes.push('fullscreen');
    
    return classes.join(' ');
  };

  // Generate CSS custom properties for font size
  const getCustomProperties = () => {
    const baseSize = fontSize;
    return {
      '--accessible-font-size': `${baseSize}px`,
      '--accessible-font-size-small': `${Math.max(12, baseSize - 2)}px`,
      '--accessible-font-size-large': `${Math.min(32, baseSize + 4)}px`,
      '--accessible-font-size-h1': `${Math.min(48, baseSize * 2.5)}px`,
      '--accessible-font-size-h2': `${Math.min(36, baseSize * 2)}px`,
      '--accessible-font-size-h3': `${Math.min(28, baseSize * 1.5)}px`,
    } as React.CSSProperties;
  };

  // Record accessibility usage
  const recordAccessibilityUsage = useCallback((action: string, details?: any) => {
    try {
      fetch(`${config.apiBaseUrl}/api/accessibility/usage`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          action,
          details,
          contentId: content?.id,
          contentType,
          userPreset: user?.recommendedPreset,
          timestamp: new Date().toISOString()
        })
      }).catch(error => console.warn('Failed to record accessibility usage:', error));
    } catch (error) {
      console.warn('Failed to record accessibility usage:', error);
    }
  }, [content?.id, contentType, user?.recommendedPreset]);

  // Record usage when viewer mounts
  useEffect(() => {
    recordAccessibilityUsage('viewer_opened', {
      preset: user?.recommendedPreset,
      settings: viewerSettings
    });
  }, []);

  return (
    <div 
      className={`${getAccessibilityClasses()} ${className}`}
      style={getCustomProperties()}
    >
      {/* Accessibility Toolbar */}
      <ContentAccessibilityToolbar
        content={content}
        contentType={contentType}
        onTextModeToggle={handleTextModeToggle}
        isTextModeActive={isTextModeVisible}
        onFontSizeChange={handleFontSizeChange}
        currentFontSize={fontSize}
      />

      {/* Main Content Area */}
      <div className="content-area">
        {/* Focus skip link for screen readers */}
        {/*<a href="#main-content" className="skip-link">*/}
        {/*  Skip to main content*/}
        {/*</a>*/}

        {/* Content wrapper with accessibility enhancements */}
        <div 
          id="main-content"
          className="main-content-wrapper"
          role="main"
          aria-label={`${contentType} content: ${content?.title || 'Untitled'}`}
          tabIndex={-1}
        >
          {/* Render the actual content */}
          {children}
        </div>

        {/* Accessibility status for screen readers */}
        <div className="sr-only" aria-live="polite" aria-atomic="true">
          {isTextModeVisible && 'Text mode activated. Content is now displayed in accessible text format.'}
        </div>
      </div>

      {/* Text Mode Overlay */}
      <TextModeOverlay
        isVisible={isTextModeVisible}
        onClose={() => setIsTextModeVisible(false)}
        content={content}
        contentType={contentType}
      />

      {/* Accessibility settings panel (if needed) */}
      {user?.recommendedPreset && (
        <div className="accessibility-status">
          <div className="status-indicator">
            <i className="fas fa-universal-access"></i>
            <span>
              Accessibility optimized for {user.recommendedPreset.replace('_', ' ').toLowerCase()}
            </span>
          </div>
        </div>
      )}

      {/* Debug info (development only) */}
      {process.env.NODE_ENV === 'development' && (
        <div className="accessibility-debug">
          <details>
            <summary>Accessibility Debug Info</summary>
            <pre>
              {JSON.stringify({
                preset: user?.recommendedPreset,
                fontSize,
                settings: viewerSettings,
                contentType,
                isTextModeVisible
              }, null, 2)}
            </pre>
          </details>
        </div>
      )}
    </div>
  );
};

export default AccessibleContentViewer;