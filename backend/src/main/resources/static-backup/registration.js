// Registration Wizard Controller
let currentStep = 1;
let registrationData = {
    basicInfo: {},
    assessmentScores: {},
    activityPreferences: {},
    studyPreferences: {}
};

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is already logged in
    if (isAuthenticated && isAuthenticated()) {
        window.location.href = '/student-home.html';
        return;
    }

    initializeRegistration();
});

function initializeRegistration() {
    // Set up age range change handler for parent email requirement
    document.getElementById('ageRange').addEventListener('change', function() {
        const ageRange = this.value;
        const parentEmailGroup = document.querySelector('.parent-email-group');
        
        if (ageRange === '5-8' || ageRange === '9-12') {
            parentEmailGroup.style.display = 'block';
            document.getElementById('parentEmail').required = true;
        } else {
            parentEmailGroup.style.display = 'none';
            document.getElementById('parentEmail').required = false;
        }
    });

    // Set up learning preference cards
    initializePreferenceCards();
    
    // Set up form validation
    setupFormValidation();
}

function initializePreferenceCards() {
    const preferenceCards = document.querySelectorAll('.preference-card');
    preferenceCards.forEach(card => {
        card.addEventListener('click', function() {
            // Toggle selection
            this.classList.toggle('selected');
            
            // Update activity preferences
            const preference = this.dataset.preference;
            if (!registrationData.activityPreferences) {
                registrationData.activityPreferences = {};
            }
            
            if (this.classList.contains('selected')) {
                registrationData.activityPreferences[preference] = 
                    (registrationData.activityPreferences[preference] || 0) + 1;
            } else {
                registrationData.activityPreferences[preference] = 
                    Math.max(0, (registrationData.activityPreferences[preference] || 0) - 1);
            }
            
            // Trigger validation update
            validateCurrentStep();
        });
    });

    // Add event listeners for radio buttons and checkboxes
    const radioButtons = document.querySelectorAll('input[name="studyTime"]');
    radioButtons.forEach(radio => {
        radio.addEventListener('change', validateCurrentStep);
    });

    const checkboxes = document.querySelectorAll('input[name="motivation"]');
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', validateCurrentStep);
    });
}

function setupFormValidation() {
    // Real-time validation for basic info
    const requiredFields = ['fullName', 'email', 'password', 'ageRange', 'gradeLevel'];
    requiredFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field) {
            field.addEventListener('input', validateCurrentStep);
            field.addEventListener('change', validateCurrentStep);
        }
    });
}

function validateCurrentStep() {
    const currentStepElement = document.querySelector(`.registration-step[data-step="${currentStep}"]`);
    let isValid = true;

    switch (currentStep) {
        case 1:
            isValid = validateBasicInfo();
            break;
        case 2:
            isValid = validateGamesCompleted();
            break;
        case 3:
            isValid = validatePreferences();
            break;
        case 4:
            isValid = true; // Completion step
            break;
    }

    // Update next button state
    const nextBtn = document.querySelector('.next-btn');
    const finishBtn = document.querySelector('.finish-btn');
    
    if (currentStep < 4) {
        nextBtn.disabled = !isValid;
    } else {
        finishBtn.style.display = isValid ? 'block' : 'none';
    }

    return isValid;
}

function validateBasicInfo() {
    const fullName = document.getElementById('fullName').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const ageRange = document.getElementById('ageRange').value;
    const gradeLevel = document.getElementById('gradeLevel').value;
    
    let isValid = fullName && email && password && ageRange && gradeLevel;
    
    // Validate parent email if required
    const parentEmailGroup = document.querySelector('.parent-email-group');
    if (parentEmailGroup.style.display !== 'none') {
        const parentEmail = document.getElementById('parentEmail').value.trim();
        isValid = isValid && parentEmail;
    }

    // Basic email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    isValid = isValid && emailRegex.test(email);

    return isValid;
}

function validateGamesCompleted() {
    return gamesCompleted >= 5; // All five assessment games must be completed
}

function validatePreferences() {
    // At least one learning preference should be selected
    const selectedPreferences = document.querySelectorAll('.preference-card.selected');
    const studyTimeSelected = document.querySelector('input[name="studyTime"]:checked');
    
    return selectedPreferences.length > 0 && studyTimeSelected;
}

function nextStep() {
    if (!validateCurrentStep()) {
        showMessage('Please complete all required fields before continuing.', 'error');
        return;
    }

    // Save current step data
    saveCurrentStepData();

    // Move to next step
    if (currentStep < 4) {
        currentStep++;
        showStep(currentStep);
        
        // Initialize games when entering step 2
        if (currentStep === 2) {
            initializeGames();
        }
    }
}

function previousStep() {
    if (currentStep > 1) {
        currentStep--;
        showStep(currentStep);
    }
}

function showStep(stepNumber) {
    // Hide all steps
    document.querySelectorAll('.registration-step').forEach(step => {
        step.classList.remove('active');
    });

    // Show current step
    document.querySelector(`.registration-step[data-step="${stepNumber}"]`).classList.add('active');

    // Update progress indicator
    document.querySelectorAll('.progress-step').forEach((step, index) => {
        step.classList.toggle('active', index + 1 <= stepNumber);
        step.classList.toggle('completed', index + 1 < stepNumber);
    });

    // Update navigation buttons
    document.querySelector('.prev-btn').disabled = stepNumber === 1;
    document.querySelector('.next-btn').style.display = stepNumber === 4 ? 'none' : 'inline-flex';
    document.querySelector('.finish-btn').style.display = stepNumber === 4 ? 'inline-flex' : 'none';

    // Validate current step
    validateCurrentStep();
}

function saveCurrentStepData() {
    switch (currentStep) {
        case 1:
            registrationData.basicInfo = {
                fullName: document.getElementById('fullName').value.trim(),
                email: document.getElementById('email').value.trim(),
                password: document.getElementById('password').value,
                ageRange: document.getElementById('ageRange').value,
                gradeLevel: document.getElementById('gradeLevel').value,
                parentEmail: document.getElementById('parentEmail').value.trim()
            };
            break;
        case 2:
            registrationData.assessmentScores = getGameScores();
            break;
        case 3:
            // Save learning preferences
            const selectedPreferences = document.querySelectorAll('.preference-card.selected');
            selectedPreferences.forEach(card => {
                const preference = card.dataset.preference;
                registrationData.activityPreferences[preference] = 
                    (registrationData.activityPreferences[preference] || 0) + 1;
            });

            // Save study time preference
            const studyTime = document.querySelector('input[name="studyTime"]:checked');
            if (studyTime) {
                registrationData.studyPreferences.preferredTime = studyTime.value;
            }

            // Save motivation preferences
            const motivations = document.querySelectorAll('input[name="motivation"]:checked');
            registrationData.studyPreferences.motivations = Array.from(motivations).map(m => m.value);
            break;
    }
}

function generateProfileSummary() {
    const summaryContainer = document.getElementById('profileSummary');
    const data = registrationData;
    
    // Determine dominant learning style
    let dominantStyle = 'Mixed';
    let maxScore = 0;
    Object.entries(data.activityPreferences).forEach(([style, score]) => {
        if (score > maxScore) {
            maxScore = score;
            dominantStyle = style.charAt(0).toUpperCase() + style.slice(1);
        }
    });

    // Determine optimal session length based on focus game
    let sessionLength = 'Standard (15-20 min)';
    const focusScore = data.assessmentScores.focusGame || 50;
    if (focusScore >= 80) {
        sessionLength = 'Extended (20-25 min)';
    } else if (focusScore < 60) {
        sessionLength = 'Short (10-15 min)';
    }

    // Determine UI preferences based on assessment
    let uiStyle = 'Balanced';
    const avgScore = Object.values(data.assessmentScores).reduce((a, b) => a + b, 0) / 3;
    if (avgScore >= 75) {
        uiStyle = 'Advanced';
    } else if (avgScore < 50) {
        uiStyle = 'Simplified';
    }

    summaryContainer.innerHTML = `
        <div class="summary-item">
            <i class="fas fa-brain"></i>
            <div>
                <strong>Learning Style</strong>
                <span>${dominantStyle} Learner</span>
            </div>
        </div>
        <div class="summary-item">
            <i class="fas fa-clock"></i>
            <div>
                <strong>Session Length</strong>
                <span>${sessionLength}</span>
            </div>
        </div>
        <div class="summary-item">
            <i class="fas fa-palette"></i>
            <div>
                <strong>Interface Style</strong>
                <span>${uiStyle}</span>
            </div>
        </div>
        <div class="summary-item">
            <i class="fas fa-sun"></i>
            <div>
                <strong>Best Study Time</strong>
                <span>${data.studyPreferences.preferredTime || 'Flexible'}</span>
            </div>
        </div>
    `;
}

async function completeRegistration() {
    const finishBtn = document.querySelector('.finish-btn');
    finishBtn.disabled = true;
    finishBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating your account...';

    try {
        // First, register the user
        const registerResponse = await fetch('http://localhost:8081/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: registrationData.basicInfo.fullName,
                email: registrationData.basicInfo.email,
                password: registrationData.basicInfo.password,
                ageRange: registrationData.basicInfo.ageRange,
                gradeLevel: registrationData.basicInfo.gradeLevel,
                parentEmail: registrationData.basicInfo.parentEmail,
                role: 'STUDENT'
            })
        });

        if (!registerResponse.ok) {
            const error = await registerResponse.json();
            throw new Error(error.message || 'Registration failed');
        }

        // Login to get token
        const loginResponse = await fetch('http://localhost:8081/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: registrationData.basicInfo.email,
                password: registrationData.basicInfo.password
            })
        });

        if (!loginResponse.ok) {
            throw new Error('Failed to login after registration');
        }

        const loginData = await loginResponse.json();
        
        // Store authentication data
        localStorage.setItem('authToken', loginData.token);
        localStorage.setItem('userRole', loginData.role);
        localStorage.setItem('userEmail', loginData.email);
        localStorage.setItem('tokenExpiry', (Date.now() + loginData.expiresIn).toString());

        // Save assessment results
        const assessmentResponse = await fetch('http://localhost:8081/api/assessment/save-results', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${loginData.token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                assessmentScores: registrationData.assessmentScores,
                activityPreferences: registrationData.activityPreferences,
                ageRange: registrationData.basicInfo.ageRange,
                gradeLevel: registrationData.basicInfo.gradeLevel,
                parentEmail: registrationData.basicInfo.parentEmail
            })
        });

        if (!assessmentResponse.ok) {
            console.warn('Failed to save assessment results, but registration succeeded');
        }

        // Show success and redirect
        finishBtn.innerHTML = '<i class="fas fa-check"></i> Success! Redirecting...';
        finishBtn.style.backgroundColor = '#2ecc71';

        showMessage('Welcome to ThinkAble! Redirecting to your personalized dashboard...', 'success');

        setTimeout(() => {
            window.location.href = '/student-home.html';
        }, 2000);

    } catch (error) {
        console.error('Registration error:', error);
        showMessage('Registration failed: ' + error.message, 'error');
        
        finishBtn.disabled = false;
        finishBtn.innerHTML = 'Try Again <i class="fas fa-retry"></i>';
    }
}

function showMessage(message, type) {
    const messageElement = document.getElementById('registrationMessage');
    messageElement.textContent = message;
    messageElement.className = `registration-message ${type}`;
    messageElement.style.display = 'block';

    if (type === 'success') {
        setTimeout(() => {
            messageElement.style.display = 'none';
        }, 5000);
    }
}

// Initialize when moving to step 4
function initializeCompletionStep() {
    generateProfileSummary();
    
    // Add celebration animation
    setTimeout(() => {
        document.querySelectorAll('.celebration-animation i').forEach((star, index) => {
            setTimeout(() => {
                star.style.animation = `twinkle 1s ease-in-out ${index * 0.2}s infinite`;
            }, index * 200);
        });
    }, 500);
}

// Custom animations and interactions
document.addEventListener('DOMContentLoaded', function() {
    // Add smooth transitions between steps
    const steps = document.querySelectorAll('.registration-step');
    steps.forEach(step => {
        step.addEventListener('transitionend', function() {
            if (this.classList.contains('active')) {
                // Step became active
                const stepNumber = parseInt(this.dataset.step);
                if (stepNumber === 4) {
                    initializeCompletionStep();
                }
            }
        });
    });
});