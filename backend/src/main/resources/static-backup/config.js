// Configuration for Static Frontend
class StaticConfig {
    constructor() {
        this.apiBaseUrl = this.getApiBaseUrl();
        this.environment = this.getEnvironment();
        this.enableDebug = this.shouldEnableDebug();
        
        if (this.enableDebug) {
            console.log('🔧 ThinkAble Static Config:', {
                apiBaseUrl: this.apiBaseUrl,
                environment: this.environment,
                hostname: window.location.hostname
            });
        }
    }

    getApiBaseUrl() {
        // Check if we're in production (not localhost)
        const hostname = window.location.hostname;
        
        if (hostname !== 'localhost' && hostname !== '127.0.0.1') {
            // Production environment
            return 'https://thinkable-backend.onrender.com';
        }
        
        // Development environment
        // Check for different common ports
        const port = window.location.port;
        if (port === '8080' || port === '8081') {
            return `http://localhost:${port}`;
        }
        
        // Default development URL
        return 'http://localhost:8080';
    }

    getEnvironment() {
        const hostname = window.location.hostname;
        return (hostname === 'localhost' || hostname === '127.0.0.1') ? 'development' : 'production';
    }

    shouldEnableDebug() {
        // Enable debug in development or if debug parameter is present
        return this.environment === 'development' || window.location.search.includes('debug=true');
    }

    // Get full API endpoint URL
    getApiUrl(endpoint) {
        // Ensure endpoint starts with /
        if (!endpoint.startsWith('/')) {
            endpoint = '/' + endpoint;
        }
        return this.apiBaseUrl + endpoint;
    }
}

// Create global config instance
const CONFIG = new StaticConfig();

// Enhanced authenticated fetch function
async function authenticatedFetch(endpoint, options = {}) {
    const url = CONFIG.getApiUrl(endpoint);
    
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        }
    };

    // Add auth token if available
    const token = localStorage.getItem('authToken') || localStorage.getItem('token');
    if (token) {
        defaultOptions.headers.Authorization = `Bearer ${token}`;
    }

    if (CONFIG.enableDebug) {
        console.log('🚀 API Request:', options.method || 'GET', url);
    }

    try {
        const response = await fetch(url, { ...defaultOptions, ...options });
        
        if (CONFIG.enableDebug) {
            console.log(`✅ API Response: ${response.status} ${url}`);
        }

        // Handle auth errors
        if (response.status === 401) {
            console.warn('🔒 Authentication failed, redirecting to login');
            localStorage.removeItem('authToken');
            localStorage.removeItem('token');
            localStorage.removeItem('userRole');
            localStorage.removeItem('userEmail');
            
            // Only redirect if not already on login page
            if (!window.location.pathname.includes('index.html') && 
                window.location.pathname !== '/' &&
                !window.location.pathname.includes('login')) {
                window.location.href = '/index.html';
            }
            throw new Error('Authentication failed');
        }

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return response;
    } catch (error) {
        if (CONFIG.enableDebug) {
            console.error('❌ API Error:', error.message, url);
        }
        throw error;
    }
}

// Specific API helper functions
const API = {
    // Authentication
    async login(email, password) {
        const response = await authenticatedFetch('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        return response.json();
    },

    async register(userData) {
        const response = await authenticatedFetch('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
        return response.json();
    },

    // Student endpoints
    async getStudentContent() {
        const response = await authenticatedFetch('/api/student/content/search');
        return response.json();
    },

    async getStudentQuiz(contentId, studentEmail) {
        const response = await authenticatedFetch(`/api/student/content/${contentId}/quiz?studentEmail=${studentEmail}`);
        return response.json();
    },

    async getStudentProgress() {
        const response = await authenticatedFetch('/api/student/progress');
        return response.json();
    },

    // Assessment endpoints
    async startAssessment(userId) {
        const response = await authenticatedFetch(`/api/assessment/start/${userId}`, {
            method: 'POST',
            body: JSON.stringify({})
        });
        return response.json();
    },

    async submitFontTest(userId, fontTestData) {
        const response = await authenticatedFetch(`/api/assessment/font-test/${userId}`, {
            method: 'POST',
            body: JSON.stringify(fontTestData)
        });
        return response.json();
    },

    async submitAssessment(userId, assessmentData) {
        const response = await authenticatedFetch(`/api/assessment/submit/${userId}`, {
            method: 'POST',
            body: JSON.stringify(assessmentData)
        });
        return response.json();
    },

    // AI recommendations (when implemented)
    async getAIRecommendations(userId) {
        try {
            const response = await authenticatedFetch(`/api/ai/recommendations/${userId}`);
            return response.json();
        } catch (error) {
            if (CONFIG.enableDebug) {
                console.log('AI recommendations not available yet');
            }
            return null;
        }
    },

    // Health check
    async healthCheck() {
        try {
            const response = await authenticatedFetch('/actuator/health');
            return response.ok;
        } catch {
            return false;
        }
    }
};

// Export for global use
window.CONFIG = CONFIG;
window.API = API;
window.authenticatedFetch = authenticatedFetch;