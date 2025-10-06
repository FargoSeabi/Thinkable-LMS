import React, { useState } from 'react';
import { Quiz, Question } from '../../types/quiz';
import './QuizPreview.css';

interface QuizPreviewProps {
  quiz: Quiz;
  onClose: () => void;
}

const QuizPreview: React.FC<QuizPreviewProps> = ({ quiz, onClose }) => {
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [showAnswers, setShowAnswers] = useState(false);

  const currentQuestion = quiz.questions[currentQuestionIndex];

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Unknown';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const nextQuestion = () => {
    if (currentQuestionIndex < quiz.questions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
    }
  };

  const prevQuestion = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(currentQuestionIndex - 1);
    }
  };

  const goToQuestion = (index: number) => {
    setCurrentQuestionIndex(index);
  };

  return (
    <div className="quiz-preview-overlay">
      <div className="quiz-preview-modal">
        <div className="quiz-preview-header">
          <div className="quiz-info">
            <h2>
              <i className="fas fa-eye"></i>
              Quiz Preview: {quiz.title}
            </h2>
            <div className="quiz-meta">
              <span className="meta-item">
                <i className="fas fa-question-circle"></i>
                {quiz.questions.length} Questions
              </span>
              <span className="meta-item">
                <i className="fas fa-calendar"></i>
                Created {formatDate(quiz.createdAt)}
              </span>
              {quiz.aiGenerated && (
                <span className="meta-item ai-badge">
                  <i className="fas fa-robot"></i>
                  AI Generated
                </span>
              )}
            </div>
          </div>
          <div className="preview-controls">
            <button
              className={`toggle-answers ${showAnswers ? 'active' : ''}`}
              onClick={() => setShowAnswers(!showAnswers)}
              title="Toggle correct answers visibility"
            >
              <i className="fas fa-lightbulb"></i>
              {showAnswers ? 'Hide Answers' : 'Show Answers'}
            </button>
            <button className="close-btn" onClick={onClose}>
              <i className="fas fa-times"></i>
            </button>
          </div>
        </div>

        <div className="quiz-preview-body">
          {/* Question Navigation */}
          <div className="question-navigation">
            <div className="nav-info">
              Question {currentQuestionIndex + 1} of {quiz.questions.length}
            </div>
            <div className="nav-buttons">
              <button
                className="nav-btn"
                onClick={prevQuestion}
                disabled={currentQuestionIndex === 0}
              >
                <i className="fas fa-chevron-left"></i>
                Previous
              </button>
              <button
                className="nav-btn"
                onClick={nextQuestion}
                disabled={currentQuestionIndex === quiz.questions.length - 1}
              >
                Next
                <i className="fas fa-chevron-right"></i>
              </button>
            </div>
          </div>

          {/* Current Question */}
          <div className="question-display">
            <div className="question-header">
              <h3>
                <span className="question-number">Q{currentQuestionIndex + 1}.</span>
                {currentQuestion.question}
              </h3>
            </div>

            <div className="options-list">
              {currentQuestion.options.map((option, index) => {
                const isCorrect = (currentQuestion.correctAnswer || currentQuestion.correctOption) === index;

                return (
                  <div
                    key={index}
                    className={`option-item ${showAnswers && isCorrect ? 'correct-answer' : ''}`}
                  >
                    <div className="option-label">
                      {String.fromCharCode(65 + index)}
                    </div>
                    <div className="option-text">
                      {option}
                    </div>
                    {showAnswers && isCorrect && (
                      <div className="correct-indicator">
                        <i className="fas fa-check-circle"></i>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Question Grid Navigator */}
          <div className="question-grid">
            <h4>Quick Navigation</h4>
            <div className="grid-buttons">
              {quiz.questions.map((_, index) => (
                <button
                  key={index}
                  className={`grid-btn ${index === currentQuestionIndex ? 'active' : ''}`}
                  onClick={() => goToQuestion(index)}
                >
                  {index + 1}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="quiz-preview-footer">
          <div className="quiz-summary">
            <strong>Quiz Summary:</strong> {quiz.questions.length} questions ready for students
          </div>
          <div className="footer-actions">
            <button className="btn btn-secondary" onClick={onClose}>
              <i className="fas fa-times"></i>
              Close Preview
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuizPreview;