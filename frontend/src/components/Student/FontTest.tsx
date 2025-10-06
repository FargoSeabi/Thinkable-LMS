import React, { useState } from 'react';
import { useNotification } from '../../contexts/NotificationContext';
import './FontTest.css';

interface FontTestProps {
  onComplete: (results: FontTestResults) => void;
}

interface FontOption {
  name: string;
  family: string;
  description: string;
}

interface FontResponse {
  fontName: string;
  rating: number;
  difficulty: 'easy' | 'medium' | 'hard';
  symptoms: {
    lettersMove: boolean;
    eyeStrain: boolean;
    slowReading: boolean;
    blurTogether: boolean;
    headache: boolean;
  };
}

interface FontTestResults {
  fontResponses: FontResponse[];
  preferredFonts: string[];
  dyslexiaIndicators: {
    serifDifficulty: boolean;
    dyslexiaFontPreference: boolean;
    likelyDyslexia: boolean;
  };
}

const FontTest: React.FC<FontTestProps> = ({ onComplete }) => {
  const { showNotification } = useNotification();
  
  const [currentFontIndex, setCurrentFontIndex] = useState(0);
  const [responses, setResponses] = useState<FontResponse[]>([]);
  const [currentRating, setCurrentRating] = useState<number | null>(null);
  const [currentSymptoms, setCurrentSymptoms] = useState({
    lettersMove: false,
    eyeStrain: false,
    slowReading: false,
    blurTogether: false,
    headache: false,
  });

  const fonts: FontOption[] = [
    {
      name: 'Arial',
      family: 'Arial, sans-serif',
      description: 'Clean, modern sans-serif font'
    },
    {
      name: 'Times New Roman',
      family: 'Times New Roman, serif',
      description: 'Traditional serif font (commonly used in books)'
    },
    {
      name: 'Comic Neue',
      family: 'Comic Neue, cursive',
      description: 'Dyslexia-friendly casual font'
    },
    {
      name: 'OpenDyslexic',
      family: 'OpenDyslexic, monospace',
      description: 'Specially designed for dyslexic readers'
    },
    {
      name: 'Verdana',
      family: 'Verdana, sans-serif',
      description: 'Wide, clear font designed for screen reading'
    },
    {
      name: 'Georgia',
      family: 'Georgia, serif',
      description: 'Screen-optimized serif font'
    }
  ];

  const testText = "The quick brown fox jumps over the lazy dog. Reading should be comfortable and easy to understand. Letters should appear clear and distinct. This text helps us understand which fonts work best for your eyes and brain. Some people find certain fonts easier to read than others, and that's completely normal. There are no wrong answers - just tell us how each font feels to you.";

  const currentFont = fonts[currentFontIndex];

  const handleRatingChange = (rating: number) => {
    setCurrentRating(rating);
  };

  const handleSymptomChange = (symptom: string, checked: boolean) => {
    setCurrentSymptoms(prev => ({
      ...prev,
      [symptom]: checked
    }));
  };

  const submitCurrentFont = () => {
    if (currentRating === null) {
      showNotification('Please rate how easy this font is to read', 'warning');
      return;
    }

    const difficulty = currentRating <= 2 ? 'hard' : currentRating >= 4 ? 'easy' : 'medium';
    
    const response: FontResponse = {
      fontName: currentFont.name,
      rating: currentRating,
      difficulty,
      symptoms: { ...currentSymptoms }
    };

    const newResponses = [...responses, response];
    setResponses(newResponses);

    // Reset for next font
    setCurrentRating(null);
    setCurrentSymptoms({
      lettersMove: false,
      eyeStrain: false,
      slowReading: false,
      blurTogether: false,
      headache: false,
    });

    if (currentFontIndex < fonts.length - 1) {
      setCurrentFontIndex(currentFontIndex + 1);
    } else {
      completeFontTest(newResponses);
    }
  };

  const completeFontTest = (allResponses: FontResponse[]) => {
    // Analyze results
    const serifDifficulty = allResponses
      .filter(r => ['Times New Roman', 'Georgia'].includes(r.fontName))
      .some(r => r.difficulty === 'hard');

    const dyslexiaFontPreference = allResponses
      .filter(r => ['Comic Neue', 'OpenDyslexic'].includes(r.fontName))
      .some(r => r.difficulty === 'easy' && r.rating >= 4);

    const likelyDyslexia = serifDifficulty && dyslexiaFontPreference;

    const preferredFonts = allResponses
      .filter(r => r.difficulty === 'easy' && r.rating >= 4)
      .map(r => r.fontName);

    const results: FontTestResults = {
      fontResponses: allResponses,
      preferredFonts,
      dyslexiaIndicators: {
        serifDifficulty,
        dyslexiaFontPreference,
        likelyDyslexia
      }
    };

    onComplete(results);
  };

  const skipFont = () => {
    // If user wants to skip, give neutral rating
    const response: FontResponse = {
      fontName: currentFont.name,
      rating: 3,
      difficulty: 'medium',
      symptoms: {
        lettersMove: false,
        eyeStrain: false,
        slowReading: false,
        blurTogether: false,
        headache: false,
      }
    };

    const newResponses = [...responses, response];
    setResponses(newResponses);

    if (currentFontIndex < fonts.length - 1) {
      setCurrentFontIndex(currentFontIndex + 1);
    } else {
      completeFontTest(newResponses);
    }
  };

  return (
    <div className="font-test-container">
      <div className="font-test-header">
        <div className="font-test-title">
          <div className="font-test-icon">
            <i className="fas fa-font"></i>
          </div>
          <h2>Font Readability Test</h2>
        </div>
        <p>
          We'll show you the same text in different fonts. Please read each sample and tell us 
          how comfortable it feels. This helps us find the best fonts for your reading experience.
        </p>
        
        <div className="progress-indicator">
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ width: `${((currentFontIndex + 1) / fonts.length) * 100}%` }}
            ></div>
          </div>
          <span className="progress-text">
            Font {currentFontIndex + 1} of {fonts.length}
          </span>
        </div>
      </div>

      <div className="font-sample-card">
        <div className="font-info">
          <h3>{currentFont.name}</h3>
          <p className="font-description">{currentFont.description}</p>
        </div>

        <div className="reading-sample-container">
          <div className="reading-instructions">
            <p>Please read the text below carefully:</p>
          </div>
          
          <div 
            className="reading-sample"
            style={{ 
              fontFamily: currentFont.family,
              fontSize: '18px',
              lineHeight: '1.6'
            }}
          >
            {testText}
          </div>
        </div>

        <div className="rating-section">
          <h4>How easy was this font to read?</h4>
          <div className="rating-scale">
            <div className="scale-labels">
              <span>Very Hard</span>
              <span>Very Easy</span>
            </div>
            <div className="rating-buttons">
              {[1, 2, 3, 4, 5].map(rating => (
                <button
                  key={rating}
                  className={`rating-btn ${currentRating === rating ? 'selected' : ''}`}
                  onClick={() => handleRatingChange(rating)}
                >
                  {rating}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="symptoms-section">
          <h4>Did you notice any of these while reading? (Check all that apply)</h4>
          <div className="symptoms-grid">
            <label className="symptom-checkbox">
              <input
                type="checkbox"
                checked={currentSymptoms.lettersMove}
                onChange={(e) => handleSymptomChange('lettersMove', e.target.checked)}
              />
              <span>Letters seem to move or jump around</span>
            </label>
            
            <label className="symptom-checkbox">
              <input
                type="checkbox"
                checked={currentSymptoms.eyeStrain}
                onChange={(e) => handleSymptomChange('eyeStrain', e.target.checked)}
              />
              <span>Eye strain or tired eyes</span>
            </label>
            
            <label className="symptom-checkbox">
              <input
                type="checkbox"
                checked={currentSymptoms.slowReading}
                onChange={(e) => handleSymptomChange('slowReading', e.target.checked)}
              />
              <span>Reading feels slower than usual</span>
            </label>
            
            <label className="symptom-checkbox">
              <input
                type="checkbox"
                checked={currentSymptoms.blurTogether}
                onChange={(e) => handleSymptomChange('blurTogether', e.target.checked)}
              />
              <span>Letters blur together</span>
            </label>
            
            <label className="symptom-checkbox">
              <input
                type="checkbox"
                checked={currentSymptoms.headache}
                onChange={(e) => handleSymptomChange('headache', e.target.checked)}
              />
              <span>Causes headache or discomfort</span>
            </label>
          </div>
        </div>

        <div className="font-test-actions">
          <button onClick={skipFont} className="skip-btn">
            Skip This Font
          </button>
          <button 
            onClick={submitCurrentFont} 
            className="next-btn"
            disabled={currentRating === null}
          >
            {currentFontIndex === fonts.length - 1 ? 'Finish Font Test' : 'Next Font'}
            <i className="fas fa-arrow-right"></i>
          </button>
        </div>
      </div>
    </div>
  );
};

export default FontTest;