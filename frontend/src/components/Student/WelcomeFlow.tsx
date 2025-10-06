import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import './WelcomeFlow.css';

interface WelcomeFlowProps {
  onComplete: () => void;
}

const WelcomeFlow: React.FC<WelcomeFlowProps> = ({ onComplete }) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [isAnimating, setIsAnimating] = useState(false);
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const navigate = useNavigate();

  const steps = [
    {
      title: "Welcome to ThinkAble!",
      subtitle: `Hi ${user?.firstName || 'there'}! 👋`,
      content: "ThinkAble is designed to support your unique learning style. We'll help you create the perfect learning environment.",
      icon: "fas fa-star",
      color: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
    },
    {
      title: "Personalized Learning",
      subtitle: "Every learner is different",
      content: "Our platform adapts to support ADHD, dyslexia, autism, and other learning differences. We'll customize everything just for you.",
      icon: "fas fa-brain",
      color: "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)"
    },
    {
      title: "Quick Assessment",
      subtitle: "5 minutes to unlock your potential",
      content: "Take our brief assessment to get personalized interface settings, timer recommendations, and learning optimizations.",
      icon: "fas fa-magic",
      color: "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)"
    }
  ];

  const handleNext = () => {
    if (currentStep < steps.length) {
      setIsAnimating(true);
      setTimeout(() => {
        setCurrentStep(currentStep + 1);
        setIsAnimating(false);
      }, 300);
    }
  };

  const handleStartAssessment = () => {
    setIsAnimating(true);
    setTimeout(() => {
      onComplete();
      navigate('/student/assessment');
    }, 300);
  };

  const handleSkip = () => {
    setIsAnimating(true);
    setTimeout(() => {
      // Mark as skipped for now, can take assessment later
      localStorage.setItem('assessmentSkipped', 'true');
      onComplete();
      showNotification('You can take the assessment anytime from your dashboard!', 'info');
    }, 300);
  };

  return (
    <div className="welcome-flow-overlay">
      <div className={`welcome-flow-container ${isAnimating ? 'animating' : ''}`}>
        <div className="welcome-progress">
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ width: `${(currentStep / steps.length) * 100}%` }}
            />
          </div>
          <span className="progress-text">Step {currentStep} of {steps.length}</span>
        </div>

        <div className="welcome-card">
          <div 
            className="welcome-icon" 
            style={{ background: steps[currentStep - 1].color }}
          >
            <i className={steps[currentStep - 1].icon}></i>
          </div>

          <div className="welcome-content">
            <h1>{steps[currentStep - 1].title}</h1>
            <h2>{steps[currentStep - 1].subtitle}</h2>
            <p>{steps[currentStep - 1].content}</p>
          </div>

          <div className="welcome-actions">
            {currentStep < steps.length ? (
              <>
                <button 
                  className="welcome-btn secondary" 
                  onClick={handleSkip}
                >
                  Skip for now
                </button>
                <button 
                  className="welcome-btn primary" 
                  onClick={handleNext}
                >
                  Continue
                </button>
              </>
            ) : (
              <>
                <button 
                  className="welcome-btn secondary" 
                  onClick={handleSkip}
                >
                  Skip Assessment
                </button>
                <button 
                  className="welcome-btn primary" 
                  onClick={handleStartAssessment}
                >
                  <i className="fas fa-magic"></i>
                  Start Assessment
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default WelcomeFlow;