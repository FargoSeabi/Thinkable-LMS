document.addEventListener('DOMContentLoaded', async function() {
    // Require student authentication
    if (!requireRole('STUDENT')) {
        return;
    }

    // Apply accessibility settings
    applyAccessibilitySettings();
    
    // Load lessons
    await loadLessons();
});

function applyAccessibilitySettings() {
    const dyslexiaMode = localStorage.getItem('dyslexiaMode') === 'true';
    const highContrastMode = localStorage.getItem('highContrastMode') === 'true';

    if (dyslexiaMode) {
        if (!document.querySelector('link[href="https://fonts.cdnfonts.com/css/open-dyslexic"]')) {
            const link = document.createElement('link');
            link.href = 'https://fonts.cdnfonts.com/css/open-dyslexic';
            link.rel = 'stylesheet';
            document.head.appendChild(link);
        }
        document.body.classList.add('dyslexia-mode');
    }

    if (highContrastMode) {
        document.body.classList.add('high-contrast');
    }
}

async function loadLessons() {
    const lessonContainer = document.getElementById('lessonContainer');
    const lessonMessage = document.getElementById('lessonMessage');
    const email = localStorage.getItem('userEmail');

    try {
        // Show loading state
        lessonContainer.innerHTML = `
            <div class="loading-container">
                <i class="fas fa-spinner fa-spin"></i>
                <p>Loading lessons...</p>
            </div>
        `;

        const response = await authenticatedFetch(`http://localhost:8081/api/student/lessons?email=${encodeURIComponent(email)}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch lessons (${response.status})`);
        }

        const lessons = await response.json();

        if (!lessons || lessons.length === 0) {
            lessonContainer.innerHTML = `
                <div class="no-content">
                    <i class="fas fa-book-open"></i>
                    <h3>No Lessons Available</h3>
                    <p>Check back later for new lessons to be added.</p>
                </div>
            `;
            return;
        }

        // Render lessons
        lessonContainer.innerHTML = '';
        lessons.forEach((lesson, index) => {
            const card = document.createElement('div');
            card.className = 'lesson-card';
            card.style.animationDelay = `${index * 0.1}s`;
            
            card.innerHTML = `
                <div class="lesson-card-header">
                    <i class="fas fa-play-circle"></i>
                </div>
                <div class="lesson-card-content">
                    <h3>${escapeHtml(lesson.title)}</h3>
                    <p class="lesson-description">${escapeHtml(lesson.description || 'No description available')}</p>
                    ${lesson.duration ? `<p class="lesson-duration"><i class="fas fa-clock"></i> ${lesson.duration} min</p>` : ''}
                </div>
                <div class="lesson-card-actions">
                    <button class="start-btn" onclick="startLesson(${lesson.id}, '${escapeHtml(lesson.title)}')" 
                            aria-label="Start ${escapeHtml(lesson.title)}">
                        <i class="fas fa-play"></i> Start Lesson
                    </button>
                </div>
            `;
            
            lessonContainer.appendChild(card);
        });

        if (lessonMessage) {
            lessonMessage.textContent = `Found ${lessons.length} lesson${lessons.length === 1 ? '' : 's'}`;
            lessonMessage.className = 'message success';
        }

    } catch (error) {
        console.error('Error loading lessons:', error);
        
        lessonContainer.innerHTML = `
            <div class="error-container">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>Error Loading Lessons</h3>
                <p>${error.message}</p>
                <button onclick="loadLessons()" class="retry-btn">
                    <i class="fas fa-retry"></i> Try Again
                </button>
            </div>
        `;

        if (lessonMessage) {
            lessonMessage.textContent = 'Error loading lessons: ' + error.message;
            lessonMessage.className = 'message error';
        }
    }
}

async function startLesson(lessonId, title) {
    try {
        // Fetch lesson details first
        const email = localStorage.getItem('userEmail');
        const lessonResponse = await authenticatedFetch(`http://localhost:8081/api/student/lessons?email=${encodeURIComponent(email)}`);
        
        if (!lessonResponse.ok) {
            throw new Error('Failed to fetch lesson details');
        }
        
        const lessons = await lessonResponse.json();
        const lesson = lessons.find(l => l.id === lessonId);
        
        if (!lesson) {
            throw new Error('Lesson not found');
        }

        const overlay = document.createElement('div');
        overlay.className = 'lesson-overlay';
        overlay.innerHTML = `
            <div class="lesson-modal">
                <div class="lesson-modal-header">
                    <h3><i class="fas fa-book-open"></i> ${escapeHtml(title)}</h3>
                    <button class="close-btn" onclick="closeLessonViewer()" aria-label="Close lesson">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="lesson-modal-content">
                    <div class="lesson-content">
                        <div class="lesson-description">
                            <h4>About This Lesson</h4>
                            <p>${escapeHtml(lesson.description || 'Learn with this interactive lesson.')}</p>
                        </div>
                        
                        ${lesson.youtubeUrl ? `
                            <div class="lesson-video">
                                <h4>Video Lesson</h4>
                                <div class="video-container">
                                    <iframe width="100%" height="315" 
                                            src="https://www.youtube.com/embed/${extractYouTubeId(lesson.youtubeUrl)}" 
                                            title="YouTube video player" 
                                            frameborder="0" 
                                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                                            allowfullscreen>
                                    </iframe>
                                </div>
                            </div>
                        ` : `
                            <div class="lesson-video-placeholder">
                                <i class="fas fa-video fa-3x"></i>
                                <p>Video content coming soon</p>
                            </div>
                        `}
                        
                        <div class="lesson-instructions">
                            <h4>Instructions</h4>
                            <ol>
                                <li>Watch the video lesson above (if available)</li>
                                <li>Take notes on key concepts</li>
                                <li>When ready, take the quiz to test your understanding</li>
                                <li>You need to score at least 70% to pass</li>
                            </ol>
                        </div>
                        
                        <div class="lesson-actions">
                            <button class="quiz-btn" onclick="startQuiz(${lessonId}, '${escapeHtml(title)}')">
                                <i class="fas fa-question-circle"></i> Take Quiz
                            </button>
                            <button class="complete-btn" onclick="completeLesson(${lessonId}, '${escapeHtml(title)}')">
                                <i class="fas fa-check"></i> Mark as Complete
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);
        document.body.style.overflow = 'hidden';
        
    } catch (error) {
        console.error('Error starting lesson:', error);
        alert('Failed to load lesson: ' + error.message);
    }
}

let currentQuiz = null;
let currentAnswers = {};
let quizStartTime = null;

async function startQuiz(lessonId, lessonTitle) {
    try {
        const email = localStorage.getItem('userEmail');
        const response = await authenticatedFetch(`http://localhost:8081/api/student/quiz/${lessonId}?email=${encodeURIComponent(email)}`);
        
        if (!response.ok) {
            if (response.status === 404) {
                alert('No quiz available for this lesson yet. Check back later!');
                return;
            }
            throw new Error(`Failed to fetch quiz (${response.status})`);
        }
        
        const quiz = await response.json();
        currentQuiz = quiz;
        currentAnswers = {};
        quizStartTime = new Date();
        
        // Close lesson viewer and show quiz
        closeLessonViewer();
        showQuizModal(quiz, lessonTitle);
        
    } catch (error) {
        console.error('Error starting quiz:', error);
        alert('Failed to load quiz: ' + error.message);
    }
}

function showQuizModal(quiz, lessonTitle) {
    const overlay = document.createElement('div');
    overlay.className = 'quiz-overlay';
    overlay.innerHTML = `
        <div class="quiz-modal">
            <div class="quiz-header">
                <h3><i class="fas fa-question-circle"></i> ${escapeHtml(quiz.title || lessonTitle + ' Quiz')}</h3>
                <div class="quiz-timer">
                    <i class="fas fa-clock"></i>
                    <span id="quizTimer">00:00</span>
                </div>
            </div>
            <div class="quiz-content">
                <div class="quiz-progress">
                    <div class="progress-bar">
                        <div class="progress-fill" id="quizProgress"></div>
                    </div>
                    <span class="progress-text">Question <span id="currentQuestion">1</span> of ${quiz.questions.length}</span>
                </div>
                
                <div id="quizQuestions">
                    ${renderQuizQuestions(quiz.questions)}
                </div>
                
                <div class="quiz-navigation">
                    <button id="prevBtn" class="nav-btn" onclick="navigateQuiz(-1)" disabled>
                        <i class="fas fa-chevron-left"></i> Previous
                    </button>
                    <button id="nextBtn" class="nav-btn" onclick="navigateQuiz(1)">
                        Next <i class="fas fa-chevron-right"></i>
                    </button>
                    <button id="submitBtn" class="submit-btn" onclick="submitQuiz()" style="display: none;">
                        <i class="fas fa-check"></i> Submit Quiz
                    </button>
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(overlay);
    document.body.style.overflow = 'hidden';
    
    // Start timer
    startQuizTimer();
    
    // Show first question
    showQuestion(0);
}

function renderQuizQuestions(questions) {
    return questions.map((question, index) => `
        <div class="question-container" id="question-${index}" style="display: none;">
            <div class="question-number">Question ${index + 1}</div>
            <div class="question-text">${escapeHtml(question.question)}</div>
            <div class="question-options">
                ${question.options.map((option, optionIndex) => `
                    <label class="option-label">
                        <input type="radio" name="question-${index}" value="${optionIndex}" 
                               onchange="selectAnswer(${index}, ${optionIndex})">
                        <span class="option-text">${escapeHtml(option)}</span>
                    </label>
                `).join('')}
            </div>
        </div>
    `).join('');
}

let currentQuestionIndex = 0;

function showQuestion(index) {
    // Hide all questions
    document.querySelectorAll('.question-container').forEach(q => q.style.display = 'none');
    
    // Show current question
    const questionElement = document.getElementById(`question-${index}`);
    if (questionElement) {
        questionElement.style.display = 'block';
    }
    
    // Update progress
    updateQuizProgress(index);
    
    // Update navigation buttons
    updateNavigationButtons(index);
    
    currentQuestionIndex = index;
}

function updateQuizProgress(index) {
    const progressFill = document.getElementById('quizProgress');
    const currentQuestionSpan = document.getElementById('currentQuestion');
    
    if (progressFill && currentQuestionSpan) {
        const progress = ((index + 1) / currentQuiz.questions.length) * 100;
        progressFill.style.width = `${progress}%`;
        currentQuestionSpan.textContent = index + 1;
    }
}

function updateNavigationButtons(index) {
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const submitBtn = document.getElementById('submitBtn');
    
    if (prevBtn) prevBtn.disabled = index === 0;
    
    if (index === currentQuiz.questions.length - 1) {
        if (nextBtn) nextBtn.style.display = 'none';
        if (submitBtn) submitBtn.style.display = 'inline-block';
    } else {
        if (nextBtn) nextBtn.style.display = 'inline-block';
        if (submitBtn) submitBtn.style.display = 'none';
    }
}

function navigateQuiz(direction) {
    const newIndex = currentQuestionIndex + direction;
    
    if (newIndex >= 0 && newIndex < currentQuiz.questions.length) {
        showQuestion(newIndex);
    }
}

function selectAnswer(questionIndex, optionIndex) {
    currentAnswers[questionIndex] = optionIndex;
    
    // Update UI to show selection
    const option = document.querySelector(`input[name="question-${questionIndex}"][value="${optionIndex}"]`);
    if (option) {
        option.checked = true;
    }
}

function startQuizTimer() {
    const timerElement = document.getElementById('quizTimer');
    if (!timerElement) return;
    
    const updateTimer = () => {
        if (!quizStartTime) return;
        
        const elapsed = Math.floor((new Date() - quizStartTime) / 1000);
        const minutes = Math.floor(elapsed / 60);
        const seconds = elapsed % 60;
        
        timerElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    };
    
    updateTimer();
    setInterval(updateTimer, 1000);
}

async function submitQuiz() {
    try {
        // Check if all questions are answered
        const unanswered = currentQuiz.questions.filter((_, index) => currentAnswers[index] === undefined);
        
        if (unanswered.length > 0) {
            if (!confirm(`You have ${unanswered.length} unanswered question(s). Submit anyway?`)) {
                return;
            }
        }
        
        const email = localStorage.getItem('userEmail');
        const submissionData = {
            answers: currentAnswers
        };
        
        const response = await authenticatedFetch(
            `http://localhost:8081/api/student/submit-quiz/${currentQuiz.lesson.id}?email=${encodeURIComponent(email)}`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(submissionData)
            }
        );
        
        if (!response.ok) {
            throw new Error(`Failed to submit quiz (${response.status})`);
        }
        
        const result = await response.json();
        
        // Show results
        showQuizResults(result);
        
    } catch (error) {
        console.error('Error submitting quiz:', error);
        alert('Failed to submit quiz: ' + error.message);
    }
}

function showQuizResults(result) {
    const score = Math.round(result.score);
    const passed = score >= 70;
    const timeTaken = Math.floor((new Date() - quizStartTime) / 1000);
    const minutes = Math.floor(timeTaken / 60);
    const seconds = timeTaken % 60;
    
    const resultsHtml = `
        <div class="quiz-results ${passed ? 'passed' : 'failed'}">
            <div class="results-header">
                <i class="fas ${passed ? 'fa-check-circle' : 'fa-times-circle'}"></i>
                <h3>${passed ? 'Congratulations!' : 'Keep Trying!'}</h3>
            </div>
            
            <div class="results-score">
                <div class="score-circle">
                    <span class="score-number">${score}%</span>
                </div>
                <p class="score-text">${passed ? 'You passed the quiz!' : 'You need 70% to pass.'}</p>
            </div>
            
            <div class="results-details">
                <div class="detail-item">
                    <i class="fas fa-question"></i>
                    <span>Questions: ${currentQuiz.questions.length}</span>
                </div>
                <div class="detail-item">
                    <i class="fas fa-check"></i>
                    <span>Correct: ${Math.round(score * currentQuiz.questions.length / 100)}</span>
                </div>
                <div class="detail-item">
                    <i class="fas fa-clock"></i>
                    <span>Time: ${minutes}m ${seconds}s</span>
                </div>
            </div>
            
            <div class="results-actions">
                ${!passed ? `
                    <button class="retry-btn" onclick="retryQuiz()">
                        <i class="fas fa-redo"></i> Try Again
                    </button>
                ` : ''}
                <button class="continue-btn" onclick="closeQuiz()">
                    <i class="fas fa-arrow-right"></i> Continue
                </button>
            </div>
        </div>
    `;
    
    // Replace quiz content with results
    const quizContent = document.querySelector('.quiz-content');
    if (quizContent) {
        quizContent.innerHTML = resultsHtml;
    }
}

function retryQuiz() {
    currentAnswers = {};
    quizStartTime = new Date();
    showQuizModal(currentQuiz, currentQuiz.title);
}

function closeQuiz() {
    const overlay = document.querySelector('.quiz-overlay');
    if (overlay) {
        overlay.remove();
        document.body.style.overflow = '';
    }
    
    // Reload lessons to show updated progress
    loadLessons();
}

async function completeLesson(lessonId, lessonTitle) {
    try {
        // In a real implementation, this could call an API to mark lesson as complete
        // For now, we'll just show a success message
        
        const confirmation = confirm(`Mark "${lessonTitle}" as complete?\n\nThis will update your progress.`);
        if (!confirmation) return;
        
        // Here you could add API call to mark lesson complete
        alert(`Great! "${lessonTitle}" has been marked as complete. Your progress has been updated.`);
        
        closeLessonViewer();
        loadLessons(); // Reload to show updated progress
        
    } catch (error) {
        console.error('Error completing lesson:', error);
        alert('Failed to complete lesson: ' + error.message);
    }
}

function closeLessonViewer() {
    const overlay = document.querySelector('.lesson-overlay');
    if (overlay) {
        overlay.remove();
        document.body.style.overflow = '';
    }
}

function extractYouTubeId(url) {
    if (!url) return '';
    
    // Handle different YouTube URL formats
    const patterns = [
        /(?:youtube\.com\/watch\?v=|youtu\.be\/|youtube\.com\/embed\/)([^&\n?#]+)/,
        /youtube\.com\/v\/([^&\n?#]+)/
    ];
    
    for (const pattern of patterns) {
        const match = url.match(pattern);
        if (match) return match[1];
    }
    
    return url; // Return as-is if no pattern matches
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function goBack() {
    window.location.href = '/student-home.html';
}