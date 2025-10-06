import React, { useState, useEffect, useRef } from 'react';
import config from '../../services/config';
import './H5PContentViewer.css';

interface H5PContentViewerProps {
  content: {
    id: number;
    title: string;
    description: string;
    fileName: string;
    contentType: string;
    h5pContentId?: string;
    h5pLibrary?: string;
    h5pMetadata?: string;
    h5pSettings?: string;
    fileUrl?: string;
  };
  onInteraction?: (interactionType: string, data?: any) => void;
}

const H5PContentViewer: React.FC<H5PContentViewerProps> = ({ 
  content, 
  onInteraction 
}) => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [h5pData, setH5pData] = useState<any>(null);
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const startTimeRef = useRef<number>(Date.now());

  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    loadH5PContent();
    startTimeRef.current = Date.now();
    
    // Track content view
    if (onInteraction) {
      onInteraction('view_start', {
        contentId: content.id,
        contentType: 'interactive',
        h5pLibrary: content.h5pLibrary
      });
    }

    return () => {
      // Track session duration on unmount
      if (onInteraction) {
        const duration = Math.round((Date.now() - startTimeRef.current) / 1000);
        onInteraction('view_end', {
          contentId: content.id,
          duration_seconds: duration
        });
      }
    };
  }, [content.id]);

  const loadH5PContent = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Get H5P content data from the view endpoint
      const response = await fetch(`${API_BASE_URL}/api/tutor/content/${content.id}/view`);
      
      if (!response.ok) {
        throw new Error(`Failed to load H5P content: ${response.statusText}`);
      }

      const data = await response.json();
      
      // Check if this is actually H5P content
      if (data.contentType !== 'interactive') {
        throw new Error('Content is not interactive H5P content');
      }

      setH5pData(data);
      
    } catch (err) {
      console.error('Error loading H5P content:', err);
      setError(err instanceof Error ? err.message : 'Failed to load H5P content');
    } finally {
      setLoading(false);
    }
  };

  const handleIframeLoad = () => {
    setLoading(false);
    
    if (onInteraction) {
      onInteraction('content_loaded', {
        contentId: content.id,
        h5pLibrary: h5pData?.h5pLibrary
      });
    }
  };

  const handleIframeError = () => {
    setError('Failed to load H5P content in iframe');
    setLoading(false);
  };

  const getH5PEmbedUrl = () => {
    if (!h5pData) return null;

    // If we have an explicit embed URL, use it (it should point to H5P player)
    if (h5pData.h5pEmbedUrl) {
      // If it's a relative URL, prepend API base URL
      if (h5pData.h5pEmbedUrl.startsWith('/')) {
        return `${API_BASE_URL}${h5pData.h5pEmbedUrl}`;
      }
      return h5pData.h5pEmbedUrl;
    }

    // Fallback: Use H5P player endpoint
    return `${API_BASE_URL}/api/h5p/player/${content.id}`;
  };

  const parseH5PMetadata = () => {
    if (!h5pData?.h5pMetadata) return null;
    
    try {
      return JSON.parse(h5pData.h5pMetadata);
    } catch (e) {
      console.warn('Failed to parse H5P metadata:', e);
      return null;
    }
  };

  const renderLoadingState = () => (
    <div className="h5p-loading">
      <div className="h5p-loading-spinner">
        <i className="fas fa-play-circle"></i>
      </div>
      <h3>Loading Interactive Content...</h3>
      <p>Please wait while we prepare your H5P content</p>
    </div>
  );

  const renderErrorState = () => (
    <div className="h5p-error">
      <div className="h5p-error-icon">
        <i className="fas fa-exclamation-triangle"></i>
      </div>
      <h3>Content Unavailable</h3>
      <p>{error}</p>
      <div className="h5p-error-actions">
        <button 
          onClick={loadH5PContent}
          className="h5p-retry-btn"
        >
          <i className="fas fa-refresh"></i>
          Try Again
        </button>
      </div>
    </div>
  );

  const renderH5PContent = () => {
    const embedUrl = getH5PEmbedUrl();
    const metadata = parseH5PMetadata();
    
    if (!embedUrl) {
      return (
        <div className="h5p-fallback">
          <div className="h5p-fallback-content">
            <i className="fas fa-puzzle-piece"></i>
            <h3>Interactive Content</h3>
            <p><strong>Title:</strong> {h5pData?.title || content.title}</p>
            <p><strong>Type:</strong> {h5pData?.h5pLibrary || 'H5P Interactive'}</p>
            <p>This content requires a compatible H5P player to display.</p>
          </div>
        </div>
      );
    }

    return (
      <div className="h5p-content-container">
        {/* H5P Info Bar */}
        <div className="h5p-info-bar">
          <div className="h5p-info-left">
            <i className="fas fa-play-circle"></i>
            <span className="h5p-library-name">
              {h5pData?.h5pLibrary?.replace('H5P.', '') || 'Interactive Content'}
            </span>
          </div>
          <div className="h5p-info-right">
            <span className="h5p-content-type">H5P</span>
          </div>
        </div>

        {/* H5P Iframe */}
        <div className="h5p-iframe-container">
          <iframe
            ref={iframeRef}
            src={embedUrl}
            title={content.title}
            className="h5p-iframe"
            width="100%"
            height="600"
            frameBorder="0"
            allowFullScreen
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            onLoad={handleIframeLoad}
            onError={handleIframeError}
            sandbox="allow-scripts allow-same-origin allow-forms allow-popups allow-presentation allow-downloads"
          />
          
          {loading && (
            <div className="h5p-iframe-loading">
              <div className="h5p-spinner">
                <i className="fas fa-spinner fa-spin"></i>
              </div>
              <p>Loading interactive content...</p>
            </div>
          )}
        </div>

        {/* H5P Metadata (optional, collapsible) */}
        {metadata && (
          <div className="h5p-metadata-section">
            <details className="h5p-metadata-details">
              <summary>Content Details</summary>
              <div className="h5p-metadata-grid">
                {metadata.title && (
                  <div className="h5p-metadata-item">
                    <strong>Title:</strong> {metadata.title}
                  </div>
                )}
                {metadata.language && (
                  <div className="h5p-metadata-item">
                    <strong>Language:</strong> {metadata.language}
                  </div>
                )}
                {metadata.license && (
                  <div className="h5p-metadata-item">
                    <strong>License:</strong> {metadata.license}
                  </div>
                )}
                {metadata.authors && Array.isArray(metadata.authors) && metadata.authors.length > 0 && (
                  <div className="h5p-metadata-item">
                    <strong>Authors:</strong> {metadata.authors.map((author: any) => author.name).join(', ')}
                  </div>
                )}
              </div>
            </details>
          </div>
        )}
      </div>
    );
  };

  if (loading && !h5pData) {
    return renderLoadingState();
  }

  if (error) {
    return renderErrorState();
  }

  return (
    <div className="clean-h5p-viewer">
      {renderH5PContent()}
    </div>
  );
};

export default H5PContentViewer;