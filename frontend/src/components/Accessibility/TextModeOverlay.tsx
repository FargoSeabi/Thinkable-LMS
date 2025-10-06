import React, { useState, useEffect, useCallback } from 'react';
import { ExtractedContent, textExtractionService } from '../../services/TextExtractionService';
import { useTextToSpeech } from '../../contexts/TextToSpeechContext';
import { useAuth } from '../../contexts/AuthContext';
import './TextModeOverlay.css';

interface TextModeOverlayProps {
  isVisible: boolean;
  onClose: () => void;
  content: any; // The original content object
  contentType: string;
}

const TextModeOverlay: React.FC<TextModeOverlayProps> = ({
  isVisible,
  onClose,
  content,
  contentType
}) => {
  const { user } = useAuth();
  const { speak, stop, state: ttsState } = useTextToSpeech();
  const [extractedContent, setExtractedContent] = useState<ExtractedContent | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fontSize, setFontSize] = useState(16);
  const [showSummaryOnly, setShowSummaryOnly] = useState(false);

  // Extract text when overlay becomes visible
  useEffect(() => {
    if (isVisible && content && !extractedContent) {
      extractText();
    }
  }, [isVisible, content]);

  const extractText = useCallback(async () => {
    if (!content) return;

    setIsLoading(true);
    setError(null);
    const startTime = Date.now();

    try {
      let extracted: ExtractedContent;

      // Choose extraction method based on content type
      switch (contentType.toLowerCase()) {
        case 'image':
        case 'jpg':
        case 'jpeg':
        case 'png':
        case 'gif':
          extracted = await textExtractionService.extractFromImage(content.fileUrl || content.url);
          break;
        case 'video':
        case 'mp4':
        case 'avi':
        case 'mov':
          extracted = await textExtractionService.extractTranscript(content.id, 'video');
          break;
        case 'audio':
        case 'mp3':
        case 'wav':
        case 'ogg':
          extracted = await textExtractionService.extractTranscript(content.id, 'audio');
          break;
        default:
          // For documents (PDF, DOC, etc.)
          extracted = await textExtractionService.extractText(
            content.id, 
            contentType,
            {
              includeImages: true,
              includeMetadata: true,
              applyPreset: user?.recommendedPreset,
              maxLength: 50000,
              preserveStructure: true
            }
          );
      }

      // Apply accessibility optimizations based on user preset
      if (user?.recommendedPreset) {
        extracted = textExtractionService.processForAccessibility(extracted, user.recommendedPreset);
      }

      setExtractedContent(extracted);

      // Record usage
      const extractionTime = Date.now() - startTime;
      textExtractionService.recordUsage('extract_success', contentType, extractionTime);

    } catch (err) {
      const extractionTime = Date.now() - startTime;
      console.error('Text extraction failed:', err);
      setError(err instanceof Error ? err.message : 'Failed to extract text');
      textExtractionService.recordUsage('extract_error', contentType, extractionTime);
    } finally {
      setIsLoading(false);
    }
  }, [content, contentType, user?.recommendedPreset]);

  const handleSpeak = useCallback(async () => {
    if (!extractedContent) return;

    try {
      const textToSpeak = showSummaryOnly 
        ? textExtractionService.createSummary(extractedContent, 500)
        : textExtractionService.createStructuredText(extractedContent, content);
      
      await speak(textToSpeak);
    } catch (error) {
      console.error('Failed to speak text:', error);
    }
  }, [extractedContent, showSummaryOnly, speak, content]);

  const handleFontSizeChange = (delta: number) => {
    const newSize = Math.max(12, Math.min(32, fontSize + delta));
    setFontSize(newSize);
  };

  const copyToClipboard = useCallback(async () => {
    if (!extractedContent) return;

    try {
      const textToCopy = textExtractionService.createStructuredText(extractedContent, content);
      await navigator.clipboard.writeText(textToCopy);
      // Could add a toast notification here
    } catch (error) {
      console.error('Failed to copy text:', error);
    }
  }, [extractedContent, content]);

  if (!isVisible) return null;

  const getPresetClassName = () => {
    switch (user?.recommendedPreset) {
      case 'DYSLEXIA_SUPPORT': return 'text-mode-dyslexia';
      case 'ADHD_SUPPORT': return 'text-mode-adhd';
      case 'AUTISM_SUPPORT': return 'text-mode-autism';
      case 'SENSORY_CALM': return 'text-mode-sensory';
      case 'READING_SUPPORT': return 'text-mode-reading';
      default: return 'text-mode-standard';
    }
  };

  return (
    <div className="text-mode-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className={`text-mode-content ${getPresetClassName()}`}>
        {/* Header */}
        <div className="text-mode-header">
          <div className="text-mode-title">
            <i className="fas fa-font"></i>
            <h2>Text Mode</h2>
            {extractedContent && (
              <span className="text-info">
                {extractedContent.metadata.wordCount} words • {extractedContent.metadata.readingTime} min read
              </span>
            )}
          </div>
          
          <div className="text-mode-controls">
            {/* Font size controls */}
            <div className="font-controls">
              <button 
                className="control-btn"
                onClick={() => handleFontSizeChange(-2)}
                title="Decrease font size"
              >
                <i className="fas fa-minus"></i>
              </button>
              <span className="font-size-display">{fontSize}px</span>
              <button 
                className="control-btn"
                onClick={() => handleFontSizeChange(2)}
                title="Increase font size"
              >
                <i className="fas fa-plus"></i>
              </button>
            </div>

            {/* View toggle */}
            <button
              className={`control-btn ${showSummaryOnly ? 'active' : ''}`}
              onClick={() => setShowSummaryOnly(!showSummaryOnly)}
              title={showSummaryOnly ? "Show full text" : "Show summary only"}
            >
              <i className={`fas ${showSummaryOnly ? 'fa-expand-alt' : 'fa-compress-alt'}`}></i>
            </button>

            {/* TTS controls */}
            {extractedContent && (
              <button
                className={`control-btn tts-btn ${ttsState.isPlaying ? 'playing' : ''}`}
                onClick={ttsState.isPlaying ? stop : handleSpeak}
                title={ttsState.isPlaying ? "Stop reading" : "Read aloud"}
              >
                <i className={`fas ${ttsState.isPlaying ? 'fa-stop' : 'fa-play'}`}></i>
              </button>
            )}

            {/* Copy button */}
            <button
              className="control-btn"
              onClick={copyToClipboard}
              title="Copy text to clipboard"
            >
              <i className="fas fa-copy"></i>
            </button>

            {/* Close button */}
            <button className="close-btn" onClick={onClose} title="Close text mode">
              <i className="fas fa-times"></i>
            </button>
          </div>
        </div>

        {/* Content area */}
        <div className="text-mode-body">
          {isLoading && (
            <div className="loading-state">
              <div className="loading-spinner">
                <i className="fas fa-spinner fa-spin"></i>
              </div>
              <h3>Extracting Text Content</h3>
              <p>Processing {contentType} file for accessibility...</p>
              <div className="loading-details">
                <span>• Extracting readable text</span>
                <span>• Applying {user?.recommendedPreset || 'standard'} accessibility settings</span>
                <span>• Optimizing for your reading preferences</span>
              </div>
            </div>
          )}

          {error && (
            <div className="error-state">
              <div className="error-icon">
                <i className="fas fa-exclamation-triangle"></i>
              </div>
              <h3>Text Extraction Failed</h3>
              <p>{error}</p>
              <div className="error-actions">
                <button className="retry-btn" onClick={extractText}>
                  <i className="fas fa-redo"></i> Try Again
                </button>
                <button className="close-btn-alt" onClick={onClose}>
                  Close
                </button>
              </div>
            </div>
          )}

          {extractedContent && (
            <div 
              className="extracted-content"
              style={{ fontSize: `${fontSize}px` }}
            >
              {showSummaryOnly ? (
                <div className="summary-content">
                  <h3>Quick Summary</h3>
                  <p>{textExtractionService.createSummary(extractedContent, 500)}</p>
                  <button 
                    className="show-full-btn"
                    onClick={() => setShowSummaryOnly(false)}
                  >
                    <i className="fas fa-expand-alt"></i> Show Full Content
                  </button>
                </div>
              ) : (
                <div 
                  className="full-content"
                  dangerouslySetInnerHTML={{
                    __html: textExtractionService.createStructuredText(extractedContent, content)
                      .replace(/\n/g, '<br>')
                      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                      .replace(/^# (.*$)/gim, '<h1>$1</h1>')
                      .replace(/^## (.*$)/gim, '<h2>$1</h2>')
                      .replace(/^### (.*$)/gim, '<h3>$1</h3>')
                  }}
                />
              )}

              {/* Metadata footer */}
              <div className="content-metadata">
                <div className="extraction-info">
                  <span><i className="fas fa-check-circle"></i> Extracted via {extractedContent.metadata.extractionMethod}</span>
                  <span><i className="fas fa-chart-bar"></i> Confidence: {Math.round(extractedContent.metadata.confidence * 100)}%</span>
                </div>
                {extractedContent.accessibility.hasImages && (
                  <div className="accessibility-info">
                    <span><i className="fas fa-image"></i> Contains {extractedContent.accessibility.imageDescriptions.length} described images</span>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Footer with accessibility preset info */}
        <div className="text-mode-footer">
          <div className="preset-info">
            <i className="fas fa-universal-access"></i>
            <span>Optimized for {user?.recommendedPreset?.replace('_', ' ').toLowerCase() || 'standard'} accessibility</span>
          </div>
          <div className="keyboard-hint">
            <span>Press <kbd>Esc</kbd> to close • <kbd>Space</kbd> to read aloud</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TextModeOverlay;