interface UIPreset {
  name: string;
  description: string;
  cssVariables: {
    [key: string]: string;
  };
  fontFamily: string;
  fontSize: string;
  lineHeight: string;
  letterSpacing: string;
  animationDuration: string;
  focusIndicatorWidth: string;
  colorScheme: 'light' | 'dark' | 'high-contrast';
}

export const adaptiveUIPresets: Record<string, UIPreset> = {
  STANDARD_ADAPTIVE: {
    name: 'Standard Adaptive',
    description: 'Balanced accommodations for general learning support',
    cssVariables: {
      '--primary-color': '#2c5aa0',
      '--secondary-color': '#28a745',
      '--background-color': '#ffffff',
      '--text-color': '#2c3e50',
      '--border-color': '#e2e8f0',
      '--focus-color': '#007bff',
      '--success-color': '#28a745',
      '--warning-color': '#ffc107',
      '--error-color': '#dc3545',
      '--card-shadow': '0 4px 15px rgba(0, 0, 0, 0.1)',
      '--border-radius': '8px',
      '--spacing-unit': '1rem'
    },
    fontFamily: 'Arial, sans-serif',
    fontSize: '16px',
    lineHeight: '1.6',
    letterSpacing: '0',
    animationDuration: '0.3s',
    focusIndicatorWidth: '2px',
    colorScheme: 'light'
  },

  FOCUS_ENHANCED: {
    name: 'Focus Enhanced',
    description: 'Reduced visual clutter with enhanced focus indicators for ADHD support',
    cssVariables: {
      '--primary-color': '#4a90e2',
      '--secondary-color': '#5cb85c',
      '--background-color': '#f8f9fa',
      '--text-color': '#2c3e50',
      '--border-color': '#d1ecf1',
      '--focus-color': '#007bff',
      '--success-color': '#5cb85c',
      '--warning-color': '#f0ad4e',
      '--error-color': '#d9534f',
      '--card-shadow': '0 2px 8px rgba(0, 0, 0, 0.08)',
      '--border-radius': '12px',
      '--spacing-unit': '1.25rem'
    },
    fontFamily: 'Segoe UI, Tahoma, Geneva, sans-serif',
    fontSize: '18px',
    lineHeight: '1.8',
    letterSpacing: '0.5px',
    animationDuration: '0.2s',
    focusIndicatorWidth: '4px',
    colorScheme: 'light'
  },

  FOCUS_CALM: {
    name: 'Focus Calm',
    description: 'Minimal distractions with calming colors for attention and sensory support',
    cssVariables: {
      '--primary-color': '#6c757d',
      '--secondary-color': '#20c997',
      '--background-color': '#f1f3f4',
      '--text-color': '#495057',
      '--border-color': '#ced4da',
      '--focus-color': '#20c997',
      '--success-color': '#20c997',
      '--warning-color': '#fd7e14',
      '--error-color': '#e74c3c',
      '--card-shadow': '0 1px 4px rgba(0, 0, 0, 0.05)',
      '--border-radius': '16px',
      '--spacing-unit': '1.5rem'
    },
    fontFamily: 'Inter, system-ui, sans-serif',
    fontSize: '17px',
    lineHeight: '2.0',
    letterSpacing: '0.3px',
    animationDuration: '0.5s',
    focusIndicatorWidth: '3px',
    colorScheme: 'light'
  },

  READING_SUPPORT: {
    name: 'Reading Support',
    description: 'Dyslexia-friendly fonts with high contrast and enhanced readability',
    cssVariables: {
      '--primary-color': '#2c5aa0',
      '--secondary-color': '#17a2b8',
      '--background-color': '#fffdf5', // Warmer cream background
      '--text-color': '#000000',        // Higher contrast black text
      '--border-color': '#c4c4c4',      // More visible borders
      '--focus-color': '#ff6b35',       // High contrast orange focus
      '--success-color': '#2d8a47',     // Darker green for contrast
      '--warning-color': '#e67e22',     // Darker orange for contrast
      '--error-color': '#c0392b',       // Darker red for contrast
      '--card-shadow': '0 4px 16px rgba(0, 0, 0, 0.15)', // More prominent shadows
      '--border-radius': '8px',
      '--spacing-unit': '2rem'          // More generous spacing
    },
    fontFamily: 'OpenDyslexic, Comic Neue, Verdana, Arial, sans-serif', // Proper dyslexia-friendly font
    fontSize: '22px',                   // Larger, more readable
    lineHeight: '2.4',                  // More line spacing
    letterSpacing: '1.2px',             // More letter spacing
    animationDuration: '0.05s',         // Minimal animations
    focusIndicatorWidth: '4px',         // Thicker focus indicators
    colorScheme: 'high-contrast'
  },

  SOCIAL_SIMPLE: {
    name: 'Social Simple',
    description: 'Clear social cues and simplified interface for autism support',
    cssVariables: {
      '--primary-color': '#5d4e75',
      '--secondary-color': '#7b68ee',
      '--background-color': '#ffffff',
      '--text-color': '#2c3e50',
      '--border-color': '#e9ecef',
      '--focus-color': '#7b68ee',
      '--success-color': '#32cd32',
      '--warning-color': '#ffa500',
      '--error-color': '#ff6347',
      '--card-shadow': '0 6px 20px rgba(0, 0, 0, 0.08)',
      '--border-radius': '4px',
      '--spacing-unit': '2rem'
    },
    fontFamily: 'Roboto, Arial, sans-serif',
    fontSize: '19px',
    lineHeight: '1.9',
    letterSpacing: '0.2px',
    animationDuration: '0.4s',
    focusIndicatorWidth: '2px',
    colorScheme: 'light'
  },

  SENSORY_CALM: {
    name: 'Sensory Calm',
    description: 'Soft colors and reduced animations for sensory processing support',
    cssVariables: {
      '--primary-color': '#8e9aaf',
      '--secondary-color': '#a8dadc',
      '--background-color': '#f8f8f8',
      '--text-color': '#457b9d',
      '--border-color': '#dde5e9',
      '--focus-color': '#f1faee',
      '--success-color': '#a8dadc',
      '--warning-color': '#f4a261',
      '--error-color': '#e76f51',
      '--card-shadow': '0 2px 6px rgba(0, 0, 0, 0.04)',
      '--border-radius': '20px',
      '--spacing-unit': '1.25rem'
    },
    fontFamily: 'Source Sans Pro, system-ui, sans-serif',
    fontSize: '16px',
    lineHeight: '1.7',
    letterSpacing: '0.1px',
    animationDuration: '0.8s',
    focusIndicatorWidth: '2px',
    colorScheme: 'light'
  }
};

export const applyUIPreset = (presetName: string): void => {
  const preset = adaptiveUIPresets[presetName];
  if (!preset) {
    console.warn(`UI Preset '${presetName}' not found. Using STANDARD_ADAPTIVE.`);
    applyUIPreset('STANDARD_ADAPTIVE');
    return;
  }

  const root = document.documentElement;
  const body = document.body;
  
  // Remove all existing adaptive classes
  body.classList.remove('standard-adaptive', 'dyslexia-adaptive', 'adhd-adaptive', 'autism-adaptive',
                       'sensory-adaptive', 'focus-adaptive');
  
  // Apply CSS custom properties
  Object.entries(preset.cssVariables).forEach(([property, value]) => {
    root.style.setProperty(property, value);
  });

  // Apply typography settings
  root.style.setProperty('--font-family', preset.fontFamily);
  root.style.setProperty('--font-size-base', preset.fontSize);
  root.style.setProperty('--line-height-base', preset.lineHeight);
  root.style.setProperty('--letter-spacing-base', preset.letterSpacing);
  root.style.setProperty('--animation-duration', preset.animationDuration);
  root.style.setProperty('--focus-indicator-width', preset.focusIndicatorWidth);

  // Apply color scheme
  root.setAttribute('data-color-scheme', preset.colorScheme);
  
  // Apply preset-specific adaptive CSS classes for dramatic visual changes
  switch (presetName) {
    case 'STANDARD_ADAPTIVE':
      body.classList.add('standard-adaptive');
      break;
    case 'READING_SUPPORT':
      body.classList.add('dyslexia-adaptive');
      break;
    case 'FOCUS_ENHANCED':
    case 'FOCUS_CALM':
      body.classList.add('adhd-adaptive', 'focus-adaptive');
      break;
    case 'SOCIAL_SIMPLE':
      body.classList.add('autism-adaptive');
      break;
    case 'SENSORY_CALM':
      body.classList.add('sensory-adaptive');
      break;
    default:
      break;
  }
  
  // Store current preset in localStorage
  localStorage.setItem('thinkable-ui-preset', presetName);
  
  console.log(`🎨 Applied UI Preset: ${preset.name}`);
  console.log(`🏷️ Body classes:`, body.className);
  console.log(`🎯 CSS Variables applied:`, Object.keys(preset.cssVariables));
  console.log(`📱 Font family:`, preset.fontFamily);
};

export const getCurrentPreset = (): string => {
  return localStorage.getItem('thinkable-ui-preset') || 'STANDARD_ADAPTIVE';
};

export const getPresetDescription = (presetName: string): string => {
  const preset = adaptiveUIPresets[presetName];
  return preset ? preset.description : 'Standard adaptive interface';
};

// Initialize UI preset on page load
export const initializeUIPreset = () => {
  const savedPreset = getCurrentPreset();
  applyUIPreset(savedPreset);
};

// Auto-apply preset based on assessment results
export const applyPresetFromAssessment = (assessmentResults: {
  recommendedPreset: string;
  attentionScore: number;
  readingDifficultyScore: number;
  sensoryProcessingScore: number;
  socialCommunicationScore: number;
}) => {
  let { recommendedPreset } = assessmentResults;
  
  // Handle different preset name formats from backend
  const presetMappings: Record<string, string> = {
    'standard': 'STANDARD_ADAPTIVE',
    'standard_adaptive': 'STANDARD_ADAPTIVE',
    'dyslexia': 'READING_SUPPORT', 
    'reading_support': 'READING_SUPPORT',
    'adhd': 'FOCUS_ENHANCED',
    'focus_enhanced': 'FOCUS_ENHANCED',
    'focus_calm': 'FOCUS_CALM',
    'autism': 'SOCIAL_SIMPLE',
    'social_simple': 'SOCIAL_SIMPLE',
    'sensory': 'SENSORY_CALM',
    'sensory_calm': 'SENSORY_CALM'
  };
  
  // Try to map lowercase/different names to uppercase constants
  if (presetMappings[recommendedPreset?.toLowerCase()]) {
    recommendedPreset = presetMappings[recommendedPreset.toLowerCase()];
  }
  
  if (adaptiveUIPresets[recommendedPreset]) {
    applyUIPreset(recommendedPreset);
    
    // Store assessment-based customizations
    localStorage.setItem('thinkable-assessment-results', JSON.stringify(assessmentResults));
    
    return recommendedPreset;
  } else {
    console.warn(`Recommended preset '${recommendedPreset}' not found. Using STANDARD_ADAPTIVE.`);
    applyUIPreset('STANDARD_ADAPTIVE');
    return 'STANDARD_ADAPTIVE';
  }
};