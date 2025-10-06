import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { textToSpeechService, TTSState, TTSSettings, TTSVoice } from '../services/TextToSpeechService';
import { useAuth } from './AuthContext';

interface TextToSpeechContextType {
  // State
  state: TTSState;
  settings: TTSSettings;
  availableVoices: TTSVoice[];
  isLoading: boolean;

  // Actions
  speak: (text: string) => Promise<void>;
  pause: () => void;
  resume: () => void;
  stop: () => void;
  skipForward: () => void;
  skipBackward: () => void;
  
  // Settings
  updateSettings: (settings: Partial<TTSSettings>) => void;
  setVoice: (voice: SpeechSynthesisVoice) => void;
  setRate: (rate: number) => void;
  setVolume: (volume: number) => void;
  setPitch: (pitch: number) => void;
  toggleWordHighlighting: () => void;
  
  // Preset integration
  applyPresetOptimizations: (preset: string) => void;
  
  // Text processing for content
  speakCurrentContent: () => Promise<void>;
  getCurrentReadableText: () => string;
  setCurrentContent: (content: any) => void;
}

const TextToSpeechContext = createContext<TextToSpeechContextType | undefined>(undefined);

interface TextToSpeechProviderProps {
  children: React.ReactNode;
}

export const TextToSpeechProvider: React.FC<TextToSpeechProviderProps> = ({ children }) => {
  const { user } = useAuth();
  const [state, setState] = useState<TTSState>(textToSpeechService.getState());
  const [settings, setSettings] = useState<TTSSettings>(textToSpeechService.getSettings());
  const [availableVoices, setAvailableVoices] = useState<TTSVoice[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [currentContent, setCurrentContent] = useState<any>(null);

  // Initialize and subscribe to service
  useEffect(() => {
    const loadVoices = async () => {
      try {
        const voices = await textToSpeechService.getAvailableVoices();
        setAvailableVoices(voices);
      } catch (error) {
        console.error('Failed to load TTS voices:', error);
      } finally {
        setIsLoading(false);
      }
    };

    loadVoices();

    // Subscribe to state changes
    const unsubscribe = textToSpeechService.subscribe((newState) => {
      setState(newState);
    });

    return unsubscribe;
  }, []);

  // Apply user's preset optimizations when user changes
  useEffect(() => {
    if (user?.recommendedPreset) {
      textToSpeechService.applyPresetOptimizations(user.recommendedPreset);
      setSettings(textToSpeechService.getSettings());
    }
  }, [user?.recommendedPreset]);

  // Core TTS actions
  const speak = useCallback(async (text: string) => {
    try {
      await textToSpeechService.speak(text);
      textToSpeechService.recordUsage('speak', { 
        textLength: text.length,
        userPreset: user?.recommendedPreset 
      });
    } catch (error) {
      console.error('Failed to speak text:', error);
      throw error;
    }
  }, [user?.recommendedPreset]);

  const pause = useCallback(() => {
    textToSpeechService.pause();
    textToSpeechService.recordUsage('pause');
  }, []);

  const resume = useCallback(() => {
    textToSpeechService.resume();
    textToSpeechService.recordUsage('resume');
  }, []);

  const stop = useCallback(() => {
    textToSpeechService.stop();
    textToSpeechService.recordUsage('stop');
  }, []);

  const skipForward = useCallback(() => {
    textToSpeechService.skipForward();
    textToSpeechService.recordUsage('skip_forward');
  }, []);

  const skipBackward = useCallback(() => {
    textToSpeechService.skipBackward();
    textToSpeechService.recordUsage('skip_backward');
  }, []);

  // Settings actions
  const updateSettings = useCallback((newSettings: Partial<TTSSettings>) => {
    textToSpeechService.updateSettings(newSettings);
    setSettings(textToSpeechService.getSettings());
  }, []);

  const setVoice = useCallback((voice: SpeechSynthesisVoice) => {
    updateSettings({ voice });
    textToSpeechService.recordUsage('voice_changed', { voiceName: voice.name });
  }, [updateSettings]);

  const setRate = useCallback((rate: number) => {
    updateSettings({ rate });
  }, [updateSettings]);

  const setVolume = useCallback((volume: number) => {
    updateSettings({ volume });
  }, [updateSettings]);

  const setPitch = useCallback((pitch: number) => {
    updateSettings({ pitch });
  }, [updateSettings]);

  const toggleWordHighlighting = useCallback(() => {
    updateSettings({ wordHighlighting: !settings.wordHighlighting });
  }, [settings.wordHighlighting, updateSettings]);

  const applyPresetOptimizations = useCallback((preset: string) => {
    textToSpeechService.applyPresetOptimizations(preset);
    setSettings(textToSpeechService.getSettings());
    textToSpeechService.recordUsage('preset_applied', { preset });
  }, []);

  // Content-specific actions
  const getCurrentReadableText = useCallback((): string => {
    if (!currentContent) return '';

    let text = '';
    
    // Add title
    if (currentContent.title) {
      text += `${currentContent.title}. `;
    }

    // Add description
    if (currentContent.description) {
      text += `${currentContent.description} `;
    }

    // Add extracted text content if available
    if (currentContent.extractedText) {
      text += currentContent.extractedText;
    } else if (currentContent.contentType) {
      // Generate appropriate content description
      switch (currentContent.contentType.toLowerCase()) {
        case 'pdf':
        case 'document':
          text += 'This is a document file. To hear the full content, please use text mode to extract the text first.';
          break;
        case 'video':
          text += 'This is a video file. ';
          if (currentContent.hasTranscript) {
            text += 'Transcript available. ';
          } else {
            text += 'To hear the content, please play the video. ';
          }
          break;
        case 'audio':
          text += 'This is an audio file. Please play the audio directly for the full experience.';
          break;
        case 'image':
          text += 'This is an image file. ';
          if (currentContent.altText) {
            text += `Image description: ${currentContent.altText}`;
          } else {
            text += 'No text description available for this image.';
          }
          break;
        default:
          text += 'Content available for viewing.';
      }
    }

    return text.trim() || 'No readable content available.';
  }, [currentContent]);

  const speakCurrentContent = useCallback(async () => {
    const text = getCurrentReadableText();
    if (text) {
      await speak(text);
      textToSpeechService.recordUsage('speak_content', {
        contentType: currentContent?.contentType,
        contentId: currentContent?.id
      });
    }
  }, [getCurrentReadableText, speak, currentContent]);

  const contextValue: TextToSpeechContextType = {
    // State
    state,
    settings,
    availableVoices,
    isLoading,

    // Actions
    speak,
    pause,
    resume,
    stop,
    skipForward,
    skipBackward,

    // Settings
    updateSettings,
    setVoice,
    setRate,
    setVolume,
    setPitch,
    toggleWordHighlighting,

    // Preset integration
    applyPresetOptimizations,

    // Content
    speakCurrentContent,
    getCurrentReadableText,
    setCurrentContent
  };

  return (
    <TextToSpeechContext.Provider value={contextValue}>
      {children}
    </TextToSpeechContext.Provider>
  );
};

export const useTextToSpeech = (): TextToSpeechContextType => {
  const context = useContext(TextToSpeechContext);
  if (context === undefined) {
    throw new Error('useTextToSpeech must be used within a TextToSpeechProvider');
  }
  return context;
};

export default TextToSpeechContext;