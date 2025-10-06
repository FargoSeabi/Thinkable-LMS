// Shared authentication utilities for all pages

// Check if user is authenticated
function isAuthenticated() {
    const token = localStorage.getItem('authToken');
    const expiry = localStorage.getItem('tokenExpiry');
    
    if (!token || !expiry) return false;
    
    return Date.now() < parseInt(expiry);
}

// Get authentication headers for API calls
function getAuthHeaders() {
    const token = localStorage.getItem('authToken');
    return token ? {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    } : {
        'Content-Type': 'application/json'
    };
}

// Logout function
function logout() {
    const userRole = localStorage.getItem('userRole');
    
    localStorage.removeItem('authToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('tokenExpiry');
    
    // Redirect to appropriate login page based on role
    if (userRole === 'TUTOR') {
        window.location.href = '/tutor-login.html';
    } else {
        window.location.href = '/index.html';
    }
}

// Require authentication for protected pages
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/index.html';
        return false;
    }
    return true;
}

// Require specific role
function requireRole(requiredRole) {
    if (!requireAuth()) return false;
    
    const userRole = localStorage.getItem('userRole');
    if (userRole !== requiredRole) {
        alert('Access denied: Insufficient permissions');
        logout();
        return false;
    }
    return true;
}

// Auto-refresh token when close to expiry
function autoRefreshToken() {
    const expiry = localStorage.getItem('tokenExpiry');
    if (!expiry) return;
    
    const timeUntilExpiry = parseInt(expiry) - Date.now();
    
    // If token is expired, logout instead of trying to refresh
    if (timeUntilExpiry <= 0) {
        console.warn('Token expired, logging out');
        logout();
    }
    // Note: Removed auto-refresh as the endpoint doesn't exist
}

// Refresh authentication token
async function refreshAuthToken() {
    try {
        const response = await authenticatedFetch('/api/auth/refresh', {
            method: 'POST',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            const { token, expiresIn } = await response.json();
            localStorage.setItem('authToken', token);
            localStorage.setItem('tokenExpiry', (Date.now() + expiresIn).toString());
        } else if (response.status === 404) {
            // Refresh endpoint doesn't exist, just continue with current token
            console.warn('Token refresh endpoint not available');
        } else {
            // If refresh fails with other errors, logout
            console.warn('Token refresh failed with status:', response.status);
            throw new Error('Refresh failed');
        }
    } catch (error) {
        console.error('Token refresh failed:', error);
        throw error; // Propagate error to caller
    }
}

// Initialize authentication check
function initAuth() {
    // For demo purposes, set up a temporary valid token if none exists
    if (!localStorage.getItem('authToken')) {
        // Set up a demo token that expires in 24 hours
        const demoToken = 'demo-token-' + Date.now();
        const expiry = Date.now() + (24 * 60 * 60 * 1000); // 24 hours
        localStorage.setItem('authToken', demoToken);
        localStorage.setItem('tokenExpiry', expiry.toString());
        localStorage.setItem('userEmail', 'demo@thinkable.com');
        localStorage.setItem('userRole', 'student');
    }
    
    // Disable periodic token refresh to prevent flickering
    // setInterval(autoRefreshToken, 5 * 60 * 1000);
}

// Display user info in navigation/header
function displayUserInfo() {
    const userEmail = localStorage.getItem('userEmail');
    const userRole = localStorage.getItem('userRole');
    
    if (userEmail && userRole) {
        const userInfoElements = document.querySelectorAll('.user-info');
        userInfoElements.forEach(element => {
            element.textContent = `${userEmail} (${userRole})`;
        });
        
        const userEmailElements = document.querySelectorAll('.user-email');
        userEmailElements.forEach(element => {
            element.textContent = userEmail;
        });
        
        const userRoleElements = document.querySelectorAll('.user-role');
        userRoleElements.forEach(element => {
            element.textContent = userRole;
        });
    }
}

// Add logout buttons functionality
function initLogoutButtons() {
    const logoutButtons = document.querySelectorAll('.logout-btn, [data-action="logout"]');
    logoutButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            if (confirm('Are you sure you want to logout?')) {
                logout();
            }
        });
    });
}

// Initialize auth when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initAuth();
    displayUserInfo();
    initLogoutButtons();
});

// Track token refresh attempts to prevent infinite loops
let refreshInProgress = false;
let refreshAttempts = 0;
const MAX_REFRESH_ATTEMPTS = 1;

// Make API calls with automatic authentication
async function authenticatedFetch(url, options = {}) {
    const authHeaders = getAuthHeaders();
    const config = {
        ...options,
        headers: {
            ...authHeaders,
            ...options.headers
        }
    };
    
    try {
        const response = await fetch(url, config);
        
        // If unauthorized, logout immediately (no refresh endpoint available)
        if (response.status === 401) {
            console.warn('Unauthorized request, logging out');
            logout();
            return response;
        }
        
        // Reset attempts on successful request
        if (response.ok) {
            refreshAttempts = 0;
        }
        
        return response;
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}