import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import config from '../../services/config';
import { Quiz, Question, LearningContent } from '../../types/quiz';
import './QuizCreator.css';

interface QuizCreatorProps {
  quizId?: number;
  onClose: () => void;
  onSave: (quiz: Quiz) => void;
}

const QuizCreator: React.FC<QuizCreatorProps> = ({ quizId, onClose, onSave }) => {
  const { user } = useAuth();
  const [quiz, setQuiz] = useState<Quiz>({
    title: '',
    questions: [{
      tempId: '1',
      question: '',
      options: ['', '', '', ''],
      correctAnswer: 0
    }]
  });
  
  const [learningContent, setLearningContent] = useState<LearningContent[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState<{[key: string]: string}>({});
  const [notification, setNotification] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    fetchLearningContent();
    if (quizId) {
      fetchQuiz(quizId);
    }
  }, [quizId]);

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

  const fetchQuiz = async (id: number) => {
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE_URL}/api/quiz/${id}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      const data = await response.json();
      if (data.success && data.quiz) {
        setQuiz({
          id: data.quiz.id,
          title: data.quiz.title,
          learningContentId: data.quiz.learningContentId,
          questions: data.quiz.questions.map((q: any) => ({
            id: q.id,
            question: q.question,
            options: q.options,
            correctAnswer: q.correctOption || q.correctAnswer
          }))
        });
      }
    } catch (error) {
      console.error('Error fetching quiz:', error);
      showNotification('Failed to load quiz', 'error');
    } finally {
      setLoading(false);
    }
  };

  const addQuestion = () => {
    const newQuestion: Question = {
      tempId: Date.now().toString(),
      question: '',
      options: ['', '', '', ''],
      correctAnswer: 0
    };
    
    setQuiz(prev => ({
      ...prev,
      questions: [...prev.questions, newQuestion]
    }));
  };

  const removeQuestion = (index: number) => {
    if (quiz.questions.length > 1) {
      setQuiz(prev => ({
        ...prev,
        questions: prev.questions.filter((_, i) => i !== index)
      }));
    }
  };

  const updateQuestion = (index: number, field: keyof Question, value: any) => {
    setQuiz(prev => ({
      ...prev,
      questions: prev.questions.map((q, i) => 
        i === index ? { ...q, [field]: value } : q
      )
    }));
    
    // Clear errors for this field
    setErrors(prev => ({
      ...prev,
      [`question_${index}_${field}`]: ''
    }));
  };

  const updateOption = (questionIndex: number, optionIndex: number, value: string) => {
    setQuiz(prev => ({
      ...prev,
      questions: prev.questions.map((q, i) => 
        i === questionIndex 
          ? { ...q, options: q.options.map((opt, oi) => oi === optionIndex ? value : opt) }
          : q
      )
    }));
  };

  const validateQuiz = (): boolean => {
    const newErrors: {[key: string]: string} = {};
    
    // Validate title
    if (!quiz.title.trim()) {
      newErrors.title = 'Quiz title is required';
    }
    
    // Validate questions
    quiz.questions.forEach((question, qIndex) => {
      if (!question.question.trim()) {
        newErrors[`question_${qIndex}_question`] = 'Question text is required';
      }
      
      // Check if all options are filled
      const emptyOptions = question.options.filter(opt => !opt.trim()).length;
      if (emptyOptions > 0) {
        newErrors[`question_${qIndex}_options`] = `${emptyOptions} option(s) are empty`;
      }
      
      // Check if correct answer option is not empty
      if (!question.options[question.correctAnswer]?.trim()) {
        newErrors[`question_${qIndex}_correct`] = 'Correct answer option cannot be empty';
      }
    });
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const saveQuiz = async () => {
    if (!validateQuiz()) {
      showNotification('Please fix all validation errors', 'error');
      return;
    }
    
    try {
      setSaving(true);
      
      const quizData = {
        title: quiz.title,
        contentId: quiz.learningContentId,
        questions: quiz.questions.map(q => ({
          question: q.question,
          options: q.options,
          correctAnswer: q.correctAnswer
        }))
      };
      
      let response;
      if (quiz.id) {
        // Update existing quiz
        response = await fetch(`${API_BASE_URL}/api/quiz/${quiz.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
          },
          body: JSON.stringify({
            title: quizData.title,
            contentId: quizData.contentId,
            questions: quizData.questions.map(q => ({
              question: q.question,
              options: q.options,
              correctAnswer: q.correctAnswer
            }))
          }),
        });
      } else {
        // Create new quiz
        response = await fetch(`${API_BASE_URL}/api/quiz/create`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
          },
          body: JSON.stringify({
            title: quizData.title,
            contentId: quizData.contentId,
            questions: quizData.questions.map(q => ({
              question: q.question,
              options: q.options,
              correctAnswer: q.correctAnswer
            }))
          }),
        });
      }
      
      const data = await response.json();
      if (data.success) {
        showNotification(
          quiz.id ? 'Quiz updated successfully!' : 'Quiz created successfully!', 
          'success'
        );
        onSave(data.quiz);
        setTimeout(() => onClose(), 2000);
      } else {
        showNotification(data.message || 'Failed to save quiz', 'error');
      }
    } catch (error) {
      console.error('Error saving quiz:', error);
      showNotification('Failed to save quiz', 'error');
    } finally {
      setSaving(false);
    }
  };

  const showNotification = (message: string, type: 'success' | 'error') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 5000);
  };

  if (loading) {
    return (
      <div className="quiz-creator-loading">
        <div className="loading-spinner"></div>
        <p>Loading quiz...</p>
      </div>
    );
  }

  return (
    <div className="quiz-creator">
      <div className="quiz-creator-header">
        <div className="header-content">
          <h1>{quiz.id ? '✏️ Edit Quiz' : '➕ Create New Quiz'}</h1>
          <p>Design engaging quizzes for your students</p>
        </div>
        <button className="close-btn" onClick={onClose}>
          <i className="fas fa-times"></i>
        </button>
      </div>

      {/* Notification */}
      {notification && (
        <div className={`notification ${notification.type}`}>
          <span>{notification.message}</span>
          <button onClick={() => setNotification(null)}>×</button>
        </div>
      )}

      <div className="quiz-creator-content">
        {/* Quiz Details */}
        <div className="quiz-details-section">
          <h2>📋 Quiz Details</h2>
          
          <div className="form-group">
            <label htmlFor="quiz-title">Quiz Title *</label>
            <input
              id="quiz-title"
              type="text"
              placeholder="Enter a compelling quiz title..."
              value={quiz.title}
              onChange={(e) => {
                setQuiz(prev => ({ ...prev, title: e.target.value }));
                setErrors(prev => ({ ...prev, title: '' }));
              }}
              className={errors.title ? 'error' : ''}
            />
            {errors.title && <span className="error-text">{errors.title}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="quiz-content">Link to Learning Content (Optional)</label>
            <select
              id="quiz-content"
              value={quiz.learningContentId || ''}
              onChange={(e) => setQuiz(prev => ({ ...prev, learningContentId: e.target.value ? parseInt(e.target.value) : undefined }))}
            >
              <option value="">Select content (optional)</option>
              {learningContent.map(content => (
                <option key={content.id} value={content.id}>
                  {content.title} ({content.contentType})
                </option>
              ))}
            </select>
            <small className="form-help">Link this quiz to specific learning content</small>
          </div>
        </div>

        {/* Questions Section */}
        <div className="questions-section">
          <div className="section-header">
            <h2>❓ Questions ({quiz.questions.length})</h2>
            <button 
              className="btn btn-primary"
              onClick={addQuestion}
            >
              <i className="fas fa-plus"></i> Add Question
            </button>
          </div>

          <div className="questions-list">
            {quiz.questions.map((question, qIndex) => (
              <div key={question.id || question.tempId} className="question-card">
                <div className="question-header">
                  <span className="question-number">Question {qIndex + 1}</span>
                  {quiz.questions.length > 1 && (
                    <button 
                      className="remove-question-btn"
                      onClick={() => removeQuestion(qIndex)}
                      title="Delete question"
                    >
                      <i className="fas fa-trash"></i>
                    </button>
                  )}
                </div>

                <div className="form-group">
                  <label>Question Text *</label>
                  <textarea
                    placeholder="Enter your question here..."
                    value={question.question}
                    onChange={(e) => updateQuestion(qIndex, 'question', e.target.value)}
                    className={errors[`question_${qIndex}_question`] ? 'error' : ''}
                    rows={3}
                  />
                  {errors[`question_${qIndex}_question`] && 
                    <span className="error-text">{errors[`question_${qIndex}_question`]}</span>
                  }
                </div>

                <div className="options-section">
                  <label>Answer Options *</label>
                  {errors[`question_${qIndex}_options`] && 
                    <span className="error-text">{errors[`question_${qIndex}_options`]}</span>
                  }
                  
                  <div className="options-list">
                    {question.options.map((option, oIndex) => (
                      <div key={oIndex} className="option-item">
                        <div className="option-input-group">
                          <input
                            type="radio"
                            name={`correct_${qIndex}`}
                            checked={question.correctAnswer === oIndex}
                            onChange={() => updateQuestion(qIndex, 'correctAnswer', oIndex)}
                            className="correct-option-radio"
                          />
                          <span className="option-label">Option {String.fromCharCode(65 + oIndex)}</span>
                          <input
                            type="text"
                            placeholder={`Enter option ${String.fromCharCode(65 + oIndex)}`}
                            value={option}
                            onChange={(e) => updateOption(qIndex, oIndex, e.target.value)}
                            className="option-input"
                          />
                        </div>
                        {question.correctAnswer === oIndex && (
                          <span className="correct-indicator">✓ Correct Answer</span>
                        )}
                      </div>
                    ))}
                  </div>
                  
                  {errors[`question_${qIndex}_correct`] && 
                    <span className="error-text">{errors[`question_${qIndex}_correct`]}</span>
                  }
                  
                  <small className="form-help">
                    Click the radio button to mark the correct answer
                  </small>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Preview Section */}
        <div className="preview-section">
          <h2>👁️ Quiz Preview</h2>
          <div className="quiz-preview">
            <div className="preview-header">
              <h3>{quiz.title || 'Untitled Quiz'}</h3>
              <p>{quiz.questions.length} question{quiz.questions.length !== 1 ? 's' : ''}</p>
            </div>
            
            {quiz.questions.map((question, index) => (
              <div key={question.id || question.tempId} className="preview-question">
                <h4>Q{index + 1}: {question.question || 'Question text will appear here...'}</h4>
                <div className="preview-options">
                  {question.options.map((option, oIndex) => (
                    <div 
                      key={oIndex} 
                      className={`preview-option ${question.correctAnswer === oIndex ? 'correct' : ''}`}
                    >
                      <span className="option-letter">{String.fromCharCode(65 + oIndex)}</span>
                      <span className="option-text">{option || `Option ${String.fromCharCode(65 + oIndex)}`}</span>
                      {question.correctAnswer === oIndex && <span className="correct-mark">✓</span>}
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Actions */}
        <div className="quiz-creator-actions">
          <button 
            className="btn btn-outline"
            onClick={onClose}
          >
            Cancel
          </button>
          <button 
            className="btn btn-primary"
            onClick={saveQuiz}
            disabled={saving}
          >
            {saving ? (
              <>
                <div className="btn-spinner"></div>
                Saving...
              </>
            ) : (
              <>
                <i className="fas fa-save"></i>
                {quiz.id ? 'Update Quiz' : 'Create Quiz'}
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default QuizCreator;