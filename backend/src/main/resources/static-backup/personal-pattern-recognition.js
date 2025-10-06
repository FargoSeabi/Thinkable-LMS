// Personal Pattern Recognition System
// Learns individual neurodivergent patterns and adapts support accordingly

class PersonalPatternRecognition {
    constructor(individualProfile, adaptiveEcosystem) {
        this.profile = individualProfile;
        this.ecosystem = adaptiveEcosystem;
        this.patterns = {
            focusPatterns: {},
            energyPatterns: {},
            toolEffectiveness: {},
            contextualPreferences: {},
            timeBasedInsights: {},
            stressIndicators: {},
            flowStateConditions: {},
            personalGrowth: {}
        };
        this.observations = [];
        this.insights = [];
        this.adaptations = [];
    }

    async initialize() {
        console.log('🧠 Initializing Personal Pattern Recognition...');
        
        this.loadExistingPatterns();
        this.startContinuousLearning();
        this.setupPatternDetection();
        
        console.log('🔍 Your personal learning patterns are being analyzed...');
    }

    // FOCUS PATTERN RECOGNITION
    analyzeFocusPatterns() {
        const profile = this.profile.profile;
        const recentUsage = this.getRecentUsageData(7); // Last 7 days
        
        // Discover optimal focus times
        const optimalTimes = this.findOptimalFocusTimes(recentUsage);
        if (optimalTimes.length > 0 && optimalTimes[0] !== profile.focus.naturalRhythm) {
            this.recordInsight('focus_timing', {
                discovered: optimalTimes[0],
                current: profile.focus.naturalRhythm,
                confidence: this.calculateConfidence(optimalTimes),
                suggestion: `You seem to focus best during ${optimalTimes[0]} - would you like to update your profile?`
            });
        }

        // Detect hyperfocus patterns
        this.detectHyperfocusPatterns(recentUsage);
        
        // Analyze attention flexibility changes
        this.analyzeAttentionFlexibility(recentUsage);
    }

    findOptimalFocusTimes(usageData) {
        const timeSlots = {
            morning: 0,
            afternoon: 0, 
            evening: 0,
            night: 0
        };

        // Count successful focus sessions by time
        usageData.filter(d => d.tool === 'focus_session' && d.context.success)
               .forEach(session => {
                   const hour = new Date(session.timestamp).getHours();
                   if (hour < 6) timeSlots.night++;
                   else if (hour < 12) timeSlots.morning++;
                   else if (hour < 18) timeSlots.afternoon++;
                   else timeSlots.evening++;
               });

        return Object.entries(timeSlots)
                    .sort(([,a], [,b]) => b - a)
                    .map(([time]) => time);
    }

    detectHyperfocusPatterns(usageData) {
        const longSessions = usageData.filter(d => 
            d.tool === 'focus_session' && 
            d.context.duration > this.profile.profile.focus.hyperfocusWarningTime
        );

        if (longSessions.length > 3) {
            const avgDuration = longSessions.reduce((sum, s) => sum + s.context.duration, 0) / longSessions.length;
            
            if (avgDuration > this.profile.profile.focus.hyperfocusWarningTime * 1.5) {
                this.recordInsight('hyperfocus_intensity', {
                    current: this.profile.profile.traits.hyperfocusIntensity,
                    observed: Math.min(10, Math.ceil(avgDuration / 15)),
                    suggestion: "Your hyperfocus sessions are longer than expected. Should we adjust your break reminders?"
                });
            }
        }
    }

    analyzeAttentionFlexibility(usageData) {
        const shortSessions = usageData.filter(d => 
            d.tool === 'focus_session' && 
            d.context.duration < this.profile.profile.focus.optimalSessionLength * 0.7
        );

        if (shortSessions.length > longSessions.length) {
            this.recordInsight('attention_flexibility', {
                current: this.profile.profile.traits.attentionFlexibility,
                observed: 8,
                suggestion: "You seem to prefer shorter, more flexible sessions. Let's adapt your tools!"
            });
        }
    }

    // ENERGY PATTERN RECOGNITION
    analyzeEnergyPatterns() {
        const energyChecks = this.getRecentUsageData(14)
                                .filter(d => d.tool === 'energy_check');
        
        if (energyChecks.length < 10) return; // Need more data

        // Find daily energy patterns
        const dailyPatterns = this.analyzeDailyEnergyRhythms(energyChecks);
        
        // Detect energy drains and boosters
        const energyFactors = this.identifyEnergyFactors(energyChecks);
        
        // Predict burnout risk
        const burnoutRisk = this.assessBurnoutRisk(energyChecks);

        if (Object.keys(dailyPatterns).length > 0) {
            this.recordInsight('energy_rhythm', {
                patterns: dailyPatterns,
                suggestion: this.generateEnergyRecommendations(dailyPatterns)
            });
        }
    }

    analyzeDailyEnergyRhythms(energyChecks) {
        const hourlyEnergy = {};
        
        energyChecks.forEach(check => {
            const hour = new Date(check.timestamp).getHours();
            if (!hourlyEnergy[hour]) hourlyEnergy[hour] = [];
            hourlyEnergy[hour].push(check.context.energyLevel);
        });

        // Find patterns
        const patterns = {};
        Object.entries(hourlyEnergy).forEach(([hour, levels]) => {
            const avg = levels.reduce((sum, l) => sum + l, 0) / levels.length;
            if (levels.length >= 3) {
                patterns[hour] = {
                    average: Math.round(avg),
                    consistency: this.calculateVariance(levels),
                    samples: levels.length
                };
            }
        });

        return patterns;
    }

    // TOOL EFFECTIVENESS LEARNING
    learnToolEffectiveness() {
        const toolUsage = this.getRecentUsageData(30);
        const effectiveness = {};

        // Group by tool and context
        toolUsage.forEach(usage => {
            const key = `${usage.tool}_${usage.context.type || 'general'}`;
            if (!effectiveness[key]) {
                effectiveness[key] = {
                    uses: 0,
                    successRate: 0,
                    userSatisfaction: 0,
                    contexts: []
                };
            }
            effectiveness[key].uses++;
            effectiveness[key].contexts.push(usage.context);
        });

        // Identify highly effective tools
        Object.entries(effectiveness).forEach(([tool, data]) => {
            if (data.uses >= 5) {
                const personalizedInsight = this.generateToolInsight(tool, data);
                if (personalizedInsight) {
                    this.recordInsight('tool_effectiveness', personalizedInsight);
                }
            }
        });
    }

    generateToolInsight(tool, data) {
        const [toolName, context] = tool.split('_');
        
        if (data.uses >= 10 && !this.profile.profile.support.personalizedStrategies?.includes(toolName)) {
            return {
                tool: toolName,
                context: context,
                usage: data.uses,
                suggestion: `You use ${toolName} frequently and it seems to help! Would you like me to make this more prominent in your toolkit?`
            };
        }

        return null;
    }

    // CONTEXTUAL LEARNING
    learnContextualPreferences() {
        const usage = this.getRecentUsageData(21);
        const contexts = {};

        // Analyze tool preferences by activity context
        usage.forEach(u => {
            const activity = this.detectCurrentActivity();
            if (!contexts[activity]) contexts[activity] = {};
            if (!contexts[activity][u.tool]) contexts[activity][u.tool] = 0;
            contexts[activity][u.tool]++;
        });

        // Find strong preferences
        Object.entries(contexts).forEach(([activity, tools]) => {
            const totalUses = Object.values(tools).reduce((sum, count) => sum + count, 0);
            const preferences = Object.entries(tools)
                                     .map(([tool, count]) => ({ tool, percentage: count / totalUses }))
                                     .filter(p => p.percentage > 0.4);

            if (preferences.length > 0) {
                this.recordInsight('contextual_preference', {
                    activity,
                    preferences,
                    suggestion: `When ${activity}, you prefer ${preferences[0].tool}. Should I prioritize this tool during ${activity}?`
                });
            }
        });
    }

    // STRESS AND OVERWHELM PATTERN DETECTION
    detectStressPatterns() {
        const escapeHatchUsage = this.getRecentUsageData(30)
                                    .filter(d => d.tool === 'overwhelm_escape');
        
        if (escapeHatchUsage.length < 3) return;

        // Find stress triggers
        const stressTriggers = this.identifyStressTriggers(escapeHatchUsage);
        
        // Find early warning signs
        const warningSignsPatterns = this.detectWarningSignPatterns(escapeHatchUsage);

        if (stressTriggers.length > 0) {
            this.recordInsight('stress_patterns', {
                triggers: stressTriggers,
                warningSignsPatterns,
                suggestion: "I've noticed some patterns in when you need calm mode. Would you like me to help you recognize these earlier?"
            });
        }
    }

    identifyStressTriggers(escapeUsage) {
        return escapeUsage.map(usage => {
            return {
                timeOfDay: this.getTimeOfDay(usage.timestamp),
                dayOfWeek: new Date(usage.timestamp).getDay(),
                sessionDuration: usage.context.sessionDuration || 0,
                recentActivity: usage.context.recentActivity
            };
        });
    }

    // FLOW STATE RECOGNITION
    detectFlowStateConditions() {
        const focusSessions = this.getRecentUsageData(30)
                                 .filter(d => d.tool === 'focus_session' && d.context.completed);
        
        const flowSessions = focusSessions.filter(s => 
            s.context.duration >= this.profile.profile.focus.optimalSessionLength * 1.2 &&
            s.context.breaks === 0
        );

        if (flowSessions.length >= 3) {
            const flowConditions = this.analyzeFlowConditions(flowSessions);
            this.recordInsight('flow_state', {
                conditions: flowConditions,
                frequency: flowSessions.length,
                suggestion: "I've identified conditions that help you enter flow state! Would you like me to help recreate these?"
            });
        }
    }

    analyzeFlowConditions(flowSessions) {
        const conditions = {
            timeOfDay: {},
            dayOfWeek: {},
            taskType: {},
            environmentFactors: {}
        };

        flowSessions.forEach(session => {
            const hour = new Date(session.timestamp).getHours();
            const timeSlot = this.getTimeSlot(hour);
            conditions.timeOfDay[timeSlot] = (conditions.timeOfDay[timeSlot] || 0) + 1;
            
            const day = new Date(session.timestamp).getDay();
            conditions.dayOfWeek[day] = (conditions.dayOfWeek[day] || 0) + 1;
            
            if (session.context.taskType) {
                conditions.taskType[session.context.taskType] = (conditions.taskType[session.context.taskType] || 0) + 1;
            }
        });

        return conditions;
    }

    // PERSONAL GROWTH TRACKING
    trackPersonalGrowth() {
        const thirtyDaysAgo = Date.now() - (30 * 24 * 60 * 60 * 1000);
        const recentData = this.observations.filter(o => o.timestamp > thirtyDaysAgo);
        
        if (recentData.length < 20) return;

        // Track improvements in self-awareness
        const selfAwarenessGrowth = this.measureSelfAwarenessGrowth(recentData);
        
        // Track coping strategy development
        const copingStrategiesGrowth = this.measureCopingStrategiesGrowth(recentData);
        
        // Track focus improvements
        const focusGrowth = this.measureFocusGrowth(recentData);

        const growthInsights = {
            selfAwareness: selfAwarenessGrowth,
            copingStrategies: copingStrategiesGrowth,
            focus: focusGrowth,
            overallProgress: this.calculateOverallGrowth([selfAwarenessGrowth, copingStrategiesGrowth, focusGrowth])
        };

        this.recordInsight('personal_growth', growthInsights);
    }

    measureSelfAwarenessGrowth(data) {
        const energyChecks = data.filter(d => d.tool === 'energy_check');
        const earlyChecks = energyChecks.slice(0, Math.floor(energyChecks.length / 2));
        const recentChecks = energyChecks.slice(Math.floor(energyChecks.length / 2));
        
        const earlyFrequency = earlyChecks.length;
        const recentFrequency = recentChecks.length;
        
        return {
            improvement: recentFrequency > earlyFrequency,
            metric: 'energy_check_frequency',
            change: recentFrequency - earlyFrequency,
            insight: recentFrequency > earlyFrequency ? 
                    'You\'re becoming more self-aware and checking in with yourself more often!' :
                    'You might benefit from more regular self-check-ins.'
        };
    }

    // ADAPTIVE RECOMMENDATIONS
    generateAdaptiveRecommendations() {
        const recommendations = [];
        
        // Analyze recent insights
        const recentInsights = this.insights.filter(i => 
            Date.now() - i.timestamp < (7 * 24 * 60 * 60 * 1000)
        );

        recentInsights.forEach(insight => {
            const recommendation = this.createRecommendationFromInsight(insight);
            if (recommendation) {
                recommendations.push(recommendation);
            }
        });

        return recommendations;
    }

    createRecommendationFromInsight(insight) {
        switch (insight.type) {
            case 'focus_timing':
                return {
                    type: 'schedule_optimization',
                    title: 'Optimize Your Focus Schedule',
                    description: insight.data.suggestion,
                    action: () => this.suggestScheduleOptimization(insight.data),
                    priority: insight.data.confidence > 0.7 ? 'high' : 'medium'
                };
                
            case 'tool_effectiveness':
                return {
                    type: 'tool_customization',
                    title: 'Customize Your Toolkit',
                    description: insight.data.suggestion,
                    action: () => this.suggestToolCustomization(insight.data),
                    priority: 'medium'
                };
                
            case 'flow_state':
                return {
                    type: 'flow_optimization',
                    title: 'Enhance Your Flow State',
                    description: insight.data.suggestion,
                    action: () => this.suggestFlowOptimization(insight.data),
                    priority: 'high'
                };
                
            default:
                return null;
        }
    }

    // UTILITY METHODS
    recordInsight(type, data) {
        const insight = {
            id: Date.now() + Math.random(),
            type,
            data,
            timestamp: Date.now(),
            confidence: this.calculateInsightConfidence(type, data),
            actionTaken: false
        };
        
        this.insights.push(insight);
        this.savePatterns();
        
        console.log(`🔍 New insight discovered: ${type}`, data);
        
        // Show insight to user if confidence is high
        if (insight.confidence > 0.8) {
            this.presentInsightToUser(insight);
        }
    }

    presentInsightToUser(insight) {
        setTimeout(() => {
            const recommendations = this.generateAdaptiveRecommendations();
            if (recommendations.length > 0) {
                this.showPersonalizedRecommendation(recommendations[0]);
            }
        }, 2000);
    }

    showPersonalizedRecommendation(recommendation) {
        // Create a gentle, non-intrusive notification
        const notification = document.createElement('div');
        notification.className = 'personal-insight-notification';
        notification.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            background: linear-gradient(135deg, #a8e6cf, #88d8a3);
            color: #2d3436;
            padding: 20px;
            border-radius: 15px;
            max-width: 350px;
            box-shadow: 0 8px 25px rgba(168, 230, 207, 0.4);
            z-index: 19000;
            animation: slideInFromRight 0.5s ease;
        `;
        
        notification.innerHTML = `
            <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
                <div style="font-size: 1.2rem;">🧠</div>
                <div style="font-weight: 600;">${recommendation.title}</div>
                <button onclick="this.parentElement.parentElement.remove()" 
                        style="margin-left: auto; background: none; border: none; font-size: 1.2rem; cursor: pointer;">×</button>
            </div>
            <div style="margin-bottom: 15px; line-height: 1.5;">${recommendation.description}</div>
            <div style="display: flex; gap: 10px;">
                <button onclick="personalPatternRecognition.acceptRecommendation('${recommendation.type}'); this.parentElement.parentElement.remove();"
                        style="background: #00b894; color: white; border: none; padding: 8px 15px; border-radius: 8px; cursor: pointer;">
                    Yes, adapt this!
                </button>
                <button onclick="this.parentElement.parentElement.remove();"
                        style="background: #636e72; color: white; border: none; padding: 8px 15px; border-radius: 8px; cursor: pointer;">
                    Maybe later
                </button>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // Auto-remove after 15 seconds
        setTimeout(() => {
            if (notification.parentElement) {
                notification.remove();
            }
        }, 15000);
    }

    acceptRecommendation(type) {
        console.log(`🎯 User accepted recommendation: ${type}`);
        // Implement specific recommendation acceptance logic
        this.ecosystem.support.showNotification(
            '🌟 Adaptation Applied!',
            'Your learning companion is now even more personalized for you!',
            'success'
        );
    }

    getRecentUsageData(days) {
        const cutoff = Date.now() - (days * 24 * 60 * 60 * 1000);
        return this.ecosystem.learningData.usagePatterns.flatMap(patterns => 
            Object.values(patterns)
        ).filter(usage => usage.timestamp > cutoff);
    }

    getTimeOfDay(timestamp) {
        const hour = new Date(timestamp).getHours();
        if (hour < 6) return 'night';
        if (hour < 12) return 'morning';
        if (hour < 18) return 'afternoon';
        return 'evening';
    }

    getTimeSlot(hour) {
        if (hour < 6) return 'late_night';
        if (hour < 9) return 'early_morning';
        if (hour < 12) return 'late_morning';
        if (hour < 15) return 'early_afternoon';
        if (hour < 18) return 'late_afternoon';
        if (hour < 21) return 'early_evening';
        return 'late_evening';
    }

    calculateConfidence(data) {
        // Simple confidence calculation - can be enhanced
        return Math.min(1, data.length / 10);
    }

    calculateInsightConfidence(type, data) {
        // Calculate confidence based on data quality and quantity
        let confidence = 0.5; // Base confidence
        
        if (data.samples > 10) confidence += 0.2;
        if (data.consistency && data.consistency < 0.3) confidence += 0.2;
        if (data.usage && data.usage > 5) confidence += 0.1;
        
        return Math.min(1, confidence);
    }

    calculateVariance(numbers) {
        const mean = numbers.reduce((sum, n) => sum + n, 0) / numbers.length;
        const squaredDiffs = numbers.map(n => Math.pow(n - mean, 2));
        return squaredDiffs.reduce((sum, sq) => sum + sq, 0) / numbers.length;
    }

    detectCurrentActivity() {
        // Simple activity detection - could be enhanced
        const currentPage = window.location.pathname;
        if (currentPage.includes('lesson')) return 'learning';
        if (currentPage.includes('book')) return 'reading';
        if (currentPage.includes('assessment')) return 'assessment';
        return 'general';
    }

    startContinuousLearning() {
        // Run pattern analysis every hour
        setInterval(() => {
            this.analyzeFocusPatterns();
            this.analyzeEnergyPatterns();
            this.learnToolEffectiveness();
            this.learnContextualPreferences();
            this.detectStressPatterns();
            this.detectFlowStateConditions();
        }, 60 * 60 * 1000);

        // Run growth analysis daily
        setInterval(() => {
            this.trackPersonalGrowth();
        }, 24 * 60 * 60 * 1000);
    }

    setupPatternDetection() {
        // Set up real-time pattern detection hooks
        document.addEventListener('neurodivergent-tool-used', (event) => {
            this.recordObservation('tool_usage', event.detail);
        });

        document.addEventListener('focus-session-completed', (event) => {
            this.recordObservation('focus_completion', event.detail);
        });
    }

    recordObservation(type, data) {
        this.observations.push({
            type,
            data,
            timestamp: Date.now(),
            context: this.getCurrentContext()
        });
    }

    getCurrentContext() {
        return {
            timeOfDay: this.getTimeOfDay(Date.now()),
            currentActivity: this.detectCurrentActivity(),
            sessionDuration: Date.now() - (this.ecosystem.support.focusSession.startTime || Date.now())
        };
    }

    savePatterns() {
        localStorage.setItem('personalPatterns', JSON.stringify({
            patterns: this.patterns,
            insights: this.insights,
            observations: this.observations.slice(-1000) // Keep last 1000 observations
        }));
    }

    loadExistingPatterns() {
        const saved = localStorage.getItem('personalPatterns');
        if (saved) {
            const data = JSON.parse(saved);
            this.patterns = data.patterns || {};
            this.insights = data.insights || [];
            this.observations = data.observations || [];
        }
    }
}

// Global instance
window.PersonalPatternRecognition = PersonalPatternRecognition;