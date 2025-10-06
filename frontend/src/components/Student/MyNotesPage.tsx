import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import { useNavigate } from 'react-router-dom';
import { apiService } from '../../services/api';
import ContentMessaging from './ContentMessaging';
import './MyNotesPage.css';

interface StudentNote {
  id: number;
  contentId: number;
  contentTitle: string;
  contentDescription: string;
  subjectArea: string;
  difficultyLevel: string;
  notes: string;
  lastUpdated: string;
  characterCount: number;
}

const MyNotesPage: React.FC = () => {
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const navigate = useNavigate();
  
  const [notes, setNotes] = useState<StudentNote[]>([]);
  const [filteredNotes, setFilteredNotes] = useState<StudentNote[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedSubject, setSelectedSubject] = useState('');
  const [sortBy, setSortBy] = useState<'recent' | 'alphabetical' | 'subject'>('recent');
  
  // Enhanced editing state
  const [expandedNoteId, setExpandedNoteId] = useState<number | null>(null);
  const [editingNoteId, setEditingNoteId] = useState<number | null>(null);
  const [editedContent, setEditedContent] = useState('');
  const [isSavingNote, setIsSavingNote] = useState(false);
  
  // AI functionality state
  const [activeAIFeature, setActiveAIFeature] = useState<'suggestions' | 'question' | 'improve' | null>(null);
  const [aiSuggestions, setAISuggestions] = useState('');
  const [aiQuestion, setAIQuestion] = useState('');
  const [aiAnswer, setAIAnswer] = useState('');
  const [isLoadingAI, setIsLoadingAI] = useState(false);
  const [aiError, setAIError] = useState<string | null>(null);
  
  // Chat functionality state
  const [showMessaging, setShowMessaging] = useState(false);
  const [activeNoteChatId, setActiveNoteChatId] = useState<number | null>(null);
  
  // Load all student notes
  const loadNotes = async () => {
    if (!user?.id) return;
    
    try {
      setIsLoading(true);
      const data = await apiService.get(`/api/student/content/notes/all?studentId=${user.id}`);
      
      setNotes(data.notes || []);
      setFilteredNotes(data.notes || []);
    } catch (error: any) {
      if (error?.response?.status === 404) {
        // Endpoint doesn't exist in production yet - graceful fallback
        console.warn('Notes endpoint not available in production');
        setNotes([]);
        setFilteredNotes([]);
        showNotification('Notes feature is being updated. Check back soon!', 'info');
      } else {
        console.error('Error loading notes:', error);
        showNotification('Failed to load notes. Please try again.', 'error');
      }
    } finally {
      setIsLoading(false);
    }
  };

  // Load notes on component mount
  useEffect(() => {
    loadNotes();
  }, [user?.id]);

  // Filter and search functionality
  useEffect(() => {
    let filtered = notes;

    // Filter by search term
    if (searchTerm.trim()) {
      filtered = filtered.filter(note =>
        note.contentTitle.toLowerCase().includes(searchTerm.toLowerCase()) ||
        note.notes.toLowerCase().includes(searchTerm.toLowerCase()) ||
        note.subjectArea.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Filter by subject
    if (selectedSubject) {
      filtered = filtered.filter(note => note.subjectArea === selectedSubject);
    }

    // Sort notes
    switch (sortBy) {
      case 'recent':
        filtered.sort((a, b) => new Date(b.lastUpdated).getTime() - new Date(a.lastUpdated).getTime());
        break;
      case 'alphabetical':
        filtered.sort((a, b) => a.contentTitle.localeCompare(b.contentTitle));
        break;
      case 'subject':
        filtered.sort((a, b) => a.subjectArea.localeCompare(b.subjectArea));
        break;
    }

    setFilteredNotes(filtered);
  }, [notes, searchTerm, selectedSubject, sortBy]);

  // Get unique subjects for filter dropdown
  const uniqueSubjects = Array.from(new Set(notes.map(note => note.subjectArea))).sort();

  // Navigate to content page
  const handleViewContent = (contentId: number) => {
    navigate(`/student/content/${contentId}`);
  };

  // Note expansion and editing functions
  const handleExpandNote = (noteId: number, currentContent: string) => {
    if (expandedNoteId === noteId) {
      setExpandedNoteId(null);
      setEditingNoteId(null);
      setActiveAIFeature(null);
    } else {
      setExpandedNoteId(noteId);
      setEditedContent(currentContent);
    }
  };

  const handleEditNote = (noteId: number, currentContent: string) => {
    setEditingNoteId(noteId);
    setEditedContent(currentContent);
  };

  const handleCancelEdit = () => {
    setEditingNoteId(null);
    setEditedContent('');
  };

  const handleSaveNote = async (noteId: number, contentId: number) => {
    if (!user?.id || !editedContent.trim()) return;

    try {
      setIsSavingNote(true);

      await apiService.post(`/api/student/content/${contentId}/interact?studentId=${user.id}`, {
        interactionType: 'notes',
        notes: editedContent
      });

      // Update the note in local state
      const updatedNotes = notes.map(note => 
        note.id === noteId 
          ? { ...note, notes: editedContent, characterCount: editedContent.length }
          : note
      );
      setNotes(updatedNotes);
      setFilteredNotes(updatedNotes.filter(note => {
        const matchesSearch = !searchTerm.trim() || 
          note.contentTitle.toLowerCase().includes(searchTerm.toLowerCase()) ||
          note.notes.toLowerCase().includes(searchTerm.toLowerCase()) ||
          note.subjectArea.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesSubject = !selectedSubject || note.subjectArea === selectedSubject;
        return matchesSearch && matchesSubject;
      }));
      
      setEditingNoteId(null);
      showNotification('Note saved successfully!', 'success');
    } catch (error) {
      console.error('Error saving note:', error);
      showNotification('Failed to save note. Please try again.', 'error');
    } finally {
      setIsSavingNote(false);
    }
  };

  // AI functionality (adapted from FloatingNotesPanel)
  const handleGetAISuggestions = async (contentId: number, noteContent: string) => {
    if (!user?.id) return;

    setIsLoadingAI(true);
    setAIError(null);
    setActiveAIFeature('suggestions');

    try {
      const data = await apiService.post(`/api/student/content/${contentId}/ai/suggestions?studentId=${user.id}`);

      setAISuggestions(data.suggestions || 'No suggestions available.');
      showNotification('AI suggestions generated!', 'success');
    } catch (error) {
      console.error('Error getting AI suggestions:', error);
      setAIError('Could not generate suggestions. Please try again.');
      showNotification('Failed to get AI suggestions', 'error');
    } finally {
      setIsLoadingAI(false);
    }
  };

  const handleAskAI = async (contentId: number) => {
    if (!user?.id || !aiQuestion.trim()) return;

    setIsLoadingAI(true);
    setAIError(null);
    setAIAnswer('');

    try {
      const data = await apiService.post(`/api/student/content/${contentId}/ai/ask?studentId=${user.id}`, {
        question: aiQuestion.trim()
      });

      setAIAnswer(data.answer || 'No answer available.');
      showNotification('AI answered your question!', 'success');
    } catch (error) {
      console.error('Error asking AI:', error);
      setAIError('Could not get an answer. Please try again.');
      showNotification('Failed to get AI answer', 'error');
    } finally {
      setIsLoadingAI(false);
    }
  };

  const handleImproveNotes = async (contentId: number, noteContent: string) => {
    if (!user?.id || !noteContent.trim()) {
      showNotification('Please have some notes to improve first', 'info');
      return;
    }

    setIsLoadingAI(true);
    setAIError(null);
    setActiveAIFeature('improve');

    try {
      const data = await apiService.post(`/api/student/content/${contentId}/ai/improve-notes?studentId=${user.id}`);

      setAISuggestions(data.improvements || 'No improvement suggestions available.');
      showNotification('AI note improvements generated!', 'success');
    } catch (error) {
      console.error('Error getting note improvements:', error);
      setAIError('Could not generate improvements. Please try again.');
      showNotification('Failed to get note improvements', 'error');
    } finally {
      setIsLoadingAI(false);
    }
  };

  // Chat with tutor functionality
  const handleChatWithTutor = (contentId: number) => {
    setActiveNoteChatId(contentId);
    setShowMessaging(true);
  };

  // Format date for display
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // Truncate notes for preview
  const truncateNotes = (notes: string, maxLength: number = 200) => {
    if (notes.length <= maxLength) return notes;
    return notes.substring(0, maxLength) + '...';
  };

  if (isLoading) {
    return (
      <div className="my-notes-page">
        <div className="notes-loading">
          <div className="loading-spinner">
            <i className="fas fa-spinner fa-spin"></i>
          </div>
          <p>Loading your notes...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="my-notes-page">
      {/* Header */}
      <div className="notes-page-header">
        <div className="header-content">
          <h1>
            <i className="fas fa-sticky-note"></i>
            My Notes
          </h1>
          <p>All your study notes in one place</p>
        </div>
        <div className="notes-stats">
          <div className="stat-item">
            <span className="stat-number">{notes.length}</span>
            <span className="stat-label">Total Notes</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">{uniqueSubjects.length}</span>
            <span className="stat-label">Subjects</span>
          </div>
        </div>
      </div>

      {/* Search and Filter Controls */}
      <div className="notes-controls">
        <div className="search-section">
          <div className="search-input">
            <i className="fas fa-search"></i>
            <input
              type="text"
              placeholder="Search notes by content, keywords, or subject..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
        
        <div className="filter-section">
          <select
            value={selectedSubject}
            onChange={(e) => setSelectedSubject(e.target.value)}
            className="subject-filter"
          >
            <option value="">All Subjects</option>
            {uniqueSubjects.map(subject => (
              <option key={subject} value={subject}>{subject}</option>
            ))}
          </select>

          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="sort-filter"
          >
            <option value="recent">Most Recent</option>
            <option value="alphabetical">Alphabetical</option>
            <option value="subject">By Subject</option>
          </select>
        </div>
      </div>

      {/* Notes Grid */}
      <div className="notes-content">
        {filteredNotes.length === 0 ? (
          <div className="empty-state">
            {notes.length === 0 ? (
              <>
                <i className="fas fa-sticky-note"></i>
                <h3>No Notes Yet</h3>
                <p>Start taking notes on learning content to see them here.</p>
                <button 
                  className="cta-button"
                  onClick={() => navigate('/student/dashboard')}
                >
                  <i className="fas fa-plus"></i>
                  Browse Content
                </button>
              </>
            ) : (
              <>
                <i className="fas fa-search"></i>
                <h3>No Matching Notes</h3>
                <p>Try adjusting your search or filter criteria.</p>
                <button 
                  className="clear-filters-btn"
                  onClick={() => {
                    setSearchTerm('');
                    setSelectedSubject('');
                  }}
                >
                  Clear Filters
                </button>
              </>
            )}
          </div>
        ) : (
          <div className="notes-grid">
            {filteredNotes.map((note) => (
              <div key={note.id} className={`note-card ${expandedNoteId === note.id ? 'expanded' : ''}`}>
                <div className="note-header">
                  <div className="note-title">
                    <h3>{note.contentTitle}</h3>
                    <div className="note-meta">
                      <span className="subject-tag">{note.subjectArea}</span>
                      <span className={`difficulty-badge difficulty-${note.difficultyLevel.toLowerCase()}`}>
                        {note.difficultyLevel}
                      </span>
                    </div>
                  </div>
                </div>
                
                <div className="note-content">
                  <p className="note-description">{note.contentDescription}</p>
                  
                  {expandedNoteId === note.id ? (
                    <div className="note-expanded">
                      {editingNoteId === note.id ? (
                        <div className="note-editor">
                          <textarea
                            className="note-textarea"
                            value={editedContent}
                            onChange={(e) => setEditedContent(e.target.value)}
                            placeholder="Edit your notes here..."
                            rows={8}
                          />
                          <div className="editor-actions">
                            <button
                              className="action-btn cancel-btn"
                              onClick={handleCancelEdit}
                              disabled={isSavingNote}
                            >
                              <i className="fas fa-times"></i>
                              Cancel
                            </button>
                            <button
                              className="action-btn save-btn"
                              onClick={() => handleSaveNote(note.id, note.contentId)}
                              disabled={isSavingNote || !editedContent.trim()}
                            >
                              {isSavingNote ? (
                                <><i className="fas fa-spinner fa-spin"></i> Saving...</>
                              ) : (
                                <><i className="fas fa-save"></i> Save</>
                              )}
                            </button>
                          </div>
                        </div>
                      ) : (
                        <div className="note-full-content">
                          <pre className="note-text">{note.notes}</pre>
                          <div className="note-actions-expanded">
                            <button
                              className="action-btn edit-btn"
                              onClick={() => handleEditNote(note.id, note.notes)}
                              title="Edit this note"
                            >
                              <i className="fas fa-edit"></i>
                              Edit
                            </button>
                            <button
                              className="action-btn ai-btn"
                              onClick={() => handleGetAISuggestions(note.contentId, note.notes)}
                              disabled={isLoadingAI}
                              title="Get AI study suggestions"
                            >
                              {isLoadingAI && activeAIFeature === 'suggestions' ? (
                                <><i className="fas fa-spinner fa-spin"></i> AI</>
                              ) : (
                                <><i className="fas fa-magic"></i> Suggestions</>
                              )}
                            </button>
                            <button
                              className="action-btn ai-btn"
                              onClick={() => handleImproveNotes(note.contentId, note.notes)}
                              disabled={isLoadingAI}
                              title="Get AI note improvements"
                            >
                              {isLoadingAI && activeAIFeature === 'improve' ? (
                                <><i className="fas fa-spinner fa-spin"></i> AI</>
                              ) : (
                                <><i className="fas fa-lightbulb"></i> Improve</>
                              )}
                            </button>
                            <button
                              className="action-btn chat-btn"
                              onClick={() => handleChatWithTutor(note.contentId)}
                              title="Chat with tutor about this content"
                            >
                              <i className="fas fa-comments"></i>
                              Chat
                            </button>
                          </div>
                        </div>
                      )}

                      {/* AI Features Panel */}
                      {expandedNoteId === note.id && (activeAIFeature === 'suggestions' || activeAIFeature === 'improve') && (
                        <div className="ai-panel-embedded">
                          <div className="ai-header-embedded">
                            <h4>
                              <i className="fas fa-robot"></i>
                              {activeAIFeature === 'suggestions' ? 'Study Suggestions' : 'Note Improvements'}
                            </h4>
                            <button
                              className="ai-close-btn"
                              onClick={() => setActiveAIFeature(null)}
                            >
                              <i className="fas fa-times"></i>
                            </button>
                          </div>
                          <div className="ai-content-embedded">
                            {aiError ? (
                              <div className="ai-error">
                                <i className="fas fa-exclamation-triangle"></i>
                                {aiError}
                              </div>
                            ) : aiSuggestions ? (
                              <div className="ai-suggestions">
                                {aiSuggestions}
                              </div>
                            ) : isLoadingAI ? (
                              <div className="ai-loading">
                                <i className="fas fa-spinner fa-spin"></i>
                                Generating...
                              </div>
                            ) : null}
                          </div>
                        </div>
                      )}

                      {/* Ask AI Feature */}
                      {expandedNoteId === note.id && (
                        <div className="ai-question-section">
                          <div className="ai-question-input">
                            <input
                              type="text"
                              placeholder="Ask AI a question about this content..."
                              value={aiQuestion}
                              onChange={(e) => setAIQuestion(e.target.value)}
                              onKeyPress={(e) => e.key === 'Enter' && handleAskAI(note.contentId)}
                              disabled={isLoadingAI}
                            />
                            <button
                              className="ai-ask-btn"
                              onClick={() => handleAskAI(note.contentId)}
                              disabled={!aiQuestion.trim() || isLoadingAI}
                            >
                              {isLoadingAI && activeAIFeature === 'question' ? (
                                <i className="fas fa-spinner fa-spin"></i>
                              ) : (
                                <i className="fas fa-paper-plane"></i>
                              )}
                            </button>
                          </div>

                          {aiAnswer && (
                            <div className="ai-answer">
                              <strong>AI Answer:</strong>
                              <p>{aiAnswer}</p>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  ) : (
                    <div className="note-preview">
                      <p>{truncateNotes(note.notes)}</p>
                    </div>
                  )}
                </div>
                
                <div className="note-footer">
                  <div className="note-info">
                    <span className="last-updated">
                      <i className="fas fa-clock"></i>
                      {formatDate(note.lastUpdated)}
                    </span>
                    <span className="char-count">
                      <i className="fas fa-file-text"></i>
                      {note.characterCount} chars
                    </span>
                  </div>
                  
                  <div className="note-actions">
                    <button
                      className="action-btn expand-btn"
                      onClick={() => handleExpandNote(note.id, note.notes)}
                      title={expandedNoteId === note.id ? "Collapse note" : "Expand note"}
                    >
                      <i className={`fas fa-${expandedNoteId === note.id ? 'compress' : 'expand'}`}></i>
                      {expandedNoteId === note.id ? 'Collapse' : 'Expand'}
                    </button>
                    <button
                      className="action-btn view-btn"
                      onClick={() => handleViewContent(note.contentId)}
                      title="View original content"
                    >
                      <i className="fas fa-eye"></i>
                      View
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Chat with Tutor Modal */}
      {showMessaging && activeNoteChatId && user && (
        <ContentMessaging
          contentId={activeNoteChatId}
          studentId={user.id}
          onClose={() => {
            setShowMessaging(false);
            setActiveNoteChatId(null);
          }}
        />
      )}

      {/* Results Summary */}
      {filteredNotes.length > 0 && (
        <div className="results-summary">
          Showing {filteredNotes.length} of {notes.length} notes
        </div>
      )}
    </div>
  );
};

export default MyNotesPage;