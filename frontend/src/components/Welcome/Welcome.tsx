import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './Welcome.css';

const Welcome: React.FC = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    // Redirect authenticated users to their dashboard
    if (isAuthenticated && user) {
      switch (user.role) {
        case 'STUDENT':
          navigate('/student');
          break;
        case 'TUTOR':
          navigate('/tutor');
          break;
        case 'ADMIN':
          navigate('/admin');
          break;
        case 'TEACHER':
          navigate('/teacher');
          break;
        default:
          navigate('/login');
      }
    }
  }, [isAuthenticated, user, navigate]);

  useEffect(() => {
    // Trigger entrance animation
    const timer = setTimeout(() => setIsVisible(true), 100);
    return () => clearTimeout(timer);
  }, []);

  const features = [
    {
      icon: '🧠',
      title: 'Adaptive Learning',
      description: 'Personalized learning paths that adapt to your unique needs and learning style'
    },
    {
      icon: '♿',
      title: 'Accessibility First',
      description: 'Designed for learners with ADHD, autism, dyslexia, and other learning differences'
    },
    {
      icon: '📊',
      title: 'Progress Tracking',
      description: 'Monitor your learning journey with detailed analytics and achievements'
    },
    {
      icon: '🎯',
      title: 'Personalized Content',
      description: 'AI-powered content recommendations tailored to your interests and goals'
    },
    {
      icon: '👥',
      title: 'Expert Tutors',
      description: 'Connect with qualified tutors who understand diverse learning needs'
    },
    {
      icon: '🏆',
      title: 'Gamified Experience',
      description: 'Earn badges, unlock achievements, and celebrate your learning milestones'
    }
  ];

  return (
    <div className={`welcome-container ${isVisible ? 'visible' : ''}`}>
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-content">
          <div className="hero-text">
            <h1 className="hero-title">
              WELCOME TO <span className="brand-name">ThinkAble</span>
            </h1>
            <p className="hero-subtitle">
              Empowering every learner with Adaptive, Accessible and Personalized education
            </p>
            <p className="hero-description">
              Join thousands of students, tutors, and educators who are transforming 
              the way we learn and teach. Experience education that adapts to you.
            </p>
            <div className="hero-actions">
              <Link to="/register" className="cta-button primary">
                Get Started Free
              </Link>
              <Link to="/login" className="cta-button secondary">
                Sign in
              </Link>
            </div>
          </div>
          <div className="hero-visual">
            <div className="floating-elements">
              <div className="floating-card card-1">
                <div className="card-icon">📚</div>
                <div className="card-text">Interactive Learning</div>
              </div>
              <div className="floating-card card-2">
                <div className="card-icon">🎨</div>
                <div className="card-text">Creative Expression</div>
              </div>
              <div className="floating-card card-3">
                <div className="card-icon">🔬</div>
                <div className="card-text">Scientific Discovery</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="features-section">
        <div className="section-header">
          <h2 className="section-title">Why Choose ThinkAble?</h2>
          <p className="section-subtitle">
            Built with accessibility, adaptability, and excellence in mind
          </p>
        </div>
        <div className="features-grid">
          {features.map((feature, index) => (
            <div key={index} className="feature-card" style={{ animationDelay: `${index * 0.1}s` }}>
              <div className="feature-icon">{feature.icon}</div>
              <h3 className="feature-title">{feature.title}</h3>
              <p className="feature-description">{feature.description}</p>
            </div>
          ))}
        </div>
      </section>

    

      {/* CTA Section */}
      <section className="cta-section">
        <div className="cta-content">
          <h2 className="cta-title">Ready to Transform Your Learning?</h2>
          <p className="cta-description">
            Join our community of learners and educators who are making education 
            accessible, adaptive, and amazing for everyone.
          </p>
          <div className="cta-actions">
            <Link to="/register" className="cta-button primary large">
              Start Your Journey
            </Link>
            <div className="cta-note">
              Free to start • No credit card required • Cancel anytime
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="welcome-footer">
        <div className="footer-content">
          <div className="footer-brand">
            <h3>ThinkAble</h3>
            <p>Adaptive learning for everyone</p>
          </div>
          <div className="footer-links">
            <div className="footer-section">
              <h4>Platform</h4>
              <Link to="/login">Sign In</Link>
              <Link to="/register">Sign Up</Link>
            </div>
            <div className="footer-section">
              <h4>Support</h4>
              <a href="#help">Help Center</a>
              <a href="#accessibility">Accessibility</a>
            </div>
          </div>
        </div>
        <div className="footer-bottom">
          <p>&copy; 2024 ThinkAble. Making education accessible for all.</p>
        </div>
      </footer>
    </div>
  );
};

export default Welcome;