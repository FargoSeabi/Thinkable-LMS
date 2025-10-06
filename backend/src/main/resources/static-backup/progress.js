document.addEventListener('DOMContentLoaded', async function() {
    // Require student authentication
    if (!requireRole('STUDENT')) {
        return;
    }

    // Apply accessibility settings
    applyAccessibilitySettings();
    
    // Load progress data
    await loadProgress();
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

async function loadProgress() {
    const progressContainer = document.getElementById('progressDetails');
    const progressMessage = document.getElementById('progressMessage');
    const progressBar = document.getElementById('progressBar');
    const progressText = document.getElementById('progressText');
    const email = localStorage.getItem('userEmail');

    try {
        // Show loading state
        if (progressContainer) {
            progressContainer.innerHTML = `
                <div class="loading-container">
                    <i class="fas fa-spinner fa-spin"></i>
                    <p>Loading your progress...</p>
                </div>
            `;
        }

        const response = await authenticatedFetch(`http://localhost:8081/api/student/progress?email=${encodeURIComponent(email)}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch progress (${response.status})`);
        }

        const progress = await response.json();
        
        // Update progress bar
        const percentage = Math.min(progress.percentage || 0, 100);
        if (progressBar) {
            progressBar.style.width = `${percentage}%`;
            progressBar.style.background = getProgressColor(percentage);
        }
        if (progressText) {
            progressText.textContent = `${percentage}%`;
        }

        // Display detailed progress
        if (progressContainer) {
            progressContainer.innerHTML = `
                <div class="progress-stats">
                    <div class="stat-card">
                        <div class="stat-icon">
                            <i class="fas fa-percentage"></i>
                        </div>
                        <div class="stat-content">
                            <h3>${percentage}%</h3>
                            <p>Overall Progress</p>
                        </div>
                    </div>
                    
                    <div class="stat-card">
                        <div class="stat-icon">
                            <i class="fas fa-check-circle"></i>
                        </div>
                        <div class="stat-content">
                            <h3>${progress.completedQuizzes || 0}</h3>
                            <p>Quizzes Completed</p>
                        </div>
                    </div>
                    
                    <div class="stat-card">
                        <div class="stat-icon">
                            <i class="fas fa-book-open"></i>
                        </div>
                        <div class="stat-content">
                            <h3>${progress.completedLessons || 0}</h3>
                            <p>Lessons Available</p>
                        </div>
                    </div>
                    
                    <div class="stat-card">
                        <div class="stat-icon">
                            <i class="fas fa-trophy"></i>
                        </div>
                        <div class="stat-content">
                            <h3>${calculateLevel(percentage)}</h3>
                            <p>Current Level</p>
                        </div>
                    </div>
                </div>

                <div class="progress-achievements">
                    <h3><i class="fas fa-medal"></i> Achievements</h3>
                    <div class="achievements-grid">
                        ${generateAchievements(progress)}
                    </div>
                </div>

                <div class="progress-chart">
                    <h3><i class="fas fa-chart-line"></i> Learning Activity</h3>
                    <div class="chart-placeholder">
                        <p>Activity chart would appear here showing daily/weekly progress</p>
                        <div class="mock-chart">
                            ${generateMockChart()}
                        </div>
                    </div>
                </div>
            `;
        }

        if (progressMessage) {
            progressMessage.textContent = 'Progress loaded successfully';
            progressMessage.className = 'message success';
        }

    } catch (error) {
        console.error('Error loading progress:', error);
        
        if (progressContainer) {
            progressContainer.innerHTML = `
                <div class="error-container">
                    <i class="fas fa-exclamation-triangle"></i>
                    <h3>Error Loading Progress</h3>
                    <p>${error.message}</p>
                    <button onclick="loadProgress()" class="retry-btn">
                        <i class="fas fa-retry"></i> Try Again
                    </button>
                </div>
            `;
        }

        if (progressMessage) {
            progressMessage.textContent = 'Error loading progress: ' + error.message;
            progressMessage.className = 'message error';
        }
    }
}

function getProgressColor(percentage) {
    if (percentage >= 75) return '#2ecc71'; // Green
    if (percentage >= 50) return '#f39c12'; // Orange
    if (percentage >= 25) return '#e67e22'; // Dark orange
    return '#e74c3c'; // Red
}

function calculateLevel(percentage) {
    return Math.floor(percentage / 20) + 1; // Level 1-6 based on progress
}

function generateAchievements(progress) {
    const achievements = [];
    
    // First Login
    achievements.push({
        icon: 'fas fa-user-plus',
        title: 'Welcome!',
        description: 'Started your learning journey',
        earned: true
    });
    
    // First Quiz
    if ((progress.completedQuizzes || 0) > 0) {
        achievements.push({
            icon: 'fas fa-question-circle',
            title: 'Quiz Master',
            description: 'Completed your first quiz',
            earned: true
        });
    }
    
    // Progress milestones
    if ((progress.percentage || 0) >= 25) {
        achievements.push({
            icon: 'fas fa-star',
            title: 'Getting Started',
            description: 'Reached 25% progress',
            earned: true
        });
    }
    
    if ((progress.percentage || 0) >= 50) {
        achievements.push({
            icon: 'fas fa-star-half-alt',
            title: 'Halfway There',
            description: 'Reached 50% progress',
            earned: true
        });
    }
    
    if ((progress.percentage || 0) >= 75) {
        achievements.push({
            icon: 'fas fa-crown',
            title: 'Almost Done',
            description: 'Reached 75% progress',
            earned: true
        });
    }
    
    if ((progress.percentage || 0) >= 100) {
        achievements.push({
            icon: 'fas fa-trophy',
            title: 'Course Complete',
            description: 'Completed the entire course',
            earned: true
        });
    }

    return achievements.map(achievement => `
        <div class="achievement-card ${achievement.earned ? 'earned' : 'locked'}">
            <div class="achievement-icon">
                <i class="${achievement.icon}"></i>
            </div>
            <div class="achievement-content">
                <h4>${achievement.title}</h4>
                <p>${achievement.description}</p>
            </div>
        </div>
    `).join('');
}

function generateMockChart() {
    // Generate a simple mock activity chart
    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const heights = [20, 35, 45, 30, 60, 25, 40]; // Mock activity levels
    
    return `
        <div class="mock-bar-chart">
            ${days.map((day, index) => `
                <div class="chart-bar">
                    <div class="bar" style="height: ${heights[index]}%"></div>
                    <span class="bar-label">${day}</span>
                </div>
            `).join('')}
        </div>
    `;
}

function goBack() {
    window.history.back();
}