import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { 
  applyUIPreset, 
  getCurrentPreset, 
  getPresetDescription,
  applyPresetFromAssessment,
  adaptiveUIPresets 
} from '../utils/adaptiveUIPresets';
import { useAuth } from './AuthContext';
import config from '../services/config';

interface AdaptiveUIContextType {
  currentPreset: string;
  presetDescription: string;
  availablePresets: string[];
  applyPreset: (presetName: string) => void;
  applyAssessmentPreset: (assessmentResults: AssessmentResults) => void;
  resetToDefault: () => void;
  isHighContrast: boolean;
  isDyslexiaFriendly: boolean;
  isADHDFriendly: boolean;
  isAutismFriendly: boolean;
  isSensoryFriendly: boolean;
  toggleAccessibilityFeature: (feature: AccessibilityFeature) => void;
}

interface AssessmentResults {
  recommendedPreset: string;
  attentionScore: number;
  readingDifficultyScore: number;
  sensoryProcessingScore: number;
  socialCommunicationScore: number;
}

type AccessibilityFeature = 'highContrast' | 'dyslexiaFriendly' | 'adhdFriendly' | 'autismFriendly' | 'sensoryFriendly';

const AdaptiveUIContext = createContext<AdaptiveUIContextType | undefined>(undefined);

export const useAdaptiveUI = () => {
  const context = useContext(AdaptiveUIContext);
  if (context === undefined) {
    throw new Error('useAdaptiveUI must be used within an AdaptiveUIProvider');
  }
  return context;
};

interface AdaptiveUIProviderProps {
  children: ReactNode;
}

export const AdaptiveUIProvider: React.FC<AdaptiveUIProviderProps> = ({ children }) => {
  const { user } = useAuth();
  const [currentPreset, setCurrentPreset] = useState<string>('STANDARD_ADAPTIVE');
  const [isHighContrast, setIsHighContrast] = useState(false);
  const [isDyslexiaFriendly, setIsDyslexiaFriendly] = useState(false);
  const [isADHDFriendly, setIsADHDFriendly] = useState(false);
  const [isAutismFriendly, setIsAutismFriendly] = useState(false);
  const [isSensoryFriendly, setIsSensoryFriendly] = useState(false);

  // Load user preferences from backend
  const loadUserPreferences = async () => {
    try {
      // Load from backend API first, fallback to localStorage
      if (user?.email) {
        const response = await fetch(`${config.apiBaseUrl}/api/user-preferences?username=${encodeURIComponent(user.email)}`);
        if (response.ok) {
          const backendPrefs = await response.json();
          const presetToApply = backendPrefs.currentPreset || 'STANDARD_ADAPTIVE';
          
          setCurrentPreset(presetToApply);
          applyUIPreset(presetToApply);
          
          // Apply accessibility features from backend
          const customSettings = backendPrefs.customSettings || {};
          setIsHighContrast(customSettings.highContrast || false);
          setIsDyslexiaFriendly(customSettings.dyslexiaFriendly || false);
          setIsADHDFriendly(customSettings.adhdFriendly || false);
          setIsAutismFriendly(customSettings.autismFriendly || false);
          setIsSensoryFriendly(customSettings.sensoryFriendly || false);
          
          // Apply features to document
          applyAccessibilityFeatures(customSettings);
          return;
        }
      }
      
      // Fallback to localStorage if backend fails or user not logged in
      const savedPreset = getCurrentPreset();
      setCurrentPreset(savedPreset);
      applyUIPreset(savedPreset);

      // Load accessibility features from localStorage
      const savedFeatures = localStorage.getItem('thinkable-accessibility-features');
      if (savedFeatures) {
        const features = JSON.parse(savedFeatures);
        setIsHighContrast(features.highContrast || false);
        setIsDyslexiaFriendly(features.dyslexiaFriendly || false);
        setIsADHDFriendly(features.adhdFriendly || false);
        setIsAutismFriendly(features.autismFriendly || false);
        setIsSensoryFriendly(features.sensoryFriendly || false);
        
        // Apply features to document
        applyAccessibilityFeatures(features);
      }

      // Check if user has assessment results and apply appropriate preset
      const assessmentResults = localStorage.getItem('thinkable-assessment-results');
      if (assessmentResults) {
        try {
          const results = JSON.parse(assessmentResults);
          if (results.recommendedPreset && results.recommendedPreset !== savedPreset) {
            applyPreset(results.recommendedPreset);
          }
        } catch (error) {
          console.warn('Failed to parse saved assessment results:', error);
        }
      }
    } catch (error) {
      console.error('Failed to load user preferences in AdaptiveUIContext:', error);
      // Fallback to localStorage
      const savedPreset = getCurrentPreset();
      setCurrentPreset(savedPreset);
      applyUIPreset(savedPreset);
    }
  };

  // Initialize on mount
  useEffect(() => {
    loadUserPreferences();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const applyAccessibilityFeatures = (features: any) => {
    const body = document.body;
    
    // Remove existing accessibility classes
    body.classList.remove('high-contrast', 'dyslexia-friendly', 'adhd-friendly', 'autism-friendly', 'sensory-friendly');
    
    // Apply new classes
    if (features.highContrast) body.classList.add('high-contrast');
    if (features.dyslexiaFriendly) body.classList.add('dyslexia-friendly');
    if (features.adhdFriendly) body.classList.add('adhd-friendly');
    if (features.autismFriendly) body.classList.add('autism-friendly');
    if (features.sensoryFriendly) body.classList.add('sensory-friendly');
  };

  const applyPreset = (presetName: string) => {
    if (adaptiveUIPresets[presetName]) {
      applyUIPreset(presetName);
      setCurrentPreset(presetName);
      
      // Auto-enable related accessibility features based on preset
      autoEnableAccessibilityFeatures(presetName);
    } else {
      console.warn(`Preset '${presetName}' not found`);
    }
  };

  const autoEnableAccessibilityFeatures = (presetName: string) => {
    let features: any = {};
    
    switch (presetName) {
      case 'READING_SUPPORT':
        features = { ...features, dyslexiaFriendly: true, highContrast: true };
        setIsDyslexiaFriendly(true);
        setIsHighContrast(true);
        break;
      case 'FOCUS_ENHANCED':
      case 'FOCUS_CALM':
        features = { ...features, adhdFriendly: true };
        setIsADHDFriendly(true);
        break;
      case 'SOCIAL_SIMPLE':
        features = { ...features, autismFriendly: true };
        setIsAutismFriendly(true);
        break;
      case 'SENSORY_CALM':
        features = { ...features, sensoryFriendly: true };
        setIsSensoryFriendly(true);
        break;
    }
    
    if (Object.keys(features).length > 0) {
      applyAccessibilityFeatures({ ...getCurrentAccessibilityFeatures(), ...features });
      saveAccessibilityFeatures({ ...getCurrentAccessibilityFeatures(), ...features });
    }
  };

  const getCurrentAccessibilityFeatures = () => ({
    highContrast: isHighContrast,
    dyslexiaFriendly: isDyslexiaFriendly,
    adhdFriendly: isADHDFriendly,
    autismFriendly: isAutismFriendly,
    sensoryFriendly: isSensoryFriendly
  });

  const saveAccessibilityFeatures = (features: any) => {
    localStorage.setItem('thinkable-accessibility-features', JSON.stringify(features));
  };

  const applyAssessmentPreset = (assessmentResults: AssessmentResults) => {
    const appliedPreset = applyPresetFromAssessment(assessmentResults);
    setCurrentPreset(appliedPreset);
    autoEnableAccessibilityFeatures(appliedPreset);
  };

  const resetToDefault = () => {
    applyPreset('STANDARD_ADAPTIVE');
    
    // Reset all accessibility features
    setIsHighContrast(false);
    setIsDyslexiaFriendly(false);
    setIsADHDFriendly(false);
    setIsAutismFriendly(false);
    setIsSensoryFriendly(false);
    
    applyAccessibilityFeatures({});
    localStorage.removeItem('thinkable-accessibility-features');
    localStorage.removeItem('thinkable-assessment-results');
  };

  const toggleAccessibilityFeature = (feature: AccessibilityFeature) => {
    let newFeatures = getCurrentAccessibilityFeatures();
    
    switch (feature) {
      case 'highContrast':
        const newHighContrast = !isHighContrast;
        setIsHighContrast(newHighContrast);
        newFeatures.highContrast = newHighContrast;
        break;
      case 'dyslexiaFriendly':
        const newDyslexiaFriendly = !isDyslexiaFriendly;
        setIsDyslexiaFriendly(newDyslexiaFriendly);
        newFeatures.dyslexiaFriendly = newDyslexiaFriendly;
        break;
      case 'adhdFriendly':
        const newADHDFriendly = !isADHDFriendly;
        setIsADHDFriendly(newADHDFriendly);
        newFeatures.adhdFriendly = newADHDFriendly;
        break;
      case 'autismFriendly':
        const newAutismFriendly = !isAutismFriendly;
        setIsAutismFriendly(newAutismFriendly);
        newFeatures.autismFriendly = newAutismFriendly;
        break;
      case 'sensoryFriendly':
        const newSensoryFriendly = !isSensoryFriendly;
        setIsSensoryFriendly(newSensoryFriendly);
        newFeatures.sensoryFriendly = newSensoryFriendly;
        break;
    }
    
    applyAccessibilityFeatures(newFeatures);
    saveAccessibilityFeatures(newFeatures);
  };

  const contextValue: AdaptiveUIContextType = {
    currentPreset,
    presetDescription: getPresetDescription(currentPreset),
    availablePresets: Object.keys(adaptiveUIPresets),
    applyPreset,
    applyAssessmentPreset,
    resetToDefault,
    isHighContrast,
    isDyslexiaFriendly,
    isADHDFriendly,
    isAutismFriendly,
    isSensoryFriendly,
    toggleAccessibilityFeature
  };

  return (
    <AdaptiveUIContext.Provider value={contextValue}>
      {children}
    </AdaptiveUIContext.Provider>
  );
};