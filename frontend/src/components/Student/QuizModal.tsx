import React, { useState, useEffect } from 'react';
import axios from 'axios';
import config from '../../services/config';
import './QuizModal.css';

interface Quiz {
  id: number;
  title: string;
  questions: Question[];
  learningContentId: number;
}

interface Question {
  id: number;
  question: string;
  options: string[];
  correctOption: number;
}

interface QuizModalProps {
  quiz: Quiz;
  contentId: number;
  studentEmail: string;
  onClose: () => void;
}

interface QuizResult {
  score: number;
  totalQuestions: number;
  correctAnswers: number;
  passed: boolean;
  message: string;
}

const QuizModal: React.FC<QuizModalProps> = ({ quiz, contentId, studentEmail, onClose }) => {
  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [answers, setAnswers] = useState<{ [key: number]: number }>({});
  const [timeRemaining, setTimeRemaining] = useState(30 * 60); // 30 minutes default
  const [quizStartTime] = useState(Date.now());
  const [showResults, setShowResults] = useState(false);
  const [results, setResults] = useState<QuizResult | null>(null);
  const [loading, setLoading] = useState(false);

  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    if (timeRemaining > 0 && !showResults) {
      const timer = setTimeout(() => setTimeRemaining(timeRemaining - 1), 1000);
      return () => clearTimeout(timer);
    } else if (timeRemaining === 0 && !showResults) {
      handleSubmitQuiz();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [timeRemaining, showResults]);

  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  const handleAnswerSelect = (questionIndex: number, optionIndex: number) => {
    setAnswers(prev => ({
      ...prev,
      [questionIndex]: optionIndex
    }));
  };

  const handleNextQuestion = () => {
    if (currentQuestion < quiz.questions.length - 1) {
      setCurrentQuestion(currentQuestion + 1);
    }
  };

  const handlePreviousQuestion = () => {
    if (currentQuestion > 0) {
      setCurrentQuestion(currentQuestion - 1);
    }
  };

  const handleSubmitQuiz = async () => {
    setLoading(true);
    try {
      const timeSpent = Math.round((Date.now() - quizStartTime) / 1000);
      
      // Convert answers to the format expected by backend
      const formattedAnswers: { [key: string]: number } = {};
      Object.entries(answers).forEach(([questionIndex, selectedOption]) => {
        formattedAnswers[questionIndex] = selectedOption;
      });
      
      const submission = {
        answers: formattedAnswers
      };

      const response = await axios.post(
        `${API_BASE_URL}/api/student/content/${contentId}/quiz/submit?studentEmail=${studentEmail}`,
        submission
      );

      setResults(response.data);
      setShowResults(true);
    } catch (error) {
      console.error('Error submitting quiz:', error);
    } finally {
      setLoading(false);
    }
  };

  const getProgressPercentage = () => {
    const answeredQuestions = Object.keys(answers).length;
    return (answeredQuestions / quiz.questions.length) * 100;
  };

  const isQuizComplete = () => {
    return Object.keys(answers).length === quiz.questions.length;
  };

  const handleRetakeQuiz = () => {
    setCurrentQuestion(0);
    setAnswers({});
    setTimeRemaining(30 * 60);
    setShowResults(false);
    setResults(null);
  };

  if (showResults && results) {
    return (
      <div className="quiz-modal-overlay">
        <div className="quiz-modal quiz-results-modal">
          <div className="quiz-results-header">
            <h2>Quiz Complete!</h2>
            <div className={`score-circle ${results.passed ? 'passed' : 'failed'}`}>
              <div className="score-text">
                <span className="score">{Math.round(results.score)}%</span>
                <span className="total">{results.correctAnswers}/{results.totalQuestions}</span>
              </div>
            </div>
            <p className="results-message">{results.message}</p>
          </div>

          <div className="quiz-results-actions">
            <button className="secondary-btn" onClick={handleRetakeQuiz}>
              <i className="fas fa-redo"></i>
              Retake Quiz
            </button>
            <button className="primary-btn" onClick={onClose}>
              <i className="fas fa-times"></i>
              Close
            </button>
          </div>
        </div>
      </div>
    );
  }

  const currentQ = quiz.questions[currentQuestion];

  return (
    <div className="quiz-modal-overlay">
      <div className="quiz-modal">
        <div className="quiz-header">
          <div className="quiz-info">
            <h2>{quiz.title}</h2>
          </div>
          <div className="quiz-stats">
            <div className="time-remaining">
              <i className="fas fa-clock"></i>
              <span>{formatTime(timeRemaining)}</span>
            </div>
            <div className="question-counter">
              Question {currentQuestion + 1} of {quiz.questions.length}
            </div>
          </div>
        </div>

        <div className="quiz-progress">
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ width: `${getProgressPercentage()}%` }}
            ></div>
          </div>
          <span className="progress-text">
            {Object.keys(answers).length} of {quiz.questions.length} answered
          </span>
        </div>

        <div className="quiz-question">
          <h3>{currentQ.question}</h3>
          <div className="quiz-options">
            {currentQ.options.map((option, index) => (
              <button
                key={index}
                className={`option-btn ${answers[currentQuestion] === index ? 'selected' : ''}`}
                onClick={() => handleAnswerSelect(currentQuestion, index)}
              >
                <span className="option-letter">{String.fromCharCode(65 + index)}</span>
                <span className="option-text">{option}</span>
              </button>
            ))}
          </div>
        </div>

        <div className="quiz-navigation">
          <button 
            className="nav-btn secondary-btn" 
            onClick={handlePreviousQuestion}
            disabled={currentQuestion === 0}
          >
            <i className="fas fa-chevron-left"></i>
            Previous
          </button>

          <button className="close-btn" onClick={onClose}>
            <i className="fas fa-times"></i>
            Close Quiz
          </button>

          {currentQuestion < quiz.questions.length - 1 ? (
            <button 
              className="nav-btn primary-btn" 
              onClick={handleNextQuestion}
            >
              Next
              <i className="fas fa-chevron-right"></i>
            </button>
          ) : (
            <button 
              className={`submit-btn ${isQuizComplete() ? 'ready' : 'disabled'}`}
              onClick={handleSubmitQuiz}
              disabled={!isQuizComplete() || loading}
            >
              {loading ? (
                <>
                  <i className="fas fa-spinner fa-spin"></i>
                  Submitting...
                </>
              ) : (
                <>
                  <i className="fas fa-check"></i>
                  Submit Quiz
                </>
              )}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default QuizModal;