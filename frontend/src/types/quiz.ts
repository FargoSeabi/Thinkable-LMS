export interface Question {
  id?: number;
  question: string;
  options: string[];
  correctOption?: number; // For display from backend
  correctAnswer: number; // For creation/editing - always required
  tempId?: string; // For temporary IDs during creation
}

export interface Quiz {
  id?: number;
  title: string;
  aiGenerated?: boolean;
  learningContentId?: number;
  contentId?: number; // Alternative name for learningContentId
  questions: Question[];
  createdAt?: string;
  lesson?: any;
  book?: any;
}

export interface LearningContent {
  id: number;
  title: string;
  contentType: string;
  status: string;
}

export interface QuizSubmissionRequest {
  userId: number;
  answers: { [questionId: number]: number };
  durationMinutes?: number;
  accessibilityTools?: string[];
}

export interface QuizSubmissionResponse {
  success: boolean;
  score: number;
  correctAnswers: number;
  totalQuestions: number;
  passed: boolean;
  currentStreak: number;
}