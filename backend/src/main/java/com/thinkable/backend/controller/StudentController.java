package com.thinkable.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thinkable.backend.model.Leaderboard;
import com.thinkable.backend.model.Quiz;
import com.thinkable.backend.model.User;
import com.thinkable.backend.model.Lesson;
import com.thinkable.backend.model.LessonProgress;
import com.thinkable.backend.repository.LeaderboardRepository;
import com.thinkable.backend.repository.LessonRepository;
import com.thinkable.backend.repository.QuizRepository;
import com.thinkable.backend.repository.UserRepository;
import com.thinkable.backend.repository.LessonProgressRepository;
import com.thinkable.backend.repository.LearningContentRepository;
import com.thinkable.backend.entity.LearningContent;
import com.thinkable.backend.service.PDFTextExtractionService;
import com.thinkable.backend.controller.JwtUtil;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private QuizRepository quizRepository;


    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PDFTextExtractionService pdfTextExtractionService;

    @Autowired
    private LearningContentRepository learningContentRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam String email) {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            return ResponseEntity.ok(new ProfileResponse(user.getName(), user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error fetching profile: " + e.getMessage()));
        }
    }

    @GetMapping("/lessons")
    public ResponseEntity<?> getLessons(@RequestParam String email) {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            return ResponseEntity.ok(lessonRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error fetching lessons: " + e.getMessage()));
        }
    }

    @GetMapping("/quiz/{lessonId}")
    public ResponseEntity<?> getQuiz(@PathVariable Long lessonId, @RequestParam String email) {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            Quiz quiz = quizRepository.findByLessonId(lessonId);
            if (quiz == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("Quiz not found for lesson"));
            }
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error fetching quiz: " + e.getMessage()));
        }
    }

    @PostMapping("/submit-quiz/{lessonId}")
    public ResponseEntity<?> submitQuiz(@RequestParam String email,
                                       @PathVariable Long lessonId,
                                       @RequestBody QuizSubmissionRequest request) {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            Quiz quiz = quizRepository.findByLessonId(lessonId);
            if (quiz == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("Quiz not found"));
            }

            int correctAnswers = 0;
            for (int i = 0; i < quiz.getQuestions().size(); i++) {
                Integer userAnswer = request.answers.get(String.valueOf(i));
                if (userAnswer != null && Objects.equals(userAnswer, quiz.getQuestions().get(i).getCorrectOption())) {
                    correctAnswers++;
                }
            }
            double score = (double) correctAnswers / quiz.getQuestions().size() * 100;

            // Save to leaderboard
            Leaderboard leaderboard = new Leaderboard();
            leaderboard.setUser(user);
            leaderboard.setQuiz(quiz);
            leaderboard.setScore((int) score);
            leaderboard.setSubmittedAt(java.time.LocalDateTime.now());
            leaderboardRepository.save(leaderboard);

            // Update lesson progress
            Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
            if (lesson != null) {
                LessonProgress progress = lessonProgressRepository.findByUserAndLesson(user, lesson)
                    .orElse(new LessonProgress(user, lesson));
                
                progress.setQuizScore((int) score);
                progress.setQuizAttempts(progress.getQuizAttempts() + 1);
                
                // Mark as completed if score >= 70%
                if (score >= 70) {
                    progress.setCompleted(true);
                }
                
                lessonProgressRepository.save(progress);
            }

            // Update user overall progress
            Integer currentProgress = user.getProgress();
            user.setProgress(currentProgress != null ? currentProgress + (int) (score / 10) : (int) (score / 10));
            userRepository.save(user);

            return ResponseEntity.ok(new QuizSubmissionResponse(score, score >= 70));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error submitting quiz: " + e.getMessage()));
        }
    }


    @PostMapping("/start-lesson/{lessonId}")
    public ResponseEntity<?> startLesson(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable Long lessonId) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            
            Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
            if (lesson == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("Lesson not found"));
            }
            
            // Get or create lesson progress
            LessonProgress progress = lessonProgressRepository.findByUserAndLesson(user, lesson)
                .orElse(new LessonProgress(user, lesson));
            
            lessonProgressRepository.save(progress);
            
            return ResponseEntity.ok(new LessonStartResponse(progress.getId(), "Lesson started successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error starting lesson: " + e.getMessage()));
        }
    }

    @PostMapping("/complete-lesson/{lessonId}")
    public ResponseEntity<?> completeLesson(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable Long lessonId,
                                          @RequestBody LessonCompletionRequest request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            
            Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
            if (lesson == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("Lesson not found"));
            }
            
            LessonProgress progress = lessonProgressRepository.findByUserAndLesson(user, lesson)
                .orElse(new LessonProgress(user, lesson));
            
            progress.setTimeSpentMinutes(request.timeSpentMinutes);
            progress.setCompleted(true);
            
            lessonProgressRepository.save(progress);
            
            return ResponseEntity.ok(new LessonCompletionResponse("Lesson completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error completing lesson: " + e.getMessage()));
        }
    }

    @GetMapping("/progress")
    public ResponseEntity<?> getProgress(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            
            Long completedLessons = lessonProgressRepository.countCompletedLessons(user);
            Long totalLessons = lessonRepository.count();
            Double averageQuizScore = lessonProgressRepository.getAverageQuizScore(user);
            Long totalTimeSpent = lessonProgressRepository.getTotalTimeSpent(user);
            Long completedQuizzes = leaderboardRepository.countByUserId(user.getId());
            
            List<LessonProgress> recentProgress = lessonProgressRepository.findByUserOrderByLastAccessedDesc(user);
            
            return ResponseEntity.ok(new EnhancedProgressResponse(
                completedLessons.intValue(),
                totalLessons.intValue(),
                averageQuizScore != null ? averageQuizScore : 0.0,
                totalTimeSpent != null ? totalTimeSpent.intValue() : 0,
                completedQuizzes.intValue(),
                recentProgress
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error fetching progress: " + e.getMessage()));
        }
    }

    @GetMapping("/lesson-progress/{lessonId}")
    public ResponseEntity<?> getLessonProgress(@RequestHeader("Authorization") String authHeader,
                                             @PathVariable Long lessonId) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            
            Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
            if (lesson == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("Lesson not found"));
            }
            
            LessonProgress progress = lessonProgressRepository.findByUserAndLesson(user, lesson).orElse(null);
            
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error fetching lesson progress: " + e.getMessage()));
        }
    }

    // Content Text Extraction Endpoints
    
    @GetMapping("/content/{contentId}/extract-text")
    public ResponseEntity<?> extractContentText(@RequestHeader("Authorization") String authHeader,
                                              @PathVariable Long contentId) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            
            LearningContent content = learningContentRepository.findById(contentId).orElse(null);
            if (content == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("Content not found"));
            }
            
            // Extract text based on content type
            String extractedText = "";
            if (content.getFileName().toLowerCase().endsWith(".pdf")) {
                PDFTextExtractionService.PDFFullText result = pdfTextExtractionService.extractFullText(content.getFileName());
                if (result.isSuccess()) {
                    // Combine all page texts into a single string
                    StringBuilder allText = new StringBuilder();
                    for (int i = 1; i <= result.getTotalPages(); i++) {
                        String pageText = result.getPagesText().get(i);
                        if (pageText != null) {
                            allText.append(pageText).append("\n\n");
                        }
                    }
                    extractedText = allText.toString().trim();
                } else {
                    return ResponseEntity.status(400).body(new ErrorResponse("Failed to extract text from PDF: " + result.getError()));
                }
            } else {
                // For non-PDF content, return placeholder or basic text
                extractedText = "Text extraction for this content type is not yet supported. Content: " + content.getTitle();
            }
            
            return ResponseEntity.ok(new TextExtractionResponse(
                contentId,
                content.getTitle(),
                extractedText,
                content.getMimeType()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error extracting content text: " + e.getMessage()));
        }
    }

    // PDF Text Extraction Endpoints

    @GetMapping("/pdf/text/{fileName}")
    public ResponseEntity<?> getPdfFullText(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable String fileName) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            
            PDFTextExtractionService.PDFFullText result = pdfTextExtractionService.extractFullText(fileName);
            
            if (!result.isSuccess()) {
                return ResponseEntity.status(400).body(new ErrorResponse(result.getError()));
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error extracting PDF text: " + e.getMessage()));
        }
    }

    @GetMapping("/pdf/text/{fileName}/page/{pageNumber}")
    public ResponseEntity<?> getPdfPageText(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable String fileName,
                                          @PathVariable int pageNumber) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            
            PDFTextExtractionService.PDFPageText result = pdfTextExtractionService.extractPageText(fileName, pageNumber);
            
            if (!result.isSuccess()) {
                return ResponseEntity.status(400).body(new ErrorResponse(result.getError()));
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error extracting page text: " + e.getMessage()));
        }
    }

    @GetMapping("/pdf/metadata/{fileName}")
    public ResponseEntity<?> getPdfMetadata(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable String fileName) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            
            PDFTextExtractionService.PDFMetadata result = pdfTextExtractionService.getPDFMetadata(fileName);
            
            if (!result.isSuccess()) {
                return ResponseEntity.status(400).body(new ErrorResponse(result.getError()));
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error getting PDF metadata: " + e.getMessage()));
        }
    }

    @GetMapping("/pdf/text/{fileName}/dyslexia-friendly")
    public ResponseEntity<?> getDyslexiaFriendlyText(@RequestHeader("Authorization") String authHeader,
                                                   @PathVariable String fileName,
                                                   @RequestParam(defaultValue = "1") int pageNumber) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }
            
            // Check if user needs dyslexia-friendly rendering based on assessment
            boolean needsDyslexiaMode = checkUserNeedsDyslexiaMode(user);
            
            PDFTextExtractionService.PDFPageText result = pdfTextExtractionService.extractPageText(fileName, pageNumber);
            
            if (!result.isSuccess()) {
                return ResponseEntity.status(400).body(new ErrorResponse(result.getError()));
            }
            
            // Create dyslexia-friendly response
            return ResponseEntity.ok(new DyslexiaFriendlyTextResponse(
                result.getText(),
                result.getPageNumber(),
                result.getTotalPages(),
                needsDyslexiaMode,
                formatForDyslexia(result.getText(), needsDyslexiaMode)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error creating dyslexia-friendly text: " + e.getMessage()));
        }
    }

    /**
     * Check if user needs dyslexia-friendly mode based on assessment data
     */
    private boolean checkUserNeedsDyslexiaMode(User user) {
        try {
            // Check if user has dyslexia indicators from assessment
            String assessmentScores = user.getAssessmentScores();
            if (assessmentScores != null && !assessmentScores.isEmpty()) {
                // Simple check - in real implementation would use more sophisticated analysis
                return assessmentScores.toLowerCase().contains("reading") || 
                       assessmentScores.toLowerCase().contains("text") ||
                       (user.getPreferences() != null && user.getPreferences().toLowerCase().contains("dyslexia"));
            }
            
            // Also check user preferences
            String preferences = user.getPreferences();
            if (preferences != null) {
                return preferences.toLowerCase().contains("dyslexia") ||
                       preferences.toLowerCase().contains("reading_difficulty");
            }
            
            return false;
        } catch (Exception e) {
            return false; // Default to false on error
        }
    }

    /**
     * Format text for dyslexia-friendly display
     */
    private String formatForDyslexia(String originalText, boolean needsDyslexiaMode) {
        if (!needsDyslexiaMode || originalText == null) {
            return originalText;
        }
        
        return originalText
            // Add extra spacing between sentences
            .replaceAll("\\. ", ".   ")
            .replaceAll("\\! ", "!   ")
            .replaceAll("\\? ", "?   ")
            // Break up long sentences (over 15 words)
            .replaceAll("([.!?])\\s+", "$1\n\n")
            // Add spacing between long words (over 8 characters)
            .replaceAll("\\b(\\w{8,})\\b", " $1 ")
            // Replace multiple spaces with single space
            .replaceAll("\\s+", " ")
            // Add paragraph breaks for better readability
            .replaceAll("([.!?]\\s*){2,}", "$1\n\n")
            // Trim whitespace
            .trim();
    }

    public static class DyslexiaFriendlyTextResponse {
        public String originalText;
        public int pageNumber;
        public int totalPages;
        public boolean dyslexiaModeRecommended;
        public String formattedText;
        
        public DyslexiaFriendlyTextResponse(String originalText, int pageNumber, int totalPages, 
                                          boolean dyslexiaModeRecommended, String formattedText) {
            this.originalText = originalText;
            this.pageNumber = pageNumber;
            this.totalPages = totalPages;
            this.dyslexiaModeRecommended = dyslexiaModeRecommended;
            this.formattedText = formattedText;
        }
    }

    public static class ProfileResponse {
        public String name;
        public String email;

        public ProfileResponse(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }

    public static class QuizSubmissionRequest {
        public Map<String, Integer> answers;
    }

    public static class QuizSubmissionResponse {
        public double score;
        public boolean passed;

        public QuizSubmissionResponse(double score, boolean passed) {
            this.score = score;
            this.passed = passed;
        }
    }

    public static class ProgressResponse {
        public int percentage;
        public int completedQuizzes;
        public int completedLessons;

        public ProgressResponse(int percentage, int completedQuizzes, int completedLessons) {
            this.percentage = percentage;
            this.completedQuizzes = completedQuizzes;
            this.completedLessons = completedLessons;
        }
    }

    public static class EnhancedProgressResponse {
        public int completedLessons;
        public int totalLessons;
        public double averageQuizScore;
        public int totalTimeSpentMinutes;
        public int completedQuizzes;
        public List<LessonProgress> recentProgress;

        public EnhancedProgressResponse(int completedLessons, int totalLessons, double averageQuizScore,
                                      int totalTimeSpentMinutes, int completedQuizzes, List<LessonProgress> recentProgress) {
            this.completedLessons = completedLessons;
            this.totalLessons = totalLessons;
            this.averageQuizScore = averageQuizScore;
            this.totalTimeSpentMinutes = totalTimeSpentMinutes;
            this.completedQuizzes = completedQuizzes;
            this.recentProgress = recentProgress;
        }
    }

    public static class LessonStartResponse {
        public Long progressId;
        public String message;

        public LessonStartResponse(Long progressId, String message) {
            this.progressId = progressId;
            this.message = message;
        }
    }

    public static class LessonCompletionRequest {
        public int timeSpentMinutes;
    }

    public static class LessonCompletionResponse {
        public String message;

        public LessonCompletionResponse(String message) {
            this.message = message;
        }
    }

    public static class TextExtractionResponse {
        public Long contentId;
        public String title;
        public String extractedText;
        public String mimeType;

        public TextExtractionResponse(Long contentId, String title, String extractedText, String mimeType) {
            this.contentId = contentId;
            this.title = title;
            this.extractedText = extractedText;
            this.mimeType = mimeType;
        }
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
