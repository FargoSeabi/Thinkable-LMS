import React, { useState, useEffect } from 'react';
import './PageTransition.css';

interface PageTransitionProps {
  children: React.ReactNode;
  loading?: boolean;
}

const PageTransition: React.FC<PageTransitionProps> = ({ children, loading = false }) => {
  const [isVisible, setIsVisible] = useState(false);
  const [contentLoaded, setContentLoaded] = useState(false);

  useEffect(() => {
    // Small delay to ensure smooth transition
    const timer = setTimeout(() => {
      setIsVisible(true);
      setContentLoaded(true);
    }, 50);

    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return (
      <div className="page-transition loading">
        <div className="loading-spinner">
          <div className="spinner-ring"></div>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`page-transition ${isVisible ? 'visible' : ''} ${contentLoaded ? 'loaded' : ''}`}>
      {children}
    </div>
  );
};

export default PageTransition;