import React from 'react';
import StudentNavigation from '../Common/StudentNavigation';
import FloatingTimer from '../Common/FloatingTimer';
import { useFocusMode } from '../../contexts/FocusModeContext';
import './StudentLayout.css';

interface StudentLayoutProps {
  children: React.ReactNode;
  className?: string;
}

const StudentLayout: React.FC<StudentLayoutProps> = ({ children, className = '' }) => {
  const { isFocusMode } = useFocusMode();
  
  return (
    <div className={`student-layout ${className} ${isFocusMode ? 'focus-mode' : ''}`}>
      {!isFocusMode && <StudentNavigation />}
      <main className={`student-content ${isFocusMode ? 'focus-mode-content' : ''}`}>
        {children}
      </main>
      {!isFocusMode && <FloatingTimer />}
    </div>
  );
};

export default StudentLayout;