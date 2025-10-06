// Utility functions for accessibility
function toggleDyslexiaMode(enabled) {
    if (enabled && !document.querySelector('link[href="https://fonts.cdnfonts.com/css/open-dyslexic"]')) {
        const link = document.createElement('link');
        link.href = 'https://fonts.cdnfonts.com/css/open-dyslexic';
        link.rel = 'stylesheet';
        document.head.appendChild(link);
    }
    document.body.classList.toggle('dyslexia-mode', enabled);
    localStorage.setItem('dyslexiaMode', enabled);
    showSettingsMessage('Dyslexia mode ' + (enabled ? 'enabled' : 'disabled'));
}

function toggleHighContrastMode(enabled) {
    document.body.classList.toggle('high-contrast', enabled);
    localStorage.setItem('highContrastMode', enabled);
    showSettingsMessage('High-contrast mode ' + (enabled ? 'enabled' : 'disabled'));
}

function showSettingsMessage(text) {
    const message = document.getElementById('settingsMessage');
    if (message) {
        message.textContent = text;
        message.className = 'message success';
        setTimeout(() => {
            message.textContent = '';
            message.className = 'message';
        }, 3000);
    }
}

// Authentication utilities
function isAuthenticated() {
    const token = localStorage.getItem('authToken');
    const expiry = localStorage.getItem('tokenExpiry');
    
    if (!token || !expiry) return false;
    
    return Date.now() < parseInt(expiry);
}

function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('tokenExpiry');
    window.location.href = '/index.html';
}

function getAuthHeaders() {
    const token = localStorage.getItem('authToken');
    return token ? {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    } : {
        'Content-Type': 'application/json'
    };
}


document.addEventListener('DOMContentLoaded', function() {
    // Only perform authentication redirect on login/index pages
    const currentPage = window.location.pathname;
    const loginPages = ['/', '/index.html', 'index.html'];
    
    if (loginPages.includes(currentPage) || loginPages.includes(currentPage.substring(1))) {
        // Check if user is already authenticated and redirect to appropriate dashboard
        if (isAuthenticated()) {
            const userRole = localStorage.getItem('userRole');
            
            if (userRole === 'ADMIN') {
                window.location.href = '/admin.html';
                return;
            } else if (userRole === 'STUDENT') {
                window.location.href = '/student-home.html';
                return;
            } else if (userRole === 'TUTOR') {
                window.location.href = '/tutor-dashboard.html';
                return;
            }
        }
    }

    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;
            const errorMessage = document.getElementById('errorMessage');
            const loginBtn = document.querySelector('.login-btn');
            
            // Clear previous errors and show loading state
            errorMessage.textContent = '';
            loginBtn.disabled = true;
            loginBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Logging in...';
            
            try {
                // Basic validation
                if (!email || !password) {
                    throw new Error('Email and password are required');
                }
                
                if (!email.includes('@')) {
                    throw new Error('Please enter a valid email address');
                }

                const loginData = await API.login(email, password);
                
                // API.login already handles the response parsing and error checking
                let { token, role, email: userEmail, expiresIn } = loginData;

                // Handle expiresIn parsing
                if (typeof expiresIn !== 'number') {
                    expiresIn = parseInt(expiresIn);
                    if (isNaN(expiresIn)) {
                        console.warn('Invalid expiresIn value, using default (24 hours)');
                        expiresIn = 24 * 60 * 60 * 1000;
                    }
                }

                // Store authentication data
                localStorage.setItem('authToken', token);
                localStorage.setItem('userRole', role);
                localStorage.setItem('userEmail', userEmail);
                localStorage.setItem('tokenExpiry', (Date.now() + expiresIn).toString());
                
                // Show success and redirect
                loginBtn.innerHTML = '<i class="fas fa-check"></i> Success! Redirecting...';
                
                setTimeout(() => {
                    if (role === 'ADMIN') {
                        window.location.href = '/admin.html';
                    } else if (role === 'STUDENT') {
                        window.location.href = '/student-home.html';
                    } else if (role === 'TUTOR') {
                        window.location.href = '/tutor-dashboard.html';
                    }
                }, 1000);
                
            } catch (error) {
                console.error('Login error:', error);
                errorMessage.textContent = error.message || 'Login failed. Please try again.';
                
                // Clear any partial auth data
                localStorage.removeItem('authToken');
                localStorage.removeItem('userRole');
                localStorage.removeItem('userEmail');
                localStorage.removeItem('tokenExpiry');
                
                // Reset button state
                loginBtn.disabled = false;
                loginBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Login';
            }
        });
    }

    // Accessibility toggles
    const dyslexiaToggle = document.getElementById('dyslexiaToggle');
    if (dyslexiaToggle) {
        dyslexiaToggle.addEventListener('click', function() {
            const currentMode = document.body.classList.contains('dyslexia-mode');
            toggleDyslexiaMode(!currentMode);
        });
    }

    const highContrastToggle = document.getElementById('highContrastToggle');
    if (highContrastToggle) {
        highContrastToggle.addEventListener('click', function() {
            const currentMode = document.body.classList.contains('high-contrast');
            toggleHighContrastMode(!currentMode);
        });
    }

    const readPageBtn = document.getElementById('readPage');
    if (readPageBtn) {
        readPageBtn.addEventListener('click', function() {
            // Stop any currently playing speech
            window.speechSynthesis.cancel();
            
            // Get readable text content (exclude UI elements)
            const mainContent = document.querySelector('.login-card') || document.body;
            const text = mainContent.innerText;
            
            if (!text.trim()) {
                showSettingsMessage('No text content to read');
                return;
            }
            
            const utterance = new SpeechSynthesisUtterance(text);
            utterance.lang = 'en-US';
            utterance.rate = 0.8; // Slightly slower for accessibility
            utterance.pitch = 1.0;
            
            utterance.onstart = () => {
                readPageBtn.innerHTML = '<i class="fas fa-stop"></i> Stop Reading';
                readPageBtn.classList.add('reading');
            };
            
            utterance.onend = () => {
                readPageBtn.innerHTML = '<i class="fas fa-volume-up"></i> Read Page';
                readPageBtn.classList.remove('reading');
            };
            
            utterance.onerror = (event) => {
                console.error('Speech synthesis error:', event);
                showSettingsMessage('Failed to read page content');
                readPageBtn.innerHTML = '<i class="fas fa-volume-up"></i> Read Page';
                readPageBtn.classList.remove('reading');
            };
            
            window.speechSynthesis.speak(utterance);
        });
    }
});