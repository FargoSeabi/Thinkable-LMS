import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import AccessibleContentViewer from '../Accessibility/AccessibleContentViewer';
import TextModeOverlay from '../Accessibility/TextModeOverlay';
import ContentMessaging from './ContentMessaging';
import FloatingNotesPanel from '../Common/FloatingNotesPanel';
import H5PContentViewer from './H5PContentViewer';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import { useTextToSpeech } from '../../contexts/TextToSpeechContext';
import { useFocusMode } from '../../contexts/FocusModeContext';
import config from '../../services/config';
import './ContentViewer.css';

interface Content {
  id: number;
  title: string;
  description: string;
  contentType: string;
  fileName: string;
  fileUrl?: string;
  subjectArea: string;
  difficultyLevel: string;
  estimatedDurationMinutes: number;
  dyslexiaFriendly: boolean;
  adhdFriendly: boolean;
  autismFriendly: boolean;
  visualImpairmentFriendly: boolean;
  hearingImpairmentFriendly: boolean;
  motorImpairmentFriendly: boolean;
  tutorName?: string;
  ratingAverage: number;
  viewCount: number;
  // H5P specific fields
  h5pContentId?: string;
  h5pLibrary?: string;
  h5pMetadata?: string;
  h5pSettings?: string;
}

const ContentViewer: React.FC = () => {
  const { contentId } = useParams<{ contentId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const { speak, stop, state: ttsState } = useTextToSpeech();
  const { isFocusMode, toggleFocusMode } = useFocusMode();
  const [content, setContent] = useState<Content | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showInfo, setShowInfo] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);
  const [showMessaging, setShowMessaging] = useState(false);
  const [showAccessibilityMenu, setShowAccessibilityMenu] = useState(false);
  const [showNotesPanel, setShowNotesPanel] = useState(false);
  const [fontSize, setFontSize] = useState(16);

  // Load saved font size on component mount
  useEffect(() => {
    const savedFontSize = localStorage.getItem('content-font-size');
    if (savedFontSize) {
      const size = parseInt(savedFontSize, 10);
      setFontSize(size);
      document.documentElement.style.setProperty('--content-font-size', `${size}px`);
    }
  }, []);
  const [isTextModeVisible, setIsTextModeVisible] = useState(false);

  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    if (!contentId) {
      setError('No content ID provided');
      setLoading(false);
      return;
    }

    fetchContent();
  }, [contentId]);

  // Click outside handling (removed keyboard shortcuts that interfere with typing)
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Element;
      if (showAccessibilityMenu && !target.closest('.accessibility-dropdown')) {
        setShowAccessibilityMenu(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showAccessibilityMenu]);

  const getContentIcon = (fileName: string, contentType?: string) => {
    const ext = fileName.toLowerCase();
    if (contentType === 'interactive' || ext.endsWith('.h5p')) return 'fas fa-puzzle-piece';
    if (ext.endsWith('.mp4') || ext.endsWith('.webm')) return 'fas fa-play-circle';
    if (ext.endsWith('.pdf')) return 'fas fa-file-pdf';
    if (ext.endsWith('.mp3') || ext.endsWith('.wav')) return 'fas fa-music';
    if (ext.match(/\.(jpg|jpeg|png|gif|svg|webp)$/)) return 'fas fa-image';
    if (ext.match(/\.(txt|doc|docx)$/)) return 'fas fa-file-text';
    return 'fas fa-file';
  };

  const getSubjectIcon = (subject: string) => {
    const s = subject.toLowerCase();
    if (s.includes('math')) return 'fas fa-calculator';
    if (s.includes('science')) return 'fas fa-flask';
    if (s.includes('history')) return 'fas fa-landmark';
    if (s.includes('english') || s.includes('language')) return 'fas fa-book-open';
    if (s.includes('art')) return 'fas fa-palette';
    if (s.includes('music')) return 'fas fa-music';
    return 'fas fa-graduation-cap';
  };

  const getDifficultyColor = (level: string) => {
    const l = level.toLowerCase();
    if (l.includes('easy') || l.includes('beginner')) return '#28a745';
    if (l.includes('medium') || l.includes('intermediate')) return '#fd7e14';
    if (l.includes('hard') || l.includes('advanced')) return '#dc3545';
    return '#6c757d';
  };

  const fetchContent = async () => {
    try {
      setLoading(true);
      
      // First get the content details
      const response = await fetch(`${API_BASE_URL}/api/student/content/${contentId}`);
      if (!response.ok) {
        throw new Error('Failed to fetch content details');
      }
      
      const contentData = await response.json();
      
      // Set the file URL for viewing
      const contentWithUrl = {
        ...contentData,
        fileUrl: `${API_BASE_URL}/api/tutor/content/${contentId}/view`
      };
      
      setContent(contentWithUrl);
      setError(null);
      
    } catch (err) {
      console.error('Error fetching content:', err);
      setError('Failed to load content');
      showNotification('Failed to load content', 'error');
    } finally {
      setLoading(false);
    }
  };

  const recordContentInteraction = async (interactionType: string, data: any = {}) => {
    if (!user?.id || !contentId) return;

    try {
      const interactionData = {
        interactionType,
        interactionData: JSON.stringify({
          timestamp: new Date().toISOString(),
          contentType: content?.contentType,
          ...data
        }),
        duration: data.duration_seconds || 0
      };

      const response = await fetch(`${API_BASE_URL}/api/student/content/${contentId}/interact?studentId=${user.id}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(interactionData)
      });

      if (!response.ok) {
        console.warn('Failed to record content interaction:', response.statusText);
      }
    } catch (error) {
      console.warn('Error recording content interaction:', error);
    }
  };

  const handleReadAloud = () => {
    if (ttsState.isPlaying) {
      stop();
    } else if (content) {
      speak(content.title + ". " + (content.description || "Learning content ready."));
    }
  };

  const handleTextMode = () => {
    setIsTextModeVisible(!isTextModeVisible);
  };

  const handleFontSizeChange = (delta: number) => {
    const newSize = Math.max(12, Math.min(24, fontSize + delta));
    setFontSize(newSize);
    document.documentElement.style.setProperty('--content-font-size', `${newSize}px`);
    localStorage.setItem('content-font-size', newSize.toString());
    
    // Apply to text-based content areas that can actually be resized
    const textElements = document.querySelectorAll(
      '.extracted-content, .text-mode-body, .full-content, .lesson-description, ' +
      '.fallback-content p, .pdf-fallback p, .doc-fallback p, .audio-help, ' +
      '.content-title, .fallback-content h3, .error-card h2, .error-card p, ' +
      '.kid-loading h2, .kid-loading p, .kid-error h2, .kid-error p'
    );
    
    textElements.forEach(element => {
      (element as HTMLElement).style.fontSize = `${newSize}px`;
    });
    
    // Apply to headings with relative scaling
    const headings = document.querySelectorAll('.fallback-content h3, .error-card h2, .kid-loading h2, .kid-error h2');
    headings.forEach(element => {
      (element as HTMLElement).style.fontSize = `${Math.min(32, newSize * 1.5)}px`;
    });
    
    // Show notification to user about what changed
    const applicableContent = textElements.length > 0 ? 'text content' : 'text mode (when available)';
    showNotification(`Font size changed to ${newSize}px. This affects ${applicableContent}.`, 'info');
  };

  const renderContentDisplay = () => {
    if (!content) return null;

    const fileExtension = content.fileName.toLowerCase();
    const fileUrl = content.fileUrl!;

    // H5P Interactive Content - Handle first
    if (content.contentType === 'interactive' || fileExtension.endsWith('.h5p')) {
      return (
        <H5PContentViewer 
          content={content}
          onInteraction={recordContentInteraction}
        />
      );
    }

    // Video Content - Clean and simple
    if (fileExtension.endsWith('.mp4') || fileExtension.endsWith('.webm') || fileExtension.endsWith('.ogg')) {
      return (
        <div className="clean-video-viewer">
          <video
            controls
            className="clean-video"
            onPlay={() => setIsPlaying(true)}
            onPause={() => setIsPlaying(false)}
            onEnded={() => setIsPlaying(false)}
          >
            <source src={fileUrl} type={`video/${fileExtension.split('.').pop()}`} />
            <div className="video-fallback">
              <p>Can't play video? <a href={fileUrl} download>Download it here</a></p>
            </div>
          </video>
        </div>
      );
    }

    // PDF Content - Clean and simple
    if (fileExtension.endsWith('.pdf')) {
      return (
        <div className="clean-pdf-viewer">
          <iframe
            src={fileUrl}
            title={content.title}
            className="clean-pdf"
          >
            <div className="pdf-fallback">
              <p>Can't view PDF? <a href={fileUrl} download>Download it here</a></p>
            </div>
          </iframe>
        </div>
      );
    }

    // Audio Content
    if (fileExtension.endsWith('.mp3') || fileExtension.endsWith('.wav') || fileExtension.endsWith('.ogg')) {
      return (
        <div className="clean-audio-viewer">
          <audio 
            controls 
            className="clean-audio"
            onPlay={() => setIsPlaying(true)}
            onPause={() => setIsPlaying(false)}
          >
            <source src={fileUrl} type={`audio/${fileExtension.split('.').pop()}`} />
          </audio>
        </div>
      );
    }

    // Image Content
    if (fileExtension.match(/\.(jpg|jpeg|png|gif|svg|webp)$/)) {
      return (
        <div className="clean-image-viewer">
          <img
            src={fileUrl}
            alt={content.title}
            className="clean-image"
          />
        </div>
      );
    }

    // Document Content
    if (fileExtension.match(/\.(txt|doc|docx)$/)) {
      return (
        <div className="clean-doc-viewer">
          <iframe
            src={fileUrl}
            title={content.title}
            className="clean-doc"
          >
            <div className="doc-fallback">
              <p>Can't view document? <a href={fileUrl} download>Download it here</a></p>
            </div>
          </iframe>
        </div>
      );
    }

    // Fallback
    return (
      <div className="clean-fallback">
        <div className="fallback-content">
          <i className={getContentIcon(content.fileName, content.contentType)}></i>
          <h3>Learning Material</h3>
          <p>Click to open your content</p>
          <a 
            href={fileUrl} 
            target="_blank" 
            rel="noopener noreferrer"
            className="open-btn-clean"
          >
            <i className="fas fa-external-link-alt"></i>
            Open Content
          </a>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="kid-loading">
        <div className="fun-spinner">
          <i className="fas fa-book-open"></i>
        </div>
        <h2>Getting your lesson ready!</h2>
        <p>Just a moment... ⭐</p>
      </div>
    );
  }

  if (error || !content) {
    return (
      <div className="kid-error">
        <div className="error-card">
          <div className="error-icon">
            <i className="fas fa-question-circle"></i>
          </div>
          <h2>Oops! Can't find that lesson</h2>
          <p>Don't worry! Let's go back and find something fun to learn.</p>
          <button onClick={() => navigate('/student/content')} className="back-btn-kid">
            <i className="fas fa-arrow-left"></i>
            Back to Learning
          </button>
        </div>
      </div>
    );
  }

  return (
    <AccessibleContentViewer
      content={content}
      contentType={content.contentType}
      className="clean-viewer"
    >
      <div className="clean-content-page">
        {/* Simple header with just title and back button */}
        <div className="clean-header">
          <button 
            onClick={() => navigate('/student/content')} 
            className="back-btn-clean"
            title="Back to Learning Hub"
          >
            <i className="fas fa-arrow-left"></i>
            Back
          </button>
          
          <h1 className="content-title">{content.title}</h1>
          
          <div className="header-actions">
            <button 
              onClick={() => setShowMessaging(true)}
              className="chat-btn-clean"
              title="Chat with Tutor"
            >
              <i className="fas fa-comments"></i>
              Chat
            </button>
            
            <button 
              onClick={handleReadAloud}
              className={`read-aloud-btn ${ttsState.isPlaying ? 'playing' : ''}`}
              title={ttsState.isPlaying ? "Stop reading" : "Read aloud"}
            >
              <i className={`fas ${ttsState.isPlaying ? 'fa-stop' : 'fa-play'}`}></i>
              {ttsState.isPlaying ? 'Stop' : 'Read'}
            </button>
            
            <button 
              onClick={handleTextMode}
              className={`text-mode-btn ${isTextModeVisible ? 'active' : ''}`}
              title="Toggle text mode"
            >
              <i className="fas fa-font"></i>
              Text
            </button>
            
            <button 
              onClick={toggleFocusMode}
              className={`focus-mode-btn ${isFocusMode ? 'active' : ''}`}
              title={isFocusMode ? "Exit focus mode" : "Enter focus mode"}
            >
              <i className={`fas ${isFocusMode ? 'fa-compress' : 'fa-expand'}`}></i>
              {isFocusMode ? 'Exit' : 'Focus'}
            </button>
            
            <button 
              onClick={() => setShowNotesPanel(!showNotesPanel)}
              className={`notes-btn-clean ${showNotesPanel ? 'active' : ''}`}
              title="Toggle study notes"
            >
              <i className="fas fa-sticky-note"></i>
              Notes
            </button>
            
            <div className="accessibility-dropdown">
              <button 
                onClick={() => setShowAccessibilityMenu(!showAccessibilityMenu)}
                className="accessibility-btn"
                title="Accessibility options"
              >
                <i className="fas fa-universal-access"></i>
                <i className="fas fa-chevron-down"></i>
              </button>
              
              {showAccessibilityMenu && (
                <div className="accessibility-menu">
                  <div className="menu-section">
                    <label>Font Size</label>
                    <div className="font-controls">
                      <button onClick={() => handleFontSizeChange(-2)} title="Decrease font size">
                        <i className="fas fa-minus"></i>
                      </button>
                      <span>{fontSize}px</span>
                      <button onClick={() => handleFontSizeChange(2)} title="Increase font size">
                        <i className="fas fa-plus"></i>
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Clean content area */}
        <div className="clean-content-area">
          {renderContentDisplay()}
        </div>

        {/* Text Mode Overlay */}
        {isTextModeVisible && content && (
          <TextModeOverlay
            isVisible={isTextModeVisible}
            onClose={() => setIsTextModeVisible(false)}
            content={content}
            contentType={content.contentType}
          />
        )}

        {/* Messaging Modal */}
        {showMessaging && user && content && (
          <ContentMessaging
            contentId={content.id}
            studentId={user.id}
            onClose={() => setShowMessaging(false)}
          />
        )}

        {/* Floating Notes Panel */}
        {content && (
          <FloatingNotesPanel
            contentId={content.id}
            isVisible={showNotesPanel}
            onClose={() => setShowNotesPanel(false)}
            contentTitle={content.title}
          />
        )}
      </div>
    </AccessibleContentViewer>
  );
};

export default ContentViewer;