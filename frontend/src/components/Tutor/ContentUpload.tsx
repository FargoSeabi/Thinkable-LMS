import React, { useState, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import LoadingSpinner from '../Common/LoadingSpinner';
import config from '../../services/config';
import axios from 'axios';
import './ContentUpload.css';

interface UploadFormData {
  title: string;
  description: string;
  subjectArea: string;
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
  hasSignLanguage: boolean;
}

const ContentUpload: React.FC = () => {
  const { user } = useAuth();
  const { showNotification } = useNotification();
  
  const [file, setFile] = useState<File | null>(null);
  const [uploadProgress, setUploadProgress] = useState<number>(0);
  const [uploading, setUploading] = useState<boolean>(false);
  const [dragActive, setDragActive] = useState<boolean>(false);
  
  const [formData, setFormData] = useState<UploadFormData>({
    title: '',
    description: '',
    subjectArea: '',
    difficultyLevel: 'Intermediate',
    targetAgeMin: 6,
    targetAgeMax: 18,
    estimatedDurationMinutes: 30,
    dyslexiaFriendly: false,
    adhdFriendly: false,
    autismFriendly: false,
    visualImpairmentFriendly: false,
    hearingImpairmentFriendly: false,
    motorImpairmentFriendly: false,
    hasAudioDescription: false,
    hasSubtitles: false,
    hasSignLanguage: false,
  });

  const API_BASE_URL = config.apiBaseUrl;

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    
    if (type === 'checkbox') {
      const checked = (e.target as HTMLInputElement).checked;
      setFormData(prev => ({ ...prev, [name]: checked }));
    } else if (type === 'number') {
      setFormData(prev => ({ ...prev, [name]: parseInt(value) || 0 }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleDrag = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  }, []);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setFile(e.dataTransfer.files[0]);
    }
  }, []);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
    }
  };

  const getFileIcon = (file: File) => {
    const extension = file.name.split('.').pop()?.toLowerCase();
    switch (extension) {
      case 'pdf': return 'fas fa-file-pdf';
      case 'mp4': case 'avi': case 'mov': return 'fas fa-file-video';
      case 'mp3': case 'wav': return 'fas fa-file-audio';
      case 'jpg': case 'jpeg': case 'png': case 'gif': return 'fas fa-file-image';
      case 'doc': case 'docx': return 'fas fa-file-word';
      case 'ppt': case 'pptx': return 'fas fa-file-powerpoint';
      case 'xls': case 'xlsx': return 'fas fa-file-excel';
      case 'h5p': return 'fas fa-puzzle-piece';
      default: return 'fas fa-file';
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!file) {
      showNotification('Please select a file to upload', 'error');
      return;
    }

    if (!formData.title.trim()) {
      showNotification('Please enter a title for the content', 'error');
      return;
    }

    if (!formData.description.trim()) {
      showNotification('Please enter a description for the content', 'error');
      return;
    }

    if (!formData.subjectArea.trim()) {
      showNotification('Please enter a subject area', 'error');
      return;
    }

    setUploading(true);
    setUploadProgress(0);

    try {
      const uploadFormData = new FormData();
      uploadFormData.append('file', file);
      uploadFormData.append('tutorUserId', user?.id?.toString() || '');
      
      // Append all form data
      Object.entries(formData).forEach(([key, value]) => {
        uploadFormData.append(key, value.toString());
      });

      // Determine endpoint based on file type
      const isH5PFile = file.name.toLowerCase().endsWith('.h5p');
      const endpoint = isH5PFile 
        ? `${API_BASE_URL}/api/tutor/content/upload-h5p`
        : `${API_BASE_URL}/api/tutor/content/upload`;

      console.log(`Uploading ${file.name} to ${endpoint}`);

      const response = await axios.post(
        endpoint,
        uploadFormData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
          },
          onUploadProgress: (progressEvent) => {
            if (progressEvent.total) {
              const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
              setUploadProgress(progress);
            }
          },
        }
      );

      showNotification('Content uploaded successfully!', 'success');
      
      // Reset form
      setFile(null);
      setFormData({
        title: '',
        description: '',
        subjectArea: '',
        difficultyLevel: 'Intermediate',
        targetAgeMin: 6,
        targetAgeMax: 18,
        estimatedDurationMinutes: 30,
        dyslexiaFriendly: false,
        adhdFriendly: false,
        autismFriendly: false,
        visualImpairmentFriendly: false,
        hearingImpairmentFriendly: false,
        motorImpairmentFriendly: false,
        hasAudioDescription: false,
        hasSubtitles: false,
        hasSignLanguage: false,
      });
      
    } catch (error: any) {
      console.error('Upload error:', error);
      const message = error.response?.data?.message || 'Upload failed. Please try again.';
      showNotification(message, 'error');
    } finally {
      setUploading(false);
      setUploadProgress(0);
    }
  };

  return (
    <div className="content-upload-container">
      <div className="upload-header">
        <h2>Upload Educational Content</h2>
        <p>Share your knowledge with students by uploading educational materials</p>
      </div>

      <form onSubmit={handleSubmit} className="upload-form">
        {/* File Upload Area */}
        <div 
          className={`file-upload-area ${dragActive ? 'drag-active' : ''} ${file ? 'has-file' : ''}`}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
        >
          {!file ? (
            <>
              <i className="fas fa-cloud-upload-alt upload-icon"></i>
              <h3>Drag and drop your file here</h3>
              <p>or</p>
              <label htmlFor="file-input" className="file-select-btn">
                Choose File
                <input
                  id="file-input"
                  type="file"
                  onChange={handleFileSelect}
                  accept=".pdf,.doc,.docx,.ppt,.pptx,.mp4,.mp3,.jpg,.jpeg,.png,.gif,.h5p"
                  style={{ display: 'none' }}
                />
              </label>
              <p className="upload-hint">
                Supported formats: PDF, DOC, PPT, MP4, MP3, JPG, PNG, H5P (Max: 50MB)
              </p>
            </>
          ) : (
            <div className="file-preview">
              <i className={`${getFileIcon(file)} file-icon`}></i>
              <div className="file-info">
                <h4>{file.name}</h4>
                <p>{formatFileSize(file.size)}</p>
              </div>
              <button
                type="button"
                onClick={() => setFile(null)}
                className="remove-file-btn"
                disabled={uploading}
              >
                <i className="fas fa-times"></i>
              </button>
            </div>
          )}
        </div>

        {uploading && (
          <div className="upload-progress">
            <div className="progress-bar">
              <div 
                className="progress-fill" 
                style={{ width: `${uploadProgress}%` }}
              ></div>
            </div>
            <span className="progress-text">{uploadProgress}% uploaded</span>
          </div>
        )}

        {/* Content Information */}
        <div className="form-section">
          <h3>Content Information</h3>
          
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="title">Title *</label>
              <input
                id="title"
                name="title"
                type="text"
                value={formData.title}
                onChange={handleInputChange}
                placeholder="Enter content title"
                required
                disabled={uploading}
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="subjectArea">Subject Area *</label>
              <input
                id="subjectArea"
                name="subjectArea"
                type="text"
                value={formData.subjectArea}
                onChange={handleInputChange}
                placeholder="e.g., Mathematics, Science, History"
                required
                disabled={uploading}
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="description">Description *</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              placeholder="Describe what students will learn from this content"
              rows={4}
              required
              disabled={uploading}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="difficultyLevel">Difficulty Level</label>
              <select
                id="difficultyLevel"
                name="difficultyLevel"
                value={formData.difficultyLevel}
                onChange={handleInputChange}
                disabled={uploading}
              >
                <option value="Beginner">Beginner</option>
                <option value="Intermediate">Intermediate</option>
                <option value="Advanced">Advanced</option>
              </select>
            </div>
            
            <div className="form-group">
              <label htmlFor="estimatedDurationMinutes">Duration (minutes)</label>
              <input
                id="estimatedDurationMinutes"
                name="estimatedDurationMinutes"
                type="number"
                min="1"
                max="300"
                value={formData.estimatedDurationMinutes}
                onChange={handleInputChange}
                disabled={uploading}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="targetAgeMin">Target Age Range</label>
              <div className="age-range-inputs">
                <input
                  id="targetAgeMin"
                  name="targetAgeMin"
                  type="number"
                  min="3"
                  max="25"
                  value={formData.targetAgeMin}
                  onChange={handleInputChange}
                  disabled={uploading}
                />
                <span>to</span>
                <input
                  name="targetAgeMax"
                  type="number"
                  min="3"
                  max="25"
                  value={formData.targetAgeMax}
                  onChange={handleInputChange}
                  disabled={uploading}
                />
              </div>
            </div>
          </div>
        </div>

        {/* Accessibility Features */}
        <div className="form-section">
          <h3>Accessibility Features</h3>
          <p className="section-description">
            Help us make your content accessible to all learners
          </p>
          
          <div className="checkbox-grid">
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="dyslexiaFriendly"
                checked={formData.dyslexiaFriendly}
                onChange={handleInputChange}
                disabled={uploading}
              />
              <span className="checkbox-text">
                <i className="fas fa-font"></i>
                Dyslexia Friendly
              </span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="adhdFriendly"
                checked={formData.adhdFriendly}
                onChange={handleInputChange}
                disabled={uploading}
              />
              <span className="checkbox-text">
                <i className="fas fa-brain"></i>
                ADHD Friendly
              </span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="autismFriendly"
                checked={formData.autismFriendly}
                onChange={handleInputChange}
                disabled={uploading}
              />
              <span className="checkbox-text">
                <i className="fas fa-puzzle-piece"></i>
                Autism Friendly
              </span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="visualImpairmentFriendly"
                checked={formData.visualImpairmentFriendly}
                onChange={handleInputChange}
                disabled={uploading}
              />
              <span className="checkbox-text">
                <i className="fas fa-eye-slash"></i>
                Visual Impairment Friendly
              </span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="hearingImpairmentFriendly"
                checked={formData.hearingImpairmentFriendly}
                onChange={handleInputChange}
                disabled={uploading}
              />
              <span className="checkbox-text">
                <i className="fas fa-deaf"></i>
                Hearing Impairment Friendly
              </span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="motorImpairmentFriendly"
                checked={formData.motorImpairmentFriendly}
                onChange={handleInputChange}
                disabled={uploading}
              />
              <span className="checkbox-text">
                <i className="fas fa-wheelchair"></i>
                Motor Impairment Friendly
              </span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="hasAudioDescription"
                checked={formData.hasAudioDescription}
                onChange={handleInputChange}
                disabled={uploading}
              />
              <span className="checkbox-text">
                <i className="fas fa-audio-description"></i>
                Has Audio Description
              </span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="hasSubtitles"
                checked={formData.hasSubtitles}
                onChange={handleInputChange}
                disabled={uploading}
              />
              <span className="checkbox-text">
                <i className="fas fa-closed-captioning"></i>
                Has Subtitles
              </span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="hasSignLanguage"
                checked={formData.hasSignLanguage}
                onChange={handleInputChange}
                disabled={uploading}
              />
              <span className="checkbox-text">
                <i className="fas fa-sign-language"></i>
                Has Sign Language
              </span>
            </label>
          </div>
        </div>

        {/* Submit Button */}
        <div className="form-actions">
          <button
            type="submit"
            className={`submit-btn ${uploading ? 'uploading' : ''}`}
            disabled={uploading || !file}
          >
            {uploading ? (
              <>
                <LoadingSpinner size="small" />
                Uploading...
              </>
            ) : (
              <>
                <i className="fas fa-upload"></i>
                Upload Content
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default ContentUpload;