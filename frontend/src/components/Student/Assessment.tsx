import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import { useAdaptiveUI } from '../../contexts/AdaptiveUIContext';
import LoadingSpinner from '../Common/LoadingSpinner';
import FontTest from './FontTest';
import { assessmentAPI } from '../../services/api';
import './Assessment.css';

interface AssessmentQuestion {
  id: number;
  category: string;
  subcategory: string;
  questionText: string;
  questionType: 'likert' | 'binary' | 'font_test';
  options?: string[];
  scoringWeight: number;
}

interface AssessmentSession {
  sessionId: string;
  userId: number;
  questions: AssessmentQuestion[];
  currentStep: number;
  totalSteps: number;
}

interface AssessmentResponse {
  questionId: number;
  response: number | boolean | string;
}

interface AssessmentResults {
  attentionScore: number;
  socialCommunicationScore: number;
  sensoryProcessingScore: number;
  readingDifficultyScore: number;
  motorSkillsScore: number;
  recommendedPreset: string;
  aiRecommendations?: string;
}

const Assessment: React.FC = () => {
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const { applyAssessmentPreset } = useAdaptiveUI();
  const navigate = useNavigate();
  
  const [currentStep, setCurrentStep] = useState<'intro' | 'font-test' | 'questions' | 'results'>('intro');
  const [session, setSession] = useState<AssessmentSession | null>(null);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [responses, setResponses] = useState<AssessmentResponse[]>([]);
  const [fontTestResults, setFontTestResults] = useState<any>(null);
  const [results, setResults] = useState<AssessmentResults | null>(null);
  const [loading, setLoading] = useState(false);

  const startAssessment = async () => {
    try {
      setLoading(true);
      const response = await assessmentAPI.startAssessment(user?.id || 0);
      setSession(response);
      setCurrentStep('font-test');
    } catch (error) {
      console.error('Failed to start assessment:', error);
      showNotification('Failed to start assessment. Please try again.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleFontTestComplete = async (fontResults: any) => {
    try {
      setLoading(true);
      const requestData = {
        fontResponses: fontResults.fontResponses
      };
      
      await assessmentAPI.submitFontTest(user?.id || 0, requestData);
      setFontTestResults(fontResults);
      setCurrentStep('questions');
    } catch (error) {
      console.error('Failed to submit font test:', error);
      showNotification('Failed to submit font test results.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleQuestionResponse = (response: number | boolean | string) => {
    if (!session || !session.questions || session.questions.length === 0) return;

    const currentQuestion = session.questions[currentQuestionIndex];
    if (!currentQuestion) return;

    const newResponse: AssessmentResponse = {
      questionId: currentQuestion.id,
      response: response
    };

    const updatedResponses = responses.filter(r => r.questionId !== currentQuestion.id);
    updatedResponses.push(newResponse);
    setResponses(updatedResponses);

    // Move to next question or finish
    if (currentQuestionIndex < session.questions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
    } else {
      submitAssessment(updatedResponses);
    }
  };

  const submitAssessment = async (finalResponses: AssessmentResponse[]) => {
    try {
      setLoading(true);
      
      // Transform responses array into a map as expected by backend
      const responsesMap: Record<string, any> = {};
      finalResponses.forEach(response => {
        responsesMap[response.questionId.toString()] = response.response;
      });
      
      const response = await assessmentAPI.submitAssessment(user?.id || 0, {
        responses: responsesMap,
        fontTestResults: fontTestResults
      });
      
      console.log('Assessment response received:', response);
      
      // Extract the assessment data from the nested response
      const assessmentData = response.assessment;
      console.log('Assessment data:', assessmentData);
      
      setResults(assessmentData);
      setCurrentStep('results');
      
      // Apply the adaptive UI preset based on assessment results
      applyAssessmentPreset(assessmentData);
      
      showNotification('Assessment completed successfully!', 'success');
    } catch (error) {
      console.error('Failed to submit assessment:', error);
      showNotification('Failed to submit assessment. Please try again.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const renderLikertScale = (question: AssessmentQuestion) => {
    const currentResponse = responses.find(r => r.questionId === question.id);
    
    return (
      <div className="likert-scale">
        <div className="scale-labels">
          <span>Strongly Disagree</span>
          <span>Strongly Agree</span>
        </div>
        <div className="scale-options">
          {[1, 2, 3, 4, 5].map(value => (
            <button
              key={value}
              className={`scale-option ${currentResponse?.response === value ? 'selected' : ''}`}
              onClick={() => handleQuestionResponse(value)}
            >
              {value}
            </button>
          ))}
        </div>
      </div>
    );
  };

  const renderBinaryChoice = (question: AssessmentQuestion) => {
    const currentResponse = responses.find(r => r.questionId === question.id);
    
    return (
      <div className="binary-choice">
        <button
          className={`choice-btn ${currentResponse?.response === true ? 'selected' : ''}`}
          onClick={() => handleQuestionResponse(true)}
        >
          Yes
        </button>
        <button
          className={`choice-btn ${currentResponse?.response === false ? 'selected' : ''}`}
          onClick={() => handleQuestionResponse(false)}
        >
          No
        </button>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="assessment-container">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="assessment-container">
      {currentStep === 'intro' && (
        <div className="assessment-intro">
          <div className="intro-card">
            <div className="assessment-header">
              <div className="assessment-icon">
                <i className="fas fa-user-cog"></i>
              </div>
              <h2>Personalize Your Learning Experience</h2>
            </div>
            <p className="intro-text">
              This brief assessment helps us understand your learning preferences and optimize 
              ThinkAble for your individual needs. The assessment includes:
            </p>
            
            <ul className="features-list">
              <li>
                <i className="fas fa-font"></i>
                <span>Font readability testing to find the best fonts for you</span>
              </li>
              <li>
                <i className="fas fa-brain"></i>
                <span>Questions about attention, focus, and learning preferences</span>
              </li>
              <li>
                <i className="fas fa-palette"></i>
                <span>Customized interface based on your responses</span>
              </li>
              <li>
                <i className="fas fa-chart-line"></i>
                <span>Personalized learning recommendations</span>
              </li>
            </ul>
            
            <div className="assessment-details">
              <div className="detail-item">
                <i className="fas fa-clock"></i>
                <span>Takes about 5-7 minutes</span>
              </div>
              <div className="detail-item">
                <i className="fas fa-shield-alt"></i>
                <span>Your responses are private and secure</span>
              </div>
              <div className="detail-item">
                <i className="fas fa-undo"></i>
                <span>You can retake this anytime</span>
              </div>
            </div>

            <button onClick={startAssessment} className="start-assessment-btn">
              <i className="fas fa-play"></i>
              Start Assessment
            </button>
          </div>
        </div>
      )}

      {currentStep === 'font-test' && (
        <FontTest onComplete={handleFontTestComplete} />
      )}

      {currentStep === 'questions' && session && (
        <div className="questions-section">
          {session.questions && session.questions.length > 0 ? (
            <>
              <div className="progress-header">
                <div className="progress-bar-container">
                  <div 
                    className="progress-bar" 
                    style={{ width: `${((currentQuestionIndex + 1) / session.questions.length) * 100}%` }}
                  ></div>
                </div>
                <span className="progress-text">
                  Question {currentQuestionIndex + 1} of {session.questions.length}
                </span>
              </div>

              <div className="question-card">
                <div className="question-category">
                  {session.questions[currentQuestionIndex]?.category?.replace(/([A-Z])/g, ' $1').trim() || 'Assessment'}
                </div>
                
                <h3 className="question-text">
                  {session.questions[currentQuestionIndex]?.questionText || 'Loading question...'}
                </h3>

                <div className="question-response">
                  {session.questions[currentQuestionIndex]?.questionType === 'likert' && 
                    renderLikertScale(session.questions[currentQuestionIndex])}
                  {session.questions[currentQuestionIndex]?.questionType === 'binary' && 
                    renderBinaryChoice(session.questions[currentQuestionIndex])}
                </div>

                {currentQuestionIndex > 0 && (
                  <button 
                    onClick={() => setCurrentQuestionIndex(currentQuestionIndex - 1)}
                    className="back-btn"
                  >
                    <i className="fas fa-arrow-left"></i>
                    Previous
                  </button>
                )}
              </div>
            </>
          ) : (
            <div className="no-questions-message">
              <div className="question-card">
                <h3>🎉 Assessment Complete!</h3>
                <p>You've completed the font test successfully. Since no additional questions are needed at this time, let's proceed to your personalized results.</p>
                <button 
                  onClick={() => submitAssessment([])}
                  className="continue-btn"
                >
                  <i className="fas fa-arrow-right"></i>
                  View Results
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {currentStep === 'results' && results && (
        <div className="results-section">
          <div className="results-card">
            <h2>🎉 Assessment Complete!</h2>
            
            <div className="profile-summary">
              <h3>Your Learning Profile</h3>
              <div className="preset-badge">
                Recommended UI: <strong>{results.recommendedPreset ? results.recommendedPreset.replace(/([A-Z])/g, ' $1').trim() : 'Standard'}</strong>
              </div>
            </div>

            <div className="scores-grid">
              <div className="score-item">
                <div className="score-label">Attention Support</div>
                <div className="score-bar">
                  <div 
                    className="score-fill" 
                    style={{ width: `${(results.attentionScore / 25) * 100}%` }}
                  ></div>
                </div>
                <span className="score-value">{results.attentionScore}/25</span>
              </div>

              <div className="score-item">
                <div className="score-label">Reading Support</div>
                <div className="score-bar">
                  <div 
                    className="score-fill" 
                    style={{ width: `${(results.readingDifficultyScore / 25) * 100}%` }}
                  ></div>
                </div>
                <span className="score-value">{results.readingDifficultyScore}/25</span>
              </div>

              <div className="score-item">
                <div className="score-label">Social Communication</div>
                <div className="score-bar">
                  <div 
                    className="score-fill" 
                    style={{ width: `${(results.socialCommunicationScore / 25) * 100}%` }}
                  ></div>
                </div>
                <span className="score-value">{results.socialCommunicationScore}/25</span>
              </div>

              <div className="score-item">
                <div className="score-label">Sensory Processing</div>
                <div className="score-bar">
                  <div 
                    className="score-fill" 
                    style={{ width: `${(results.sensoryProcessingScore / 25) * 100}%` }}
                  ></div>
                </div>
                <span className="score-value">{results.sensoryProcessingScore}/25</span>
              </div>
            </div>

            {results.aiRecommendations && (
              <div className="ai-recommendations">
                <h4>Personalized Recommendations</h4>
                <div className="recommendations-text">
                  {results.aiRecommendations}
                </div>
              </div>
            )}

            <div className="results-actions">
              <button 
                onClick={() => {
                  // Set success message for dashboard
                  localStorage.setItem('showAssessmentSuccess', 'true');
                  localStorage.setItem('assessmentCompleted', 'true');
                  // Navigate with smooth transition
                  navigate('/student');
                }}
                className="continue-btn"
              >
                <i className="fas fa-arrow-right"></i>
                Continue to Dashboard
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Assessment;