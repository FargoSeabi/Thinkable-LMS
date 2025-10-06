// ThinkAble Neurodivergent Learning Support System
class NeurodivergentSupport {
    constructor() {
        this.userId = this.getCurrentUserId();
        this.apiBaseUrl = '/api/neurodivergent';
        this.focusSession = {
            startTime: null,
            currentTask: null,
            energyLevel: 7, // 1-10 scale
            breaksTaken: 0,
            hyperfocusWarning: false
        };
        
        this.settings = {
            breakInterval: 25, // minutes
            hyperfocusLimit: 90, // minutes
            energyCheckInterval: 30, // minutes
            enableBreakReminders: true,
            enableHyperfocusShield: true,
            preferredBreakTypes: ['movement', 'breathing', 'sensory']
        };
        
        this.achievements = {
            breaksTaken: 0,
            sessionsCompleted: 0,
            selfCarePoints: 0,
            adaptiveChoices: 0
        };
        
        this.isInitialized = false;
        this.breakTimer = null;
        this.energyTimer = null;
        this.hyperfocusTimer = null;
    }

    async initialize() {
        if (this.isInitialized) return;
        
        console.log('🧠 Initializing Neurodivergent Support System...');
        
        // Load saved settings and achievements
        this.loadSettings();
        this.loadAchievements();
        
        // Create the support interface
        this.createSupportInterface();
        
        // Load personalized configurations from backend
        await this.loadToolPriorities();
        
        // Load and display insights periodically
        this.loadInsights();
        setInterval(() => {
            this.loadInsights();
        }, 10 * 60 * 1000); // Check for new insights every 10 minutes
        
        // Start monitoring systems
        this.startFocusTracking();
        
        // Energy monitoring (simple check every 30 minutes)
        setInterval(() => {
            if (Math.random() < 0.3) { // 30% chance every 30 minutes
                this.showEnergyCheck();
            }
        }, 30 * 60 * 1000);
        
        // Set up event listeners
        this.setupEventListeners();
        
        // Make panel draggable
        this.makePanelDraggable();
        
        // Load saved position preference
        this.loadPanelPosition();
        
        // Load saved panel state (minimized/hidden)
        this.loadPanelState();
        
        // Initialize adaptive tool ecosystem
        this.initializeAdaptiveEcosystem();
        
        this.isInitialized = true;
        console.log('✨ Neurodivergent Support System ready!');
        
        // Show a gentle welcome message
        setTimeout(() => {
            this.showWelcomeMessage();
        }, 2000);
    }

    createSupportInterface() {
        // Check if panel already exists
        if (document.getElementById('neurodivergent-support-panel')) {
            console.log('Support panel already exists');
            return;
        }
        
        console.log('Creating support interface...');
        
        // Create floating support panel
        const supportPanel = document.createElement('div');
        supportPanel.id = 'neurodivergent-support-panel';
        supportPanel.innerHTML = `
            <div class="support-panel" id="support-panel-inner">
                <!-- Panel Controls -->
                <div class="panel-header" id="panel-header">
                    <span class="panel-title">🧠 Focus Helper</span>
                    <div class="panel-controls">
                        <button id="position-panel" class="control-btn" title="📍 Move Panel - Choose where to place your Focus Helper">
                            <i class="fas fa-arrows-alt"></i>
                        </button>
                        <button id="minimize-panel" class="control-btn" title="➖ Minimize - Shrink to a small button">
                            <i class="fas fa-minus"></i>
                        </button>
                        <button id="close-panel" class="control-btn" title="✖️ Hide - Hide completely (shows restore button)">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                </div>
                
                <div class="panel-content" id="panel-content">
                    <!-- Overwhelm Escape Hatch -->
                    <button id="overwhelm-escape" class="escape-hatch" title="🆘 Need a break right now? Click here for immediate calm and breathing support. You're safe, and it's okay to take a moment.">
                        <i class="fas fa-life-ring"></i>
                        <span>I Need Calm Now</span>
                    </button>
                
                <!-- Focus Status -->
                <div class="focus-status" id="focus-status">
                    <div class="focus-timer">
                        <i class="fas fa-brain"></i>
                        <span id="focus-time">Ready to focus</span>
                    </div>
                    <div class="energy-level">
                        Energy: <span id="energy-display">❤️❤️❤️❤️❤️❤️❤️</span>
                    </div>
                </div>
                
                <!-- Quick Tools -->
                <div class="quick-tools">
                    <button id="fidget-tool" class="tool-btn" title="🤲 Fidget Tools - Virtual stress ball, bubble wrap, and sand tray to help you focus">
                        <i class="fas fa-hand-paper"></i>
                    </button>
                    <button id="breathing-tool" class="tool-btn" title="🫁 Breathing Exercise - Guided breathing to calm your mind and reset your focus">
                        <i class="fas fa-lungs"></i>
                    </button>
                    <button id="break-timer" class="tool-btn" title="⏰ Focus Session - Set a focus timer with smart break reminders to help you work sustainably">
                        <i class="fas fa-clock"></i>
                    </button>
                    <button id="energy-check" class="tool-btn" title="🔋 Energy Check - Track how you're feeling so I can give you better support">
                        <i class="fas fa-battery-half"></i>
                    </button>
                </div>
                
                <!-- Achievement Indicator -->
                <div class="achievement-indicator" id="achievement-indicator" style="display: none;">
                    <i class="fas fa-star"></i>
                    <span>You're doing great!</span>
                </div>
                </div>
            </div>
            
            <!-- Minimized state button -->
            <div class="minimized-panel" id="minimized-panel" style="display: none;">
                <button id="restore-panel" title="Open Focus Helper">
                    🧠
                </button>
            </div>
        `;
        
        document.body.appendChild(supportPanel);
        console.log('✅ Support panel added to DOM');
        
        // Verify the panel is in the DOM
        const addedPanel = document.getElementById('neurodivergent-support-panel');
        if (addedPanel) {
            console.log('✅ Panel confirmed in DOM');
        } else {
            console.error('❌ Panel not found after adding');
        }
        
        // Create the calm mode overlay (hidden by default)
        this.createCalmModeOverlay();
        
        // Create sensory tools modal
        this.createSensoryToolsModal();
        
        // Create break reminder modal
        this.createBreakReminderModal();
    }

    createCalmModeOverlay() {
        const calmOverlay = document.createElement('div');
        calmOverlay.id = 'calm-mode-overlay';
        calmOverlay.style.display = 'none';
        calmOverlay.innerHTML = `
            <div class="calm-mode-content">
                <h2><i class="fas fa-dove"></i> Take a Breath</h2>
                <p>You're safe. Let's reset together.</p>
                
                <div class="breathing-circle" id="breathing-circle">
                    <div class="circle-inner">
                        <span id="breath-instruction">Breathe with the circle</span>
                    </div>
                </div>
                
                <div class="calm-options">
                    <button class="calm-btn" onclick="neurodivergentSupport.startBreathingExercise()">
                        <i class="fas fa-lungs"></i> Guided Breathing
                    </button>
                    <button class="calm-btn" onclick="neurodivergentSupport.playFocusSounds()">
                        <i class="fas fa-volume-up"></i> Calming Sounds
                    </button>
                    <button class="calm-btn" onclick="neurodivergentSupport.showAffirmations()">
                        <i class="fas fa-heart"></i> Positive Reminders
                    </button>
                    <button class="calm-btn" onclick="neurodivergentSupport.hideCalmMode()">
                        <i class="fas fa-arrow-left"></i> I'm Ready to Continue
                    </button>
                </div>
            </div>
        `;
        
        document.body.appendChild(calmOverlay);
    }

    createSensoryToolsModal() {
        const sensoryModal = document.createElement('div');
        sensoryModal.id = 'sensory-tools-modal';
        sensoryModal.style.display = 'none';
        sensoryModal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3><i class="fas fa-hand-sparkles"></i> Fidget & Focus Tools</h3>
                    <button class="close-btn" onclick="neurodivergentSupport.closeSensoryTools()">×</button>
                </div>
                <div class="modal-body">
                    <div class="fidget-tools">
                        <div class="fidget-item" id="stress-ball">
                            <div class="stress-ball" onclick="neurodivergentSupport.squishBall()">
                                <div class="ball-inner">Click to Squish</div>
                            </div>
                        </div>
                        
                        <div class="fidget-item" id="pop-bubble">
                            <div class="bubble-wrap">
                                <!-- Bubbles will be generated -->
                            </div>
                        </div>
                        
                        <div class="fidget-item" id="sand-tray">
                            <canvas id="sand-canvas" width="300" height="200"></canvas>
                            <p>Draw in the sand to calm your mind</p>
                        </div>
                        
                        <div class="fidget-item" id="focus-sounds">
                            <h4>Focus Soundscapes</h4>
                            <div class="sound-controls">
                                <button onclick="neurodivergentSupport.playSound('rain')">🌧️ Rain</button>
                                <button onclick="neurodivergentSupport.playSound('forest')">🌲 Forest</button>
                                <button onclick="neurodivergentSupport.playSound('brown-noise')">🎵 Brown Noise</button>
                                <button onclick="neurodivergentSupport.playSound('coffee-shop')">☕ Coffee Shop</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        document.body.appendChild(sensoryModal);
        this.setupBubbleWrap();
        this.setupSandTray();
    }

    createBreakReminderModal() {
        const breakModal = document.createElement('div');
        breakModal.id = 'break-reminder-modal';
        breakModal.style.display = 'none';
        breakModal.innerHTML = `
            <div class="break-modal-content">
                <h3><i class="fas fa-pause-circle"></i> Time for a Brain Break!</h3>
                <p id="break-message">You've been focusing hard! Your brain needs a refresh.</p>
                
                <div class="break-options">
                    <button class="break-option movement" onclick="neurodivergentSupport.startMovementBreak()">
                        <i class="fas fa-walking"></i>
                        <h4>Movement Break</h4>
                        <p>2-3 minutes of gentle movement</p>
                    </button>
                    
                    <button class="break-option breathing" onclick="neurodivergentSupport.startBreathingBreak()">
                        <i class="fas fa-lungs"></i>
                        <h4>Breathing Break</h4>
                        <p>5 minutes of mindful breathing</p>
                    </button>
                    
                    <button class="break-option sensory" onclick="neurodivergentSupport.startSensoryBreak()">
                        <i class="fas fa-hand-paper"></i>
                        <h4>Sensory Break</h4>
                        <p>Fidget tools and calming activities</p>
                    </button>
                    
                    <button class="break-option hydrate" onclick="neurodivergentSupport.startHydrationBreak()">
                        <i class="fas fa-tint"></i>
                        <h4>Hydration Break</h4>
                        <p>Drink water and reset</p>
                    </button>
                </div>
                
                <div class="break-actions">
                    <button class="skip-break" onclick="neurodivergentSupport.skipBreak()">
                        I'm in hyperfocus - remind me in 15 min
                    </button>
                    <button class="snooze-break" onclick="neurodivergentSupport.snoozeBreak()">
                        Just 5 more minutes
                    </button>
                </div>
            </div>
        `;
        
        document.body.appendChild(breakModal);
    }

    startFocusTracking() {
        this.focusSession.startTime = Date.now();
        this.updateFocusDisplay();
        
        // Update focus timer every 30 seconds
        setInterval(() => {
            this.updateFocusDisplay();
            this.checkForBreakReminder();
            this.checkForHyperfocus();
        }, 30000);
    }

    updateFocusDisplay() {
        if (!this.focusSession.startTime) return;
        
        const elapsed = Math.floor((Date.now() - this.focusSession.startTime) / 60000);
        const focusTimeElement = document.getElementById('focus-time');
        
        if (focusTimeElement) {
            if (elapsed < 60) {
                focusTimeElement.textContent = `${elapsed} min focused`;
            } else {
                const hours = Math.floor(elapsed / 60);
                const minutes = elapsed % 60;
                focusTimeElement.textContent = `${hours}h ${minutes}m focused`;
            }
        }
        
        // Update energy hearts
        this.updateEnergyDisplay();
    }

    updateEnergyDisplay() {
        const energyElement = document.getElementById('energy-display');
        if (!energyElement) return;
        
        const hearts = '❤️'.repeat(this.focusSession.energyLevel) + 
                     '🤍'.repeat(10 - this.focusSession.energyLevel);
        energyElement.textContent = hearts;
    }

    checkForBreakReminder() {
        if (!this.settings.enableBreakReminders) return;
        
        const elapsed = Math.floor((Date.now() - this.focusSession.startTime) / 60000);
        
        if (elapsed > 0 && elapsed % this.settings.breakInterval === 0) {
            this.showBreakReminder();
        }
    }

    checkForHyperfocus() {
        if (!this.settings.enableHyperfocusShield) return;
        
        const elapsed = Math.floor((Date.now() - this.focusSession.startTime) / 60000);
        
        if (elapsed >= this.settings.hyperfocusLimit && !this.focusSession.hyperfocusWarning) {
            this.showHyperfocusWarning();
            this.focusSession.hyperfocusWarning = true;
        }
    }

    showBreakReminder() {
        const elapsed = Math.floor((Date.now() - this.focusSession.startTime) / 60000);
        const message = document.getElementById('break-message');
        
        if (message) {
            message.textContent = `You've been focused for ${elapsed} minutes. Your brain will work better after a short break!`;
        }
        
        document.getElementById('break-reminder-modal').style.display = 'flex';
        this.playGentleChime();
    }

    showHyperfocusWarning() {
        this.showNotification(
            '🧠 Hyperfocus Detected!', 
            'You\'ve been intensely focused for over 90 minutes. This is amazing, but your brain needs care too. Consider a 10-minute break.',
            'warning'
        );
    }

    setupEventListeners() {
        // Wait for DOM elements to be available
        setTimeout(() => {
            // Overwhelm escape hatch
            const escapeBtn = document.getElementById('overwhelm-escape');
            if (escapeBtn) {
                escapeBtn.addEventListener('click', () => {
                    console.log('Escape hatch clicked!');
                    this.showCalmMode();
                });
            } else {
                console.error('Overwhelm escape button not found');
            }
            
            // Quick tools
            const fidgetBtn = document.getElementById('fidget-tool');
            if (fidgetBtn) {
                fidgetBtn.addEventListener('click', () => {
                    console.log('Fidget tool clicked!');
                    this.showSensoryTools();
                });
            }
            
            const breathingBtn = document.getElementById('breathing-tool');
            if (breathingBtn) {
                breathingBtn.addEventListener('click', () => {
                    console.log('🫁 Starting breathing exercise...');
                    this.showToolActivation('breathing-tool');
                    setTimeout(() => {
                        this.startBreathingExercise();
                    }, 300);
                });
            }
            
            const energyBtn = document.getElementById('energy-check');
            if (energyBtn) {
                energyBtn.addEventListener('click', () => {
                    console.log('Energy check clicked!');
                    this.showEnergyCheck();
                });
            }
            
            const timerBtn = document.getElementById('break-timer');
            if (timerBtn) {
                timerBtn.addEventListener('click', () => {
                    console.log('Focus session clicked!');
                    this.startFocusSession();
                });
            }
            
            // Panel controls
            const positionBtn = document.getElementById('position-panel');
            if (positionBtn) {
                positionBtn.addEventListener('click', () => {
                    this.showPositionOptions();
                });
            }
            
            const minimizeBtn = document.getElementById('minimize-panel');
            if (minimizeBtn) {
                minimizeBtn.addEventListener('click', () => {
                    this.minimizePanel();
                });
            }
            
            const closeBtn = document.getElementById('close-panel');
            if (closeBtn) {
                closeBtn.addEventListener('click', () => {
                    this.hidePanel();
                });
            }
            
            const restoreBtn = document.getElementById('restore-panel');
            if (restoreBtn) {
                restoreBtn.addEventListener('click', () => {
                    this.restorePanel();
                });
            }
        }, 500); // Wait 500ms for DOM to be ready
    }

    makePanelDraggable() {
        const panel = document.getElementById('neurodivergent-support-panel');
        const header = document.getElementById('panel-header');
        
        if (!panel || !header) return;
        
        let isDragging = false;
        let currentX;
        let currentY;
        let initialX;
        let initialY;
        let xOffset = 0;
        let yOffset = 0;
        
        header.style.cursor = 'move';
        
        function dragStart(e) {
            if (e.type === "touchstart") {
                initialX = e.touches[0].clientX - xOffset;
                initialY = e.touches[0].clientY - yOffset;
            } else {
                initialX = e.clientX - xOffset;
                initialY = e.clientY - yOffset;
            }
            
            if (e.target === header || header.contains(e.target)) {
                isDragging = true;
                panel.style.transition = 'none';
            }
        }
        
        function dragEnd(e) {
            initialX = currentX;
            initialY = currentY;
            isDragging = false;
            panel.style.transition = 'all 0.3s ease';
        }
        
        function drag(e) {
            if (isDragging) {
                e.preventDefault();
                
                if (e.type === "touchmove") {
                    currentX = e.touches[0].clientX - initialX;
                    currentY = e.touches[0].clientY - initialY;
                } else {
                    currentX = e.clientX - initialX;
                    currentY = e.clientY - initialY;
                }
                
                xOffset = currentX;
                yOffset = currentY;
                
                // Keep panel within viewport
                const rect = panel.getBoundingClientRect();
                const maxX = window.innerWidth - rect.width;
                const maxY = window.innerHeight - rect.height;
                
                currentX = Math.max(0, Math.min(currentX, maxX));
                currentY = Math.max(0, Math.min(currentY, maxY));
                
                panel.style.transform = `translate(${currentX}px, ${currentY}px)`;
            }
        }
        
        header.addEventListener("mousedown", dragStart);
        document.addEventListener("mousemove", drag);
        document.addEventListener("mouseup", dragEnd);
        
        // Touch events
        header.addEventListener("touchstart", dragStart);
        document.addEventListener("touchmove", drag);
        document.addEventListener("touchend", dragEnd);
    }

    minimizePanel() {
        document.getElementById('support-panel-inner').style.display = 'none';
        document.getElementById('minimized-panel').style.display = 'block';
        
        // Save minimized state
        localStorage.setItem('neurodivergentPanelState', 'minimized');
        console.log('Panel minimized and state saved');
    }

    restorePanel() {
        document.getElementById('support-panel-inner').style.display = 'block';
        document.getElementById('minimized-panel').style.display = 'none';
        
        // Save expanded state
        localStorage.setItem('neurodivergentPanelState', 'expanded');
        console.log('Panel restored and state saved');
    }

    hidePanel() {
        document.getElementById('neurodivergent-support-panel').style.display = 'none';
        
        // Save hidden state
        localStorage.setItem('neurodivergentPanelState', 'hidden');
        console.log('Panel hidden and state saved');
        
        // Show a small restore button at the bottom
        this.createRestoreButton();
    }

    createRestoreButton() {
        if (document.getElementById('restore-helper-btn')) return;
        
        const restoreBtn = document.createElement('button');
        restoreBtn.id = 'restore-helper-btn';
        restoreBtn.innerHTML = '🧠';
        restoreBtn.title = 'Show Focus Helper';
        restoreBtn.style.cssText = `
            position: fixed;
            bottom: 20px;
            right: 20px;
            z-index: 99998;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            border: none;
            cursor: pointer;
            font-size: 20px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.3);
            transition: all 0.3s ease;
        `;
        
        restoreBtn.addEventListener('click', () => {
            document.getElementById('neurodivergent-support-panel').style.display = 'block';
            // Save expanded state
            localStorage.setItem('neurodivergentPanelState', 'expanded');
            restoreBtn.remove();
            console.log('Panel restored from button and state saved');
        });
        
        restoreBtn.addEventListener('mouseenter', () => {
            restoreBtn.style.transform = 'scale(1.1)';
        });
        
        restoreBtn.addEventListener('mouseleave', () => {
            restoreBtn.style.transform = 'scale(1)';
        });
        
        document.body.appendChild(restoreBtn);
    }

    showPositionOptions() {
        const positionModal = document.createElement('div');
        positionModal.id = 'position-options-modal';
        positionModal.style.cssText = `
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.8); z-index: 15000; display: flex;
            align-items: center; justify-content: center;
        `;
        
        positionModal.innerHTML = `
            <div style="background: white; border-radius: 20px; padding: 40px; text-align: center; max-width: 400px;">
                <h3><i class="fas fa-arrows-alt"></i> Choose Panel Position</h3>
                <p>Pick where you'd like the Focus Helper to appear:</p>
                
                <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; margin: 20px 0;">
                    <button onclick="neurodivergentSupport.setPosition('top-right')" style="padding: 15px; border: 2px solid #667eea; 
                           border-radius: 10px; background: white; cursor: pointer; color: #667eea;">
                        <i class="fas fa-arrow-up"></i><i class="fas fa-arrow-right"></i><br>Top Right
                    </button>
                    <button onclick="neurodivergentSupport.setPosition('top-left')" style="padding: 15px; border: 2px solid #667eea; 
                           border-radius: 10px; background: white; cursor: pointer; color: #667eea;">
                        <i class="fas fa-arrow-up"></i><i class="fas fa-arrow-left"></i><br>Top Left
                    </button>
                    <button onclick="neurodivergentSupport.setPosition('bottom-right')" style="padding: 15px; border: 2px solid #667eea; 
                           border-radius: 10px; background: white; cursor: pointer; color: #667eea;">
                        <i class="fas fa-arrow-down"></i><i class="fas fa-arrow-right"></i><br>Bottom Right
                    </button>
                    <button onclick="neurodivergentSupport.setPosition('bottom-left')" style="padding: 15px; border: 2px solid #667eea; 
                           border-radius: 10px; background: white; cursor: pointer; color: #667eea;">
                        <i class="fas fa-arrow-down"></i><i class="fas fa-arrow-left"></i><br>Bottom Left
                    </button>
                </div>
                
                <button onclick="neurodivergentSupport.closePositionOptions()" 
                        style="background: #6c757d; color: white; border: none; padding: 12px 20px; 
                               border-radius: 8px; cursor: pointer; margin-top: 20px;">
                    Cancel
                </button>
            </div>
        `;
        
        document.body.appendChild(positionModal);
    }

    setPosition(position, silent = false) {
        const panel = document.getElementById('neurodivergent-support-panel');
        if (!panel) return;
        
        // Reset transform to get accurate positioning
        panel.style.transform = 'none';
        
        switch(position) {
            case 'top-right':
                panel.style.top = '20px';
                panel.style.right = '20px';
                panel.style.bottom = 'auto';
                panel.style.left = 'auto';
                break;
            case 'top-left':
                panel.style.top = '20px';
                panel.style.left = '20px';
                panel.style.bottom = 'auto';
                panel.style.right = 'auto';
                break;
            case 'bottom-right':
                panel.style.bottom = '20px';
                panel.style.right = '20px';
                panel.style.top = 'auto';
                panel.style.left = 'auto';
                break;
            case 'bottom-left':
                panel.style.bottom = '20px';
                panel.style.left = '20px';
                panel.style.top = 'auto';
                panel.style.right = 'auto';
                break;
        }
        
        // Save position preference only if not loading silently
        if (!silent) {
            localStorage.setItem('neurodivergentPanelPosition', position);
            
            this.showNotification(
                '📍 Position Updated!',
                `Panel moved to ${position.replace('-', ' ')}`,
                'success'
            );
            
            this.closePositionOptions();
        }
    }

    closePositionOptions() {
        const modal = document.getElementById('position-options-modal');
        if (modal) modal.remove();
    }

    loadPanelPosition() {
        const savedPosition = localStorage.getItem('neurodivergentPanelPosition');
        if (savedPosition) {
            console.log('Loading saved panel position:', savedPosition);
            this.setPosition(savedPosition, true); // Silent load
        }
    }

    loadPanelState() {
        const savedState = localStorage.getItem('neurodivergentPanelState');
        console.log('Loading saved panel state:', savedState);
        
        // Small delay to ensure DOM is ready
        setTimeout(() => {
            const panel = document.getElementById('neurodivergent-support-panel');
            const panelInner = document.getElementById('support-panel-inner');
            const minimizedPanel = document.getElementById('minimized-panel');
            
            if (!panel || !panelInner || !minimizedPanel) {
                console.log('Panel elements not found for state loading');
                return;
            }
            
            switch(savedState) {
                case 'minimized':
                    panelInner.style.display = 'none';
                    minimizedPanel.style.display = 'block';
                    panel.style.display = 'block';
                    console.log('✅ Panel loaded in minimized state');
                    break;
                    
                case 'hidden':
                    panel.style.display = 'none';
                    this.createRestoreButton();
                    console.log('✅ Panel loaded in hidden state');
                    break;
                    
                case 'expanded':
                default:
                    panelInner.style.display = 'block';
                    minimizedPanel.style.display = 'none';
                    panel.style.display = 'block';
                    console.log('✅ Panel loaded in expanded state');
                    break;
            }
        }, 500);
    }

    async initializeAdaptiveEcosystem() {
        try {
            if (typeof AdaptiveToolEcosystem !== 'undefined' && typeof individualProfile !== 'undefined') {
                console.log('🧬 Initializing adaptive ecosystem...');
                this.adaptiveEcosystem = new AdaptiveToolEcosystem(this, individualProfile);
                await this.adaptiveEcosystem.initialize();
                
                // Make accessible globally for the adaptive functions
                window.adaptiveToolEcosystem = this.adaptiveEcosystem;
                
                console.log('✨ Individual neurodivergent companion ready!');
            } else {
                console.log('⏸️ Adaptive ecosystem dependencies not loaded yet');
            }
        } catch (error) {
            console.log('📝 Running in standard mode - adaptive features will be available after assessment');
        }
    }

    showCalmMode() {
        console.log('🧘 Activating calm mode...');
        this.showToolActivation('overwhelm-escape');
        
        // Record tool usage
        this.recordToolUsage('escape_hatch', 'overwhelm', null, null);
        
        setTimeout(() => {
            document.getElementById('calm-mode-overlay').style.display = 'flex';
            this.startBreathingCircle();
            this.logAchievement('selfCare', 'Used calm mode when feeling overwhelmed');
        }, 300);
    }

    hideCalmMode() {
        document.getElementById('calm-mode-overlay').style.display = 'none';
    }

    showToolActivation(buttonId) {
        const button = document.getElementById(buttonId);
        if (!button) return;
        
        // Add activation effect
        button.style.transform = 'scale(0.95)';
        button.style.filter = 'brightness(1.2)';
        
        // Create ripple effect
        const ripple = document.createElement('div');
        ripple.style.cssText = `
            position: absolute;
            top: 50%;
            left: 50%;
            width: 0;
            height: 0;
            background: rgba(255, 255, 255, 0.5);
            border-radius: 50%;
            transform: translate(-50%, -50%);
            animation: rippleEffect 0.6s ease-out;
            pointer-events: none;
            z-index: 1000;
        `;
        
        button.style.position = 'relative';
        button.appendChild(ripple);
        
        // Reset button after animation
        setTimeout(() => {
            button.style.transform = '';
            button.style.filter = '';
            if (ripple.parentNode) {
                ripple.remove();
            }
        }, 600);
    }

    startBreathingCircle() {
        const circle = document.getElementById('breathing-circle');
        const instruction = document.getElementById('breath-instruction');
        
        let phase = 'inhale'; // inhale, hold, exhale, pause
        let count = 0;
        
        const breathingCycle = () => {
            switch(phase) {
                case 'inhale':
                    instruction.textContent = 'Breathe in...';
                    circle.classList.add('expand');
                    count++;
                    if (count >= 4) {
                        phase = 'hold';
                        count = 0;
                    }
                    break;
                case 'hold':
                    instruction.textContent = 'Hold...';
                    count++;
                    if (count >= 2) {
                        phase = 'exhale';
                        count = 0;
                    }
                    break;
                case 'exhale':
                    instruction.textContent = 'Breathe out...';
                    circle.classList.remove('expand');
                    count++;
                    if (count >= 4) {
                        phase = 'pause';
                        count = 0;
                    }
                    break;
                case 'pause':
                    instruction.textContent = 'Rest...';
                    count++;
                    if (count >= 2) {
                        phase = 'inhale';
                        count = 0;
                    }
                    break;
            }
        };
        
        // Start the breathing cycle
        const breathingInterval = setInterval(breathingCycle, 1000);
        
        // Stop after 2 minutes
        setTimeout(() => {
            clearInterval(breathingInterval);
            instruction.textContent = 'Great job! You did it.';
        }, 120000);
    }

    showSensoryTools() {
        console.log('🤲 Opening fidget tools...');
        this.showToolActivation('fidget-tool');
        
        setTimeout(() => {
            document.getElementById('sensory-tools-modal').style.display = 'flex';
        }, 300);
    }

    closeSensoryTools() {
        document.getElementById('sensory-tools-modal').style.display = 'none';
    }

    // Achievement and motivation system
    logAchievement(type, description) {
        this.achievements[type]++;
        this.showAchievementNotification(description);
        this.saveAchievements();
    }

    showAchievementNotification(description) {
        const indicator = document.getElementById('achievement-indicator');
        if (indicator) {
            indicator.querySelector('span').textContent = description;
            indicator.style.display = 'block';
            indicator.style.animation = 'achievementPulse 0.5s ease';
            
            // Add celebration particle effect
            this.createCelebrationParticles();
            
            setTimeout(() => {
                indicator.style.opacity = '0';
                indicator.style.transform = 'translateY(-20px)';
                setTimeout(() => {
                    indicator.style.display = 'none';
                    indicator.style.opacity = '1';
                    indicator.style.transform = 'translateY(0)';
                }, 300);
            }, 3000);
        }
    }

    createCelebrationParticles() {
        const panel = document.getElementById('neurodivergent-support-panel');
        if (!panel) return;

        for (let i = 0; i < 5; i++) {
            const particle = document.createElement('div');
            particle.innerHTML = ['✨', '⭐', '🎉', '💫', '🌟'][i];
            particle.style.cssText = `
                position: absolute;
                top: 50%;
                left: 50%;
                font-size: 20px;
                pointer-events: none;
                z-index: 10000;
                animation: particleFloat 2s ease-out forwards;
                animation-delay: ${i * 0.1}s;
            `;
            
            panel.appendChild(particle);
            
            setTimeout(() => {
                if (particle.parentNode) {
                    particle.remove();
                }
            }, 2000);
        }
    }

    showNotification(title, message, type = 'info') {
        // Create temporary notification
        const notification = document.createElement('div');
        notification.className = `nd-notification nd-${type}`;
        notification.innerHTML = `
            <h4>${title}</h4>
            <p>${message}</p>
            <button onclick="this.parentElement.remove()">Got it!</button>
        `;
        
        document.body.appendChild(notification);
        
        // Auto-remove after 10 seconds
        setTimeout(() => {
            if (notification.parentElement) {
                notification.remove();
            }
        }, 10000);
    }

    playGentleChime() {
        // Create a gentle, non-startling notification sound
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();
        
        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);
        
        oscillator.frequency.value = 440; // A note
        gainNode.gain.setValueAtTime(0, audioContext.currentTime);
        gainNode.gain.linearRampToValueAtTime(0.1, audioContext.currentTime + 0.1);
        gainNode.gain.linearRampToValueAtTime(0, audioContext.currentTime + 0.5);
        
        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.5);
    }

    // Break management methods
    startMovementBreak() {
        this.hideBreakReminder();
        this.showMovementBreakInstructions();
    }

    startBreathingBreak() {
        this.hideBreakReminder();
        this.showCalmMode();
    }

    startSensoryBreak() {
        this.hideBreakReminder();
        this.showSensoryTools();
    }

    startHydrationBreak() {
        this.hideBreakReminder();
        this.showHydrationReminder();
    }

    skipBreak() {
        this.hideBreakReminder();
        // Remind again in 15 minutes
        setTimeout(() => {
            this.showBreakReminder();
        }, 15 * 60 * 1000);
    }

    snoozeBreak() {
        this.hideBreakReminder();
        // Remind again in 5 minutes
        setTimeout(() => {
            this.showBreakReminder();
        }, 5 * 60 * 1000);
    }

    hideBreakReminder() {
        document.getElementById('break-reminder-modal').style.display = 'none';
    }

    showMovementBreakInstructions() {
        this.showNotification(
            '🚶‍♀️ Movement Break Time!',
            'Try: Stand up and stretch, walk around, do some jumping jacks, or gentle neck rolls. Even 2 minutes helps!',
            'success'
        );
        this.logAchievement('breaksTaken', 'Took a movement break');
    }

    showHydrationReminder() {
        this.showNotification(
            '💧 Hydration Check!',
            'Grab some water! Your brain is 75% water and needs regular refills to work its best.',
            'info'
        );
        this.logAchievement('breaksTaken', 'Took a hydration break');
    }

    // Energy management
    showEnergyCheck() {
        console.log('🔋 Opening energy check...');
        this.showToolActivation('energy-check');
        
        setTimeout(() => {
            this.createEnergyModal();
        }, 300);
    }

    createEnergyModal() {
        const energyModal = document.createElement('div');
        energyModal.id = 'energy-check-modal';
        energyModal.style.cssText = `
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.8); z-index: 15000; display: flex;
            align-items: center; justify-content: center;
        `;
        
        energyModal.innerHTML = `
            <div style="background: white; border-radius: 20px; padding: 40px; text-align: center; max-width: 500px;">
                <h3><i class="fas fa-battery-half"></i> How's Your Energy?</h3>
                <p>Understanding your energy helps me support you better!</p>
                
                <div style="display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; margin: 30px 0;">
                    ${[1,2,3,4,5,6,7,8,9,10].map(level => `
                        <button onclick="neurodivergentSupport.setEnergyLevel(${level})" 
                                style="padding: 15px; border: 2px solid #ddd; border-radius: 10px; 
                                       background: white; cursor: pointer; transition: all 0.3s ease;">
                            <div style="font-size: 1.5rem;">${'❤️'.repeat(Math.ceil(level/2))}</div>
                            <div style="font-size: 0.8rem; margin-top: 5px;">${level}</div>
                        </button>
                    `).join('')}
                </div>
                
                <div style="margin-top: 30px;">
                    <button onclick="neurodivergentSupport.closeEnergyCheck()" 
                            style="background: #667eea; color: white; border: none; padding: 12px 20px; 
                                   border-radius: 8px; cursor: pointer;">
                        Done
                    </button>
                </div>
            </div>
        `;
        
        document.body.appendChild(energyModal);
    }

    setEnergyLevel(level) {
        this.focusSession.energyLevel = level;
        this.updateEnergyDisplay();
        
        // Record tool usage for energy check
        this.recordToolUsage('energy_check', 'level_setting', null, null);
        
        // Adjust recommendations based on energy
        if (level <= 3) {
            this.showNotification(
                '🔋 Low Energy Detected',
                'Consider taking a longer break, getting some fresh air, or having a healthy snack.',
                'warning'
            );
        } else if (level >= 8) {
            this.showNotification(
                '⚡ High Energy!',
                'Great! This might be a good time for challenging tasks or creative work.',
                'success'
            );
        }
        
        this.closeEnergyCheck();
        this.logAchievement('adaptiveChoices', 'Checked energy level');
    }

    closeEnergyCheck() {
        const modal = document.getElementById('energy-check-modal');
        if (modal) modal.remove();
    }

    // Focus session management
    startFocusSession() {
        console.log('⏰ Starting focus session...');
        this.showToolActivation('break-timer');
        
        setTimeout(() => {
            this.createFocusSessionModal();
        }, 300);
    }

    createFocusSessionModal() {
        console.log('Creating focus session modal...');
        
        const sessionModal = document.createElement('div');
        sessionModal.id = 'focus-session-modal';
        sessionModal.style.cssText = `
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.8); z-index: 15000; display: flex;
            align-items: center; justify-content: center;
        `;
        
        sessionModal.innerHTML = `
            <div style="background: white; border-radius: 20px; padding: 40px; text-align: center; max-width: 500px;">
                <h3><i class="fas fa-clock"></i> Start Focus Session</h3>
                <p>What would you like to focus on?</p>
                
                <input type="text" id="focus-task" placeholder="e.g., Math homework, Reading chapter 3..." 
                       style="width: 100%; padding: 15px; border: 2px solid #ddd; border-radius: 10px; 
                              font-size: 16px; margin: 20px 0;">
                
                <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin: 20px 0;">
                    <button onclick="neurodivergentSupport.startSession(15)" style="padding: 15px; border: 2px solid #667eea; 
                           border-radius: 10px; background: white; cursor: pointer; color: #667eea;">
                        15 min<br><small>Quick burst</small>
                    </button>
                    <button onclick="neurodivergentSupport.startSession(25)" style="padding: 15px; border: 2px solid #667eea; 
                           border-radius: 10px; background: #667eea; cursor: pointer; color: white;">
                        25 min<br><small>Standard</small>
                    </button>
                    <button onclick="neurodivergentSupport.startSession(45)" style="padding: 15px; border: 2px solid #667eea; 
                           border-radius: 10px; background: white; cursor: pointer; color: #667eea;">
                        45 min<br><small>Deep work</small>
                    </button>
                </div>
                
                <button onclick="neurodivergentSupport.closeFocusSession()" 
                        style="background: #6c757d; color: white; border: none; padding: 12px 20px; 
                               border-radius: 8px; cursor: pointer; margin-top: 20px;">
                    Cancel
                </button>
            </div>
        `;
        
        document.body.appendChild(sessionModal);
        console.log('Focus session modal added to DOM');
    }

    startSession(minutes) {
        const task = document.getElementById('focus-task')?.value || 'Focused work';
        this.focusSession.currentTask = task;
        this.focusSession.startTime = Date.now();
        
        // Record tool usage for focus timer
        this.recordToolUsage('focus_timer', 'session_start', minutes, null);
        
        this.showNotification(
            '🧠 Focus Session Started!',
            `Working on: ${task} for ${minutes} minutes. You've got this!`,
            'success'
        );
        
        this.closeFocusSession();
        this.logAchievement('sessionsCompleted', 'Started a focus session');
        
        // Set break reminder for this session
        setTimeout(() => {
            this.showBreakReminder();
        }, minutes * 60 * 1000);
    }

    closeFocusSession() {
        console.log('Closing focus session modal...');
        const modal = document.getElementById('focus-session-modal');
        if (modal) {
            modal.remove();
            console.log('Focus session modal removed');
        } else {
            console.log('Focus session modal not found');
        }
    }

    // Sensory tools implementation
    setupBubbleWrap() {
        const bubbleWrap = document.querySelector('.bubble-wrap');
        if (!bubbleWrap) return;
        
        // Create 40 bubbles
        for (let i = 0; i < 40; i++) {
            const bubble = document.createElement('div');
            bubble.className = 'bubble';
            bubble.onclick = () => this.popBubble(bubble);
            bubbleWrap.appendChild(bubble);
        }
    }

    popBubble(bubble) {
        bubble.classList.add('popped');
        
        // Play pop sound
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();
        
        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);
        
        oscillator.frequency.value = 800;
        gainNode.gain.setValueAtTime(0, audioContext.currentTime);
        gainNode.gain.linearRampToValueAtTime(0.1, audioContext.currentTime + 0.01);
        gainNode.gain.linearRampToValueAtTime(0, audioContext.currentTime + 0.1);
        
        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.1);
        
        this.logAchievement('selfCarePoints', 'Used fidget tools');
    }

    setupSandTray() {
        const canvas = document.getElementById('sand-canvas');
        if (!canvas) return;
        
        const ctx = canvas.getContext('2d');
        let drawing = false;
        
        canvas.addEventListener('mousedown', () => drawing = true);
        canvas.addEventListener('mouseup', () => drawing = false);
        canvas.addEventListener('mouseleave', () => drawing = false);
        
        canvas.addEventListener('mousemove', (e) => {
            if (!drawing) return;
            
            const rect = canvas.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            ctx.globalCompositeOperation = 'source-over';
            ctx.fillStyle = '#8B4513';
            ctx.beginPath();
            ctx.arc(x, y, 5, 0, Math.PI * 2);
            ctx.fill();
        });
        
        // Clear button
        const clearBtn = document.createElement('button');
        clearBtn.textContent = 'Clear Sand';
        clearBtn.style.cssText = 'margin-top: 10px; padding: 8px 15px; background: #d4af37; color: white; border: none; border-radius: 5px; cursor: pointer;';
        clearBtn.onclick = () => ctx.clearRect(0, 0, canvas.width, canvas.height);
        canvas.parentElement.appendChild(clearBtn);
    }

    squishBall() {
        const ball = document.querySelector('.stress-ball');
        ball.style.transform = 'scale(0.8)';
        setTimeout(() => {
            ball.style.transform = 'scale(1)';
        }, 200);
        
        this.logAchievement('selfCarePoints', 'Used stress ball');
    }

    playSound(type) {
        // Placeholder for sound implementation
        this.showNotification(
            '🎵 Playing ' + type.replace('-', ' '),
            'Sounds would play here in a full implementation!',
            'info'
        );
    }

    startBreathingExercise() {
        // Record tool usage for breathing exercise
        this.recordToolUsage('breathing_tool', 'exercise_start', null, null);
        this.showCalmMode();
    }

    playFocusSounds() {
        this.showNotification(
            '🎵 Focus Sounds',
            'Calming background sounds would play here!',
            'info'
        );
    }

    showAffirmations() {
        const affirmations = [
            "Your brain works beautifully in its own unique way.",
            "Taking breaks shows wisdom, not weakness.",
            "You are capable of amazing things.",
            "Every small step forward is worth celebrating.",
            "Your neurodivergent mind is a gift to the world.",
            "You're learning and growing every day.",
            "It's okay to need support - everyone does.",
            "Your focus style is perfectly valid.",
            "You bring unique perspectives that matter.",
            "You're exactly who you're meant to be."
        ];
        
        const randomAffirmation = affirmations[Math.floor(Math.random() * affirmations.length)];
        
        this.showNotification(
            '💙 You Are Amazing',
            randomAffirmation,
            'success'
        );
    }

    showWelcomeMessage() {
        // Only show welcome if it's a new session (not shown in last hour)
        const lastWelcome = localStorage.getItem('neurodivergentLastWelcome');
        const now = Date.now();
        const oneHour = 60 * 60 * 1000;
        
        if (!lastWelcome || (now - parseInt(lastWelcome)) > oneHour) {
            const welcomeMessages = [
                "🌟 Your Focus Helper is here to support you!",
                "💙 Ready to help you learn at your own pace!",
                "✨ Here when you need a moment to breathe or refocus!",
                "🧠 Supporting your amazing neurodivergent mind!",
                "🤗 Your personal learning companion is ready!"
            ];
            
            const randomMessage = welcomeMessages[Math.floor(Math.random() * welcomeMessages.length)];
            
            this.showNotification(
                '🌈 Welcome!',
                randomMessage,
                'success'
            );
            
            localStorage.setItem('neurodivergentLastWelcome', now.toString());
        }
    }

    // Save/Load functions
    saveSettings() {
        localStorage.setItem('neurodivergentSettings', JSON.stringify(this.settings));
    }

    loadSettings() {
        const saved = localStorage.getItem('neurodivergentSettings');
        if (saved) {
            this.settings = { ...this.settings, ...JSON.parse(saved) };
        }
    }

    saveAchievements() {
        localStorage.setItem('neurodivergentAchievements', JSON.stringify(this.achievements));
    }

    loadAchievements() {
        const saved = localStorage.getItem('neurodivergentAchievements');
        if (saved) {
            this.achievements = { ...this.achievements, ...JSON.parse(saved) };
        }
    }
    
    // Get current user ID (replace with actual authentication)
    getCurrentUserId() {
        // TODO: Integrate with actual authentication system
        return localStorage.getItem('currentUserId') || '1'; // Default to user 1 for demo
    }
    
    // Record tool usage for pattern analysis
    async recordToolUsage(toolName, context = 'learning', duration = null, successRating = null) {
        try {
            const usageData = {
                toolName: toolName,
                toolContext: context,
                sessionDurationMinutes: duration,
                successRating: successRating,
                userEnergyLevel: this.focusSession.energyLevel,
                activityContext: 'neurodivergent_support'
            };
            
            const response = await fetch(`${this.apiBaseUrl}/usage/${this.userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(usageData)
            });
            
            if (response.ok) {
                console.log(`📊 Tool usage recorded: ${toolName}`);
                return true;
            } else {
                console.warn(`Failed to record tool usage for ${toolName}`);
                return false;
            }
        } catch (error) {
            console.error('Error recording tool usage:', error);
            return false;
        }
    }
    
    // Load personalized tool priorities from backend
    async loadToolPriorities() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/profile/${this.userId}/tool-priorities`);
            
            if (response.ok) {
                const priorities = await response.json();
                this.applyToolPriorities(priorities);
                console.log('🛠️ Tool priorities loaded and applied');
                return priorities;
            } else {
                console.warn('Failed to load tool priorities from backend');
                return null;
            }
        } catch (error) {
            console.error('Error loading tool priorities:', error);
            return null;
        }
    }
    
    // Apply tool priorities to the interface
    applyToolPriorities(priorities) {
        // Update button order and visibility based on priorities
        const toolContainer = document.getElementById('neurodivergent-panel-content');
        if (!toolContainer) return;
        
        const buttons = toolContainer.querySelectorAll('button');
        const sortedButtons = Array.from(buttons).sort((a, b) => {
            const aPriority = this.getButtonPriority(a, priorities);
            const bPriority = this.getButtonPriority(b, priorities);
            return bPriority - aPriority; // Higher priority first
        });
        
        // Reorder buttons
        sortedButtons.forEach((button, index) => {
            const priority = this.getButtonPriority(button, priorities);
            
            // Add priority indicators for high-priority tools
            if (priority >= 8) {
                button.style.border = '2px solid #ffd700'; // Golden border
                button.style.background = 'linear-gradient(135deg, #fff9c4, #fff)';
                
                // Add sparkle indicator
                if (!button.querySelector('.priority-indicator')) {
                    const indicator = document.createElement('span');
                    indicator.className = 'priority-indicator';
                    indicator.innerHTML = '✨';
                    indicator.style.cssText = `
                        position: absolute;
                        top: -5px;
                        right: -5px;
                        font-size: 12px;
                        animation: sparkle 2s infinite;
                    `;
                    button.style.position = 'relative';
                    button.appendChild(indicator);
                }
            }
            
            toolContainer.appendChild(button);
        });
    }
    
    // Get priority for a button based on its tool name
    getButtonPriority(button, priorities) {
        const buttonId = button.id;
        
        if (buttonId === 'escape-hatch') return priorities.escape_hatch || 5;
        if (buttonId === 'break-timer') return priorities.focus_timer || 5;
        if (buttonId === 'fidget-tools') return priorities.fidget_tools || 5;
        if (buttonId === 'breathing-tool') return priorities.breathing_tool || 5;
        if (buttonId === 'energy-check') return priorities.energy_check || 5;
        
        return 5; // Default priority
    }
    
    // Load personalized insights
    async loadInsights() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/insights/${this.userId}`);
            
            if (response.ok) {
                const insights = await response.json();
                this.displayInsights(insights);
                console.log('💡 Personalized insights loaded');
                return insights;
            } else {
                console.warn('No insights available from backend');
                return [];
            }
        } catch (error) {
            console.error('Error loading insights:', error);
            return [];
        }
    }
    
    // Display insights to user
    displayInsights(insights) {
        if (insights.length === 0) return;
        
        // Create insights notification
        const insightModal = document.createElement('div');
        insightModal.id = 'insights-modal';
        insightModal.style.cssText = `
            position: fixed; top: 20px; right: 20px; 
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white; border-radius: 15px; padding: 20px;
            max-width: 350px; z-index: 16000;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
            animation: slideInFromRight 0.5s ease-out;
        `;
        
        insightModal.innerHTML = `
            <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 15px;">
                <i class="fas fa-lightbulb" style="font-size: 20px;"></i>
                <h4 style="margin: 0;">Personal Insight</h4>
                <button onclick="this.parentElement.parentElement.remove()" 
                        style="margin-left: auto; background: none; border: none; color: white; cursor: pointer;">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <h5 style="margin: 0 0 10px 0;">${insights[0].insightTitle}</h5>
            <p style="margin: 0 0 15px 0; opacity: 0.9;">${insights[0].insightDescription}</p>
            <div style="display: flex; gap: 10px;">
                <button onclick="neurodivergentSupport.respondToInsight(${insights[0].id}, 'accepted')"
                        style="flex: 1; background: rgba(255,255,255,0.2); border: none; color: white; 
                               padding: 8px; border-radius: 5px; cursor: pointer;">
                    👍 Helpful
                </button>
                <button onclick="neurodivergentSupport.respondToInsight(${insights[0].id}, 'rejected')"
                        style="flex: 1; background: rgba(255,255,255,0.2); border: none; color: white; 
                               padding: 8px; border-radius: 5px; cursor: pointer;">
                    👎 Not useful
                </button>
            </div>
        `;
        
        document.body.appendChild(insightModal);
        
        // Auto-remove after 30 seconds if no response
        setTimeout(() => {
            if (insightModal.parentNode) {
                insightModal.remove();
            }
        }, 30000);
    }
    
    // Respond to insight
    async respondToInsight(insightId, response) {
        try {
            await fetch(`${this.apiBaseUrl}/insights/${insightId}/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ response: response })
            });
            
            // Remove the insight modal
            const modal = document.getElementById('insights-modal');
            if (modal) modal.remove();
            
            console.log(`📝 Insight response recorded: ${response}`);
        } catch (error) {
            console.error('Error responding to insight:', error);
        }
    }
}

// Initialize the system
const neurodivergentSupport = new NeurodivergentSupport();

// Auto-initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('🧠 Starting Neurodivergent Support System...');
    
    // Initialize after a short delay to ensure all other scripts are loaded
    setTimeout(() => {
        neurodivergentSupport.initialize().then(() => {
            console.log('✅ Neurodivergent Support System fully loaded!');
        }).catch(error => {
            console.error('❌ Error initializing Neurodivergent Support:', error);
        });
    }, 1000);
});

// Export for global use
window.NeurodivergentSupport = NeurodivergentSupport;
window.neurodivergentSupport = neurodivergentSupport;