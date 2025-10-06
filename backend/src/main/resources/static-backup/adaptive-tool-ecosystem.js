// Adaptive Tool Ecosystem
// Creates personalized tool experiences based on individual neurodivergent profiles

class AdaptiveToolEcosystem {
    constructor(neurodivergentSupport, individualProfile) {
        this.support = neurodivergentSupport;
        this.profile = individualProfile;
        this.currentContext = {
            activity: 'general',      // 'reading', 'writing', 'math', 'general'
            timeOfDay: 'unknown',
            energyLevel: 7,
            stressLevel: 3,
            sessionDuration: 0
        };
        this.learningData = {
            toolEffectiveness: {},    // User feedback on tools
            usagePatterns: {},        // When/how tools are used
            contextualPreferences: {},// Tool preferences by context
            adaptationHistory: []     // How recommendations change
        };
    }

    async initialize() {
        console.log('🧬 Initializing Adaptive Tool Ecosystem...');
        
        // Load individual profile
        if (!this.profile.load()) {
            await this.profile.initializeFromAssessment();
        }
        
        // Load learning data
        this.loadLearningData();
        
        // Customize tool ecosystem
        this.customizeToolLayout();
        this.personalizeToolBehavior();
        this.setupContextualAdaptation();
        this.enablePatternLearning();
        
        // Initialize personal pattern recognition
        this.initializePatternRecognition();
        
        console.log('✨ Your personalized tool ecosystem is ready!');
    }

    customizeToolLayout() {
        const profile = this.profile.profile;
        console.log('🎨 Customizing tools for your unique mind...');
        
        // Determine tool prominence based on individual traits
        const toolPriorities = this.calculateToolPriorities(profile);
        
        // Customize panel layout
        this.adaptPanelLayout(toolPriorities, profile);
        
        // Add personal elements
        this.addPersonalElements(profile);
    }

    calculateToolPriorities(profile) {
        const priorities = {
            escapeHatch: 5,        // Base priority
            focusTimer: 5,
            fidgetTools: 5,
            breathingTool: 5,
            energyCheck: 5
        };

        // Hyperfocus management - critical for intense hyperfocusers
        if (profile.traits.hyperfocusIntensity > 7) {
            priorities.focusTimer = 9;  // Make timer VERY prominent
            priorities.escapeHatch = 8; // Easy access to breaks
        }

        // Attention flexibility - quick tools for rapid switchers
        if (profile.traits.attentionFlexibility > 7) {
            priorities.fidgetTools = 8; // Quick sensory regulation
            priorities.energyCheck = 7; // Frequent check-ins
            priorities.focusTimer = 3;  // Less emphasis on long focus
        }

        // Sensory processing - overwhelm prevention priority
        if (profile.traits.sensoryProcessing > 7) {
            priorities.escapeHatch = 9; // Critical for overwhelm
            priorities.breathingTool = 8;
            priorities.fidgetTools = 7;
        }

        // Executive function - structure and planning support
        if (profile.traits.executiveFunction < 5) {
            priorities.focusTimer = 8;  // External structure
            priorities.energyCheck = 7; // Self-awareness
        }

        // Emotional regulation - calm and stability tools
        if (profile.traits.emotionalRegulation < 5) {
            priorities.breathingTool = 9;
            priorities.escapeHatch = 8;
        }

        return priorities;
    }

    adaptPanelLayout(priorities, profile) {
        // Sort tools by priority
        const sortedTools = Object.entries(priorities)
            .sort(([,a], [,b]) => b - a)
            .map(([tool]) => tool);

        // Customize escape hatch based on individual needs
        this.customizeEscapeHatch(profile);
        
        // Arrange tools based on priority and preferences
        this.arrangeToolsByPriority(sortedTools, profile);
        
        // Add individual-specific tools
        this.addIndividualTools(profile);
    }

    customizeEscapeHatch(profile) {
        const escapeBtn = document.getElementById('overwhelm-escape');
        if (!escapeBtn) return;

        // Customize based on overwhelming triggers
        if (profile.sensory.overwhelmSigns.includes('light_discomfort')) {
            escapeBtn.innerHTML = `
                <i class="fas fa-life-ring"></i>
                <span>I Need Darkness & Calm</span>
            `;
        } else if (profile.sensory.overwhelmSigns.includes('noise_sensitivity')) {
            escapeBtn.innerHTML = `
                <i class="fas fa-life-ring"></i>
                <span>I Need Quiet & Peace</span>
            `;
        } else if (profile.support.communicationStyle === 'gentle') {
            escapeBtn.innerHTML = `
                <i class="fas fa-life-ring"></i>
                <span>I Need A Gentle Moment</span>
            `;
        }

        // Adjust prominence based on sensory sensitivity
        if (profile.traits.sensoryProcessing > 7) {
            escapeBtn.style.fontSize = '18px';
            escapeBtn.style.padding = '18px';
            escapeBtn.classList.add('high-priority-tool');
        }
    }

    arrangeToolsByPriority(sortedTools, profile) {
        const toolsContainer = document.querySelector('.quick-tools');
        if (!toolsContainer) return;

        // Reorder tools based on individual priorities
        const toolElements = {};
        sortedTools.forEach(toolName => {
            const element = this.getToolElement(toolName);
            if (element) {
                toolElements[toolName] = element;
                element.remove(); // Remove from current position
            }
        });

        // Add back in priority order
        sortedTools.forEach(toolName => {
            if (toolElements[toolName]) {
                toolsContainer.appendChild(toolElements[toolName]);
            }
        });

        // Customize individual tool appearance
        this.customizeIndividualTools(profile);
    }

    getToolElement(toolName) {
        const toolMap = {
            'focusTimer': 'break-timer',
            'fidgetTools': 'fidget-tool',
            'breathingTool': 'breathing-tool',
            'energyCheck': 'energy-check'
        };
        return document.getElementById(toolMap[toolName]);
    }

    customizeIndividualTools(profile) {
        // Customize focus timer for individual patterns
        const timerBtn = document.getElementById('break-timer');
        if (timerBtn && profile.focus.optimalSessionLength !== 25) {
            timerBtn.title = `⏰ Focus Session - Your optimal length: ${profile.focus.optimalSessionLength} minutes`;
        }

        // Customize fidget tools based on tactile preferences
        const fidgetBtn = document.getElementById('fidget-tool');
        if (fidgetBtn) {
            if (profile.sensory.tactileComfort === 'smooth') {
                fidgetBtn.title = '🤲 Smooth Fidget Tools - Stress ball and sand tray for your tactile comfort';
            } else if (profile.sensory.tactileComfort === 'textured') {
                fidgetBtn.title = '🤲 Textured Fidget Tools - Bubble wrap and varied textures for stimulation';
            }
        }

        // Customize breathing tool based on calming preferences
        const breathingBtn = document.getElementById('breathing-tool');
        if (breathingBtn && profile.sensory.calmingStrategies.includes('movement')) {
            breathingBtn.title = '🫁 Active Breathing - Movement-based breathing exercises for your style';
        }
    }

    addIndividualTools(profile) {
        // Add tools specific to individual needs
        const panelContent = document.getElementById('panel-content');
        if (!panelContent) return;

        // Add personal mantra button for individuals who benefit from affirmations
        if (profile.support.personalMantras.length > 0) {
            this.addPersonalMantraTool(panelContent, profile);
        }

        // Add quick sensory adjustment for light-sensitive individuals
        if (profile.sensory.lightSensitivity === 'high') {
            this.addLightingAdjustmentTool(panelContent);
        }

        // Add movement break tool for individuals needing active breaks
        if (profile.sensory.movementNeed === 'active') {
            this.addMovementBreakTool(panelContent);
        }

        // Add transition helper for those needing structure
        if (profile.traits.structurePreference > 7) {
            this.addTransitionHelperTool(panelContent);
        }
    }

    addPersonalMantraTool(container, profile) {
        const mantraBtn = document.createElement('button');
        mantraBtn.id = 'personal-mantra';
        mantraBtn.className = 'tool-btn personal-tool';
        mantraBtn.title = '💭 Your Personal Mantras - Affirmations that resonate with YOU';
        mantraBtn.innerHTML = '<i class="fas fa-heart"></i>';
        
        mantraBtn.addEventListener('click', () => {
            this.showPersonalMantras(profile.support.personalMantras);
        });

        // Add to tools container
        const toolsContainer = container.querySelector('.quick-tools');
        if (toolsContainer) {
            toolsContainer.appendChild(mantraBtn);
        }
    }

    addLightingAdjustmentTool(container) {
        const lightBtn = document.createElement('button');
        lightBtn.id = 'lighting-adjust';
        lightBtn.className = 'tool-btn personal-tool';
        lightBtn.title = '💡 Quick Light Adjustment - Reduce screen brightness and blue light';
        lightBtn.innerHTML = '<i class="fas fa-adjust"></i>';
        
        lightBtn.addEventListener('click', () => {
            this.adjustLightingForComfort();
        });

        const toolsContainer = container.querySelector('.quick-tools');
        if (toolsContainer) {
            toolsContainer.appendChild(lightBtn);
        }
    }

    addMovementBreakTool(container) {
        const moveBtn = document.createElement('button');
        moveBtn.id = 'movement-break';
        moveBtn.className = 'tool-btn personal-tool';
        moveBtn.title = '🏃 Movement Break - Quick exercises designed for your movement needs';
        moveBtn.innerHTML = '<i class="fas fa-running"></i>';
        
        moveBtn.addEventListener('click', () => {
            this.startPersonalizedMovementBreak();
        });

        const toolsContainer = container.querySelector('.quick-tools');
        if (toolsContainer) {
            toolsContainer.appendChild(moveBtn);
        }
    }

    addTransitionHelperTool(container) {
        const transitionBtn = document.createElement('button');
        transitionBtn.id = 'transition-helper';
        transitionBtn.className = 'tool-btn personal-tool';
        transitionBtn.title = '🔄 Transition Helper - Structured support for switching between activities';
        transitionBtn.innerHTML = '<i class="fas fa-route"></i>';
        
        transitionBtn.addEventListener('click', () => {
            this.showTransitionSupport();
        });

        const toolsContainer = container.querySelector('.quick-tools');
        if (toolsContainer) {
            toolsContainer.appendChild(transitionBtn);
        }
    }

    personalizeToolBehavior() {
        console.log('⚙️ Personalizing tool behavior...');
        
        // Customize focus sessions
        this.personalizeF FocusSessions();
        
        // Customize break recommendations
        this.personalizeBreakRecommendations();
        
        // Customize overwhelm response
        this.personalizeOverwhelmResponse();
        
        // Customize feedback style
        this.personalizeFeedbackStyle();
    }

    personalizeFocusSessions() {
        const profile = this.profile.profile;
        
        // Override default focus session behavior
        const originalStartSession = this.support.startFocusSession;
        this.support.startFocusSession = () => {
            this.support.showToolActivation('break-timer');
            setTimeout(() => {
                this.createPersonalizedFocusSession(profile);
            }, 300);
        };
    }

    createPersonalizedFocusSession(profile) {
        const sessionModal = document.createElement('div');
        sessionModal.id = 'focus-session-modal';
        sessionModal.style.cssText = `
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.8); z-index: 15000; display: flex;
            align-items: center; justify-content: center;
        `;
        
        // Create personalized session options
        const personalizedOptions = this.generatePersonalizedSessionOptions(profile);
        
        sessionModal.innerHTML = `
            <div style="background: white; border-radius: 20px; padding: 40px; text-align: center; max-width: 600px;">
                <h3><i class="fas fa-brain"></i> Your Personal Focus Session</h3>
                <p>Based on your individual focus patterns, here are your optimal session options:</p>
                
                <input type="text" id="focus-task" placeholder="What are you working on today?" 
                       style="width: 100%; padding: 15px; border: 2px solid #ddd; border-radius: 10px; 
                              font-size: 16px; margin: 20px 0;">
                
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin: 20px 0;">
                    ${personalizedOptions.map(option => `
                        <button onclick="adaptiveToolEcosystem.startPersonalSession(${option.duration}, '${option.type}')" 
                                style="padding: 20px; border: 2px solid #667eea; border-radius: 15px; 
                                       background: ${option.recommended ? '#667eea' : 'white'}; 
                                       color: ${option.recommended ? 'white' : '#667eea'}; 
                                       cursor: pointer; text-align: center;">
                            <div style="font-size: 1.2rem; font-weight: bold;">${option.duration} min</div>
                            <div style="font-size: 0.9rem; margin-top: 5px;">${option.name}</div>
                            <div style="font-size: 0.8rem; margin-top: 5px; opacity: 0.8;">${option.description}</div>
                            ${option.recommended ? '<div style="margin-top: 5px;">⭐ Perfect for you</div>' : ''}
                        </button>
                    `).join('')}
                </div>
                
                <div style="margin-top: 20px; padding: 15px; background: #f8f9fa; border-radius: 10px;">
                    <small style="color: #666;">
                        💡 These recommendations are based on your individual focus patterns and preferences.
                        ${profile.focus.naturalRhythm !== 'variable' ? 
                          `Best time for you: ${profile.focus.naturalRhythm}` : ''}
                    </small>
                </div>
                
                <button onclick="adaptiveToolEcosystem.closeFocusSession()" 
                        style="background: #6c757d; color: white; border: none; padding: 12px 20px; 
                               border-radius: 8px; cursor: pointer; margin-top: 20px;">
                    Cancel
                </button>
            </div>
        `;
        
        document.body.appendChild(sessionModal);
    }

    generatePersonalizedSessionOptions(profile) {
        const options = [];
        
        // Primary recommendation based on optimal session length
        const optimal = profile.focus.optimalSessionLength;
        options.push({
            duration: optimal,
            type: 'optimal',
            name: 'Your Sweet Spot',
            description: 'Perfect for your focus style',
            recommended: true
        });
        
        // Micro-session for flexibility-focused individuals
        if (profile.traits.attentionFlexibility > 6) {
            options.push({
                duration: Math.max(10, optimal - 10),
                type: 'micro',
                name: 'Quick Burst',
                description: 'For when you need flexibility',
                recommended: false
            });
        }
        
        // Extended session for hyperfocus individuals
        if (profile.traits.hyperfocusIntensity > 6) {
            options.push({
                duration: Math.min(90, optimal + 20),
                type: 'extended',
                name: 'Deep Dive',
                description: 'For deep, sustained work',
                recommended: false
            });
        }
        
        // Structured session for executive function support
        if (profile.traits.executiveFunction < 5) {
            options.push({
                duration: 25,
                type: 'structured',
                name: 'Structured Flow',
                description: 'With built-in check-ins',
                recommended: false
            });
        }
        
        return options;
    }

    startPersonalSession(duration, type) {
        const task = document.getElementById('focus-task')?.value || 'Focused work';
        const profile = this.profile.profile;
        
        // Track usage for learning
        this.recordToolUsage('focus_session', { duration, type, task });
        
        // Start session with personalized settings
        this.support.focusSession.currentTask = task;
        this.support.focusSession.startTime = Date.now();
        
        // Personalized notification
        let message = `Working on: ${task} for ${duration} minutes.`;
        if (type === 'optimal') {
            message += ' This is your perfect focus length! ✨';
        } else if (type === 'micro') {
            message += ' Perfect for your flexible mind! 🔄';
        } else if (type === 'extended') {
            message += ' Dive deep - I\'ll watch for burnout! 🧠';
        }
        
        this.support.showNotification(
            '🎯 Personal Focus Session Started!',
            message,
            'success'
        );
        
        this.closeFocusSession();
        this.support.logAchievement('sessionsCompleted', 'Started a personalized focus session');
        
        // Set personalized break reminder
        const breakTime = this.calculatePersonalizedBreakTime(duration, type, profile);
        setTimeout(() => {
            this.showPersonalizedBreakReminder(type, profile);
        }, breakTime * 60 * 1000);
    }

    calculatePersonalizedBreakTime(duration, type, profile) {
        // Adjust break timing based on individual characteristics
        if (profile.traits.hyperfocusIntensity > 7 && type === 'extended') {
            return Math.min(duration, profile.focus.hyperfocusWarningTime);
        }
        return duration;
    }

    closeFocusSession() {
        const modal = document.getElementById('focus-session-modal');
        if (modal) modal.remove();
    }

    // Tool behavior methods
    showPersonalMantras(mantras) {
        const randomMantra = mantras[Math.floor(Math.random() * mantras.length)];
        this.support.showNotification(
            '💭 Your Personal Truth',
            randomMantra,
            'success'
        );
        this.recordToolUsage('personal_mantra', { mantra: randomMantra });
    }

    adjustLightingForComfort() {
        // Apply blue light filter and reduce brightness
        document.body.style.filter = 'brightness(0.8) sepia(0.1)';
        this.support.showNotification(
            '💡 Lighting Adjusted',
            'Screen brightness reduced for your comfort',
            'success'
        );
        this.recordToolUsage('lighting_adjustment', {});
        
        // Auto-restore after 30 minutes
        setTimeout(() => {
            document.body.style.filter = '';
        }, 30 * 60 * 1000);
    }

    startPersonalizedMovementBreak() {
        const movements = [
            'gentle neck rolls',
            'shoulder shrugs',
            'ankle circles', 
            'deep breathing with arm raises',
            'seated spinal twists'
        ];
        
        const randomMovement = movements[Math.floor(Math.random() * movements.length)];
        
        this.support.showNotification(
            '🏃 Your Movement Break',
            `Try some ${randomMovement} - your body will thank you!`,
            'info'
        );
        this.recordToolUsage('movement_break', { type: randomMovement });
    }

    showTransitionSupport() {
        this.support.showNotification(
            '🔄 Transition Helper',
            'Take 3 deep breaths. What do you need to let go of from your last task? What mindset do you need for the next one?',
            'info'
        );
        this.recordToolUsage('transition_support', {});
    }

    // Pattern learning and adaptation
    recordToolUsage(tool, context) {
        const timestamp = Date.now();
        const usage = {
            tool,
            context,
            timestamp,
            timeOfDay: this.getCurrentTimeOfDay(),
            sessionDuration: this.currentContext.sessionDuration,
            energyLevel: this.currentContext.energyLevel
        };
        
        if (!this.learningData.usagePatterns[tool]) {
            this.learningData.usagePatterns[tool] = [];
        }
        this.learningData.usagePatterns[tool].push(usage);
        
        this.saveLearningData();
        console.log(`📊 Recorded usage: ${tool}`, context);
    }

    getCurrentTimeOfDay() {
        const hour = new Date().getHours();
        if (hour < 6) return 'night';
        if (hour < 12) return 'morning';
        if (hour < 18) return 'afternoon';
        return 'evening';
    }

    saveLearningData() {
        localStorage.setItem('adaptiveToolLearningData', JSON.stringify(this.learningData));
    }

    loadLearningData() {
        const saved = localStorage.getItem('adaptiveToolLearningData');
        if (saved) {
            this.learningData = { ...this.learningData, ...JSON.parse(saved) };
        }
    }

    async initializePatternRecognition() {
        try {
            if (typeof PersonalPatternRecognition !== 'undefined') {
                console.log('🔍 Initializing personal pattern recognition...');
                this.patternRecognition = new PersonalPatternRecognition(this.profile, this);
                await this.patternRecognition.initialize();
                
                // Make accessible globally
                window.personalPatternRecognition = this.patternRecognition;
                
                console.log('🧠 Your personal learning patterns are now being tracked!');
            }
        } catch (error) {
            console.log('📝 Pattern recognition will be available after more usage data');
        }
    }
}

// Global instance - will be initialized by neurodivergent support system
window.AdaptiveToolEcosystem = AdaptiveToolEcosystem;