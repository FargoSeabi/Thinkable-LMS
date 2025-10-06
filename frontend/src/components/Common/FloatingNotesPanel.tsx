import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import { apiService } from '../../services/api';
import './FloatingNotesPanel.css';

interface FloatingNotesPanelProps {
  contentId: number;
  isVisible: boolean;
  onClose: () => void;
  contentTitle: string;
}

interface StudentNote {
  id?: number;
  contentId: number;
  studentId: number;
  notes: string;
  lastUpdated: string;
}

const FloatingNotesPanel: React.FC<FloatingNotesPanelProps> = ({
  contentId,
  isVisible,
  onClose,
  contentTitle
}) => {
  const { user } = useAuth();
  const { showNotification } = useNotification();
  
  const [notes, setNotes] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [lastSaved, setLastSaved] = useState<Date | null>(null);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  
  // AI functionality state
  const [showAIPanel, setShowAIPanel] = useState(false);
  const [aiSuggestions, setAISuggestions] = useState<string>('');
  const [aiQuestion, setAIQuestion] = useState('');
  const [aiAnswer, setAIAnswer] = useState('');
  const [isLoadingAI, setIsLoadingAI] = useState(false);
  const [aiError, setAIError] = useState<string | null>(null);

  // Load existing notes
  const loadNotes = useCallback(async () => {
    if (!user?.id || !contentId) return;

    try {
      setIsLoading(true);
      const data = await apiService.get(`/api/student/content/${contentId}/notes?studentId=${user.id}`);
      
      if (data.notes) {
        setNotes(data.notes);
        setLastSaved(new Date(data.lastUpdated));
      }
    } catch (error: any) {
      if (error?.response?.status === 404) {
        // 404 is expected for new content without notes
        console.log('No existing notes found for this content');
      } else {
        console.error('Error loading notes:', error);
      }
    } finally {
      setIsLoading(false);
    }
  }, [user?.id, contentId]);

  // Save notes
  const saveNotes = useCallback(async (noteText: string) => {
    if (!user?.id || !contentId) return;

    try {
      setIsSaving(true);
      
      await apiService.post(`/api/student/content/${contentId}/interact?studentId=${user.id}`, {
        interactionType: 'notes',
        notes: noteText
      });

      setLastSaved(new Date());
      setHasUnsavedChanges(false);
      showNotification('Notes saved successfully', 'success');
    } catch (error) {
      console.error('Error saving notes:', error);
      showNotification('Failed to save notes. Please try again.', 'error');
    } finally {
      setIsSaving(false);
    }
  }, [user?.id, contentId, showNotification]);

  // Auto-save with debounce
  useEffect(() => {
    if (!hasUnsavedChanges) return;

    const autoSaveDelay = setTimeout(() => {
      saveNotes(notes);
    }, 2000); // Auto-save after 2 seconds of inactivity

    return () => clearTimeout(autoSaveDelay);
  }, [notes, hasUnsavedChanges, saveNotes]);

  // Load notes when panel becomes visible
  useEffect(() => {
    if (isVisible && contentId) {
      loadNotes();
    }
  }, [isVisible, contentId, loadNotes]);

  // Handle text changes
  const handleNotesChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setNotes(e.target.value);
    setHasUnsavedChanges(true);
  };

  // Manual save
  const handleSave = () => {
    saveNotes(notes);
  };

  // Clear notes
  const handleClear = () => {
    if (window.confirm('Are you sure you want to clear all notes? This action cannot be undone.')) {
      setNotes('');
      setHasUnsavedChanges(true);
    }
  };

  // Get AI study suggestions
  const handleGetSuggestions = async () => {
    if (!user?.id || !contentId) return;
    
    setIsLoadingAI(true);
    setAIError(null);
    
    try {
      const data = await apiService.post(`/api/student/content/${contentId}/ai/suggestions?studentId=${user.id}`);
      
      setAISuggestions(data.suggestions || 'No suggestions available.');
      setShowAIPanel(true);
      showNotification('AI suggestions generated!', 'success');
    } catch (error) {
      console.error('Error getting AI suggestions:', error);
      setAIError('Could not generate suggestions. Please try again.');
      showNotification('Failed to get AI suggestions', 'error');
    } finally {
      setIsLoadingAI(false);
    }
  };

  // Ask AI a question
  const handleAskAI = async () => {
    if (!user?.id || !contentId || !aiQuestion.trim()) return;
    
    setIsLoadingAI(true);
    setAIError(null);
    setAIAnswer('');
    
    try {
      const data = await apiService.post(`/api/student/content/${contentId}/ai/ask?studentId=${user.id}`, {
        question: aiQuestion.trim()
      });
      
      setAIAnswer(data.answer || 'No answer available.');
      showNotification('AI answered your question!', 'success');
    } catch (error: any) {
      console.error('Error asking AI:', error);
      if (error?.response?.status === 404) {
        setAIError('AI features are being updated. Check back soon!');
        showNotification('AI features coming soon!', 'info');
      } else {
        setAIError('Could not get an answer. Please try again.');
        showNotification('Failed to get AI answer', 'error');
      }
    } finally {
      setIsLoadingAI(false);
    }
  };

  // Get AI note improvement suggestions
  const handleImproveNotes = async () => {
    if (!user?.id || !contentId || !notes.trim()) {
      showNotification('Please take some notes first before asking for improvements', 'info');
      return;
    }
    
    setIsLoadingAI(true);
    setAIError(null);
    
    try {
      const data = await apiService.post(`/api/student/content/${contentId}/ai/improve-notes?studentId=${user.id}`);
      
      setAISuggestions(data.improvements || 'No improvement suggestions available.');
      setShowAIPanel(true);
      showNotification('AI note improvements generated!', 'success');
    } catch (error) {
      console.error('Error getting note improvements:', error);
      setAIError('Could not generate improvements. Please try again.');
      showNotification('Failed to get note improvements', 'error');
    } finally {
      setIsLoadingAI(false);
    }
  };

  if (!isVisible) return null;

  return (
    <div className="floating-notes-panel">
      <div className="notes-header">
        <div className="notes-title">
          <i className="fas fa-sticky-note"></i>
          <h3>Study Notes</h3>
        </div>
        <div className="notes-actions">
          {hasUnsavedChanges && (
            <span className="unsaved-indicator">
              <i className="fas fa-circle"></i>
              Unsaved changes
            </span>
          )}
          {isSaving && (
            <span className="saving-indicator">
              <i className="fas fa-spinner fa-spin"></i>
              Saving...
            </span>
          )}
          {lastSaved && !hasUnsavedChanges && (
            <span className="saved-indicator">
              <i className="fas fa-check"></i>
              Saved {lastSaved.toLocaleTimeString()}
            </span>
          )}
          <button 
            className="notes-btn ai-btn" 
            onClick={handleGetSuggestions}
            disabled={isLoadingAI}
            title="Get AI study suggestions"
          >
            {isLoadingAI ? <i className="fas fa-spinner fa-spin"></i> : <i className="fas fa-magic"></i>}
          </button>
          <button 
            className="notes-btn ai-btn" 
            onClick={() => setShowAIPanel(!showAIPanel)}
            title={showAIPanel ? "Hide AI assistant" : "Show AI assistant"}
          >
            <i className="fas fa-robot"></i>
          </button>
          <button className="notes-btn" onClick={onClose} title="Close notes">
            <i className="fas fa-times"></i>
          </button>
        </div>
      </div>

      <div className="notes-content-info">
        <span className="content-title">Notes for: {contentTitle}</span>
      </div>

      <div className="notes-body">
        {isLoading ? (
          <div className="notes-loading">
            <i className="fas fa-spinner fa-spin"></i>
            Loading notes...
          </div>
        ) : (
          <textarea
            className="notes-textarea"
            placeholder="Start taking notes about this content...

Tips:
• Write key concepts and definitions
• Note important examples or formulas  
• Record questions you have
• Summarize main points in your own words"
            value={notes}
            onChange={handleNotesChange}
            disabled={isSaving}
          />
        )}
      </div>

      <div className="notes-footer">
        <div className="notes-controls">
          <button 
            className="notes-btn secondary" 
            onClick={handleClear}
            disabled={!notes || isSaving}
          >
            <i className="fas fa-trash"></i>
            Clear
          </button>
          <button 
            className="notes-btn primary" 
            onClick={handleSave}
            disabled={!hasUnsavedChanges || isSaving}
          >
            <i className="fas fa-save"></i>
            {isSaving ? 'Saving...' : 'Save Now'}
          </button>
        </div>
        <div className="notes-char-count">
          <span className="character-count">{notes.length} characters</span>
        </div>
      </div>

      {/* AI Assistant Panel */}
      {showAIPanel && (
        <div className="ai-panel">
          <div className="ai-header">
            <h4><i className="fas fa-robot"></i> AI Study Assistant</h4>
            <button className="ai-close-btn" onClick={() => setShowAIPanel(false)}>
              <i className="fas fa-times"></i>
            </button>
          </div>
          
          <div className="ai-content">
            {/* AI Suggestions Section */}
            {aiSuggestions && (
              <div className="ai-section">
                <h5><i className="fas fa-lightbulb"></i> Study Suggestions</h5>
                <div className="ai-suggestions">
                  {aiSuggestions}
                </div>
              </div>
            )}

            {/* Ask AI Section */}
            <div className="ai-section">
              <h5><i className="fas fa-question-circle"></i> Ask AI</h5>
              <div className="ai-question-input">
                <input
                  type="text"
                  placeholder="Ask a question about this content..."
                  value={aiQuestion}
                  onChange={(e) => setAIQuestion(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleAskAI()}
                  disabled={isLoadingAI}
                />
                <button
                  className="ai-ask-btn"
                  onClick={handleAskAI}
                  disabled={!aiQuestion.trim() || isLoadingAI}
                >
                  {isLoadingAI ? <i className="fas fa-spinner fa-spin"></i> : <i className="fas fa-paper-plane"></i>}
                </button>
              </div>
              
              {aiAnswer && (
                <div className="ai-answer">
                  <strong>AI Answer:</strong>
                  <p>{aiAnswer}</p>
                </div>
              )}
            </div>

            {/* Note Improvement Section */}
            <div className="ai-section">
              <h5><i className="fas fa-edit"></i> Improve Notes</h5>
              <button
                className="ai-improve-btn"
                onClick={handleImproveNotes}
                disabled={!notes.trim() || isLoadingAI}
              >
                {isLoadingAI ? 
                  <><i className="fas fa-spinner fa-spin"></i> Analyzing...</> : 
                  <><i className="fas fa-magic"></i> Get Improvement Tips</>
                }
              </button>
            </div>

            {/* Error Display */}
            {aiError && (
              <div className="ai-error">
                <i className="fas fa-exclamation-triangle"></i>
                {aiError}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default FloatingNotesPanel;