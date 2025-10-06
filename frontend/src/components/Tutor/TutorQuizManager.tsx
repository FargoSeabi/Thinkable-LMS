import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import config from '../../services/config';
import QuizCreator from './QuizCreator';
import QuizPreview from './QuizPreview';
import { Quiz, Question, LearningContent } from '../../types/quiz';
import './TutorQuizManager.css';

const TutorQuizManager: React.FC = () => {
  const { user } = useAuth();
  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [learningContent, setLearningContent] = useState<LearningContent[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showPreviewModal, setShowPreviewModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedQuiz, setSelectedQuiz] = useState<Quiz | null>(null);
  const [selectedContentForAI, setSelectedContentForAI] = useState<number | null>(null);
  const [notification, setNotification] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    fetchQuizzes();
    fetchLearningContent();
  }, []);

  const fetchQuizzes = async () => {
    try {
      if (!user?.id) {
        console.log('No user ID available for fetching quizzes');
        setLoading(false);
        return;
      }
      
      const response = await fetch(`${API_BASE_URL}/api/tutor/content/tutor/${user.id}/quizzes`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });
      
      if (response.ok) {
        const data = await response.json();
        setQuizzes(data.quizzes || []);
      } else {
        console.error('Failed to fetch quizzes:', response.status);
        showNotification('Failed to load quizzes', 'error');
        setQuizzes([]);
      }
    } catch (error) {
      console.error('Error fetching quizzes:', error);
      showNotification('Failed to load quizzes', 'error');
      // Fallback to empty array instead of showing error to user
      setQuizzes([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchLearningContent = async () => {
    try {
      if (!user?.id) {
        console.log('No user ID available for fetching content');
        return;
      }
      
      const response = await fetch(`${API_BASE_URL}/api/tutor/content/tutor/${user.id}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });
      
      if (response.ok) {
        const data = await response.json();
        setLearningContent(data.content || []);
      } else {
        console.warn('Failed to fetch learning content:', response.status);
      }
    } catch (error) {
      console.error('Error fetching learning content:', error);
    }
  };

  const deleteQuiz = async (quizId: number) => {
    if (!window.confirm('Are you sure you want to delete this quiz? This action cannot be undone.')) {
      return;
    }

    try {
      if (!user?.id) {
        showNotification('User not authenticated', 'error');
        return;
      }

      const response = await fetch(`${API_BASE_URL}/api/quiz/${quizId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      const data = await response.json();
      if (response.ok) {
        setQuizzes(quizzes.filter(q => q.id !== quizId));
        showNotification(data.message || 'Quiz deleted successfully!', 'success');
      } else {
        showNotification(data.error || 'Failed to delete quiz', 'error');
      }
    } catch (error) {
      console.error('Error deleting quiz:', error);
      showNotification('Failed to delete quiz', 'error');
    }
  };

  const generateAIQuiz = async (contentId: number) => {
    try {
      setLoading(true);
      if (!user?.id) {
        showNotification('User not authenticated', 'error');
        return;
      }
      
      const response = await fetch(`${API_BASE_URL}/api/tutor/content/${contentId}/generate-quiz?tutorUserId=${user.id}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      const data = await response.json();
      if (data.success || response.ok) {
        await fetchQuizzes(); // Refresh the list
        showNotification(data.message || 'AI Quiz generated successfully!', 'success');
      } else {
        showNotification(data.message || data.error || 'Failed to generate quiz', 'error');
      }
    } catch (error) {
      console.error('Error generating AI quiz:', error);
      showNotification('Failed to generate AI quiz', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showNotification = (message: string, type: 'success' | 'error') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 5000);
  };

  const handlePreviewQuiz = (quiz: Quiz) => {
    setSelectedQuiz(quiz);
    setShowPreviewModal(true);
  };

  const handleEditQuiz = (quiz: Quiz) => {
    setSelectedQuiz(quiz);
    setShowEditModal(true);
  };

  const handleQuizUpdated = (updatedQuiz: Quiz) => {
    setQuizzes(prev => prev.map(q => q.id === updatedQuiz.id ? updatedQuiz : q));
    setShowEditModal(false);
    setSelectedQuiz(null);
  };

  const closeModals = () => {
    setShowCreateModal(false);
    setShowPreviewModal(false);
    setShowEditModal(false);
    setSelectedQuiz(null);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Unknown';
    return new Date(dateString).toLocaleDateString();
  };

  if (loading && quizzes.length === 0) {
    return (
      <div className="quiz-manager-loading">
        <div className="loading-spinner"></div>
        <p>Loading your quizzes...</p>
      </div>
    );
  }

  return (
    <div className="quiz-manager">
      <div className="quiz-manager-header">
        <div className="header-content">
          <h1>📝 Quiz Management</h1>
          <p>Create, manage, and analyze your interactive quizzes</p>
        </div>
        <div className="header-actions">
          <button 
            className="btn btn-primary"
            onClick={() => setShowCreateModal(true)}
          >
            <i className="fas fa-plus"></i> Create New Quiz
          </button>
        </div>
      </div>

      {/* Notification */}
      {notification && (
        <div className={`notification ${notification.type}`}>
          <span>{notification.message}</span>
          <button onClick={() => setNotification(null)}>×</button>
        </div>
      )}

      {/* Stats Overview */}
      <div className="quiz-stats">
        <div className="stat-card">
          <div className="stat-icon">📊</div>
          <div className="stat-content">
            <h3>{quizzes.length}</h3>
            <p>Total Quizzes</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">🤖</div>
          <div className="stat-content">
            <h3>{quizzes.filter(q => q.aiGenerated).length}</h3>
            <p>AI Generated</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">✍️</div>
          <div className="stat-content">
            <h3>{quizzes.filter(q => !q.aiGenerated).length}</h3>
            <p>Manual Created</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">❓</div>
          <div className="stat-content">
            <h3>{quizzes.reduce((total, quiz) => total + quiz.questions.length, 0)}</h3>
            <p>Total Questions</p>
          </div>
        </div>
      </div>

      {/* AI Quiz Generation Section */}
      <div className="ai-quiz-section">
        <h2>🧠 AI Quiz Generation</h2>
        <p>Generate quizzes automatically from your learning content using AI</p>
        
        {learningContent.length > 0 ? (
          <div className="content-grid">
            {learningContent.slice(0, 6).map(content => (
              <div key={content.id} className="content-card">
                <div className="content-info">
                  <h4>{content.title}</h4>
                  <span className="content-type">{content.contentType}</span>
                </div>
                <button 
                  className="btn btn-ai"
                  onClick={() => generateAIQuiz(content.id)}
                  disabled={loading}
                >
                  <i className="fas fa-magic"></i> Generate Quiz
                </button>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <i className="fas fa-upload fa-3x"></i>
            <h3>No Learning Content Found</h3>
            <p>Upload some content first to generate AI quizzes</p>
            <a href="/tutor/upload" className="btn btn-primary">Upload Content</a>
          </div>
        )}
      </div>

      {/* Quiz List */}
      <div className="quiz-list-section">
        <h2>📋 Your Quizzes</h2>
        
        {quizzes.length > 0 ? (
          <div className="quiz-grid">
            {quizzes.map(quiz => (
              <div key={quiz.id} className="quiz-card">
                <div className="quiz-card-header">
                  <div className="quiz-title">
                    <h3>{quiz.title}</h3>
                    <div className="quiz-badges">
                      {quiz.aiGenerated ? (
                        <span className="badge ai-badge">🤖 AI Generated</span>
                      ) : (
                        <span className="badge manual-badge">✍️ Manual</span>
                      )}
                    </div>
                  </div>
                  <div className="quiz-menu">
                    <button className="menu-btn">
                      <i className="fas fa-ellipsis-v"></i>
                    </button>
                  </div>
                </div>

                <div className="quiz-stats-mini">
                  <div className="stat-mini">
                    <i className="fas fa-question"></i>
                    <span>{quiz.questions.length} questions</span>
                  </div>
                  <div className="stat-mini">
                    <i className="fas fa-calendar"></i>
                    <span>Created {formatDate(quiz.createdAt)}</span>
                  </div>
                </div>

                <div className="quiz-preview">
                  <h4>Sample Questions:</h4>
                  <ul>
                    {quiz.questions.slice(0, 2).map(q => (
                      <li key={q.id}>{q.question}</li>
                    ))}
                    {quiz.questions.length > 2 && (
                      <li className="more-questions">
                        +{quiz.questions.length - 2} more questions...
                      </li>
                    )}
                  </ul>
                </div>

                <div className="quiz-actions">
                  <button
                    className="btn btn-outline"
                    onClick={() => handlePreviewQuiz(quiz)}
                  >
                    <i className="fas fa-eye"></i> Preview
                  </button>
                  <button
                    className="btn btn-outline"
                    onClick={() => handleEditQuiz(quiz)}
                  >
                    <i className="fas fa-edit"></i> Edit
                  </button>
                  <button className="btn btn-outline" disabled>
                    <i className="fas fa-chart-line"></i> Analytics
                  </button>
                  <button
                    className="btn btn-danger"
                    onClick={() => quiz.id && deleteQuiz(quiz.id)}
                  >
                    <i className="fas fa-trash"></i> Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <i className="fas fa-question-circle fa-3x"></i>
            <h3>No Quizzes Yet</h3>
            <p>Create your first quiz to get started!</p>
            <button
              className="btn btn-primary"
              onClick={() => setShowCreateModal(true)}
            >
              <i className="fas fa-plus"></i> Create Your First Quiz
            </button>
          </div>
        )}
      </div>

      {/* Quiz Creator Modal */}
      {showCreateModal && (
        <QuizCreator
          onClose={closeModals}
          onSave={(newQuiz) => {
            setQuizzes([...quizzes, newQuiz]);
            showNotification('Quiz created successfully!', 'success');
            closeModals();
          }}
        />
      )}

      {/* Quiz Preview Modal */}
      {showPreviewModal && selectedQuiz && (
        <QuizPreview
          quiz={selectedQuiz}
          onClose={closeModals}
        />
      )}

      {/* Quiz Edit Modal */}
      {showEditModal && selectedQuiz && (
        <QuizCreator
          quizId={selectedQuiz.id}
          onClose={closeModals}
          onSave={handleQuizUpdated}
        />
      )}
    </div>
  );
};

export default TutorQuizManager;