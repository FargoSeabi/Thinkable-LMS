// Global Study Timer Management
class StudyTimer {
    constructor() {
        this.studyTimer = null;
        this.breakTimer = null;
        this.isStudyTimerRunning = false;
        this.isBreakTimerRunning = false;
        
        // Adaptive settings
        this.adaptiveSettings = null;
        this.currentPreset = 'standard';
        
        // Load state from localStorage
        this.loadTimerState();
        
        // Initialize timer on page load
        this.init();
    }

    init() {
        // Load adaptive settings
        this.loadAdaptiveSettings();
        
        // Add timer HTML to page if not exists
        this.addTimerHTML();
        
        // Setup event listeners
        this.setupTimerControls();
        this.setupBreakTimerControls();
        this.setupAdaptiveListeners();
        
        // Update displays
        this.updateStudyTimerDisplay();
        this.updateBreakTimerDisplay();
        
        // Restore timer state
        this.restoreTimerState();
        
        // Apply adaptive styling
        this.applyAdaptiveStyles();
        
        // Request notification permission
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    }

    loadTimerState() {
        const savedState = localStorage.getItem('studyTimerState');
        if (savedState) {
            const state = JSON.parse(savedState);
            this.studyTimeLeft = state.studyTimeLeft || 25 * 60;
            this.breakTimeLeft = state.breakTimeLeft || 5 * 60;
            this.isStudyTimerRunning = state.isStudyTimerRunning || false;
            this.isBreakTimerRunning = state.isBreakTimerRunning || false;
            this.studyTimerVisible = state.studyTimerVisible || false;
            this.breakTimerVisible = state.breakTimerVisible || false;
            this.studyTimerMinimized = state.studyTimerMinimized || false;
            this.breakTimerMinimized = state.breakTimerMinimized || false;
            this.lastUpdateTime = state.lastUpdateTime || Date.now();
        } else {
            // Default timer durations (will be updated by adaptive settings)
            this.studyTimeLeft = 25 * 60;
            this.breakTimeLeft = 5 * 60;
            this.studyTimerVisible = false;
            this.breakTimerVisible = false;
            this.studyTimerMinimized = false;
            this.breakTimerMinimized = false;
            this.lastUpdateTime = Date.now();
        }
    }

    saveTimerState() {
        const state = {
            studyTimeLeft: this.studyTimeLeft,
            breakTimeLeft: this.breakTimeLeft,
            isStudyTimerRunning: this.isStudyTimerRunning,
            isBreakTimerRunning: this.isBreakTimerRunning,
            studyTimerVisible: this.studyTimerVisible,
            breakTimerVisible: this.breakTimerVisible,
            studyTimerMinimized: this.studyTimerMinimized,
            breakTimerMinimized: this.breakTimerMinimized,
            lastUpdateTime: Date.now()
        };
        localStorage.setItem('studyTimerState', JSON.stringify(state));
    }

    restoreTimerState() {
        // Calculate time elapsed since last update
        const now = Date.now();
        const elapsed = Math.floor((now - this.lastUpdateTime) / 1000);
        
        // Update timer based on elapsed time
        if (this.isStudyTimerRunning && elapsed > 0) {
            this.studyTimeLeft = Math.max(0, this.studyTimeLeft - elapsed);
            if (this.studyTimeLeft <= 0) {
                this.isStudyTimerRunning = false;
                this.showBreakTimerPopup();
                this.playNotificationSound();
            }
        }
        
        if (this.isBreakTimerRunning && elapsed > 0) {
            this.breakTimeLeft = Math.max(0, this.breakTimeLeft - elapsed);
            if (this.breakTimeLeft <= 0) {
                this.isBreakTimerRunning = false;
                this.hideBreakTimerPopup();
                this.playNotificationSound();
            }
        }

        // Restore popup visibility and state
        if (this.studyTimerVisible) {
            this.showStudyTimerPopup();
            if (this.studyTimerMinimized) {
                document.getElementById('studyTimerPopup').classList.add('minimized');
            }
        }
        
        if (this.breakTimerVisible) {
            this.showBreakTimerPopup();
            if (this.breakTimerMinimized) {
                document.getElementById('breakTimerPopup').classList.add('minimized');
            }
        }

        // Continue timers if they were running
        if (this.isStudyTimerRunning) {
            this.startStudyTimer();
        }
        
        if (this.isBreakTimerRunning) {
            this.startBreakTimerCountdown();
        }
    }

    addTimerHTML() {
        // Check if timer HTML already exists
        if (document.getElementById('studyTimerPopup')) return;

        const timerHTML = `
            <!-- Study Timer Popup -->
            <div id="studyTimerPopup" class="timer-popup" style="display: none;">
                <div class="timer-header">
                    <div class="timer-title">
                        <i class="fas fa-clock"></i>
                        <span>Study Session</span>
                    </div>
                    <div class="timer-controls">
                        <button id="minimizeTimer" class="timer-control-btn" title="Minimize">
                            <i class="fas fa-minus"></i>
                        </button>
                        <button id="closeTimer" class="timer-control-btn" title="Close">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                </div>
                <div class="timer-content">
                    <div class="timer-display" id="studyTimerDisplay">25:00</div>
                    <div class="timer-buttons">
                        <button id="playPauseBtn" class="timer-btn primary">
                            <i class="fas fa-play"></i> Start
                        </button>
                        <button id="resetTimerBtn" class="timer-btn secondary">
                            <i class="fas fa-redo"></i> Reset
                        </button>
                    </div>
                </div>
            </div>

            <!-- Break Timer Popup -->
            <div id="breakTimerPopup" class="timer-popup break-popup" style="display: none;">
                <div class="timer-header">
                    <div class="timer-title">
                        <i class="fas fa-coffee"></i>
                        <span>Break Time!</span>
                    </div>
                    <div class="timer-controls">
                        <button id="minimizeBreakTimer" class="timer-control-btn" title="Minimize">
                            <i class="fas fa-minus"></i>
                        </button>
                        <button id="closeBreakTimer" class="timer-control-btn" title="Close">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                </div>
                <div class="timer-content">
                    <div class="timer-display" id="breakTimerDisplay">5:00</div>
                    <div class="timer-message">Great work! Take a 5-minute break.</div>
                    <div class="timer-buttons">
                        <button id="startBreakBtn" class="timer-btn primary">
                            <i class="fas fa-play"></i> Start Break
                        </button>
                        <button id="skipBreakBtn" class="timer-btn secondary">
                            <i class="fas fa-forward"></i> Skip Break
                        </button>
                    </div>
                </div>
            </div>
        `;

        // Add CSS if not exists
        if (!document.getElementById('timerStyles')) {
            const timerCSS = `
                <style id="timerStyles">
                    /* Timer Popup Styles */
                    .timer-popup {
                        position: fixed;
                        bottom: 20px;
                        left: 20px;
                        width: 300px;
                        background: white;
                        border-radius: 12px;
                        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
                        border: 1px solid #e0e6ed;
                        z-index: 1000;
                        font-family: Arial, sans-serif;
                        transition: all 0.3s ease;
                    }

                    .timer-popup.minimized {
                        height: 45px;
                        overflow: hidden;
                    }

                    .timer-popup.minimized .timer-content {
                        display: none;
                    }

                    .timer-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        padding: 12px 15px;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        border-radius: 12px 12px 0 0;
                        cursor: move;
                    }

                    .break-popup .timer-header {
                        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                    }

                    .timer-title {
                        display: flex;
                        align-items: center;
                        gap: 8px;
                        font-weight: 600;
                        font-size: 14px;
                    }

                    .timer-controls {
                        display: flex;
                        gap: 5px;
                    }

                    .timer-control-btn {
                        background: rgba(255, 255, 255, 0.2);
                        border: none;
                        color: white;
                        width: 25px;
                        height: 25px;
                        border-radius: 4px;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        transition: background 0.2s ease;
                    }

                    .timer-control-btn:hover {
                        background: rgba(255, 255, 255, 0.3);
                    }

                    .timer-content {
                        padding: 20px;
                        text-align: center;
                    }

                    .timer-display {
                        font-size: 32px;
                        font-weight: bold;
                        color: #333;
                        margin-bottom: 15px;
                        font-family: 'Courier New', monospace;
                    }

                    .timer-message {
                        color: #666;
                        margin-bottom: 15px;
                        font-size: 14px;
                    }

                    .timer-buttons {
                        display: flex;
                        gap: 10px;
                        justify-content: center;
                    }

                    .timer-btn {
                        padding: 10px 16px;
                        border: none;
                        border-radius: 6px;
                        cursor: pointer;
                        font-size: 13px;
                        font-weight: 500;
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        transition: all 0.2s ease;
                    }

                    .timer-btn.primary {
                        background: #667eea;
                        color: white;
                    }

                    .timer-btn.primary:hover {
                        background: #5a67d8;
                        transform: translateY(-1px);
                    }

                    .timer-btn.secondary {
                        background: #e2e8f0;
                        color: #4a5568;
                    }

                    .timer-btn.secondary:hover {
                        background: #cbd5e0;
                        transform: translateY(-1px);
                    }

                    .timer-btn:disabled {
                        opacity: 0.6;
                        cursor: not-allowed;
                    }

                    .timer-btn:disabled:hover {
                        transform: none;
                    }

                    @media (max-width: 768px) {
                        .timer-popup {
                            width: calc(100vw - 40px);
                            left: 20px;
                            right: 20px;
                        }
                    }
                </style>
            `;
            document.head.insertAdjacentHTML('beforeend', timerCSS);
        }

        // Add HTML to body
        document.body.insertAdjacentHTML('beforeend', timerHTML);
    }

    startBreakTimer(minutes) {
        this.studyTimeLeft = minutes * 60;
        this.showStudyTimerPopup();
    }

    showStudyTimerPopup() {
        const popup = document.getElementById('studyTimerPopup');
        popup.style.display = 'block';
        this.studyTimerVisible = true;
        this.updateStudyTimerDisplay();
        this.saveTimerState();
    }

    showBreakTimerPopup() {
        const popup = document.getElementById('breakTimerPopup');
        popup.style.display = 'block';
        // Use adaptive break duration
        const duration = this.getAdaptiveBreakDuration();
        this.breakTimeLeft = duration * 60;
        this.breakTimerVisible = true;
        this.updateBreakTimerDisplay();
        this.saveTimerState();
    }

    hideStudyTimerPopup() {
        document.getElementById('studyTimerPopup').style.display = 'none';
        this.studyTimerVisible = false;
        this.stopStudyTimer();
        this.saveTimerState();
    }

    hideBreakTimerPopup() {
        document.getElementById('breakTimerPopup').style.display = 'none';
        this.breakTimerVisible = false;
        this.stopBreakTimer();
        this.saveTimerState();
    }

    updateStudyTimerDisplay() {
        const minutes = Math.floor(this.studyTimeLeft / 60);
        const seconds = this.studyTimeLeft % 60;
        const display = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        const displayElement = document.getElementById('studyTimerDisplay');
        if (displayElement) {
            displayElement.textContent = display;
        }
    }

    updateBreakTimerDisplay() {
        const minutes = Math.floor(this.breakTimeLeft / 60);
        const seconds = this.breakTimeLeft % 60;
        const display = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        const displayElement = document.getElementById('breakTimerDisplay');
        if (displayElement) {
            displayElement.textContent = display;
        }
    }

    startStudyTimer() {
        this.isStudyTimerRunning = true;
        this.studyTimer = setInterval(() => {
            this.studyTimeLeft--;
            this.updateStudyTimerDisplay();
            this.saveTimerState();
            
            if (this.studyTimeLeft <= 0) {
                this.stopStudyTimer();
                this.showBreakTimerPopup();
                this.playNotificationSound();
                
                this.showAdaptiveNotification('Study Session Complete!', 'Great work! Time for a break.', 'study');
            }
        }, 1000);
        
        this.updatePlayPauseButton();
        this.saveTimerState();
    }

    stopStudyTimer() {
        this.isStudyTimerRunning = false;
        if (this.studyTimer) {
            clearInterval(this.studyTimer);
            this.studyTimer = null;
        }
        this.updatePlayPauseButton();
        this.saveTimerState();
    }

    resetStudyTimer() {
        this.stopStudyTimer();
        // Use adaptive duration if available
        const duration = this.getAdaptiveStudyDuration();
        this.studyTimeLeft = duration * 60;
        this.updateStudyTimerDisplay();
        this.saveTimerState();
    }

    startBreakTimerCountdown() {
        this.isBreakTimerRunning = true;
        this.breakTimer = setInterval(() => {
            this.breakTimeLeft--;
            this.updateBreakTimerDisplay();
            this.saveTimerState();
            
            if (this.breakTimeLeft <= 0) {
                this.stopBreakTimer();
                this.hideBreakTimerPopup();
                this.playNotificationSound();
                
                this.showAdaptiveNotification('Break Complete!', 'Ready to get back to studying?', 'break');
            }
        }, 1000);
        
        this.updateBreakTimerButtons();
        this.saveTimerState();
    }

    stopBreakTimer() {
        this.isBreakTimerRunning = false;
        if (this.breakTimer) {
            clearInterval(this.breakTimer);
            this.breakTimer = null;
        }
        this.updateBreakTimerButtons();
        this.saveTimerState();
    }

    updatePlayPauseButton() {
        const btn = document.getElementById('playPauseBtn');
        if (!btn) return;
        
        const icon = btn.querySelector('i');
        
        if (this.isStudyTimerRunning) {
            icon.className = 'fas fa-pause';
            btn.innerHTML = '<i class="fas fa-pause"></i> Pause';
        } else {
            icon.className = 'fas fa-play';
            btn.innerHTML = '<i class="fas fa-play"></i> Start';
        }
    }

    updateBreakTimerButtons() {
        const btn = document.getElementById('startBreakBtn');
        if (!btn) return;
        
        const icon = btn.querySelector('i');
        
        if (this.isBreakTimerRunning) {
            icon.className = 'fas fa-pause';
            btn.innerHTML = '<i class="fas fa-pause"></i> Pause';
        } else {
            icon.className = 'fas fa-play';
            btn.innerHTML = '<i class="fas fa-play"></i> Start Break';
        }
    }

    playNotificationSound() {
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();
            
            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);
            
            oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
            gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);
            
            oscillator.start(audioContext.currentTime);
            oscillator.stop(audioContext.currentTime + 0.5);
        } catch (e) {
            console.log('Audio notification not supported');
        }
    }

    setupTimerControls() {
        // Play/Pause button
        const playPauseBtn = document.getElementById('playPauseBtn');
        if (playPauseBtn) {
            playPauseBtn.onclick = () => {
                if (this.isStudyTimerRunning) {
                    this.stopStudyTimer();
                } else {
                    this.startStudyTimer();
                }
            };
        }

        // Reset button
        const resetBtn = document.getElementById('resetTimerBtn');
        if (resetBtn) {
            resetBtn.onclick = () => {
                this.resetStudyTimer();
            };
        }

        // Minimize button
        const minimizeBtn = document.getElementById('minimizeTimer');
        if (minimizeBtn) {
            minimizeBtn.onclick = () => {
                const popup = document.getElementById('studyTimerPopup');
                popup.classList.toggle('minimized');
                this.studyTimerMinimized = popup.classList.contains('minimized');
                
                const icon = minimizeBtn.querySelector('i');
                if (popup.classList.contains('minimized')) {
                    icon.className = 'fas fa-window-maximize';
                    minimizeBtn.title = 'Maximize';
                } else {
                    icon.className = 'fas fa-minus';
                    minimizeBtn.title = 'Minimize';
                }
                this.saveTimerState();
            };
        }

        // Close button
        const closeBtn = document.getElementById('closeTimer');
        if (closeBtn) {
            closeBtn.onclick = () => {
                this.hideStudyTimerPopup();
            };
        }

        // Double-click header to minimize/maximize
        const header = document.querySelector('#studyTimerPopup .timer-header');
        if (header) {
            header.ondblclick = () => {
                const minimizeBtn = document.getElementById('minimizeTimer');
                if (minimizeBtn) minimizeBtn.click();
            };
        }
    }

    setupBreakTimerControls() {
        // Start break button
        const startBreakBtn = document.getElementById('startBreakBtn');
        if (startBreakBtn) {
            startBreakBtn.onclick = () => {
                if (this.isBreakTimerRunning) {
                    this.stopBreakTimer();
                } else {
                    this.startBreakTimerCountdown();
                }
            };
        }

        // Skip break button
        const skipBreakBtn = document.getElementById('skipBreakBtn');
        if (skipBreakBtn) {
            skipBreakBtn.onclick = () => {
                this.hideBreakTimerPopup();
            };
        }

        // Minimize button
        const minimizeBreakBtn = document.getElementById('minimizeBreakTimer');
        if (minimizeBreakBtn) {
            minimizeBreakBtn.onclick = () => {
                const popup = document.getElementById('breakTimerPopup');
                popup.classList.toggle('minimized');
                this.breakTimerMinimized = popup.classList.contains('minimized');
                
                const icon = minimizeBreakBtn.querySelector('i');
                if (popup.classList.contains('minimized')) {
                    icon.className = 'fas fa-window-maximize';
                    minimizeBreakBtn.title = 'Maximize';
                } else {
                    icon.className = 'fas fa-minus';
                    minimizeBreakBtn.title = 'Minimize';
                }
                this.saveTimerState();
            };
        }

        // Close button
        const closeBreakBtn = document.getElementById('closeBreakTimer');
        if (closeBreakBtn) {
            closeBreakBtn.onclick = () => {
                this.hideBreakTimerPopup();
            };
        }

        // Double-click header to minimize/maximize
        const breakHeader = document.querySelector('#breakTimerPopup .timer-header');
        if (breakHeader) {
            breakHeader.ondblclick = () => {
                const minimizeBtn = document.getElementById('minimizeBreakTimer');
                if (minimizeBtn) minimizeBtn.click();
            };
        }
    }

    // Adaptive timer integration methods
    loadAdaptiveSettings() {
        try {
            // Check if adaptive UI system is available
            if (window.adaptiveUI) {
                this.adaptiveSettings = window.adaptiveUI.getTimerSettings();
                this.currentPreset = window.adaptiveUI.getCurrentPreset().id;
                console.log('Loaded adaptive timer settings:', this.adaptiveSettings);
            } else {
                // Fall back to localStorage preset
                this.currentPreset = localStorage.getItem('adaptiveUIPreset') || 'standard';
                this.adaptiveSettings = this.getDefaultTimerSettings();
            }
        } catch (error) {
            console.warn('Failed to load adaptive settings, using defaults:', error);
            this.adaptiveSettings = this.getDefaultTimerSettings();
        }
    }

    getDefaultTimerSettings() {
        return {
            sessionDuration: 25,
            breakDuration: 5,
            longBreakDuration: 15,
            backgroundColor: '#ffffff',
            borderColor: '#dee2e6',
            textColor: '#495057',
            accentColor: '#007bff'
        };
    }

    getAdaptiveStudyDuration() {
        if (this.adaptiveSettings) {
            return this.adaptiveSettings.sessionDuration;
        }
        
        // Preset-specific defaults when adaptive UI isn't available
        switch (this.currentPreset) {
            case 'adhd':
            case 'dyslexia-adhd':
                return 15; // Shorter sessions for ADHD
            case 'autism':
                return 30; // Longer, predictable sessions
            default:
                return 25; // Standard pomodoro
        }
    }

    getAdaptiveBreakDuration() {
        if (this.adaptiveSettings) {
            return this.adaptiveSettings.breakDuration;
        }
        
        // Preset-specific defaults
        switch (this.currentPreset) {
            case 'adhd':
            case 'dyslexia-adhd':
                return 3; // Shorter breaks for ADHD
            case 'autism':
                return 10; // Longer breaks for processing
            case 'sensory':
                return 8; // Longer breaks for sensory processing
            default:
                return 5; // Standard break
        }
    }

    applyAdaptiveStyles() {
        if (!this.adaptiveSettings) return;
        
        const studyPopup = document.getElementById('studyTimerPopup');
        const breakPopup = document.getElementById('breakTimerPopup');
        
        if (studyPopup) {
            studyPopup.classList.add('adaptive-timer');
            this.applyTimerStyling(studyPopup);
        }
        
        if (breakPopup) {
            breakPopup.classList.add('adaptive-timer');
            this.applyTimerStyling(breakPopup);
        }
        
        // Update timer messages for different presets
        this.updateTimerMessages();
    }

    applyTimerStyling(popup) {
        if (!this.adaptiveSettings || !popup) return;
        
        const settings = this.adaptiveSettings;
        
        // Apply background and text colors
        popup.style.backgroundColor = settings.backgroundColor;
        popup.style.color = settings.textColor;
        
        // Apply border styling
        popup.style.borderColor = settings.borderColor;
        
        // Update button colors
        const buttons = popup.querySelectorAll('.timer-btn.primary');
        buttons.forEach(btn => {
            btn.style.backgroundColor = settings.accentColor;
        });
        
        // Apply preset-specific enhancements
        this.applyPresetSpecificStyling(popup);
    }

    applyPresetSpecificStyling(popup) {
        // Remove existing preset classes
        const presetClasses = ['timer-adhd', 'timer-dyslexia', 'timer-autism', 'timer-sensory'];
        presetClasses.forEach(cls => popup.classList.remove(cls));
        
        // Add current preset class
        if (this.currentPreset !== 'standard') {
            popup.classList.add(`timer-${this.currentPreset}`);
        }
        
        // Apply specific styling based on preset
        switch (this.currentPreset) {
            case 'adhd':
            case 'dyslexia-adhd':
                // Enhanced focus indicators
                popup.style.borderWidth = '3px';
                popup.style.boxShadow = '0 0 20px rgba(0, 86, 179, 0.3)';
                break;
                
            case 'dyslexia':
                // Enhanced readability
                const displays = popup.querySelectorAll('.timer-display');
                displays.forEach(display => {
                    display.style.fontFamily = '"Comic Neue", Arial, sans-serif';
                    display.style.letterSpacing = '0.05em';
                });
                break;
                
            case 'autism':
                // Consistent, predictable styling
                popup.style.borderRadius = '8px';
                popup.style.boxShadow = '0 2px 4px rgba(0, 0, 0, 0.1)';
                break;
                
            case 'sensory':
                // Minimal, calming styling
                popup.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
                popup.style.backgroundColor = '#fafafa';
                break;
        }
    }

    updateTimerMessages() {
        const breakMessage = document.querySelector('#breakTimerPopup .timer-message');
        if (!breakMessage) return;
        
        const duration = this.getAdaptiveBreakDuration();
        let message = `Great work! Take a ${duration}-minute break.`;
        
        // Customize message based on preset
        switch (this.currentPreset) {
            case 'adhd':
            case 'dyslexia-adhd':
                message = `Excellent focus! Take a quick ${duration}-minute break to recharge.`;
                break;
            case 'autism':
                message = `Study session complete. Break time: ${duration} minutes.`;
                break;
            case 'sensory':
                message = `Good work! Enjoy a calm ${duration}-minute break.`;
                break;
            case 'dyslexia':
                message = `Reading session finished! Take ${duration} minutes to rest your eyes.`;
                break;
        }
        
        breakMessage.textContent = message;
    }

    setupAdaptiveListeners() {
        // Listen for adaptive UI changes
        document.addEventListener('adaptiveUIChanged', (event) => {
            console.log('Adaptive UI changed, updating timer:', event.detail);
            this.currentPreset = event.detail.preset;
            this.loadAdaptiveSettings();
            this.applyAdaptiveStyles();
            
            // Update current timer durations if not running
            if (!this.isStudyTimerRunning && this.studyTimerVisible) {
                const newDuration = this.getAdaptiveStudyDuration();
                this.studyTimeLeft = newDuration * 60;
                this.updateStudyTimerDisplay();
            }
        });
        
        // Listen for preset changes
        document.addEventListener('presetChange', (event) => {
            this.currentPreset = event.detail.preset;
            this.loadAdaptiveSettings();
            this.applyAdaptiveStyles();
        });
        
        // Listen for assessment completion
        document.addEventListener('assessmentCompleted', (event) => {
            const { preset } = event.detail;
            if (preset) {
                this.currentPreset = preset;
                this.loadAdaptiveSettings();
                this.applyAdaptiveStyles();
            }
        });
    }

    // Enhanced notification methods for different presets
    showAdaptiveNotification(title, body, type = 'study') {
        if (Notification.permission !== 'granted') return;
        
        let icon = '/favicon.ico';
        let customBody = body;
        
        // Customize notifications based on preset
        switch (this.currentPreset) {
            case 'adhd':
            case 'dyslexia-adhd':
                if (type === 'study') {
                    customBody = '🎯 ' + body + ' Time for a focused break!';
                } else {
                    customBody = '⚡ ' + body + ' Ready to focus again?';
                }
                break;
                
            case 'autism':
                // Clear, direct notifications
                if (type === 'study') {
                    customBody = 'Study timer finished. Break time started.';
                } else {
                    customBody = 'Break timer finished. Ready for next study session.';
                }
                break;
                
            case 'sensory':
                // Calmer notifications
                if (type === 'study') {
                    customBody = '🌸 Study time complete. Gentle break time.';
                } else {
                    customBody = '🌿 Break finished. Ready when you are.';
                }
                break;
                
            case 'dyslexia':
                // Supportive messages
                if (type === 'study') {
                    customBody = '📚 Reading session complete! Rest your eyes.';
                } else {
                    customBody = '✨ Break over. Ready for more learning?';
                }
                break;
        }
        
        const notification = new Notification(title, {
            body: customBody,
            icon: icon,
            silent: this.currentPreset === 'sensory' // Quiet for sensory-sensitive users
        });
        
        // Auto-close after preset-appropriate time
        const closeTime = this.currentPreset === 'autism' ? 3000 : 5000;
        setTimeout(() => notification.close(), closeTime);
    }

    // Override existing notification calls
    playNotificationSound() {
        // Skip sound for sensory-sensitive users
        if (this.currentPreset === 'sensory') return;
        
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();
            
            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);
            
            // Adjust frequency and volume based on preset
            let frequency = 800;
            let volume = 0.3;
            
            switch (this.currentPreset) {
                case 'adhd':
                case 'dyslexia-adhd':
                    frequency = 900; // Slightly higher, more attention-grabbing
                    volume = 0.4;
                    break;
                case 'autism':
                    frequency = 600; // Lower, less startling
                    volume = 0.2;
                    break;
                case 'dyslexia':
                    frequency = 700; // Gentle middle frequency
                    volume = 0.25;
                    break;
            }
            
            oscillator.frequency.setValueAtTime(frequency, audioContext.currentTime);
            gainNode.gain.setValueAtTime(volume, audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);
            
            oscillator.start(audioContext.currentTime);
            oscillator.stop(audioContext.currentTime + 0.5);
        } catch (e) {
            console.log('Audio notification not supported');
        }
    }

    // Method to manually update timer for new preset
    updateForNewPreset(preset) {
        this.currentPreset = preset;
        this.loadAdaptiveSettings();
        this.applyAdaptiveStyles();
        
        // Reset timers to new durations if not currently running
        if (!this.isStudyTimerRunning) {
            this.studyTimeLeft = this.getAdaptiveStudyDuration() * 60;
            this.updateStudyTimerDisplay();
        }
        
        if (!this.isBreakTimerRunning) {
            this.breakTimeLeft = this.getAdaptiveBreakDuration() * 60;
            this.updateBreakTimerDisplay();
        }
        
        this.saveTimerState();
        console.log(`Timer updated for ${preset} preset`);
    }
}

// Global timer instance
let globalStudyTimer;

// Initialize timer when page loads
document.addEventListener('DOMContentLoaded', function() {
    globalStudyTimer = new StudyTimer();
});

// Global function to start timer (called from other pages)
function startBreakTimer(minutes = 25) {
    if (globalStudyTimer) {
        globalStudyTimer.startBreakTimer(minutes);
    }
}