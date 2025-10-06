// ThinkAble Adaptive UI System
class AdaptiveUISystem {
    constructor() {
        this.apiBase = '/api/assessment';
        this.userId = this.getCurrentUserId();
        this.currentPreset = 'standard';
        this.customSettings = {};
        this.isInitialized = false;
        this.persistenceKey = 'adaptiveUISettings';
        
        // Default CSS variables for all presets
        this.defaultVariables = {
            // Colors
            '--primary-color': '#007bff',
            '--secondary-color': '#6c757d',
            '--success-color': '#28a745',
            '--warning-color': '#ffc107',
            '--danger-color': '#dc3545',
            '--light-color': '#f8f9fa',
            '--dark-color': '#343a40',
            
            // Background colors
            '--bg-primary': '#ffffff',
            '--bg-secondary': '#f8f9fa',
            '--bg-card': '#ffffff',
            '--bg-nav': '#ffffff',
            
            // Text colors
            '--text-primary': '#212529',
            '--text-secondary': '#6c757d',
            '--text-muted': '#868e96',
            '--text-white': '#ffffff',
            
            // Typography
            '--font-family-primary': '"Segoe UI", Tahoma, Geneva, Verdana, sans-serif',
            '--font-family-secondary': '"Georgia", "Times New Roman", serif',
            '--font-size-base': '16px',
            '--font-size-small': '14px',
            '--font-size-large': '18px',
            '--font-size-h1': '2.5rem',
            '--font-size-h2': '2rem',
            '--font-size-h3': '1.75rem',
            '--line-height-base': '1.5',
            '--font-weight-normal': '400',
            '--font-weight-bold': '700',
            
            // Spacing
            '--spacing-xs': '0.25rem',
            '--spacing-sm': '0.5rem',
            '--spacing-md': '1rem',
            '--spacing-lg': '1.5rem',
            '--spacing-xl': '3rem',
            
            // Layout
            '--border-radius': '0.375rem',
            '--border-width': '1px',
            '--shadow-sm': '0 0.125rem 0.25rem rgba(0, 0, 0, 0.075)',
            '--shadow-md': '0 0.5rem 1rem rgba(0, 0, 0, 0.15)',
            '--shadow-lg': '0 1rem 3rem rgba(0, 0, 0, 0.175)',
            
            // Animation
            '--transition-base': '0.3s ease',
            '--transition-fast': '0.15s ease',
            '--transition-slow': '0.5s ease',
            
            // Focus and interaction
            '--focus-ring': '0 0 0 0.2rem rgba(0, 123, 255, 0.25)',
            '--hover-opacity': '0.8',
            
            // Timer specific
            '--timer-bg': '#ffffff',
            '--timer-border': '#dee2e6',
            '--timer-text': '#495057',
            '--timer-accent': '#007bff'
        };
        
        // Preset configurations
        this.presets = {
            standard: {
                name: 'Standard Mode',
                description: 'Balanced, accessible design for general use',
                variables: {}  // Uses defaults
            },
            
            adhd: {
                name: 'ADHD Support',
                description: 'Larger text, clear focus, reduced distractions',
                variables: {
                    // Simplified color scheme with strong contrast
                    '--primary-color': '#0056b3',
                    '--secondary-color': '#868e96',
                    '--bg-primary': '#ffffff',
                    '--bg-secondary': '#f1f3f5',
                    '--bg-card': '#ffffff',
                    
                    // Enhanced focus indicators
                    '--focus-ring': '0 0 0 0.3rem rgba(0, 86, 179, 0.6)',
                    '--border-width': '3px',
                    
                    // Larger, bolder typography for ADHD
                    '--font-family-primary': '"Segoe UI", Arial, sans-serif',
                    '--font-size-base': '20px',
                    '--font-size-small': '18px',
                    '--font-size-large': '24px',
                    '--font-size-h1': '3.2rem',
                    '--font-size-h2': '2.8rem',
                    '--font-size-h3': '2.4rem',
                    '--line-height-base': '1.7',
                    '--font-weight-normal': '600',
                    '--font-weight-bold': '800',
                    
                    // Increased spacing for clarity
                    '--spacing-xs': '0.5rem',
                    '--spacing-sm': '0.8rem',
                    '--spacing-md': '2rem',
                    '--spacing-lg': '3rem',
                    '--spacing-xl': '4.5rem',
                    '--border-radius': '0.8rem',
                    '--shadow-md': '0 0.8rem 1.5rem rgba(0, 0, 0, 0.2)',
                    
                    // Calmer animations
                    '--transition-base': '0.15s ease',
                    '--hover-opacity': '0.9',
                    
                    // Timer optimizations for ADHD
                    '--timer-bg': '#f8f9fa',
                    '--timer-border': '#0056b3',
                    '--timer-text': '#212529',
                    '--timer-accent': '#0056b3'
                }
            },
            
            dyslexia: {
                name: 'Dyslexia Support',
                description: 'Enhanced readability with OpenDyslexic fonts',
                variables: {
                    // Dyslexia-friendly typography with OpenDyslexic
                    '--font-family-primary': 'OpenDyslexic, Arial, sans-serif',
                    '--font-family-secondary': 'OpenDyslexic, Arial, sans-serif',
                    '--font-size-base': '19px',
                    '--font-size-small': '17px',
                    '--font-size-large': '22px',
                    '--font-size-h1': '2.8rem',
                    '--font-size-h2': '2.3rem',
                    '--font-size-h3': '2rem',
                    '--line-height-base': '1.9',
                    '--font-weight-normal': '400',
                    
                    // Enhanced contrast and spacing
                    '--text-primary': '#1a1a1a',
                    '--bg-primary': '#fffef7', // Cream background
                    '--bg-secondary': '#f7f6ed',
                    '--bg-card': '#fffef7',
                    
                    // Improved readability spacing
                    '--spacing-xs': '0.5rem',
                    '--spacing-sm': '0.8rem',
                    '--spacing-md': '1.8rem',
                    '--spacing-lg': '2.5rem',
                    '--spacing-xl': '4rem',
                    '--border-radius': '0.8rem',
                    '--border-width': '3px',
                    '--shadow-md': '0 0.5rem 1rem rgba(0, 0, 0, 0.15)',
                    
                    // Text-friendly colors
                    '--primary-color': '#0066cc',
                    '--text-secondary': '#4a4a4a',
                    '--text-muted': '#666666'
                }
            },
            
            autism: {
                name: 'Autism Support',
                description: 'Predictable, structured interface with minimal changes',
                variables: {
                    // Consistent, calming colors
                    '--primary-color': '#4a90a4',
                    '--secondary-color': '#7c7c7c',
                    '--success-color': '#5cb85c',
                    '--warning-color': '#f0ad4e',
                    '--danger-color': '#d9534f',
                    
                    // Calming backgrounds
                    '--bg-primary': '#fdfdfd',
                    '--bg-secondary': '#f0f4f7',
                    '--bg-card': '#ffffff',
                    '--bg-nav': '#f8f9fa',
                    
                    // Clear typography
                    '--font-family-primary': '"Arial", sans-serif',
                    '--font-size-base': '16px',
                    '--line-height-base': '1.6',
                    '--font-weight-normal': '400',
                    
                    // Structured spacing
                    '--spacing-md': '1.2rem',
                    '--border-radius': '0.25rem',
                    '--border-width': '1px',
                    
                    // Minimal animations
                    '--transition-base': '0.1s ease',
                    '--transition-fast': '0.1s ease',
                    '--hover-opacity': '0.95',
                    
                    // Clear focus indicators
                    '--focus-ring': '0 0 0 0.2rem rgba(74, 144, 164, 0.3)'
                }
            },
            
            sensory: {
                name: 'Sensory-Friendly',
                description: 'Low stimulation design with reduced visual noise',
                variables: {
                    // Muted color palette
                    '--primary-color': '#6c7b7f',
                    '--secondary-color': '#8e9194',
                    '--success-color': '#7ca982',
                    '--warning-color': '#d4c17a',
                    '--danger-color': '#c49c9c',
                    
                    // Soft backgrounds
                    '--bg-primary': '#fafafa',
                    '--bg-secondary': '#f5f5f5',
                    '--bg-card': '#ffffff',
                    '--text-primary': '#2d3436',
                    '--text-secondary': '#636e72',
                    '--text-muted': '#95a5a6',
                    
                    // Gentle typography
                    '--font-family-primary': '"Verdana", Arial, sans-serif',
                    '--font-size-base': '16px',
                    '--line-height-base': '1.7',
                    '--font-weight-normal': '400',
                    
                    // Soft edges and minimal shadows
                    '--border-radius': '0.75rem',
                    '--shadow-sm': '0 0.125rem 0.25rem rgba(0, 0, 0, 0.05)',
                    '--shadow-md': '0 0.25rem 0.5rem rgba(0, 0, 0, 0.08)',
                    
                    // Very gentle animations
                    '--transition-base': '0.4s ease',
                    '--hover-opacity': '0.92',
                    
                    // Calm focus indicators
                    '--focus-ring': '0 0 0 0.15rem rgba(108, 123, 127, 0.2)'
                }
            },
            
            'dyslexia-adhd': {
                name: 'Combined Support',
                description: 'Optimized for both reading and attention needs',
                variables: {
                    // Combined font optimizations
                    '--font-family-primary': '"Comic Neue", Arial, sans-serif',
                    '--font-size-base': '18px',
                    '--line-height-base': '1.8',
                    '--font-weight-normal': '500',
                    
                    // Enhanced readability + focus
                    '--bg-primary': '#fffef7',
                    '--bg-secondary': '#f6f5ec',
                    '--text-primary': '#1a1a1a',
                    '--primary-color': '#0056b3',
                    
                    // Strong focus indicators
                    '--focus-ring': '0 0 0 0.3rem rgba(0, 86, 179, 0.4)',
                    '--border-width': '2px',
                    
                    // Optimized spacing
                    '--spacing-md': '1.5rem',
                    '--spacing-lg': '2.25rem',
                    '--border-radius': '0.5rem',
                    
                    // Balanced animations
                    '--transition-base': '0.25s ease',
                    '--hover-opacity': '0.88'
                }
            }
        };
    }

    async initialize() {
        if (this.isInitialized) return;
        
        try {
            console.log('Initializing Adaptive UI System for user:', this.userId);
            
            // Restore persisted settings first
            this.restorePersistedSettings();
            
            // Load user profile and settings
            await this.loadUserProfile();
            
            // Apply initial preset
            await this.applyCurrentPreset();
            
            // Set up event listeners and persistence
            this.setupEventListeners();
            this.setupCrossPagePersistence();
            
            this.isInitialized = true;
            console.log('Adaptive UI System initialized successfully');
            
        } catch (error) {
            console.error('Failed to initialize Adaptive UI System:', error);
            // Fall back to standard preset
            this.applyPreset('standard');
        }
    }

    // Cross-page persistence methods
    saveSettingsToStorage() {
        const settingsToSave = {
            currentPreset: this.currentPreset,
            customSettings: this.customSettings,
            timestamp: Date.now(),
            userId: this.userId
        };
        
        try {
            localStorage.setItem(this.persistenceKey, JSON.stringify(settingsToSave));
            console.log('Settings saved to localStorage');
        } catch (error) {
            console.warn('Failed to save settings to localStorage:', error);
        }
    }

    restorePersistedSettings() {
        try {
            const saved = localStorage.getItem(this.persistenceKey);
            if (saved) {
                const settings = JSON.parse(saved);
                
                // Check if settings are for current user and not too old (24 hours)
                const maxAge = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
                if (settings.userId === this.userId && 
                    (Date.now() - settings.timestamp) < maxAge) {
                    
                    this.currentPreset = settings.currentPreset || 'standard';
                    this.customSettings = settings.customSettings || {};
                    console.log('Restored persisted settings:', this.currentPreset);
                }
            }
        } catch (error) {
            console.warn('Failed to restore persisted settings:', error);
        }
    }

    setupCrossPagePersistence() {
        // Listen for storage changes from other tabs/windows
        window.addEventListener('storage', (event) => {
            if (event.key === this.persistenceKey && event.newValue) {
                try {
                    const newSettings = JSON.parse(event.newValue);
                    if (newSettings.userId === this.userId) {
                        this.currentPreset = newSettings.currentPreset;
                        this.customSettings = newSettings.customSettings;
                        this.applyCurrentPreset();
                        console.log('Applied settings from another tab');
                    }
                } catch (error) {
                    console.warn('Failed to sync settings from another tab:', error);
                }
            }
        });

        // Auto-save settings when page visibility changes
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'hidden') {
                this.saveSettingsToStorage();
            }
        });

        // Save settings before page unload
        window.addEventListener('beforeunload', () => {
            this.saveSettingsToStorage();
        });
    }

    async loadUserProfile() {
        if (!this.userId) {
            console.log('No user ID found, using standard preset');
            return;
        }

        try {
            const response = await fetch(`${this.apiBase}/profile/${this.userId}`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const profile = await response.json();
                
                if (profile.hasAssessment && profile.assessmentCompleted) {
                    this.currentPreset = profile.recommendedPreset || 'standard';
                    
                    // Load custom UI settings if available
                    if (profile.uiSettings) {
                        this.customSettings = profile.uiSettings;
                    }
                    
                    console.log('Loaded user profile, preset:', this.currentPreset);
                } else {
                    console.log('User has no completed assessment, using standard preset');
                }
            }
        } catch (error) {
            console.warn('Failed to load user profile:', error);
        }
    }

    async applyCurrentPreset() {
        await this.applyPreset(this.currentPreset);
    }

    async applyPreset(presetName) {
        const preset = this.presets[presetName];
        if (!preset) {
            console.warn(`Unknown preset: ${presetName}, using standard`);
            presetName = 'standard';
        }

        console.log(`Applying ${presetName} preset with config:`, preset);
        
        // Combine default variables with preset variables
        const variables = { ...this.defaultVariables, ...preset.variables };
        
        // Apply custom overrides if any
        if (this.customSettings.cssVariables) {
            Object.assign(variables, this.customSettings.cssVariables);
        }
        
        console.log('Final variables to apply:', variables);
        
        // Apply variables to document
        this.applyCSSVariables(variables);
        
        // Apply preset-specific classes
        this.applyPresetClasses(presetName);
        
        // Apply custom settings
        this.applyCustomSettings();
        
        // Store current preset
        this.currentPreset = presetName;
        localStorage.setItem('adaptiveUIPreset', presetName);
        localStorage.setItem('recommendedPreset', presetName);
        
        // Save all settings to storage for cross-page persistence
        this.saveSettingsToStorage();
        
        // Trigger preset change event
        this.triggerPresetChangeEvent(presetName, preset);
        
        console.log(`Successfully applied ${presetName} preset`);
    }

    applyCSSVariables(variables) {
        const root = document.documentElement;
        
        console.log('Applying CSS variables:', variables);
        
        for (const [property, value] of Object.entries(variables)) {
            root.style.setProperty(property, value);
            console.log(`Set ${property} to ${value}`);
        }
        
        // Force a repaint
        document.body.style.display = 'none';
        document.body.offsetHeight; // Trigger reflow
        document.body.style.display = '';
    }

    applyPresetClasses(presetName) {
        const body = document.body;
        
        // Remove existing preset classes
        Object.keys(this.presets).forEach(preset => {
            body.classList.remove(`preset-${preset}`);
        });
        
        // Add current preset class
        body.classList.add(`preset-${presetName}`);
        
        // Add specific accessibility classes based on preset
        if (presetName === 'dyslexia' || presetName === 'dyslexia-adhd') {
            body.classList.add('dyslexia-friendly');
        }
        
        if (presetName === 'adhd' || presetName === 'dyslexia-adhd') {
            body.classList.add('adhd-friendly');
        }
        
        if (presetName === 'autism') {
            body.classList.add('autism-friendly');
        }
        
        if (presetName === 'sensory') {
            body.classList.add('sensory-friendly');
        }
    }

    applyCustomSettings() {
        if (!this.customSettings) return;
        
        // Apply font size adjustments
        if (this.customSettings.fontSize) {
            document.documentElement.style.setProperty('--font-size-base', this.customSettings.fontSize + 'px');
        }
        
        // Apply contrast adjustments
        if (this.customSettings.highContrast) {
            document.body.classList.add('high-contrast');
        }
        
        // Apply animation preferences
        if (this.customSettings.reduceAnimations) {
            document.body.classList.add('reduce-animations');
        }
        
        // Apply focus enhancements
        if (this.customSettings.enhancedFocus) {
            document.body.classList.add('enhanced-focus');
        }
    }

    setupEventListeners() {
        // Listen for assessment completion
        document.addEventListener('assessmentCompleted', (event) => {
            const { preset } = event.detail;
            if (preset) {
                this.applyPreset(preset);
            }
        });
        
        // Listen for manual preset changes
        document.addEventListener('presetChange', (event) => {
            const { preset } = event.detail;
            this.applyPreset(preset);
        });
        
        // Listen for setting updates
        document.addEventListener('settingUpdate', (event) => {
            const { setting, value } = event.detail;
            this.updateSetting(setting, value);
        });
        
        // Check for new profile parameter
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('newProfile') === 'true') {
            // Reload profile after assessment completion
            setTimeout(() => this.loadUserProfile().then(() => this.applyCurrentPreset()), 1000);
        }
    }

    triggerPresetChangeEvent(presetName, preset) {
        const event = new CustomEvent('adaptiveUIChanged', {
            detail: {
                preset: presetName,
                presetConfig: preset,
                timestamp: new Date().toISOString()
            }
        });
        document.dispatchEvent(event);
    }

    updateSetting(setting, value) {
        if (!this.customSettings) {
            this.customSettings = {};
        }
        
        this.customSettings[setting] = value;
        
        // Apply the specific setting
        switch (setting) {
            case 'fontSize':
                document.documentElement.style.setProperty('--font-size-base', value + 'px');
                break;
            case 'highContrast':
                document.body.classList.toggle('high-contrast', value);
                break;
            case 'reduceAnimations':
                document.body.classList.toggle('reduce-animations', value);
                break;
            case 'enhancedFocus':
                document.body.classList.toggle('enhanced-focus', value);
                break;
        }
        
        // Save to localStorage
        localStorage.setItem('adaptiveUICustomSettings', JSON.stringify(this.customSettings));
    }

    getCurrentUserId() {
        const token = localStorage.getItem('authToken');
        if (token) {
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                return payload.userId || payload.sub;
            } catch (e) {
                console.error('Failed to parse user ID from token');
            }
        }
        return null;
    }

    // Public methods for manual control
    getAvailablePresets() {
        return Object.keys(this.presets).map(key => ({
            id: key,
            name: this.presets[key].name,
            description: this.presets[key].description
        }));
    }

    getCurrentPreset() {
        return {
            id: this.currentPreset,
            name: this.presets[this.currentPreset].name,
            description: this.presets[this.currentPreset].description
        };
    }

    async saveCustomSettings() {
        if (!this.userId || !this.customSettings) return;
        
        try {
            const response = await fetch(`/api/assessment/ui-settings/${this.userId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(this.customSettings)
            });
            
            if (response.ok) {
                console.log('Custom settings saved successfully');
            }
        } catch (error) {
            console.error('Failed to save custom settings:', error);
        }
    }

    // Timer-specific integration methods
    getTimerSettings() {
        const timerSettings = {
            sessionDuration: 25, // Default pomodoro
            breakDuration: 5,
            longBreakDuration: 15,
            backgroundColor: 'var(--timer-bg)',
            borderColor: 'var(--timer-border)',
            textColor: 'var(--timer-text)',
            accentColor: 'var(--timer-accent)'
        };
        
        // Adjust based on preset
        switch (this.currentPreset) {
            case 'adhd':
            case 'dyslexia-adhd':
                timerSettings.sessionDuration = 15; // Shorter sessions for ADHD
                timerSettings.breakDuration = 3;
                break;
            case 'autism':
                timerSettings.sessionDuration = 30; // Longer, predictable sessions
                timerSettings.breakDuration = 10;
                break;
            case 'sensory':
                timerSettings.breakDuration = 8; // Longer breaks for sensory processing
                break;
        }
        
        return timerSettings;
    }
}

// Initialize the adaptive UI system
const adaptiveUI = new AdaptiveUISystem();

// Auto-initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    adaptiveUI.initialize();
});

// Export for global use
window.AdaptiveUISystem = AdaptiveUISystem;
window.adaptiveUI = adaptiveUI;