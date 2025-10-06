// ThinkAble Assessment Engine
class ThinkAbleAssessmentEngine {
    constructor() {
        this.apiBase = '/api/assessment';
        this.currentStep = 0;
        this.totalSteps = 0;
        this.responses = {};
        this.fontTestResults = {};
        this.userId = this.getCurrentUserId();
        this.questions = [];
        this.currentQuestionIndex = 0;
        this.sessionId = this.generateSessionId();
    }

    async startAssessment() {
        try {
            console.log('Starting assessment for user:', this.userId);
            
            // Show loading
            this.showLoading('Preparing your personalized assessment...');
            
            const response = await fetch(`${this.apiBase}/start/${this.userId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to start assessment');
            }
            
            const session = await response.json();
            console.log('Assessment session started:', session);
            
            this.questions = session.questions;
            this.totalSteps = this.questions.length + 2; // +2 for font test and results
            
            // Hide loading and start with welcome
            this.hideLoading();
            this.showWelcomeScreen(session);
            
        } catch (error) {
            console.error('Assessment start failed:', error);
            this.showError('Failed to start assessment. Please try again.');
        }
    }

    showWelcomeScreen(session) {
        const container = this.getMainContainer();
        
        container.innerHTML = `
            <div class="assessment-container">
                <div class="assessment-card welcome-card">
                    <div class="assessment-header">
                        <h1><i class="fas fa-brain"></i> Personalized Learning Assessment</h1>
                        <div class="assessment-badge">AI-Powered</div>
                    </div>
                    
                    <div class="welcome-content">
                        <p class="lead">Welcome to your personalized learning assessment! This quick evaluation will help us optimize ThinkAble specifically for your learning style and needs.</p>
                        
                        <div class="assessment-info">
                            <div class="info-item">
                                <i class="fas fa-clock"></i>
                                <span>Estimated time: ${Math.ceil(session.estimatedTimeMinutes)} minutes</span>
                            </div>
                            <div class="info-item">
                                <i class="fas fa-question-circle"></i>
                                <span>${session.totalQuestions} questions total</span>
                            </div>
                            <div class="info-item">
                                <i class="fas fa-shield-alt"></i>
                                <span>Completely private and secure</span>
                            </div>
                        </div>
                        
                        <div class="assessment-benefits">
                            <h3>What you'll get:</h3>
                            <ul>
                                <li><i class="fas fa-palette"></i> <strong>Personalized Interface:</strong> Colors, fonts, and layouts optimized for you</li>
                                <li><i class="fas fa-clock"></i> <strong>Custom Study Timer:</strong> Break intervals tailored to your attention patterns</li>
                                <li><i class="fas fa-book-open"></i> <strong>Adaptive Content:</strong> Lessons presented in your preferred learning style</li>
                                <li><i class="fas fa-lightbulb"></i> <strong>Smart Recommendations:</strong> AI-powered study suggestions based on your profile</li>
                            </ul>
                        </div>
                        
                        <div class="privacy-note">
                            <i class="fas fa-lock"></i>
                            <small>Your responses help us create a better learning experience for you. All data is kept private and secure.</small>
                        </div>
                        
                        <div class="welcome-actions">
                            <button class="btn btn-primary btn-large" onclick="globalAssessment.startFontTest()">
                                <i class="fas fa-play"></i> Start Assessment
                            </button>
                            <button class="btn btn-secondary" onclick="globalAssessment.skipAssessment()">
                                Skip for now
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    async startFontTest() {
        this.currentStep = 1;
        this.updateProgress();
        
        const container = this.getMainContainer();
        
        container.innerHTML = `
            <div class="assessment-container">
                <div class="assessment-card">
                    <div class="assessment-header">
                        <h2><i class="fas fa-font"></i> Font Readability Test</h2>
                        <p>This helps us choose the best fonts for your reading comfort</p>
                    </div>
                    
                    <div class="progress-container">
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: ${(this.currentStep / this.totalSteps) * 100}%"></div>
                        </div>
                        <span class="progress-text">Step ${this.currentStep} of ${this.totalSteps}</span>
                    </div>
                    
                    <div id="font-test-area" class="font-test-area">
                        <div class="font-test-instructions">
                            <p>We'll show you the same text in different fonts. For each one, tell us:</p>
                            <ul>
                                <li>How easy it is to read (1-5 scale)</li>
                                <li>Any visual difficulties you notice</li>
                            </ul>
                            <p><strong>There are no wrong answers</strong> - we just want to know what works best for you!</p>
                        </div>
                    </div>
                </div>
            </div>
        `;

        const fonts = [
            { name: 'Arial', family: 'Arial, sans-serif' },
            { name: 'Times New Roman', family: 'Times New Roman, serif' },
            { name: 'Comic Neue', family: 'Comic Neue, cursive' },
            { name: 'OpenDyslexic', family: 'OpenDyslexic, monospace' },
            { name: 'Verdana', family: 'Verdana, sans-serif' }
        ];

        await this.renderFontTest(fonts);
    }

    async renderFontTest(fonts) {
        const testArea = document.getElementById('font-test-area');
        let currentFontIndex = 0;

        const testText = "Learning should be accessible to everyone. Good typography makes reading comfortable and enjoyable. When text is easy to read, students can focus on understanding rather than struggling with the words themselves.";

        const showFont = (index) => {
            if (index >= fonts.length) {
                this.completeFontTest();
                return;
            }

            const font = fonts[index];
            const startTime = Date.now();

            testArea.innerHTML = `
                <div class="font-sample-container">
                    <div class="font-info">
                        <h4>Font ${index + 1} of ${fonts.length}: ${font.name}</h4>
                        <p class="font-instruction">Read the text below and rate how comfortable it is for you</p>
                    </div>
                    
                    <div class="reading-sample" style="font-family: ${font.family}; font-size: 18px; line-height: 1.6; margin: 20px 0; padding: 25px; border: 2px solid #e0e6ed; border-radius: 12px; background: #ffffff;">
                        ${testText}
                    </div>
                    
                    <div class="rating-section">
                        <h5>How easy was this font to read?</h5>
                        <div class="rating-buttons">
                            <button class="rating-btn" data-rating="1">
                                <span class="rating-number">1</span>
                                <span class="rating-label">Very Hard</span>
                            </button>
                            <button class="rating-btn" data-rating="2">
                                <span class="rating-number">2</span>
                                <span class="rating-label">Hard</span>
                            </button>
                            <button class="rating-btn" data-rating="3">
                                <span class="rating-number">3</span>
                                <span class="rating-label">Neutral</span>
                            </button>
                            <button class="rating-btn" data-rating="4">
                                <span class="rating-number">4</span>
                                <span class="rating-label">Easy</span>
                            </button>
                            <button class="rating-btn" data-rating="5">
                                <span class="rating-number">5</span>
                                <span class="rating-label">Very Easy</span>
                            </button>
                        </div>
                        
                        <div class="symptoms-section">
                            <h6>Did you notice any of these while reading? (Check all that apply)</h6>
                            <div class="symptoms-grid">
                                <label class="symptom-option">
                                    <input type="checkbox" id="lettersMove">
                                    <i class="fas fa-arrows-alt"></i>
                                    <span>Letters appear to move or blur</span>
                                </label>
                                <label class="symptom-option">
                                    <input type="checkbox" id="eyeStrain">
                                    <i class="fas fa-eye"></i>
                                    <span>Eye strain or fatigue</span>
                                </label>
                                <label class="symptom-option">
                                    <input type="checkbox" id="slowReading">
                                    <i class="fas fa-hourglass-half"></i>
                                    <span>Slowed reading speed</span>
                                </label>
                                <label class="symptom-option">
                                    <input type="checkbox" id="skipLines">
                                    <i class="fas fa-level-down-alt"></i>
                                    <span>Difficulty tracking lines</span>
                                </label>
                            </div>
                        </div>
                        
                        <div id="rating-selected" class="rating-confirmation" style="display: none;">
                            <p><i class="fas fa-check-circle"></i> Rating recorded! Moving to next font...</p>
                        </div>
                    </div>
                </div>
            `;

            // Add event listeners for rating buttons
            testArea.querySelectorAll('.rating-btn').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const rating = parseInt(e.currentTarget.dataset.rating);
                    const endTime = Date.now();
                    const readingTime = endTime - startTime;
                    
                    // Highlight selected rating
                    testArea.querySelectorAll('.rating-btn').forEach(b => b.classList.remove('selected'));
                    e.currentTarget.classList.add('selected');
                    
                    // Record response
                    this.fontTestResults[font.name] = {
                        rating: rating,
                        difficulty: rating <= 2 ? 'hard' : rating >= 4 ? 'easy' : 'medium',
                        readingTimeMs: readingTime,
                        lettersMove: document.getElementById('lettersMove')?.checked || false,
                        eyeStrain: document.getElementById('eyeStrain')?.checked || false,
                        slowReading: document.getElementById('slowReading')?.checked || false,
                        skipLines: document.getElementById('skipLines')?.checked || false
                    };
                    
                    // Show confirmation and move to next
                    document.getElementById('rating-selected').style.display = 'block';
                    
                    setTimeout(() => showFont(index + 1), 1000);
                });
            });
        };

        showFont(0);
    }

    async completeFontTest() {
        try {
            this.showLoading('Analyzing your font preferences...');
            
            const response = await fetch(`${this.apiBase}/font-test/${this.userId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    fontResponses: Object.keys(this.fontTestResults).map(fontName => ({
                        fontName: fontName,
                        rating: this.fontTestResults[fontName].rating,
                        difficulty: this.fontTestResults[fontName].difficulty,
                        symptoms: {
                            lettersMove: this.fontTestResults[fontName].lettersMove,
                            eyeStrain: this.fontTestResults[fontName].eyeStrain,
                            slowReading: this.fontTestResults[fontName].slowReading,
                            skipLines: this.fontTestResults[fontName].skipLines
                        }
                    }))
                })
            });

            const results = await response.json();
            
            this.hideLoading();
            this.showFontTestResults(results);
            
        } catch (error) {
            console.error('Font test submission failed:', error);
            this.hideLoading();
            this.showError('Failed to save font test results. Continuing with assessment...');
            this.startMainAssessment();
        }
    }

    showFontTestResults(results) {
        const container = this.getMainContainer();
        
        let analysisHtml = '<div class="font-analysis">';
        
        if (results.dyslexiaIndicators?.likelyDyslexia) {
            analysisHtml += `
                <div class="analysis-result positive">
                    <i class="fas fa-eye"></i>
                    <h4>Reading Support Recommended</h4>
                    <p>We've detected patterns that suggest you might benefit from reading accommodations. We'll optimize your interface with:</p>
                    <ul>
                        <li>Dyslexia-friendly fonts</li>
                        <li>Increased line spacing</li>
                        <li>Enhanced text-to-speech options</li>
                        <li>Customizable background colors</li>
                    </ul>
                </div>
            `;
        } else {
            analysisHtml += `
                <div class="analysis-result neutral">
                    <i class="fas fa-check-circle"></i>
                    <h4>Good Font Flexibility</h4>
                    <p>You show comfort with various font types. We'll provide standard fonts with full customization options available.</p>
                </div>
            `;
        }
        
        if (results.recommendedFonts && results.recommendedFonts.length > 0) {
            analysisHtml += `
                <div class="recommended-fonts">
                    <h5>Your Recommended Fonts:</h5>
                    <div class="font-chips">
                        ${results.recommendedFonts.map(font => `<span class="font-chip">${font}</span>`).join('')}
                    </div>
                </div>
            `;
        }
        
        analysisHtml += '</div>';
        
        container.innerHTML = `
            <div class="assessment-container">
                <div class="assessment-card">
                    <div class="assessment-header">
                        <h2><i class="fas fa-check-circle"></i> Font Test Complete!</h2>
                        <p>Here's what we discovered about your reading preferences</p>
                    </div>
                    
                    ${analysisHtml}
                    
                    <div class="continue-section">
                        <p>Ready to continue with the main assessment? This will help us personalize your entire learning experience.</p>
                        <button class="btn btn-primary btn-large" onclick="globalAssessment.startMainAssessment()">
                            <i class="fas fa-arrow-right"></i> Continue Assessment
                        </button>
                    </div>
                </div>
            </div>
        `;
    }

    startMainAssessment() {
        this.currentStep = 2;
        this.currentQuestionIndex = 0;
        this.showQuestion();
    }

    showQuestion() {
        if (this.currentQuestionIndex >= this.questions.length) {
            this.completeAssessment();
            return;
        }

        const question = this.questions[this.currentQuestionIndex];
        const progress = ((this.currentStep + (this.currentQuestionIndex / this.questions.length)) / this.totalSteps) * 100;
        
        const container = this.getMainContainer();
        
        container.innerHTML = `
            <div class="assessment-container">
                <div class="assessment-card question-card">
                    <div class="assessment-header">
                        <h3>Question ${this.currentQuestionIndex + 1} of ${this.questions.length}</h3>
                        <p class="question-category">${this.formatCategory(question.category)}</p>
                    </div>
                    
                    <div class="progress-container">
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: ${progress}%"></div>
                        </div>
                        <span class="progress-text">${Math.round(progress)}% Complete</span>
                    </div>
                    
                    <div class="question-content">
                        <h4 class="question-text">${question.questionText}</h4>
                        
                        <div class="answer-options" id="answer-options">
                            ${this.renderAnswerOptions(question)}
                        </div>
                        
                        <div class="question-navigation">
                            <button class="btn btn-secondary" onclick="globalAssessment.previousQuestion()" 
                                    ${this.currentQuestionIndex === 0 ? 'disabled' : ''}>
                                <i class="fas fa-arrow-left"></i> Previous
                            </button>
                            <button class="btn btn-primary" id="next-btn" onclick="globalAssessment.nextQuestion()" disabled>
                                Next <i class="fas fa-arrow-right"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Add event listeners for answer selection
        this.setupAnswerListeners(question);
    }

    renderAnswerOptions(question) {
        if (question.questionType === 'likert') {
            const options = question.options ? JSON.parse(question.options) : { scale: 5, labels: ['Never', 'Rarely', 'Sometimes', 'Often', 'Always'] };
            let html = '<div class="likert-scale">';
            
            for (let i = 1; i <= options.scale; i++) {
                const label = options.labels[i - 1] || `Option ${i}`;
                html += `
                    <label class="likert-option">
                        <input type="radio" name="response" value="${i}">
                        <div class="likert-button">
                            <span class="likert-number">${i}</span>
                            <span class="likert-label">${label}</span>
                        </div>
                    </label>
                `;
            }
            
            html += '</div>';
            return html;
        } else if (question.questionType === 'binary') {
            return `
                <div class="binary-options">
                    <label class="binary-option">
                        <input type="radio" name="response" value="1">
                        <div class="binary-button yes">
                            <i class="fas fa-check"></i>
                            <span>Yes</span>
                        </div>
                    </label>
                    <label class="binary-option">
                        <input type="radio" name="response" value="0">
                        <div class="binary-button no">
                            <i class="fas fa-times"></i>
                            <span>No</span>
                        </div>
                    </label>
                </div>
            `;
        }
        
        return '<p>Question type not supported</p>';
    }

    setupAnswerListeners(question) {
        const options = document.querySelectorAll('input[name="response"]');
        const nextBtn = document.getElementById('next-btn');
        
        options.forEach(option => {
            option.addEventListener('change', () => {
                // Remove previous selections
                document.querySelectorAll('.likert-option, .binary-option').forEach(opt => {
                    opt.classList.remove('selected');
                });
                
                // Add selection to current option
                option.closest('.likert-option, .binary-option').classList.add('selected');
                
                // Store response
                this.responses[question.id] = option.value;
                
                // Enable next button
                nextBtn.disabled = false;
            });
        });
    }

    nextQuestion() {
        this.currentQuestionIndex++;
        this.showQuestion();
    }

    previousQuestion() {
        if (this.currentQuestionIndex > 0) {
            this.currentQuestionIndex--;
            this.showQuestion();
        }
    }

    async completeAssessment() {
        try {
            this.showLoading('Processing your responses and creating your personalized profile...');
            
            const response = await fetch(`${this.apiBase}/submit/${this.userId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    responses: this.responses,
                    sessionId: this.sessionId
                })
            });

            if (!response.ok) {
                throw new Error('Failed to submit assessment');
            }

            const results = await response.json();
            
            this.hideLoading();
            this.showResults(results);
            
        } catch (error) {
            console.error('Assessment submission failed:', error);
            this.hideLoading();
            this.showError('Failed to process assessment. Please try again.');
        }
    }

    showResults(results) {
        const container = this.getMainContainer();
        
        const traits = this.analyzeTraits(results.assessment);
        
        container.innerHTML = `
            <div class="assessment-container">
                <div class="assessment-card results-card">
                    <div class="assessment-header success">
                        <h1><i class="fas fa-trophy"></i> Assessment Complete!</h1>
                        <p>Your personalized learning profile is ready</p>
                    </div>
                    
                    <div class="results-summary">
                        <div class="preset-info">
                            <h3>Your Personalized Mode:</h3>
                            <div class="preset-badge ${results.assessment.recommendedPreset}">
                                ${this.getPresetDisplay(results.assessment.recommendedPreset)}
                            </div>
                            <p class="preset-description">${this.getPresetDescription(results.assessment.recommendedPreset)}</p>
                        </div>
                        
                        <div class="traits-overview">
                            <h3>Your Learning Profile:</h3>
                            <div class="traits-grid">
                                ${traits.map(trait => `
                                    <div class="trait-item ${trait.level}">
                                        <i class="${trait.icon}"></i>
                                        <div class="trait-info">
                                            <h4>${trait.name}</h4>
                                            <p>${trait.description}</p>
                                        </div>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                        
                        <div class="recommendations-preview">
                            <h3>What's optimized for you:</h3>
                            <div class="optimization-list">
                                ${this.getOptimizationList(results.assessment.recommendedPreset)}
                            </div>
                        </div>
                    </div>
                    
                    <div class="results-actions">
                        <button class="btn btn-primary btn-large" onclick="globalAssessment.startLearning()">
                            <i class="fas fa-rocket"></i> Start Learning with My Profile
                        </button>
                        <button class="btn btn-secondary" onclick="globalAssessment.viewDetailedResults()">
                            <i class="fas fa-chart-bar"></i> View Detailed Results
                        </button>
                    </div>
                </div>
            </div>
        `;
    }

    // Helper methods
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

    generateSessionId() {
        return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }

    getMainContainer() {
        return document.getElementById('main-content') || document.querySelector('.student-home-container') || document.body;
    }

    updateProgress() {
        const progressElements = document.querySelectorAll('.progress-fill');
        const progressPercentage = (this.currentStep / this.totalSteps) * 100;
        progressElements.forEach(el => {
            el.style.width = `${progressPercentage}%`;
        });
    }

    formatCategory(category) {
        const categoryMap = {
            'attention': 'Focus & Attention',
            'reading': 'Reading & Language',
            'social': 'Communication & Social',
            'sensory': 'Sensory Processing',
            'motor': 'Motor Skills'
        };
        return categoryMap[category] || category;
    }

    analyzeTraits(assessment) {
        const traits = [];
        
        if (assessment.attentionScore >= 18) {
            traits.push({
                name: 'Attention Support',
                description: 'Benefits from focused, shorter study sessions',
                icon: 'fas fa-bullseye',
                level: 'high'
            });
        }
        
        if (assessment.readingDifficultyScore >= 15) {
            traits.push({
                name: 'Reading Support',
                description: 'Benefits from enhanced text presentation',
                icon: 'fas fa-book-open',
                level: 'high'
            });
        }
        
        if (assessment.socialCommunicationScore >= 16) {
            traits.push({
                name: 'Communication Support',
                description: 'Benefits from clear, structured information',
                icon: 'fas fa-comments',
                level: 'high'
            });
        }
        
        if (assessment.sensoryProcessingScore >= 14) {
            traits.push({
                name: 'Sensory Support',
                description: 'Benefits from calm, organized environments',
                icon: 'fas fa-adjust',
                level: 'high'
            });
        }
        
        if (traits.length === 0) {
            traits.push({
                name: 'Flexible Learning',
                description: 'Adapts well to various learning formats',
                icon: 'fas fa-star',
                level: 'standard'
            });
        }
        
        return traits;
    }

    getPresetDisplay(preset) {
        const presetMap = {
            'adhd': 'ADHD Support',
            'dyslexia': 'Dyslexia Support',
            'autism': 'Autism Support',
            'sensory': 'Sensory-Friendly',
            'dyslexia-adhd': 'Combined Support',
            'standard': 'Standard Mode'
        };
        return presetMap[preset] || 'Personalized Mode';
    }

    getPresetDescription(preset) {
        const descriptions = {
            'adhd': 'Optimized for attention and focus with shorter sessions and visual cues',
            'dyslexia': 'Enhanced readability with dyslexia-friendly fonts and text formatting',
            'autism': 'Structured, predictable interface with clear navigation',
            'sensory': 'Calm, low-stimulation design with reduced visual noise',
            'dyslexia-adhd': 'Combined optimizations for both reading and attention support',
            'standard': 'Balanced learning environment with standard accessibility features'
        };
        return descriptions[preset] || 'Customized for your unique learning needs';
    }

    getOptimizationList(preset) {
        const optimizations = {
            'adhd': [
                'Shorter 15-minute study sessions',
                'Enhanced visual progress indicators',
                'Simplified navigation and reduced distractions',
                'Quick break reminders'
            ],
            'dyslexia': [
                'Dyslexia-friendly fonts (Comic Neue, OpenDyslexic)',
                'Increased line spacing and text size',
                'Enhanced text-to-speech features',
                'Customizable background colors'
            ],
            'autism': [
                'Predictable, consistent layouts',
                'Clear step-by-step instructions',
                'Reduced animations and transitions',
                'Structured learning pathways'
            ],
            'sensory': [
                'Calming color schemes',
                'Reduced visual complexity',
                'Adjustable brightness controls',
                'Minimal motion and animations'
            ],
            'dyslexia-adhd': [
                'Combined reading and attention supports',
                'Shorter sessions with enhanced fonts',
                'Comprehensive text-to-speech',
                'Both visual and auditory learning aids'
            ],
            'standard': [
                'Balanced, accessible design',
                'Standard 25-minute study sessions',
                'Full customization options',
                'Adaptive difficulty levels'
            ]
        };
        
        const list = optimizations[preset] || optimizations['standard'];
        return list.map(item => `<div class="optimization-item"><i class="fas fa-check"></i> ${item}</div>`).join('');
    }

    showLoading(message) {
        const container = this.getMainContainer();
        container.innerHTML = `
            <div class="assessment-container">
                <div class="loading-card">
                    <div class="loading-spinner">
                        <i class="fas fa-brain fa-spin"></i>
                    </div>
                    <h3>Processing...</h3>
                    <p>${message}</p>
                </div>
            </div>
        `;
    }

    hideLoading() {
        // Loading will be replaced by next content
    }

    showError(message) {
        const container = this.getMainContainer();
        container.innerHTML = `
            <div class="assessment-container">
                <div class="error-card">
                    <i class="fas fa-exclamation-triangle"></i>
                    <h3>Oops! Something went wrong</h3>
                    <p>${message}</p>
                    <button class="btn btn-primary" onclick="window.location.href='/student-home.html'">
                        Return to Dashboard
                    </button>
                </div>
            </div>
        `;
    }

    skipAssessment() {
        if (confirm('Are you sure you want to skip the assessment? You can always take it later from your settings.')) {
            window.location.href = '/student-home.html';
        }
    }

    startLearning() {
        // Apply the new UI settings immediately
        window.location.href = '/student-home.html?newProfile=true';
    }

    viewDetailedResults() {
        // Could navigate to a detailed results page
        window.location.href = '/student-profile.html';
    }
}

// Initialize global assessment instance
let globalAssessment;

// Auto-start assessment if on assessment page
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('assessment') || 
        window.location.search.includes('startAssessment=true')) {
        globalAssessment = new ThinkAbleAssessmentEngine();
        globalAssessment.startAssessment();
    }
});

// Export for use in other files
window.ThinkAbleAssessmentEngine = ThinkAbleAssessmentEngine;