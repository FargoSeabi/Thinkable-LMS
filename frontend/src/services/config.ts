interface Config {
  apiBaseUrl: string;
  environment: 'development' | 'production';
  enableDebug: boolean;
}

class ConfigService {
  private config: Config;

  constructor() {
    this.config = {
      apiBaseUrl: this.getApiBaseUrl(),
      environment: (process.env.NODE_ENV as 'development' | 'production') || 'development',
      enableDebug: process.env.REACT_APP_ENABLE_DEBUG === 'true'
    };

    if (this.config.enableDebug) {
      console.log('🔧 ThinkAble Config:', this.config);
    }
  }

  private getApiBaseUrl(): string {
    // Priority order:
    // 1. Environment variable
    // 2. Auto-detect based on hostname
    // 3. Fallback to localhost

    // Check environment variable first
    if (process.env.REACT_APP_API_BASE_URL) {
      return process.env.REACT_APP_API_BASE_URL;
    }

    // Auto-detect based on hostname
    if (typeof window !== 'undefined') {
      const hostname = window.location.hostname;
      
      // If we're not on localhost, assume production
      if (hostname !== 'localhost' && hostname !== '127.0.0.1') {
        return 'https://thinkable-backend.onrender.com';
      }
    }

    // Development fallback
    return 'http://localhost:8081';
  }

  get apiBaseUrl(): string {
    return this.config.apiBaseUrl;
  }

  get environment(): string {
    return this.config.environment;
  }

  get isProduction(): boolean {
    return this.config.environment === 'production';
  }

  get isDevelopment(): boolean {
    return this.config.environment === 'development';
  }

  get enableDebug(): boolean {
    return this.config.enableDebug;
  }
}

export const config = new ConfigService();
export default config;