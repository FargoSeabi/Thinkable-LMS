package com.thinkable.backend.controller;

import com.thinkable.backend.model.Quiz;
import com.thinkable.backend.model.Question;
import com.thinkable.backend.service.ActivityTrackingService;
import com.thinkable.backend.service.AIQuizGenerationService;
import com.thinkable.backend.repository.QuizRepository;
import com.thinkable.backend.repository.QuestionRepository;
import com.thinkable.backend.repository.LeaderboardRepository;
import com.thinkable.backend.entity.LearningContent;
import com.thinkable.backend.repository.LearningContentRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {
    
    private static final Logger logger = LoggerFactory.getLogger(QuizController.class);
    
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final LearningContentRepository learningContentRepository;
    private final ActivityTrackingService activityTrackingService;
    private final AIQuizGenerationService aiQuizGenerationService;
    
    /**
     * Get quiz by ID
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<Map<String, Object>> getQuiz(@PathVariable Long quizId) {
        try {
            Optional<Quiz> quiz = quizRepository.findById(quizId);
            if (quiz.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quiz", quiz.get());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching quiz {}: {}", quizId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch quiz");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get quizzes for learning content
     */
    @GetMapping("/content/{contentId}")
    public ResponseEntity<Map<String, Object>> getContentQuizzes(@PathVariable Long contentId) {
        try {
            List<Quiz> quizzes = quizRepository.findByLearningContentId(contentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quizzes", quizzes);
            response.put("count", quizzes.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching quizzes for content {}: {}", contentId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch content quizzes");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Submit quiz answers and record completion
     */
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<Map<String, Object>> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizSubmissionRequest request) {
        try {
            Optional<Quiz> quizOpt = quizRepository.findById(quizId);
            if (quizOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Quiz quiz = quizOpt.get();
            
            // Calculate score
            int correctAnswers = 0;
            int totalQuestions = quiz.getQuestions().size();
            
            for (Question question : quiz.getQuestions()) {
                Integer userAnswer = request.getAnswers().get(question.getId());
                if (userAnswer != null && userAnswer.equals(question.getCorrectOption())) {
                    correctAnswers++;
                }
            }
            
            int scorePercentage = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
            
            // Record quiz completion in activity tracking
            String quizIdentifier = "quiz-" + quizId;
            activityTrackingService.recordQuizCompletion(
                request.getUserId(),
                quizIdentifier,
                scorePercentage,
                100,
                request.getDurationMinutes() != null ? request.getDurationMinutes() : 10,
                request.getAccessibilityTools()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("score", scorePercentage);
            response.put("correctAnswers", correctAnswers);
            response.put("totalQuestions", totalQuestions);
            response.put("passed", scorePercentage >= 60);
            response.put("currentStreak", activityTrackingService.calculateCurrentStreak(request.getUserId()));
            
            logger.info("Quiz {} completed by user {} with score {}%", quizId, request.getUserId(), scorePercentage);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error submitting quiz {}: {}", quizId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to submit quiz");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Create manual quiz (for tutors)
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createQuiz(@RequestBody CreateQuizRequest request) {
        try {
            Quiz quiz = new Quiz();
            quiz.setTitle(request.getTitle());
            quiz.setAiGenerated(false);
            quiz.setLearningContentId(request.getContentId());
            
            // Set learning content if provided
            if (request.getContentId() != null) {
                Optional<LearningContent> content = learningContentRepository.findById(request.getContentId());
                if (content.isEmpty()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Learning content not found");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
            
            // Create questions
            List<Question> questions = new ArrayList<>();
            for (CreateQuizRequest.QuestionRequest qReq : request.getQuestions()) {
                Question question = new Question();
                question.setQuestion(qReq.getQuestion());
                question.setOptions(qReq.getOptions());
                question.setCorrectOption(qReq.getCorrectAnswer());
                questions.add(question);
            }
            
            quiz.setQuestions(questions);
            Quiz savedQuiz = quizRepository.save(quiz);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quiz created successfully!");
            response.put("quiz", savedQuiz);
            
            logger.info("Manual quiz created: {} with {} questions", savedQuiz.getTitle(), questions.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating quiz: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create quiz: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Generate AI quiz for learning content
     */
    @PostMapping("/generate/{contentId}")
    public ResponseEntity<Map<String, Object>> generateQuiz(@PathVariable Long contentId) {
        try {
            Optional<LearningContent> contentOpt = learningContentRepository.findById(contentId);
            if (contentOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Learning content not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            LearningContent content = contentOpt.get();
            
            // For now, create a mock quiz since Claude API key isn't configured
            Quiz quiz = new Quiz();
            quiz.setTitle("AI Quiz: " + content.getTitle());
            quiz.setLearningContentId(contentId);
            quiz.setAiGenerated(true);
            
            // Create sample questions based on content
            List<Question> questions = createSampleQuestions(content);
            quiz.setQuestions(questions);
            
            Quiz savedQuiz = quizRepository.save(quiz);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "AI Quiz generated successfully! (Mock version - Claude API not configured)");
            response.put("quiz", savedQuiz);
            
            logger.info("AI quiz generated for content: {} (mock version)", content.getTitle());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating AI quiz: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to generate quiz");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Update existing quiz
     */
    @PutMapping("/{quizId}")
    public ResponseEntity<Map<String, Object>> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody CreateQuizRequest request) {
        try {
            Optional<Quiz> quizOpt = quizRepository.findById(quizId);
            if (quizOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Quiz not found");
                return ResponseEntity.notFound().build();
            }

            Quiz quiz = quizOpt.get();

            // Update basic quiz info
            quiz.setTitle(request.getTitle());
            quiz.setLearningContentId(request.getContentId());

            // Validate content if provided
            if (request.getContentId() != null) {
                Optional<LearningContent> content = learningContentRepository.findById(request.getContentId());
                if (content.isEmpty()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Learning content not found");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            // Delete existing questions from database first
            questionRepository.deleteByQuizId(quizId);

            // Create new questions
            List<Question> questions = new ArrayList<>();
            for (CreateQuizRequest.QuestionRequest qReq : request.getQuestions()) {
                Question question = new Question();
                question.setQuestion(qReq.getQuestion());
                question.setOptions(qReq.getOptions());
                question.setCorrectOption(qReq.getCorrectAnswer());
                question.setQuiz(quiz);
                questions.add(question);
            }

            quiz.setQuestions(questions);
            Quiz updatedQuiz = quizRepository.save(quiz);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quiz updated successfully!");
            response.put("quiz", updatedQuiz);

            logger.info("Quiz updated: {} with {} questions", updatedQuiz.getTitle(), questions.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating quiz {}: {}", quizId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update quiz: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Delete quiz
     */
    @DeleteMapping("/{quizId}")
    public ResponseEntity<Map<String, Object>> deleteQuiz(@PathVariable Long quizId) {
        try {
            if (!quizRepository.existsById(quizId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Quiz not found");
                return ResponseEntity.notFound().build();
            }
            
            // Delete related leaderboard entries first to avoid foreign key constraint violation
            logger.info("Deleting leaderboard entries for quiz {}", quizId);
            leaderboardRepository.deleteByQuizId(quizId);
            
            // Now delete the quiz
            quizRepository.deleteById(quizId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quiz deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting quiz {}: {}", quizId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete quiz");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Create sample questions for content (mock version)
     */
    private List<Question> createSampleQuestions(LearningContent content) {
        List<Question> questions = new ArrayList<>();
        
        // Question 1 - About content type
        Question q1 = new Question();
        q1.setQuestion("What type of learning material is \"" + content.getTitle() + "\"?");
        q1.setOptions(Arrays.asList(
            content.getContentType() != null ? content.getContentType() : "Educational Material",
            "Novel", 
            "Dictionary", 
            "Magazine"
        ));
        q1.setCorrectOption(0);
        questions.add(q1);
        
        // Question 2 - About difficulty
        Question q2 = new Question();
        q2.setQuestion("What is the difficulty level of this content?");
        q2.setOptions(Arrays.asList(
            "Beginner", 
            "Intermediate",
            content.getDifficultyLevel() != null ? content.getDifficultyLevel() : "Advanced",
            "Expert"
        ));
        q2.setCorrectOption(2);
        questions.add(q2);
        
        // Question 3 - About subject area
        Question q3 = new Question();
        q3.setQuestion("Which subject area does this content cover?");
        q3.setOptions(Arrays.asList(
            content.getSubjectArea() != null ? content.getSubjectArea() : "General Education",
            "Sports",
            "Cooking",
            "Travel"
        ));
        q3.setCorrectOption(0);
        questions.add(q3);
        
        return questions;
    }
    
    // Request DTOs
    public static class QuizSubmissionRequest {
        private Long userId;
        private Map<Long, Integer> answers; // questionId -> selectedOption
        private Integer durationMinutes;
        private List<String> accessibilityTools;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Map<Long, Integer> getAnswers() { return answers; }
        public void setAnswers(Map<Long, Integer> answers) { this.answers = answers; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public List<String> getAccessibilityTools() { return accessibilityTools; }
        public void setAccessibilityTools(List<String> accessibilityTools) { this.accessibilityTools = accessibilityTools; }
    }
    
    public static class CreateQuizRequest {
        private String title;
        private Long contentId;
        private List<QuestionRequest> questions;
        
        public static class QuestionRequest {
            private String question;
            private List<String> options;
            private Integer correctAnswer;
            
            // Getters and setters
            public String getQuestion() { return question; }
            public void setQuestion(String question) { this.question = question; }
            public List<String> getOptions() { return options; }
            public void setOptions(List<String> options) { this.options = options; }
            public Integer getCorrectAnswer() { return correctAnswer; }
            public void setCorrectAnswer(Integer correctAnswer) { this.correctAnswer = correctAnswer; }
        }
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Long getContentId() { return contentId; }
        public void setContentId(Long contentId) { this.contentId = contentId; }
        public List<QuestionRequest> getQuestions() { return questions; }
        public void setQuestions(List<QuestionRequest> questions) { this.questions = questions; }
    }
}
