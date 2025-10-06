// Individual Neurodivergent Profile Builder
// Creates a comprehensive, personalized learning companion

class IndividualNeurodivergentProfile {
    constructor() {
        this.userId = this.getCurrentUserId();
        this.apiBaseUrl = '/api/neurodivergent';
        this.profile = {
            // Core Identity
            preferredName: '',
            pronouns: '',
            
            // Multi-dimensional Traits (0-10 scales, not binary)
            traits: {
                hyperfocusIntensity: 5,        // How intense hyperfocus episodes are
                attentionFlexibility: 5,       // How easily attention shifts
                sensoryProcessing: 5,          // Sensitivity to sensory input
                executiveFunction: 5,          // Planning and organization strength
                socialBattery: 5,              // Energy for social interaction
                changeAdaptability: 5,         // Comfort with routine changes
                emotionalRegulation: 5,        // Managing emotional responses
                informationProcessing: 5,      // Speed/style of processing info
                creativityExpression: 5,       // How creativity manifests
                structurePreference: 5         // Need for predictable structure
            },
            
            // Personal Focus Characteristics
            focus: {
                optimalSessionLength: 25,      // Ideal focus time in minutes
                naturalRhythm: 'morning',      // 'morning', 'afternoon', 'evening', 'variable'
                hyperfocusWarningTime: 90,    // When to gently interrupt hyperfocus
                distractionTriggers: [],      // What commonly breaks focus
                focusEnhancers: [],           // What helps concentration
                breakPreferences: [],         // Preferred break activities
                transitionTime: 5,            // Time needed between tasks
                deepWorkCapacity: 2           // Hours of deep work per day
            },
            
            // Sensory Profile
            sensory: {
                auditoryPreference: 'moderate',   // 'silence', 'soft', 'moderate', 'stimulating'
                visualPreference: 'calm',         // 'minimal', 'calm', 'rich', 'dynamic'
                tactileComfort: 'smooth',         // 'smooth', 'textured', 'warm', 'cool'
                movementNeed: 'moderate',         // 'still', 'subtle', 'moderate', 'active'
                lightSensitivity: 'medium',       // 'low', 'medium', 'high'
                temperaturePreference: 'cool',    // 'cool', 'neutral', 'warm'
                overwhelmSigns: [],               // Early warning signs of overwhelm
                calmingStrategies: []             // What actually works to calm down
            },
            
            // Energy & Emotional Patterns
            energy: {
                dailyPattern: 'variable',         // 'consistent', 'variable', 'cyclical'
                energyTriggers: [],              // What drains energy
                energyRestorers: [],             // What restores energy
                burnoutWarnings: [],             // Early signs of burnout
                emotionalPatterns: {},           // Tracked emotional data
                stressManifestations: [],        // How stress shows up
                recoveryStrategies: []           // Personal recovery methods
            },
            
            // Learning Style & Preferences
            learning: {
                primaryStyle: 'mixed',           // 'visual', 'auditory', 'kinesthetic', 'mixed'
                informationChunking: 'small',    // 'small', 'medium', 'large'
                feedbackPreference: 'gentle',    // 'immediate', 'gentle', 'detailed', 'minimal'
                mistakeHandling: 'supportive',   // 'perfectionist', 'supportive', 'experimental'
                motivationStyle: 'internal',     // 'internal', 'achievement', 'social', 'creative'
                challengeLevel: 'adaptive',      // 'consistent', 'adaptive', 'pushing'
                learningEnvironment: 'quiet',    // 'social', 'quiet', 'dynamic', 'flexible'
                timePreference: 'flexible'       // 'rigid', 'flexible', 'spontaneous'
            },
            
            // Personal Support System
            support: {
                personalMantras: [],             // Individual affirmations that resonate
                comfortItems: [],               // Things that provide comfort
                supportPeople: [],              // Who to reach out to
                coping mechanisms: [],          // Proven personal strategies
                celebrationStyle: 'quiet',     // 'quiet', 'moderate', 'enthusiastic'
                communicationStyle: 'direct',  // 'direct', 'gentle', 'detailed'
                autonomyLevel: 'high',          // 'guided', 'moderate', 'high'
                privacyNeeds: 'respected'       // 'minimal', 'respected', 'maximum'
            },
            
            // Individual Goals & Growth
            goals: {
                learningObjectives: [],         // What they want to achieve
                challengeAreas: [],            // Areas needing support
                strengthAreas: [],             // Natural abilities to leverage
                growthMetrics: {},             // Personal progress indicators
                milestonePreferences: [],      // How they like to track progress
                adaptationSpeed: 'gradual'     // 'immediate', 'gradual', 'experimental'
            },
            
            // Usage Patterns (learned over time)
            patterns: {
                toolUsageFrequency: {},        // Which tools used most
                timeBasedPreferences: {},      // When different tools are used
                activityContexts: {},          // Tools used for different activities
                effectivenessRatings: {},      // User feedback on tool effectiveness
                adaptationHistory: [],         // How preferences change over time
                personalInsights: []           // AI-discovered patterns
            }
        };
        
        this.lastUpdated = Date.now();
        this.version = '1.0';
    }
    
    // Initialize from assessment results
    async initializeFromAssessment() {
        const assessmentData = this.getAssessmentResults();
        if (!assessmentData) return;
        
        console.log('🧬 Building your unique neurodivergent profile...');
        
        // Try to update profile via backend first
        const backendSuccess = await this.updateFromAssessment(assessmentData);
        
        if (!backendSuccess) {
            // Fallback to local processing
            console.log('🔄 Using local profile building...');
            this.profile = await this.buildIndividualProfile(assessmentData);
            await this.save(); // Save locally built profile to backend
        }
        
        // Create personalized tool ecosystem
        this.createPersonalizedTools();
        
        // Start pattern learning
        this.beginPatternLearning();
        
        console.log('✨ Your personal learning companion is ready!');
    }
    
    buildIndividualProfile(assessmentData) {
        // This goes far beyond basic categorization
        // Analyzes nuanced responses to build multi-dimensional profile
        
        const answers = assessmentData.answers || {};
        const profile = { ...this.profile };
        
        // Analyze attention patterns
        this.analyzeAttentionCharacteristics(answers, profile);
        
        // Understand sensory preferences
        this.buildSensoryProfile(answers, profile);
        
        // Discover individual focus patterns
        this.mapFocusCharacteristics(answers, profile);
        
        // Identify personal triggers and strengths
        this.identifyPersonalFactors(answers, profile);
        
        // Create individual support strategies
        this.designSupportStrategies(answers, profile);
        
        return profile;
    }
    
    analyzeAttentionCharacteristics(answers, profile) {
        // Question: "When you're really interested in something, you..."
        if (answers['attention_interest'] === 'completely_absorbed') {
            profile.traits.hyperfocusIntensity = 9;
            profile.focus.hyperfocusWarningTime = 60; // Shorter warning time
            profile.focus.optimalSessionLength = 45;  // Longer sessions
        } else if (answers['attention_interest'] === 'jump_around') {
            profile.traits.attentionFlexibility = 8;
            profile.focus.optimalSessionLength = 15;  // Shorter bursts
            profile.focus.transitionTime = 2;        // Quick transitions
        }
        
        // Question: "Background noise while studying..."
        if (answers['background_noise'] === 'helps_focus') {
            profile.sensory.auditoryPreference = 'stimulating';
            profile.learning.learningEnvironment = 'dynamic';
        } else if (answers['background_noise'] === 'very_distracting') {
            profile.sensory.auditoryPreference = 'silence';
            profile.learning.learningEnvironment = 'quiet';
        }
        
        // Question: "You prefer to learn by..."
        if (answers['learning_preference'] === 'doing_hands_on') {
            profile.learning.primaryStyle = 'kinesthetic';
            profile.sensory.movementNeed = 'active';
        } else if (answers['learning_preference'] === 'reading_detailed') {
            profile.learning.primaryStyle = 'visual';
            profile.learning.informationChunking = 'large';
        }
    }
    
    buildSensoryProfile(answers, profile) {
        // Question: "Bright lights make you feel..."
        if (answers['bright_lights'] === 'uncomfortable_overwhelming') {
            profile.sensory.lightSensitivity = 'high';
            profile.sensory.overwhelmSigns.push('light_discomfort');
            profile.sensory.calmingStrategies.push('dim_lighting');
        }
        
        // Question: "When overwhelmed, you need to..."
        if (answers['overwhelm_response'] === 'quiet_alone_space') {
            profile.sensory.calmingStrategies.push('solitude', 'quiet_space');
            profile.support.communicationStyle = 'gentle';
        } else if (answers['overwhelm_response'] === 'physical_movement') {
            profile.sensory.calmingStrategies.push('movement', 'exercise');
            profile.sensory.movementNeed = 'active';
        }
        
        // Question: "Fidgeting or moving while thinking..."
        if (answers['fidgeting'] === 'absolutely_helps') {
            profile.sensory.movementNeed = 'active';
            profile.sensory.tactileComfort = 'textured';
            profile.focus.focusEnhancers.push('fidget_tools', 'movement');
        }
    }
    
    mapFocusCharacteristics(answers, profile) {
        // Question: "Your ideal study environment is..."
        if (answers['study_environment'] === 'completely_organized') {
            profile.traits.structurePreference = 9;
            profile.learning.timePreference = 'rigid';
        } else if (answers['study_environment'] === 'comfortable_flexible') {
            profile.traits.structurePreference = 3;
            profile.learning.timePreference = 'flexible';
        }
        
        // Question: "When starting a big project, you..."
        if (answers['project_approach'] === 'break_down_detailed') {
            profile.traits.executiveFunction = 8;
            profile.learning.informationChunking = 'small';
            profile.focus.transitionTime = 10; // Needs more transition time
        } else if (answers['project_approach'] === 'jump_in_figure_out') {
            profile.traits.creativityExpression = 8;
            profile.learning.challengeLevel = 'pushing';
        }
        
        // Question: "You work best..."
        if (answers['work_timing'] === 'early_morning') {
            profile.focus.naturalRhythm = 'morning';
        } else if (answers['work_timing'] === 'late_evening') {
            profile.focus.naturalRhythm = 'evening';
        }
    }
    
    identifyPersonalFactors(answers, profile) {
        // Question: "Social interactions during learning..."
        if (answers['social_learning'] === 'draining_prefer_alone') {
            profile.traits.socialBattery = 3;
            profile.energy.energyTriggers.push('social_interaction');
            profile.learning.learningEnvironment = 'quiet';
        }
        
        // Question: "Unexpected changes in routine..."
        if (answers['routine_changes'] === 'very_stressful') {
            profile.traits.changeAdaptability = 2;
            profile.energy.stressManifestations.push('routine_disruption');
            profile.support.communicationStyle = 'detailed'; // Needs advance notice
        }
        
        // Question: "Making mistakes while learning..."
        if (answers['mistake_response'] === 'very_upset_frustrated') {
            profile.learning.mistakeHandling = 'perfectionist';
            profile.learning.feedbackPreference = 'gentle';
            profile.support.celebrationStyle = 'quiet'; // Less pressure
        }
        
        // Question: "You feel most creative when..."
        if (answers['creativity_context'] === 'under_pressure') {
            profile.learning.challengeLevel = 'pushing';
            profile.traits.creativityExpression = 8;
        } else if (answers['creativity_context'] === 'relaxed_no_pressure') {
            profile.learning.challengeLevel = 'adaptive';
            profile.support.autonomyLevel = 'high';
        }
    }
    
    designSupportStrategies(answers, profile) {
        // Create personalized mantras based on profile
        if (profile.traits.hyperfocusIntensity > 7) {
            profile.support.personalMantras.push(
                "My intense focus is a superpower - I just need to remember to take breaks",
                "It's okay to pause my deep work to take care of myself"
            );
        }
        
        if (profile.traits.attentionFlexibility > 7) {
            profile.support.personalMantras.push(
                "My curious mind explores many paths - that's how I learn best",
                "Following my interest is not a distraction, it's my learning style"
            );
        }
        
        if (profile.sensory.lightSensitivity === 'high') {
            profile.support.comfortItems.push('soft_lighting', 'blue_light_filter');
            profile.sensory.calmingStrategies.push('adjust_lighting');
        }
        
        if (profile.learning.mistakeHandling === 'perfectionist') {
            profile.support.personalMantras.push(
                "Mistakes are information, not failures",
                "My worth isn't measured by perfect performance"
            );
            profile.learning.feedbackPreference = 'gentle';
        }
        
        // Create individual coping mechanisms
        profile.support.copingMechanisms = this.generatePersonalCopingStrategies(profile);
    }
    
    generatePersonalCopingStrategies(profile) {
        const strategies = [];
        
        // Based on sensory preferences
        if (profile.sensory.movementNeed === 'active') {
            strategies.push('micro_movement_breaks', 'standing_desk_option', 'fidget_tools');
        }
        
        if (profile.sensory.auditoryPreference === 'stimulating') {
            strategies.push('background_music', 'nature_sounds', 'binaural_beats');
        }
        
        // Based on focus characteristics
        if (profile.focus.optimalSessionLength < 20) {
            strategies.push('pomodoro_technique', 'micro_breaks', 'task_switching');
        } else {
            strategies.push('deep_work_blocks', 'hyperfocus_management', 'transition_rituals');
        }
        
        // Based on emotional regulation
        if (profile.traits.emotionalRegulation < 5) {
            strategies.push('breathing_exercises', 'emotion_check_ins', 'self_compassion_practice');
        }
        
        return strategies;
    }
    
    getAssessmentResults() {
        return JSON.parse(localStorage.getItem('assessmentResults') || 'null');
    }
    
    // Get current user ID (replace with actual authentication)
    getCurrentUserId() {
        // TODO: Integrate with actual authentication system
        return localStorage.getItem('currentUserId') || '1'; // Default to user 1 for demo
    }
    
    // API Methods
    async save() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/profile/${this.userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(this.transformToBackendFormat())
            });
            
            if (response.ok) {
                const savedProfile = await response.json();
                console.log('💾 Individual profile saved to backend');
                return savedProfile;
            } else {
                console.error('Failed to save profile:', response.statusText);
                // Fallback to localStorage
                this.saveToLocalStorage();
            }
        } catch (error) {
            console.error('Error saving profile:', error);
            // Fallback to localStorage
            this.saveToLocalStorage();
        }
    }
    
    async load() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/profile/${this.userId}`);
            
            if (response.ok) {
                const backendProfile = await response.json();
                this.transformFromBackendFormat(backendProfile);
                console.log('📂 Individual profile loaded from backend');
                return true;
            } else {
                console.warn('Profile not found in backend, trying localStorage');
                return this.loadFromLocalStorage();
            }
        } catch (error) {
            console.error('Error loading profile from backend:', error);
            return this.loadFromLocalStorage();
        }
    }
    
    // Transform frontend format to backend format
    transformToBackendFormat() {
        return {
            preferredName: this.profile.preferredName,
            pronouns: this.profile.pronouns,
            
            // Map traits to backend field names
            hyperfocusIntensity: this.profile.traits.hyperfocusIntensity,
            attentionFlexibility: this.profile.traits.attentionFlexibility,
            sensoryProcessing: this.profile.traits.sensoryProcessing,
            executiveFunction: this.profile.traits.executiveFunction,
            socialBattery: this.profile.traits.socialBattery,
            changeAdaptability: this.profile.traits.changeAdaptability,
            emotionalRegulation: this.profile.traits.emotionalRegulation,
            informationProcessing: this.profile.traits.informationProcessing,
            creativityExpression: this.profile.traits.creativityExpression,
            structurePreference: this.profile.traits.structurePreference,
            
            // Focus characteristics
            optimalSessionLength: this.profile.focus.optimalSessionLength,
            naturalRhythm: this.profile.focus.naturalRhythm,
            hyperfocusWarningTime: this.profile.focus.hyperfocusWarningTime,
            transitionTime: this.profile.focus.transitionTime,
            deepWorkCapacity: this.profile.focus.deepWorkCapacity,
            
            // Sensory preferences
            auditoryPreference: this.profile.sensory.auditoryPreference,
            visualPreference: this.profile.sensory.visualPreference,
            tactileComfort: this.profile.sensory.tactileComfort,
            movementNeed: this.profile.sensory.movementNeed,
            lightSensitivity: this.profile.sensory.lightSensitivity,
            temperaturePreference: this.profile.sensory.temperaturePreference,
            
            // Learning preferences
            primaryLearningStyle: this.profile.learning.primaryStyle,
            informationChunking: this.profile.learning.informationChunking,
            feedbackPreference: this.profile.learning.feedbackPreference,
            mistakeHandling: this.profile.learning.mistakeHandling,
            motivationStyle: this.profile.learning.motivationStyle,
            challengeLevel: this.profile.learning.challengeLevel,
            learningEnvironment: this.profile.learning.learningEnvironment,
            timePreference: this.profile.learning.timePreference,
            
            // Support preferences
            celebrationStyle: this.profile.support.celebrationStyle,
            communicationStyle: this.profile.support.communicationStyle,
            autonomyLevel: this.profile.support.autonomyLevel,
            privacyNeeds: this.profile.support.privacyNeeds,
            
            // Goals and growth
            adaptationSpeed: this.profile.goals.adaptationSpeed
        };
    }
    
    // Transform backend format to frontend format
    transformFromBackendFormat(backendProfile) {
        this.profile.preferredName = backendProfile.preferredName || '';
        this.profile.pronouns = backendProfile.pronouns || '';
        
        // Map backend fields to frontend structure
        this.profile.traits = {
            hyperfocusIntensity: backendProfile.hyperfocusIntensity || 5,
            attentionFlexibility: backendProfile.attentionFlexibility || 5,
            sensoryProcessing: backendProfile.sensoryProcessing || 5,
            executiveFunction: backendProfile.executiveFunction || 5,
            socialBattery: backendProfile.socialBattery || 5,
            changeAdaptability: backendProfile.changeAdaptability || 5,
            emotionalRegulation: backendProfile.emotionalRegulation || 5,
            informationProcessing: backendProfile.informationProcessing || 5,
            creativityExpression: backendProfile.creativityExpression || 5,
            structurePreference: backendProfile.structurePreference || 5
        };
        
        this.profile.focus = {
            optimalSessionLength: backendProfile.optimalSessionLength || 25,
            naturalRhythm: backendProfile.naturalRhythm || 'morning',
            hyperfocusWarningTime: backendProfile.hyperfocusWarningTime || 90,
            transitionTime: backendProfile.transitionTime || 5,
            deepWorkCapacity: backendProfile.deepWorkCapacity || 2.0,
            // Keep existing arrays from current profile
            distractionTriggers: this.profile.focus.distractionTriggers || [],
            focusEnhancers: this.profile.focus.focusEnhancers || [],
            breakPreferences: this.profile.focus.breakPreferences || []
        };
        
        this.profile.sensory = {
            auditoryPreference: backendProfile.auditoryPreference || 'moderate',
            visualPreference: backendProfile.visualPreference || 'calm',
            tactileComfort: backendProfile.tactileComfort || 'smooth',
            movementNeed: backendProfile.movementNeed || 'moderate',
            lightSensitivity: backendProfile.lightSensitivity || 'medium',
            temperaturePreference: backendProfile.temperaturePreference || 'cool',
            // Keep existing arrays
            overwhelmSigns: this.profile.sensory.overwhelmSigns || [],
            calmingStrategies: this.profile.sensory.calmingStrategies || []
        };
        
        this.profile.learning = {
            primaryStyle: backendProfile.primaryLearningStyle || 'mixed',
            informationChunking: backendProfile.informationChunking || 'small',
            feedbackPreference: backendProfile.feedbackPreference || 'gentle',
            mistakeHandling: backendProfile.mistakeHandling || 'supportive',
            motivationStyle: backendProfile.motivationStyle || 'internal',
            challengeLevel: backendProfile.challengeLevel || 'adaptive',
            learningEnvironment: backendProfile.learningEnvironment || 'quiet',
            timePreference: backendProfile.timePreference || 'flexible'
        };
        
        this.profile.support = {
            celebrationStyle: backendProfile.celebrationStyle || 'quiet',
            communicationStyle: backendProfile.communicationStyle || 'direct',
            autonomyLevel: backendProfile.autonomyLevel || 'high',
            privacyNeeds: backendProfile.privacyNeeds || 'respected',
            // Keep existing arrays
            personalMantras: this.profile.support.personalMantras || [],
            comfortItems: this.profile.support.comfortItems || [],
            supportPeople: this.profile.support.supportPeople || [],
            copingMechanisms: this.profile.support.copingMechanisms || []
        };
        
        this.profile.goals = {
            adaptationSpeed: backendProfile.adaptationSpeed || 'gradual',
            // Keep existing arrays
            learningObjectives: this.profile.goals.learningObjectives || [],
            challengeAreas: this.profile.goals.challengeAreas || [],
            strengthAreas: this.profile.goals.strengthAreas || [],
            growthMetrics: this.profile.goals.growthMetrics || {},
            milestonePreferences: this.profile.goals.milestonePreferences || []
        };
        
        this.lastUpdated = backendProfile.updatedAt ? new Date(backendProfile.updatedAt).getTime() : Date.now();
        this.version = backendProfile.version || '1.0';
    }
    
    // Fallback methods for localStorage
    saveToLocalStorage() {
        localStorage.setItem('individualNeurodivergentProfile', JSON.stringify({
            profile: this.profile,
            lastUpdated: this.lastUpdated,
            version: this.version
        }));
        console.log('💾 Individual profile saved to localStorage (fallback)');
    }
    
    loadFromLocalStorage() {
        const saved = localStorage.getItem('individualNeurodivergentProfile');
        if (saved) {
            const data = JSON.parse(saved);
            this.profile = data.profile;
            this.lastUpdated = data.lastUpdated;
            this.version = data.version;
            console.log('📂 Individual profile loaded from localStorage');
            return true;
        }
        return false;
    }
    
    // Update profile from assessment results
    async updateFromAssessment(assessmentData) {
        try {
            const response = await fetch(`${this.apiBaseUrl}/profile/${this.userId}/from-assessment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(assessmentData)
            });
            
            if (response.ok) {
                const updatedProfile = await response.json();
                this.transformFromBackendFormat(updatedProfile);
                console.log('✨ Profile updated from assessment via backend');
                return true;
            } else {
                console.warn('Failed to update from assessment via backend, using local processing');
                return false;
            }
        } catch (error) {
            console.error('Error updating from assessment:', error);
            return false;
        }
    }
    
    // Get personalized recommendations
    async getRecommendations() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/profile/${this.userId}/recommendations`);
            
            if (response.ok) {
                const recommendations = await response.json();
                console.log('🎯 Personalized recommendations loaded');
                return recommendations;
            } else {
                console.warn('Failed to load recommendations from backend');
                return null;
            }
        } catch (error) {
            console.error('Error loading recommendations:', error);
            return null;
        }
    }
    
    // Get tool priorities
    async getToolPriorities() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/profile/${this.userId}/tool-priorities`);
            
            if (response.ok) {
                const priorities = await response.json();
                console.log('🛠️ Tool priorities loaded');
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
    
    // Record tool usage
    async recordToolUsage(toolName, context, duration, successRating, energyLevel) {
        try {
            const usageData = {
                toolName: toolName,
                toolContext: context,
                sessionDurationMinutes: duration,
                successRating: successRating,
                userEnergyLevel: energyLevel,
                activityContext: 'learning'
            };
            
            const response = await fetch(`${this.apiBaseUrl}/usage/${this.userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(usageData)
            });
            
            if (response.ok) {
                console.log('📊 Tool usage recorded');
                return true;
            } else {
                console.warn('Failed to record tool usage');
                return false;
            }
        } catch (error) {
            console.error('Error recording tool usage:', error);
            return false;
        }
    }
}

// Global instance
window.individualProfile = new IndividualNeurodivergentProfile();