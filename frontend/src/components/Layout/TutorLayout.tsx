import React from 'react';
import TutorNavigation from '../Common/TutorNavigation';
import './TutorLayout.css';

interface TutorLayoutProps {
  children: React.ReactNode;
  className?: string;
}

const TutorLayout: React.FC<TutorLayoutProps> = ({ children, className = '' }) => {
  return (
    <div className={`tutor-layout ${className}`}>
      <TutorNavigation />
      <main className="tutor-content">
        {children}
      </main>
    </div>
  );
};

export default TutorLayout;