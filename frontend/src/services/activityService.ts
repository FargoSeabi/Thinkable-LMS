import config from './config';

export interface ActivityRequest {
  userId: number;
  lessonId?: string;
  quizId?: string;
  score?: number;
  maxScore?: number;
  durationMinutes?: number;
  activityType?: string;
  action?: string;
  accessibilityTools?: string[];
}

export interface ActivityResponse {
  success: boolean;
  message: string;
  sessionId?: number;
  scorePercentage?: number;
  currentStreak?: number;
}

class ActivityService {
  private readonly apiUrl = config.apiBaseUrl;

  /**
   * Record a lesson completion
   */
  async recordLessonCompletion(request: ActivityRequest): Promise<ActivityResponse> {
    try {
      const response = await fetch(`${this.apiUrl}/api/activity/lesson-completed`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: request.userId,
          lessonId: request.lessonId,
          durationMinutes: request.durationMinutes || 15,
          accessibilityTools: request.accessibilityTools || []
        }),
      });

      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Failed to record lesson completion');
      }

      return data;
    } catch (error) {
      console.error('Error recording lesson completion:', error);
      throw error;
    }
  }

  /**
   * Record a quiz completion
   */
  async recordQuizCompletion(request: ActivityRequest): Promise<ActivityResponse> {
    try {
      const response = await fetch(`${this.apiUrl}/api/activity/quiz-completed`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: request.userId,
          quizId: request.quizId,
          score: request.score,
          maxScore: request.maxScore || 100,
          durationMinutes: request.durationMinutes || 10,
          accessibilityTools: request.accessibilityTools || []
        }),
      });

      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Failed to record quiz completion');
      }

      return data;
    } catch (error) {
      console.error('Error recording quiz completion:', error);
      throw error;
    }
  }

  /**
   * Record a general study session
   */
  async recordStudySession(request: ActivityRequest): Promise<ActivityResponse> {
    try {
      const response = await fetch(`${this.apiUrl}/api/activity/study-session`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: request.userId,
          activityType: request.activityType || 'PRACTICE_SESSION',
          durationMinutes: request.durationMinutes || 5,
          accessibilityTools: request.accessibilityTools || []
        }),
      });

      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Failed to record study session');
      }

      return data;
    } catch (error) {
      console.error('Error recording study session:', error);
      throw error;
    }
  }

  /**
   * Record quick activities (reading, watching, exploring)
   */
  async recordQuickActivity(userId: number, action: string, durationMinutes: number = 2): Promise<ActivityResponse> {
    try {
      const response = await fetch(`${this.apiUrl}/api/activity/quick-activity`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId,
          action,
          durationMinutes
        }),
      });

      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Failed to record activity');
      }

      return data;
    } catch (error) {
      console.error('Error recording quick activity:', error);
      throw error;
    }
  }

  /**
   * Get user's current streak
   */
  async getCurrentStreak(userId: number): Promise<any> {
    try {
      const response = await fetch(`${this.apiUrl}/api/activity/user/${userId}/streak`);
      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Failed to get streak');
      }

      return data;
    } catch (error) {
      console.error('Error getting current streak:', error);
      throw error;
    }
  }

  /**
   * Get user's activity summary
   */
  async getActivitySummary(userId: number, days: number = 7): Promise<any> {
    try {
      const response = await fetch(`${this.apiUrl}/api/activity/user/${userId}/summary?days=${days}`);
      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Failed to get activity summary');
      }

      return data;
    } catch (error) {
      console.error('Error getting activity summary:', error);
      throw error;
    }
  }

  /**
   * Easy methods for common tracking scenarios
   */

  // Track when user spends time reading content
  async trackReading(userId: number, durationMinutes: number = 3) {
    return this.recordQuickActivity(userId, 'read', durationMinutes);
  }

  // Track when user watches a video
  async trackVideoWatching(userId: number, durationMinutes: number = 5) {
    return this.recordQuickActivity(userId, 'watch', durationMinutes);
  }

  // Track when user explores content discovery features
  async trackContentExploration(userId: number, durationMinutes: number = 2) {
    return this.recordQuickActivity(userId, 'explore', durationMinutes);
  }

  // Track any general interaction
  async trackInteraction(userId: number, durationMinutes: number = 1) {
    return this.recordQuickActivity(userId, 'interact', durationMinutes);
  }

  // Track lesson completion with accessibility tools
  async trackLessonWithTools(userId: number, lessonId: string, durationMinutes: number, tools: string[]) {
    return this.recordLessonCompletion({
      userId,
      lessonId,
      durationMinutes,
      accessibilityTools: tools
    });
  }

  // Track quiz completion with score
  async trackQuizWithScore(userId: number, quizId: string, score: number, maxScore: number = 100, durationMinutes: number = 10) {
    return this.recordQuizCompletion({
      userId,
      quizId,
      score,
      maxScore,
      durationMinutes
    });
  }
}

export const activityService = new ActivityService();
export default activityService;