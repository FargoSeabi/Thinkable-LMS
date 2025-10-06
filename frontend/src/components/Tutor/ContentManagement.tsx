import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import LoadingSpinner from '../Common/LoadingSpinner';
import config from '../../services/config';
import axios from 'axios';
import './ContentManagement.css';

interface Content {
  id: number;
  title: string;
  description: string;
  subjectArea: string;
  fileName: string;
  status: string;
  createdAt: string;
  publishedAt?: string;
  difficultyLevel: string;
  targetAgeMin: number;
  targetAgeMax: number;
  estimatedDurationMinutes: number;
  dyslexiaFriendly: boolean;
  adhdFriendly: boolean;
  autismFriendly: boolean;
  visualImpairmentFriendly: boolean;
  hearingImpairmentFriendly: boolean;
  motorImpairmentFriendly: boolean;
  hasAudioDescription: boolean;
  hasSubtitles: boolean;
}

const ContentManagement: React.FC = () => {
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const [contents, setContents] = useState<Content[]>([]);
  const [loading, setLoading] = useState(true);
  const [publishing, setPublishing] = useState<Set<number>>(new Set());

  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    fetchTutorContent();
  }, []);

  const fetchTutorContent = async () => {
    try {
      setLoading(true);
      const response = await axios.get(
        `${API_BASE_URL}/api/tutor/content/tutor/${user?.id}`,
        {
          headers: {
            'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
          },
        }
      );
      setContents(response.data.content || []);
    } catch (error) {
      console.error('Error fetching tutor content:', error);
      showNotification('Failed to load content', 'error');
    } finally {
      setLoading(false);
    }
  };

  const publishContent = async (contentId: number) => {
    try {
      setPublishing(prev => new Set(prev).add(contentId));
      
      await axios.post(
        `${API_BASE_URL}/api/tutor/content/${contentId}/publish`,
        null,
        {
          params: { tutorUserId: user?.id },
          headers: {
            'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
          },
        }
      );

      showNotification('Content published successfully!', 'success');
      await fetchTutorContent(); // Refresh the list
    } catch (error: any) {
      console.error('Error publishing content:', error);
      const message = error.response?.data?.error || 'Failed to publish content';
      showNotification(message, 'error');
    } finally {
      setPublishing(prev => {
        const newSet = new Set(prev);
        newSet.delete(contentId);
        return newSet;
      });
    }
  };

  const deleteContent = async (contentId: number) => {
    if (!window.confirm('Are you sure you want to delete this content? This action cannot be undone.')) {
      return;
    }

    try {
      await axios.delete(
        `${API_BASE_URL}/api/tutor/content/${contentId}`,
        {
          params: { tutorUserId: user?.id },
          headers: {
            'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
          },
        }
      );

      showNotification('Content deleted successfully', 'success');
      await fetchTutorContent(); // Refresh the list
    } catch (error: any) {
      console.error('Error deleting content:', error);
      const message = error.response?.data?.error || 'Failed to delete content';
      showNotification(message, 'error');
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'published':
        return '#28a745';
      case 'draft':
        return '#6c757d';
      case 'pending':
        return '#ffc107';
      default:
        return '#6c757d';
    }
  };

  const getAccessibilityTags = (content: Content) => {
    const tags = [];
    if (content.dyslexiaFriendly) tags.push('Dyslexia-friendly');
    if (content.adhdFriendly) tags.push('ADHD-friendly');
    if (content.autismFriendly) tags.push('Autism-friendly');
    if (content.visualImpairmentFriendly) tags.push('Vision-accessible');
    if (content.hearingImpairmentFriendly) tags.push('Hearing-accessible');
    if (content.motorImpairmentFriendly) tags.push('Motor-accessible');
    if (content.hasAudioDescription) tags.push('Audio descriptions');
    if (content.hasSubtitles) tags.push('Subtitles');
    return tags;
  };

  if (loading) {
    return (
      <div className="content-management-container">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="content-management-container">
      <div className="content-header">
        <h2>My Content</h2>
        <p>Manage your uploaded educational content</p>
      </div>

      {contents.length === 0 ? (
        <div className="no-content">
          <i className="fas fa-folder-open"></i>
          <h3>No content uploaded yet</h3>
          <p>Start by uploading some educational materials to share with students.</p>
        </div>
      ) : (
        <div className="content-grid">
          {contents.map(content => (
            <div key={content.id} className="content-card">
              <div className="content-card-header">
                <div className="content-title">
                  <h3>{content.title}</h3>
                  <span 
                    className="status-badge" 
                    style={{ backgroundColor: getStatusColor(content.status) }}
                  >
                    {content.status.charAt(0).toUpperCase() + content.status.slice(1)}
                  </span>
                </div>
                <div className="content-actions">
                  {content.status === 'draft' && (
                    <button
                      onClick={() => publishContent(content.id)}
                      disabled={publishing.has(content.id)}
                      className="publish-btn"
                    >
                      {publishing.has(content.id) ? (
                        <>
                          <LoadingSpinner size="small" />
                          Publishing...
                        </>
                      ) : (
                        <>
                          <i className="fas fa-paper-plane"></i>
                          Publish
                        </>
                      )}
                    </button>
                  )}
                  <button
                    onClick={() => deleteContent(content.id)}
                    className="delete-btn"
                  >
                    <i className="fas fa-trash"></i>
                    Delete
                  </button>
                </div>
              </div>

              <div className="content-details">
                <p className="content-description">{content.description}</p>
                
                <div className="content-meta">
                  <div className="meta-item">
                    <i className="fas fa-book"></i>
                    <span>{content.subjectArea}</span>
                  </div>
                  <div className="meta-item">
                    <i className="fas fa-chart-line"></i>
                    <span>{content.difficultyLevel}</span>
                  </div>
                  <div className="meta-item">
                    <i className="fas fa-clock"></i>
                    <span>{content.estimatedDurationMinutes} min</span>
                  </div>
                  <div className="meta-item">
                    <i className="fas fa-users"></i>
                    <span>Ages {content.targetAgeMin}-{content.targetAgeMax}</span>
                  </div>
                </div>

                <div className="accessibility-tags">
                  {getAccessibilityTags(content).map(tag => (
                    <span key={tag} className="accessibility-tag">
                      {tag}
                    </span>
                  ))}
                </div>

                <div className="content-footer">
                  <div className="content-file">
                    <i className="fas fa-file"></i>
                    <span>{content.fileName}</span>
                  </div>
                  <div className="content-dates">
                    <div className="date-item">
                      <small>Created: {formatDate(content.createdAt)}</small>
                    </div>
                    {content.publishedAt && (
                      <div className="date-item">
                        <small>Published: {formatDate(content.publishedAt)}</small>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ContentManagement;