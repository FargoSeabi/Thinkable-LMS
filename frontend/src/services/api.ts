import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import config from './config';

interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

class ApiService {
  private axiosInstance: AxiosInstance;

  constructor() {
    this.axiosInstance = axios.create({
      baseURL: config.apiBaseUrl,
      timeout: 10000, // 10 second timeout
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor to add auth token
    this.axiosInstance.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('authToken') || localStorage.getItem('token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        if (process.env.NODE_ENV === 'development') {
          console.log('🚀 API Request:', config.method?.toUpperCase(), config.url);
        }

        return config;
      },
      (error) => {
        console.error('❌ API Request Error:', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor for error handling
    this.axiosInstance.interceptors.response.use(
      (response: AxiosResponse) => {
        if (process.env.NODE_ENV === 'development') {
          console.log('✅ API Response:', response.status, response.config.url);
        }
        return response;
      },
      (error) => {
        if (process.env.NODE_ENV === 'development') {
          console.error('❌ API Error:', error.response?.status, error.config?.url, error.response?.data);
        }

        // Handle common errors
        if (error.response?.status === 401) {
          // Token expired or invalid
          localStorage.removeItem('authToken');
          localStorage.removeItem('token');
          localStorage.removeItem('userRole');
          localStorage.removeItem('userEmail');
          
          // Only redirect if we're not already on login page
          if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/')) {
            window.location.href = '/login';
          }
        }

        return Promise.reject(error);
      }
    );
  }

  // Generic GET request
  async get<T = any>(endpoint: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.get(endpoint, config);
    return response.data;
  }

  // Generic POST request
  async post<T = any>(endpoint: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.post(endpoint, data, config);
    return response.data;
  }

  // Generic PUT request
  async put<T = any>(endpoint: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.put(endpoint, data, config);
    return response.data;
  }

  // Generic DELETE request
  async delete<T = any>(endpoint: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.delete(endpoint, config);
    return response.data;
  }

  // File upload helper
  async uploadFile<T = any>(endpoint: string, file: File, additionalData?: any): Promise<T> {
    const formData = new FormData();
    formData.append('file', file);
    
    if (additionalData) {
      Object.keys(additionalData).forEach(key => {
        formData.append(key, additionalData[key]);
      });
    }

    const response = await this.axiosInstance.post(endpoint, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response.data;
  }

  // Get current API base URL
  getBaseUrl(): string {
    return config.apiBaseUrl;
  }

  // Health check
  async healthCheck(): Promise<boolean> {
    try {
      await this.get('/actuator/health');
      return true;
    } catch {
      return false;
    }
  }
}

// Export singleton instance
export const apiService = new ApiService();
export default apiService;

// Export specific API methods for different modules
export const authAPI = {
  login: (email: string, password: string) => 
    apiService.post('/api/auth/login', { email, password }),
  register: (userData: any) => 
    apiService.post('/api/auth/register', userData),
  refreshToken: () => 
    apiService.post('/api/auth/refresh'),
  getCurrentUser: () => 
    apiService.get('/api/auth/me'),
};

export const studentAPI = {
  getContent: (params?: any) => 
    apiService.get('/api/student/content/search', { params }),
  getContentById: (id: number) => 
    apiService.get(`/api/student/content/${id}`),
  getQuiz: (contentId: number, studentEmail: string) => 
    apiService.get(`/api/student/content/${contentId}/quiz?studentEmail=${studentEmail}`),
  submitQuiz: (data: any) => 
    apiService.post('/api/student/quiz/submit', data),
  getProgress: () => 
    apiService.get('/api/student/progress'),
  getDashboard: () => 
    apiService.get('/api/student/dashboard'),
  // Favorites API
  getFavorites: (studentId: number) => 
    apiService.get(`/api/student/content/favorites/${studentId}`),
  toggleFavorite: (contentId: number, studentId: number) => 
    apiService.post(`/api/student/content/${contentId}/bookmark/${studentId}/toggle`),
  checkBookmarkStatus: (contentId: number, studentId: number) => 
    apiService.get(`/api/student/content/${contentId}/bookmark/${studentId}/status`),
};

export const assessmentAPI = {
  startAssessment: (userId: number) => 
    apiService.post(`/api/assessment/start/${userId}`),
  submitFontTest: (userId: number, fontTestData: any) => 
    apiService.post(`/api/assessment/font-test/${userId}`, fontTestData),
  submitAssessment: (userId: number, assessmentData: any) => 
    apiService.post(`/api/assessment/submit/${userId}`, assessmentData),
  getUserProfile: (userId: number) => 
    apiService.get(`/api/assessment/profile/${userId}`),
};