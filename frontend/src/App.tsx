import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';
import './styles/adaptive-ui.css';
import './styles/comprehensive-adaptive.css';
import { injectDynamicFontCSS } from './utils/fontLoader';
import Welcome from './components/Welcome/Welcome';
import Login from './components/Auth/Login';
import Register from './components/Auth/Register';
import StudentDashboard from './components/Student/StudentDashboard';
import TutorDashboard from './components/Tutor/TutorDashboard';
import ContentUpload from './components/Tutor/ContentUpload';
import ContentManagement from './components/Tutor/ContentManagement';
import TutorMessages from './components/Tutor/TutorMessages';
import TutorQuizManager from './components/Tutor/TutorQuizManager';
import AdminDashboard from './components/Admin/AdminDashboard';
import ContentDiscovery from './components/Student/ContentDiscovery';
import ContentViewer from './components/Student/ContentViewer';
import Assessment from './components/Student/Assessment';
import ProgressDashboard from './components/Student/ProgressDashboard';
import Settings from './components/Student/Settings';
import MyNotesPage from './components/Student/MyNotesPage';
import TeacherDashboard from './components/Teacher/TeacherDashboard';
import PresetVisualVerification from './components/Testing/PresetVisualVerification';
import AccessibilityExample from './components/Examples/AccessibilityExample';
import ProtectedRoute from './components/Auth/ProtectedRoute';
import StudentLayout from './components/Layout/StudentLayout';
import TutorLayout from './components/Layout/TutorLayout';
import ErrorBoundary from './components/Common/ErrorBoundary';
import NotificationContainer from './components/Common/NotificationContainer';
import { AuthProvider } from './contexts/AuthContext';
import { NotificationProvider } from './contexts/NotificationContext';
import { AdaptiveUIProvider } from './contexts/AdaptiveUIContext';
import { TextToSpeechProvider } from './contexts/TextToSpeechContext';
import { TimerProvider } from './contexts/TimerContext';
import { FocusModeProvider } from './contexts/FocusModeContext';
import { useAdaptiveInterface } from './hooks/useAdaptiveInterface';

function AppContent() {
  useAdaptiveInterface();
  
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Welcome />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route 
            path="/student" 
            element={
              <ProtectedRoute requiredRole="STUDENT">
                <StudentLayout>
                  <StudentDashboard />
                </StudentLayout>
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/student/content" 
            element={
              <ProtectedRoute requiredRole="STUDENT">
                <StudentLayout>
                  <ContentDiscovery />
                </StudentLayout>
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/student/content/:contentId" 
            element={
              <ProtectedRoute requiredRole="STUDENT">
                <StudentLayout>
                  <ContentViewer />
                </StudentLayout>
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/student/assessment" 
            element={
              <ProtectedRoute requiredRole="STUDENT">
                <StudentLayout>
                  <Assessment />
                </StudentLayout>
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/student/progress" 
            element={
              <ProtectedRoute requiredRole="STUDENT">
                <StudentLayout>
                  <ProgressDashboard />
                </StudentLayout>
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/student/settings" 
            element={
              <ProtectedRoute requiredRole="STUDENT">
                <StudentLayout>
                  <Settings />
                </StudentLayout>
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/student/notes" 
            element={
              <ProtectedRoute requiredRole="STUDENT">
                <StudentLayout>
                  <MyNotesPage />
                </StudentLayout>
              </ProtectedRoute>
            } 
          />
          <Route
            path="/tutor"
            element={
              <ProtectedRoute requiredRole="TUTOR">
                <TutorLayout>
                  <TutorDashboard />
                </TutorLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/tutor/upload"
            element={
              <ProtectedRoute requiredRole="TUTOR">
                <TutorLayout>
                  <ContentUpload />
                </TutorLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/tutor/content"
            element={
              <ProtectedRoute requiredRole="TUTOR">
                <TutorLayout>
                  <ContentManagement />
                </TutorLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/tutor/messages"
            element={
              <ProtectedRoute requiredRole="TUTOR">
                <TutorLayout>
                  <TutorMessages />
                </TutorLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/tutor/quizzes"
            element={
              <ProtectedRoute requiredRole="TUTOR">
                <TutorLayout>
                  <TutorQuizManager />
                </TutorLayout>
              </ProtectedRoute>
            }
          />
          <Route 
            path="/admin" 
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/teacher" 
            element={
              <ProtectedRoute requiredRole="TEACHER">
                <TeacherDashboard />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/testing/preset-verification" 
            element={<PresetVisualVerification />} 
          />
          <Route 
            path="/example/accessibility" 
            element={<AccessibilityExample />} 
          />
        </Routes>
        <NotificationContainer />
      </div>
    </Router>
  );
}

function App() {
  // Initialize dynamic fonts based on environment
  useEffect(() => {
    injectDynamicFontCSS();
  }, []);

  return (
    <ErrorBoundary>
      <NotificationProvider>
        <AuthProvider>
          <AdaptiveUIProvider>
            <TextToSpeechProvider>
              <TimerProvider>
                <FocusModeProvider>
                  <AppContent />
                </FocusModeProvider>
              </TimerProvider>
            </TextToSpeechProvider>
          </AdaptiveUIProvider>
        </AuthProvider>
      </NotificationProvider>
    </ErrorBoundary>
  );
}

export default App;
