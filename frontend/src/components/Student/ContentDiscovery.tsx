import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { studentAPI } from '../../services/api';
import QuizModal from './QuizModal';
import './ContentDiscovery.css';

interface Content {
  id: number;
  title: string;
  description: string;
  subjectArea: string;
  contentType: string;
  filePath: string;
  fileName: string;
  mimeType: string;
  difficultyLevel: string;
  status: string;
  createdAt: string;
  viewCount: number;
  ratingAverage: number;
  ratingCount: number;
  tutorName?: string;
  tutorId?: number;
  accessibilityTags: any[];
}

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

const ContentDiscovery: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [contents, setContents] = useState<Content[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDifficulty, setSelectedDifficulty] = useState('');
  const [selectedContentType, setSelectedContentType] = useState('');
  const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);
  const [currentQuiz, setCurrentQuiz] = useState<Quiz | null>(null);
  const [currentContentId, setCurrentContentId] = useState<number | null>(null);
  const [favorites, setFavorites] = useState<number[]>([]);

  useEffect(() => {
    fetchContents();
    loadFavorites();
    
    // Check if we should show favorites only from URL parameter
    const showFavorites = searchParams.get('favorites') === 'true';
    setShowFavoritesOnly(showFavorites);
  }, [user, searchParams]);

  const loadFavorites = async () => {
    if (!user?.id) return;
    
    try {
      const response = await studentAPI.getFavorites(user.id);
      const favoriteIds = response.favorites.map((fav: any) => fav.id);
      setFavorites(favoriteIds);
    } catch (error) {
      console.error('Error loading favorites:', error);
      // Fallback to localStorage for now
      const savedFavorites = localStorage.getItem('kidFavorites');
      if (savedFavorites) {
        setFavorites(JSON.parse(savedFavorites));
      }
    }
  };

  const toggleFavorite = async (contentId: number) => {
    if (!user?.id) return;
    
    try {
      const response = await studentAPI.toggleFavorite(contentId, user.id);
      const isBookmarked = response.isBookmarked;
      
      if (isBookmarked) {
        setFavorites([...favorites, contentId]);
      } else {
        setFavorites(favorites.filter(id => id !== contentId));
      }
      
      // Remove localStorage usage (keep as backup migration)
      const savedFavorites = localStorage.getItem('kidFavorites');
      if (savedFavorites) {
        localStorage.removeItem('kidFavorites');
      }
      
    } catch (error) {
      console.error('Error toggling favorite:', error);
      setError('Failed to update favorite. Please try again.');
    }
  };

  const toggleFavoritesFilter = () => {
    const newShowFavorites = !showFavoritesOnly;
    setShowFavoritesOnly(newShowFavorites);
    
    // Update URL to reflect filter state
    if (newShowFavorites) {
      navigate('/student/content?favorites=true', { replace: true });
    } else {
      navigate('/student/content', { replace: true });
    }
  };

  const fetchContents = async () => {
    try {
      setLoading(true);
      const response = await studentAPI.getContent();
      setContents(response.content || []);
    } catch (error) {
      console.error('Error fetching contents:', error);
      setError('Failed to load content. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleTakeQuiz = async (contentId: number) => {
    try {
      const response = await studentAPI.getQuiz(contentId, user?.email || '');
      setCurrentQuiz(response.quiz);
      setCurrentContentId(contentId);
    } catch (error) {
      console.error('Error fetching quiz:', error);
      setError('Failed to load quiz. Please try again.');
    }
  };

  const handleCloseQuiz = () => {
    setCurrentQuiz(null);
    setCurrentContentId(null);
    fetchContents();
  };

  const getContentIcon = (mimeType: string, fileName: string) => {
    const ext = fileName.toLowerCase();
    if (mimeType.includes('video') || ext.includes('.mp4')) return 'fas fa-play-circle';
    if (mimeType.includes('pdf') || ext.includes('.pdf')) return 'fas fa-file-pdf';
    if (mimeType.includes('audio') || ext.includes('.mp3')) return 'fas fa-music';
    if (mimeType.includes('image') || ext.match(/\.(jpg|jpeg|png|gif)$/)) return 'fas fa-image';
    if (mimeType.includes('document') || ext.includes('.docx')) return 'fas fa-file-text';
    return 'fas fa-book';
  };

  const getSubjectIcon = (subject: string) => {
    const s = subject.toLowerCase();
    if (s.includes('math')) return 'fas fa-calculator';
    if (s.includes('science')) return 'fas fa-flask';
    if (s.includes('history')) return 'fas fa-landmark';
    if (s.includes('english') || s.includes('language')) return 'fas fa-book-open';
    if (s.includes('art')) return 'fas fa-palette';
    if (s.includes('music')) return 'fas fa-music';
    return 'fas fa-graduation-cap';
  };

  const getDifficultyColor = (level: string) => {
    const l = level.toLowerCase();
    if (l.includes('easy') || l.includes('beginner')) return '#28a745';
    if (l.includes('medium') || l.includes('intermediate')) return '#fd7e14';
    if (l.includes('hard') || l.includes('advanced')) return '#dc3545';
    return '#6c757d';
  };

  const getContentStatus = (content: Content) => {
    const now = new Date();
    const createdDate = new Date(content.createdAt);
    const daysSinceCreated = Math.floor((now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24));
    
    // New content (less than 7 days old)
    if (daysSinceCreated <= 7) {
      return { type: 'new', label: 'New!', icon: 'fas fa-sparkles', color: '#74b9ff' };
    }
    
    // Popular content (high view count)
    if (content.viewCount >= 100) {
      return { type: 'popular', label: 'Popular!', icon: 'fas fa-fire', color: '#fd79a8' };
    }
    
    // High quality content (high rating with sufficient reviews)
    if (content.ratingAverage >= 4.0 && content.ratingCount >= 5) {
      return { type: 'quality', label: 'Awesome!', icon: 'fas fa-thumbs-up', color: '#00cec9' };
    }
    
    // Trending content (good engagement recently)
    if (content.viewCount >= 20 && daysSinceCreated <= 30) {
      return { type: 'trending', label: 'Trending!', icon: 'fas fa-chart-line', color: '#fdcb6e' };
    }
    
    // Featured content (very high rating)
    if (content.ratingAverage >= 4.5) {
      return { type: 'featured', label: 'Featured!', icon: 'fas fa-star', color: '#e17055' };
    }
    
    return null;
  };

  const filteredContents = contents.filter(content => {
    const matchesSearch = content.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         content.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         content.subjectArea.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesDifficulty = !selectedDifficulty || content.difficultyLevel === selectedDifficulty;
    const matchesContentType = !selectedContentType || content.mimeType.includes(selectedContentType);
    const matchesFavorites = !showFavoritesOnly || favorites.includes(content.id);
    
    return matchesSearch && matchesDifficulty && matchesContentType && matchesFavorites;
  });

  if (loading) {
    return (
      <div className="kid-discovery-page">
        <div className="kid-loading">
          <div className="fun-spinner">
            <i className="fas fa-book-open"></i>
          </div>
          <h2>Finding awesome stuff to learn!</h2>
          <p>Just a moment... ✨</p>
        </div>
      </div>
    );
  }

  return (
    <div className="kid-discovery-page">
      {/* Fun, colorful header */}
      <div className="kid-discovery-header">
        <div className="welcome-section">
          <h1><i className="fas fa-rocket"></i> Learning Adventure!</h1>
          <p>What cool stuff do you want to learn today?</p>
        </div>
        
        <div className="quick-stats">
          <div className="stat-badge">
            <i className="fas fa-book"></i>
            <span>{contents.length} Adventures</span>
          </div>
          <button 
            className={`stat-badge clickable ${showFavoritesOnly ? 'active' : ''}`}
            onClick={toggleFavoritesFilter}
            title={showFavoritesOnly ? 'Show all content' : 'Show only favorites'}
          >
            <i className={`fas fa-heart ${showFavoritesOnly ? 'favorited' : ''}`}></i>
            <span>{favorites.length} Favorites</span>
          </button>
        </div>
      </div>

      {/* Simple, kid-friendly filters */}
      <div className="kid-filters">
        <div className="search-adventure">
          <i className="fas fa-search"></i>
          <input
            type="text"
            placeholder="What do you want to learn?"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        
        <div className="filter-buttons">
          <button 
            className={`filter-btn ${selectedDifficulty === '' ? 'active' : ''}`}
            onClick={() => setSelectedDifficulty('')}
          >
            <i className="fas fa-globe"></i> All Levels
          </button>
          <button 
            className={`filter-btn ${selectedDifficulty === 'Beginner' ? 'active' : ''}`}
            onClick={() => setSelectedDifficulty('Beginner')}
          >
            <i className="fas fa-seedling"></i> Easy
          </button>
          <button 
            className={`filter-btn ${selectedDifficulty === 'Intermediate' ? 'active' : ''}`}
            onClick={() => setSelectedDifficulty('Intermediate')}
          >
            <i className="fas fa-star"></i> Medium
          </button>
          <button 
            className={`filter-btn ${selectedDifficulty === 'Advanced' ? 'active' : ''}`}
            onClick={() => setSelectedDifficulty('Advanced')}
          >
            <i className="fas fa-trophy"></i> Hard
          </button>
        </div>
        
        <div className="type-filters">
          <button 
            className={`type-btn ${selectedContentType === '' ? 'active' : ''}`}
            onClick={() => setSelectedContentType('')}
          >
            <i className="fas fa-th-large"></i> Everything
          </button>
          <button 
            className={`type-btn ${selectedContentType === 'video' ? 'active' : ''}`}
            onClick={() => setSelectedContentType('video')}
          >
            <i className="fas fa-play-circle"></i> Videos
          </button>
          <button 
            className={`type-btn ${selectedContentType === 'pdf' ? 'active' : ''}`}
            onClick={() => setSelectedContentType('pdf')}
          >
            <i className="fas fa-file-pdf"></i> Books
          </button>
          <button 
            className={`type-btn ${selectedContentType === 'image' ? 'active' : ''}`}
            onClick={() => setSelectedContentType('image')}
          >
            <i className="fas fa-image"></i> Pictures
          </button>
        </div>
      </div>

      {error && (
        <div className="kid-error-message">
          <i className="fas fa-sad-tear"></i>
          <p>Oops! {error}</p>
        </div>
      )}

      <div className="adventures-grid">
        {filteredContents.length === 0 ? (
          <div className="no-adventures">
            <div className="empty-state">
              <i className="fas fa-telescope"></i>
              <h3>No adventures found!</h3>
              <p>Try searching for something else or pick different filters</p>
              <button 
                className="reset-filters-btn"
                onClick={() => {
                  setSearchTerm('');
                  setSelectedDifficulty('');
                  setSelectedContentType('');
                }}
              >
                <i className="fas fa-refresh"></i> Show All Adventures
              </button>
            </div>
          </div>
        ) : (
          filteredContents.map((content) => (
            <div key={content.id} className="adventure-card">
              {/* Fun header with icons and favorite button */}
              <div className="adventure-header">
                <div className="content-icon">
                  <i className={getContentIcon(content.mimeType, content.fileName)}></i>
                </div>
                <button 
                  className={`favorite-btn ${favorites.includes(content.id) ? 'favorited' : ''}`}
                  onClick={() => toggleFavorite(content.id)}
                  title="Add to favorites"
                >
                  <i className={favorites.includes(content.id) ? 'fas fa-heart' : 'far fa-heart'}></i>
                </button>
              </div>

              {/* Content info */}
              <div className="adventure-content">
                <div className="subject-tag">
                  <i className={getSubjectIcon(content.subjectArea)}></i>
                  <span>{content.subjectArea}</span>
                </div>
                
                <h3 className="adventure-title">{content.title}</h3>
                <p className="adventure-description">{content.description}</p>
                
                <div className="adventure-badges">
                  <div 
                    className="difficulty-badge"
                    style={{ backgroundColor: getDifficultyColor(content.difficultyLevel) }}
                  >
                    <i className="fas fa-star"></i>
                    {content.difficultyLevel}
                  </div>
                  
                  {(() => {
                    const status = getContentStatus(content);
                    return status ? (
                      <div 
                        className="status-badge"
                        style={{ backgroundColor: status.color }}
                      >
                        <i className={status.icon}></i>
                        {status.label}
                      </div>
                    ) : null;
                  })()}
                  
                  {content.tutorName && (
                    <div className="tutor-badge">
                      <i className="fas fa-user-graduate"></i>
                      by {content.tutorName}
                    </div>
                  )}
                </div>
              </div>

              {/* Action buttons */}
              <div className="adventure-actions">
                <button 
                  className="start-learning-btn"
                  onClick={() => navigate(`/student/content/${content.id}`)}
                >
                  <i className="fas fa-play"></i>
                  Start Learning!
                </button>
                
                <button
                  className="quiz-btn"
                  onClick={() => handleTakeQuiz(content.id)}
                  title="Test what you know!"
                >
                  <i className="fas fa-brain"></i>
                  Quiz Me!
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {currentQuiz && currentContentId && (
        <QuizModal
          quiz={currentQuiz}
          contentId={currentContentId}
          studentEmail={user?.email || ''}
          onClose={handleCloseQuiz}
        />
      )}
    </div>
  );
};

export default ContentDiscovery;