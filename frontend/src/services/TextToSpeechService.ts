// Text-to-Speech Service for Accessibility
// Provides comprehensive TTS functionality with adaptive features

export interface TTSVoice {
  voice: SpeechSynthesisVoice;
  name: string;
  lang: string;
  isDefault: boolean;
}

export interface TTSSettings {
  rate: number;
  volume: number;
  pitch: number;
  voice: SpeechSynthesisVoice | null;
  wordHighlighting: boolean;
  pauseOnPunctuation: boolean;
}

export interface TTSState {
  isAvailable: boolean;
  isPlaying: boolean;
  isPaused: boolean;
  currentText: string;
  currentPosition: number;
  totalLength: number;
  error: string | null;
}

class TextToSpeechService {
  private utterance: SpeechSynthesisUtterance | null = null;
  private settings: TTSSettings;
  private state: TTSState;
  private listeners: Set<(state: TTSState) => void> = new Set();
  private textChunks: string[] = [];
  private currentChunkIndex: number = 0;
  private maxChunkLength: number = 200; // Optimal chunk size for reliability

  constructor() {
    this.settings = {
      rate: 1.0,
      volume: 1.0,
      pitch: 1.0,
      voice: null,
      wordHighlighting: true,
      pauseOnPunctuation: false
    };

    this.state = {
      isAvailable: 'speechSynthesis' in window,
      isPlaying: false,
      isPaused: false,
      currentText: '',
      currentPosition: 0,
      totalLength: 0,
      error: null
    };

    this.loadSettings();
    this.initializeVoices();
  }

  // Public API Methods
  public subscribe(listener: (state: TTSState) => void): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  public getState(): TTSState {
    return { ...this.state };
  }

  public getSettings(): TTSSettings {
    return { ...this.settings };
  }

  public async getAvailableVoices(): Promise<TTSVoice[]> {
    return new Promise((resolve) => {
      const getVoices = () => {
        const voices = speechSynthesis.getVoices();
        const ttsVoices: TTSVoice[] = voices.map(voice => ({
          voice,
          name: voice.name,
          lang: voice.lang,
          isDefault: voice.default
        }));
        resolve(ttsVoices);
      };

      if (speechSynthesis.getVoices().length > 0) {
        getVoices();
      } else {
        speechSynthesis.onvoiceschanged = getVoices;
      }
    });
  }

  public updateSettings(newSettings: Partial<TTSSettings>): void {
    this.settings = { ...this.settings, ...newSettings };
    this.saveSettings();
    this.notifyListeners();
  }

  public async speak(text: string): Promise<void> {
    if (!this.state.isAvailable) {
      throw new Error('Text-to-speech is not supported in this browser');
    }

    this.stop(); // Stop any current speech
    
    this.state.currentText = text;
    this.state.totalLength = text.length;
    this.state.currentPosition = 0;
    this.state.error = null;

    // Split text into manageable chunks
    this.textChunks = this.chunkText(text);
    this.currentChunkIndex = 0;

    await this.speakNextChunk();
  }

  public pause(): void {
    if (this.state.isPlaying && !this.state.isPaused) {
      speechSynthesis.pause();
      this.state.isPaused = true;
      this.notifyListeners();
    }
  }

  public resume(): void {
    if (this.state.isPaused) {
      speechSynthesis.resume();
      this.state.isPaused = false;
      this.notifyListeners();
    }
  }

  public stop(): void {
    speechSynthesis.cancel();
    this.utterance = null;
    this.state.isPlaying = false;
    this.state.isPaused = false;
    this.state.currentPosition = 0;
    this.textChunks = [];
    this.currentChunkIndex = 0;
    this.notifyListeners();
  }

  public skipForward(seconds: number = 5): void {
    // Implementation for skipping forward in long text
    if (this.state.isPlaying && this.textChunks.length > 1) {
      this.currentChunkIndex = Math.min(this.currentChunkIndex + 1, this.textChunks.length - 1);
      this.speakNextChunk();
    }
  }

  public skipBackward(seconds: number = 5): void {
    // Implementation for skipping backward in long text
    if (this.state.isPlaying && this.textChunks.length > 1) {
      this.currentChunkIndex = Math.max(this.currentChunkIndex - 1, 0);
      this.speakNextChunk();
    }
  }

  // Neurodivergent-specific adaptations
  public applyPresetOptimizations(preset: string): void {
    switch (preset) {
      case 'ADHD_SUPPORT':
        this.updateSettings({
          rate: 0.9, // Slightly slower for ADHD
          pauseOnPunctuation: true,
          wordHighlighting: true
        });
        break;
      case 'DYSLEXIA_SUPPORT':
        this.updateSettings({
          rate: 0.8, // Slower for comprehension
          wordHighlighting: true,
          pauseOnPunctuation: true
        });
        break;
      case 'AUTISM_SUPPORT':
        this.updateSettings({
          rate: 1.0, // Standard rate for predictability
          pauseOnPunctuation: false,
          wordHighlighting: false // Less visual distraction
        });
        break;
      case 'SENSORY_CALM':
        this.updateSettings({
          rate: 0.7, // Very slow and calm
          volume: 0.8, // Slightly quieter
          pauseOnPunctuation: true
        });
        break;
      default:
        this.updateSettings({
          rate: 1.0,
          volume: 1.0,
          pauseOnPunctuation: false,
          wordHighlighting: true
        });
    }
  }

  // Private methods
  private async initializeVoices(): Promise<void> {
    const voices = await this.getAvailableVoices();
    if (voices.length > 0 && !this.settings.voice) {
      // Try to find a good English voice
      const englishVoice = voices.find(v => 
        v.lang.startsWith('en') && (
          v.name.includes('Natural') || 
          v.name.includes('Enhanced') ||
          v.isDefault
        )
      );
      
      this.settings.voice = englishVoice?.voice || voices[0].voice;
    }
  }

  private chunkText(text: string): string[] {
    if (text.length <= this.maxChunkLength) {
      return [text];
    }

    const sentences = text.split(/[.!?]+/).filter(s => s.trim().length > 0);
    const chunks: string[] = [];
    let currentChunk = '';

    for (const sentence of sentences) {
      const trimmedSentence = sentence.trim();
      if (currentChunk.length + trimmedSentence.length <= this.maxChunkLength) {
        currentChunk += (currentChunk ? '. ' : '') + trimmedSentence;
      } else {
        if (currentChunk) {
          chunks.push(currentChunk + '.');
        }
        currentChunk = trimmedSentence;
      }
    }

    if (currentChunk) {
      chunks.push(currentChunk + '.');
    }

    return chunks.length > 0 ? chunks : [text];
  }

  private async speakNextChunk(): Promise<void> {
    if (this.currentChunkIndex >= this.textChunks.length) {
      this.onSpeechComplete();
      return;
    }

    const chunk = this.textChunks[this.currentChunkIndex];
    
    this.utterance = new SpeechSynthesisUtterance(chunk);
    this.utterance.rate = this.settings.rate;
    this.utterance.volume = this.settings.volume;
    this.utterance.pitch = this.settings.pitch;

    if (this.settings.voice) {
      this.utterance.voice = this.settings.voice;
    }

    // Set up event listeners
    this.utterance.onstart = () => {
      this.state.isPlaying = true;
      this.state.isPaused = false;
      this.notifyListeners();
    };

    this.utterance.onend = () => {
      this.currentChunkIndex++;
      setTimeout(() => this.speakNextChunk(), 100); // Small pause between chunks
    };

    this.utterance.onerror = (event) => {
      console.error('TTS Error:', event.error);
      this.state.error = event.error;
      this.state.isPlaying = false;
      this.notifyListeners();
    };

    // Handle word boundary for highlighting
    if (this.settings.wordHighlighting) {
      this.utterance.onboundary = (event) => {
        if (event.name === 'word') {
          this.state.currentPosition = this.getGlobalPosition(this.currentChunkIndex, event.charIndex);
          this.notifyListeners();
        }
      };
    }

    speechSynthesis.speak(this.utterance);
  }

  private getGlobalPosition(chunkIndex: number, chunkPosition: number): number {
    let position = 0;
    for (let i = 0; i < chunkIndex; i++) {
      position += this.textChunks[i].length;
    }
    return position + chunkPosition;
  }

  private onSpeechComplete(): void {
    this.state.isPlaying = false;
    this.state.isPaused = false;
    this.state.currentPosition = this.state.totalLength;
    this.utterance = null;
    this.textChunks = [];
    this.currentChunkIndex = 0;
    this.notifyListeners();
  }

  private notifyListeners(): void {
    this.listeners.forEach(listener => listener(this.state));
  }

  private saveSettings(): void {
    try {
      const settingsToSave = {
        ...this.settings,
        voice: this.settings.voice ? {
          name: this.settings.voice.name,
          lang: this.settings.voice.lang
        } : null
      };
      localStorage.setItem('ttsSettings', JSON.stringify(settingsToSave));
    } catch (error) {
      console.warn('Failed to save TTS settings:', error);
    }
  }

  private loadSettings(): void {
    try {
      const saved = localStorage.getItem('ttsSettings');
      if (saved) {
        const parsedSettings = JSON.parse(saved);
        this.settings = {
          ...this.settings,
          ...parsedSettings,
          voice: null // Voice will be set when voices are loaded
        };
      }
    } catch (error) {
      console.warn('Failed to load TTS settings:', error);
    }
  }

  // Analytics and usage tracking
  public recordUsage(action: string, context?: any): void {
    try {
      const usageData = {
        action,
        context,
        timestamp: new Date().toISOString(),
        settings: this.settings
      };
      
      // Send to backend analytics
      fetch('/api/accessibility/tts-usage', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(usageData)
      }).catch(error => console.warn('Failed to record TTS usage:', error));
    } catch (error) {
      console.warn('Failed to record TTS usage:', error);
    }
  }
}

// Export singleton instance
export const textToSpeechService = new TextToSpeechService();
export default TextToSpeechService;