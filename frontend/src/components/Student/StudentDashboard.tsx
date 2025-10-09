import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useAdaptiveUI } from '../../contexts/AdaptiveUIContext';
import { useTimer } from '../../contexts/TimerContext';
import { studentAPI } from '../../services/api';
import WelcomeFlow from './WelcomeFlow';
import PageTransition from '../Common/PageTransition';
import './Dashboard.css';
import '../Dashboard/Dashboard.css';

const StudentDashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const { currentPreset } = useAdaptiveUI();
  const { startStudySession, toggleTimer, showTimer } = useTimer();
  const navigate = useNavigate();
  
  const [assessmentCompleted, setAssessmentCompleted] = useState(false);
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [showWelcomeFlow, setShowWelcomeFlow] = useState(false);
  const [favoriteContent, setFavoriteContent] = useState<any[]>([]);
  const [loadingFavorites, setLoadingFavorites] = useState(false);

  useEffect(() => {
    checkAssessmentStatus();
    checkForSuccessMessage();
    checkFirstTimeUser();
    loadFavorites();
  }, [user]);

  const loadFavorites = async () => {
    if (!user?.id) return;
    
    try {
      setLoadingFavorites(true);
      const response = await studentAPI.getFavorites(user.id);
      setFavoriteContent(response.favorites.slice(0, 3)); // Show only 3 recent favorites
    } catch (error) {
      console.error('Error loading favorites:', error);
    } finally {
      setLoadingFavorites(false);
    }
  };

  const checkAssessmentStatus = () => {
    const completed = localStorage.getItem('assessmentCompleted') === 'true';
    setAssessmentCompleted(completed);
  };

  const checkForSuccessMessage = () => {
    const showSuccess = localStorage.getItem('showAssessmentSuccess') === 'true';
    if (showSuccess) {
      setShowSuccessMessage(true);
      localStorage.removeItem('showAssessmentSuccess');
      
      // Hide success message after 5 seconds
      setTimeout(() => {
        setShowSuccessMessage(false);
      }, 5000);
    }
  };

  const checkFirstTimeUser = () => {
    const hasVisited = localStorage.getItem('hasVisitedDashboard');
    const assessmentSkipped = localStorage.getItem('assessmentSkipped');
    const assessmentCompleted = localStorage.getItem('assessmentCompleted');
    
    // Show welcome flow for first-time users who haven't done assessment
    if (!hasVisited && !assessmentSkipped && !assessmentCompleted) {
      setShowWelcomeFlow(true);
    }
    
    // Mark as visited
    localStorage.setItem('hasVisitedDashboard', 'true');
  };

  const handleWelcomeComplete = () => {
    setShowWelcomeFlow(false);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getPresetName = (preset: string) => {
    const presetNames: Record<string, string> = {
      'standard': 'Standard Mode',
      'adhd': 'ADHD Support Mode',
      'dyslexia': 'Dyslexia Support Mode', 
      'autism': 'Autism Support Mode',
      'sensory': 'Sensory-Friendly Mode',
      'dyslexia-adhd': 'Combined Support Mode',
      'STANDARD_ADAPTIVE': 'Standard Mode',
      'FOCUS_ENHANCED': 'Focus Enhanced Mode',
      'FOCUS_CALM': 'Focus Calm Mode',
      'READING_SUPPORT': 'Reading Support Mode',
      'SOCIAL_SIMPLE': 'Social Simple Mode',
      'SENSORY_CALM': 'Sensory Calm Mode'
    };
    return presetNames[preset] || 'Standard Mode';
  };



  const handleStartFocusSession = () => {
    startStudySession();
    if (!showTimer) {
      toggleTimer();
    }
  };


  return (
    <PageTransition>
      <div className="dashboard-container no-header">

      {/* Success Message */}
      {showSuccessMessage && (
        <div className="success-message">
          <h3><i className="fas fa-star"></i> Assessment Complete!</h3>
          <p>Your learning environment has been personalized. Enjoy your optimized ThinkAble experience!</p>
        </div>
      )}

      {/* Assessment Status Banner - Only show if assessment not completed */}
      {!assessmentCompleted && (
        <div className="assessment-status-banner assessment-incomplete">
          <div className="banner-content">
            <h3><i className="fas fa-brain"></i> Personalize Your Learning Experience</h3>
            <p>Take our quick 5-minute assessment to unlock features tailored just for you!</p>
            <Link to="/student/assessment" className="banner-btn">
              <i className="fas fa-magic"></i> Start Assessment
            </Link>
          </div>
        </div>
      )}

      <div className="dashboard-main">
        <div className="dashboard-grid">
          <div className="dashboard-card lesson-card">
            <div className="card-icon">
              <i className="fas fa-book-open"></i>
            </div>
            <h3>Lessons</h3>
            <p>Learn new concepts with interactive lessons and quizzes.</p>
            <Link to="/student/content" className="card-button">
              Start Learning
            </Link>
          </div>

          <div className="dashboard-card progress-card">
            <div className="card-icon">
              <i className="fas fa-chart-line"></i>
            </div>
            <h3>Progress</h3>
            <p>Track your learning journey and accessibility insights.</p>
            <Link to="/student/progress" className="card-button">
              View Progress
            </Link>
          </div>

          <div className="dashboard-card study-timer-card">
            <div className="card-icon">
              <i className="fas fa-clock"></i>
            </div>
            <h3>Study Timer</h3>
            <p>Personalized focus sessions based on your learning profile.</p>
            <div className="card-badge new-badge">Smart!</div>
            <button className="card-button" onClick={handleStartFocusSession}>
              Start Focus Session
            </button>
          </div>

          <div className="dashboard-card favorites-card">
            <div className="card-icon">
              <i className="fas fa-heart"></i>
            </div>
            <h3>Your Favorites</h3>
            {loadingFavorites ? (
              <p>Loading your favorite content...</p>
            ) : favoriteContent.length > 0 ? (
              <div className="favorites-preview">
                <p>{favoriteContent.length} favorite{favoriteContent.length !== 1 ? 's' : ''} saved</p>
                <div className="favorites-list">
                  {favoriteContent.map((content, index) => (
                    <div key={content.id} className="favorite-item">
                      <i className="fas fa-bookmark"></i>
                      <span>{content.title.length > 25 ? content.title.substring(0, 25) + '...' : content.title}</span>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <p>No favorites yet. Start by hearting content you love!</p>
            )}
            <Link 
              to={favoriteContent.length > 0 ? "/student/content?favorites=true" : "/student/content"} 
              className="card-button"
            >
              {favoriteContent.length > 0 ? 'View All Favorites' : 'Discover Content'}
            </Link>
          </div>

          {!assessmentCompleted && (
            <div className="dashboard-card assessment-card">
              <div className="card-icon">
                <i className="fas fa-brain"></i>
              </div>
              <h3>Learning Assessment</h3>
              <p>Personalize your learning experience with our adaptive assessment.</p>
              <div className="card-badge new-badge">New!</div>
              <Link to="/student/assessment" className="card-button">
                Take Assessment
              </Link>
            </div>
          )}
        </div>



        {/* Activity Tracker Modal */}

        {/* Welcome Flow for First-Time Users */}
        {showWelcomeFlow && (
          <WelcomeFlow onComplete={handleWelcomeComplete} />
        )}
        </div>

      </div>
    </PageTransition>
  );
};

export default StudentDashboard;