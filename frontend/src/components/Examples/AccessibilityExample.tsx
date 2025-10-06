import React, { useState } from 'react';
import AccessibleContentViewer from '../Accessibility/AccessibleContentViewer';
import FocusHelper from '../NeurodivergentSupport/FocusHelper';
import { useAuth } from '../../contexts/AuthContext';

// Example of how to use the new accessibility system
const AccessibilityExample: React.FC = () => {
  const { user } = useAuth();
  const [isFocusHelperMinimized, setIsFocusHelperMinimized] = useState(false);

  // Example content - this could be any content from your database
  const exampleContent = {
    id: 'example-1',
    title: 'Introduction to Mathematics',
    description: 'A comprehensive guide to basic mathematical concepts including addition, subtraction, multiplication, and division.',
    contentType: 'pdf',
    fileName: 'math-basics.pdf',
    fileUrl: '/uploads/math-basics.pdf',
    subjectArea: 'Mathematics',
    difficultyLevel: 'Beginner',
    estimatedDurationMinutes: 30,
    dyslexiaFriendly: true,
    adhdFriendly: true,
    autismFriendly: false,
    visualImpairmentFriendly: true,
    hearingImpairmentFriendly: false
  };

  return (
    <div style={{ padding: '2rem' }}>
      {/* 
        STEP 1: Wrap your content with AccessibleContentViewer
        This automatically adds:
        - Text-to-Speech toolbar
        - Text Mode button  
        - Font size controls
        - Accessibility settings
        - Keyboard shortcuts
      */}
      <AccessibleContentViewer
        content={exampleContent}
        contentType="pdf"
        className="my-content-page"
      >
        {/* Your actual content goes inside here */}
        <div style={{ 
          padding: '2rem', 
          background: 'white', 
          borderRadius: '8px',
          minHeight: '500px'
        }}>
          <h1>📚 Your Content Goes Here</h1>
          <p>This is where you would display your actual content - PDFs, videos, images, text, etc.</p>
          
          <p>
            The AccessibleContentViewer wrapper automatically provides:
          </p>
          
          <ul>
            <li>🔊 <strong>Text-to-Speech</strong> - Click the "Read Aloud" button in the toolbar above</li>
            <li>📝 <strong>Text Mode</strong> - Click "Text Mode" to extract and format text with your accessibility settings</li>
            <li>🔍 <strong>Font Controls</strong> - Use +/- buttons to adjust font size</li>
            <li>⚡ <strong>Keyboard Shortcuts</strong>:
              <ul>
                <li><kbd>T</kbd> - Toggle Text Mode</li>
                <li><kbd>Space</kbd> - Play/Pause Text-to-Speech</li>
                <li><kbd>Ctrl + +/-</kbd> - Adjust font size</li>
                <li><kbd>Esc</kbd> - Close modals</li>
              </ul>
            </li>
            <li>🎨 <strong>Auto-Adaptation</strong> - Interface automatically adapts based on your assessment results</li>
          </ul>

          <h2>How It Adapts to You</h2>
          {user?.recommendedPreset ? (
            <div style={{ 
              background: '#e8f4f8', 
              padding: '1rem', 
              borderRadius: '8px',
              border: '1px solid #a8dadc'
            }}>
              <strong>🧠 Your Profile:</strong> {user.recommendedPreset.replace('_', ' ').toLowerCase()}
              <p>The interface is automatically optimized for your specific needs!</p>
            </div>
          ) : (
            <div style={{ 
              background: '#fff3cd', 
              padding: '1rem', 
              borderRadius: '8px',
              border: '1px solid #ffeaa7'
            }}>
              <strong>💡 Take the Assessment:</strong> Complete your accessibility assessment to get personalized adaptations!
              <br />
              <a href="/student/assessment" style={{ color: '#007bff', textDecoration: 'none' }}>
                Take Assessment Now →
              </a>
            </div>
          )}

          <h2>Sample Content for Testing</h2>
          <p>
            This is sample text content that you can test with the accessibility features. 
            Try using the Text-to-Speech feature to hear this content read aloud. 
            The system will automatically adjust the reading speed and style based on your accessibility profile.
          </p>
          
          <p>
            For users with dyslexia, the text will be processed with enhanced spacing and structure. 
            For users with ADHD, the content will be broken into manageable chunks. 
            For users with autism, the interface will maintain consistency and predictability.
          </p>
        </div>
      </AccessibleContentViewer>

      {/* 
        STEP 2: Add the Focus Helper for neurodivergent support
        This provides:
        - Focus timers
        - Energy level tracking
        - Break reminders
        - Overwhelm escape hatch
        - Breathing exercises
      */}
      <FocusHelper
        isMinimized={isFocusHelperMinimized}
        onMinimize={() => setIsFocusHelperMinimized(true)}
        onRestore={() => setIsFocusHelperMinimized(false)}
      />

      {/* Usage Instructions */}
      <div style={{ 
        marginTop: '2rem', 
        padding: '1rem', 
        background: '#f8f9fa', 
        borderRadius: '8px',
        fontSize: '0.9rem'
      }}>
        <h3>🚀 How to Use This System:</h3>
        <ol>
          <li><strong>Take Assessment:</strong> Go to <code>/student/assessment</code> to get your accessibility profile</li>
          <li><strong>Customize Settings:</strong> Visit <code>/student/settings</code> to fine-tune preferences</li>
          <li><strong>Use Focus Helper:</strong> The brain icon (🧠) in the bottom-right provides focus support</li>
          <li><strong>Try Text Mode:</strong> Click "Text Mode" button to see extracted, formatted content</li>
          <li><strong>Test TTS:</strong> Click "Read Aloud" to hear content spoken with your preferred settings</li>
        </ol>
      </div>
    </div>
  );
};

export default AccessibilityExample;