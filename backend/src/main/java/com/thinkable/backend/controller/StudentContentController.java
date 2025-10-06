package com.thinkable.backend.controller;

import com.thinkable.backend.entity.LearningContent;
import com.thinkable.backend.entity.StudentContentInteraction;
import com.thinkable.backend.model.Quiz;
import com.thinkable.backend.model.Question;
import com.thinkable.backend.model.User;
import com.thinkable.backend.model.Leaderboard;
import com.thinkable.backend.repository.QuizRepository;
import com.thinkable.backend.repository.UserRepository;
import com.thinkable.backend.repository.LeaderboardRepository;
import com.thinkable.backend.repository.StudentContentInteractionRepository;
import com.thinkable.backend.service.TutorContentService;
import com.thinkable.backend.service.TutorContentService.ContentSearchRequest;
import com.thinkable.backend.service.TutorContentService.InteractionRequest;
import com.thinkable.backend.service.ActivityTrackingService;
import com.thinkable.backend.service.SmartNotesAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for student content discovery and interaction
 */
@RestController
@RequestMapping("/api/student/content")
public class StudentContentController {
    
    @Autowired
    private TutorContentService contentService;
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LeaderboardRepository leaderboardRepository;
    
    @Autowired
    private ActivityTrackingService activityTrackingService;
    
    @Autowired
    private SmartNotesAIService smartNotesAIService;
    
    @Autowired
    private StudentContentInteractionRepository studentInteractionRepository;
    
    /**
     * Search and filter content based on accessibility needs
     */
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchContent(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "subject", required = false) String subjectArea,
            @RequestParam(value = "dyslexiaFriendly", defaultValue = "false") Boolean dyslexiaFriendly,
            @RequestParam(value = "adhdFriendly", defaultValue = "false") Boolean adhdFriendly,
            @RequestParam(value = "autismFriendly", defaultValue = "false") Boolean autismFriendly,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        try {
            // Create search request
            ContentSearchRequest searchRequest = new ContentSearchRequest();
            searchRequest.setQuery(query);
            searchRequest.setSubjectArea(subjectArea);
            searchRequest.setDyslexiaFriendly(dyslexiaFriendly);
            searchRequest.setAdhdFriendly(adhdFriendly);
            searchRequest.setAutismFriendly(autismFriendly);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<LearningContent> contentPage = contentService.searchContent(searchRequest, pageable);
            
            // Enrich content with tutor information
            List<Map<String, Object>> enrichedContent = enrichContentWithTutorInfo(contentPage.getContent());
            
            return ResponseEntity.ok(Map.of(
                "content", enrichedContent,
                "totalElements", contentPage.getTotalElements(),
                "totalPages", contentPage.getTotalPages(),
                "currentPage", page,
                "size", size,
                "hasNext", contentPage.hasNext(),
                "hasPrevious", contentPage.hasPrevious()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to search content: " + e.getMessage()));
        }
    }
    
    /**
     * Get content details by ID
     */
    @GetMapping("/{contentId}")
    public ResponseEntity<?> getContentById(@PathVariable Long contentId) {
        try {
            LearningContent content = contentService.getContentById(contentId);
            
            if (content == null || !content.getStatus().equals("published")) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(content);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get content: " + e.getMessage()));
        }
    }

    /**
     * Get personalized content recommendations for a student
     */
    @GetMapping("/recommendations/{studentId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPersonalizedContent(@PathVariable Long studentId) {
        try {
            List<LearningContent> recommendations = contentService.getPersonalizedContent(studentId);
            
            // Enrich content with tutor information
            List<Map<String, Object>> enrichedRecommendations = enrichContentWithTutorInfo(recommendations);
            
            return ResponseEntity.ok(Map.of(
                "recommendations", enrichedRecommendations,
                "count", enrichedRecommendations.size(),
                "studentId", studentId,
                "message", "Personalized content based on your learning profile"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get recommendations: " + e.getMessage()));
        }
    }
    
    /**
     * Get content filtered by specific accessibility feature
     */
    @GetMapping("/accessible/{type}")
    public ResponseEntity<?> getAccessibleContent(@PathVariable String type) {
        try {
            List<LearningContent> content;
            
            switch (type.toLowerCase()) {
                case "dyslexia":
                    // This would call repository method directly or through service
                    content = List.of(); // Placeholder
                    break;
                case "adhd":
                    content = List.of(); // Placeholder
                    break;
                case "autism":
                    content = List.of(); // Placeholder
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid accessibility type. Use: dyslexia, adhd, autism"));
            }
            
            return ResponseEntity.ok(Map.of(
                "content", content,
                "accessibilityType", type,
                "count", content.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch accessible content: " + e.getMessage()));
        }
    }
    
    /**
     * Record student interaction with content
     */
    @PostMapping("/{contentId}/interact")
    public ResponseEntity<?> recordInteraction(
            @PathVariable Long contentId,
            @RequestParam Long studentId,
            @RequestBody InteractionRequest request) {
        
        try {
            contentService.recordInteraction(studentId, contentId, request);
            
            return ResponseEntity.ok(Map.of(
                "message", "Interaction recorded successfully",
                "contentId", contentId,
                "studentId", studentId,
                "interactionType", request.getInteractionType()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to record interaction: " + e.getMessage()));
        }
    }
    
    /**
     * Get content details with accessibility information
     */
    @GetMapping("/{contentId}/details")
    public ResponseEntity<?> getContentDetails(@PathVariable Long contentId) {
        try {
            // This would fetch content with full accessibility details
            return ResponseEntity.ok(Map.of(
                "message", "Content details fetched successfully",
                "contentId", contentId
                // Would include full content details with accessibility tags
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Content not found: " + e.getMessage()));
        }
    }
    
    /**
     * Get popular content (most viewed/rated)
     */
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularContent(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        try {
            // This would fetch popular content from repository
            return ResponseEntity.ok(Map.of(
                "content", List.of(), // Placeholder
                "message", "Popular content fetched successfully",
                "limit", limit
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch popular content: " + e.getMessage()));
        }
    }
    
    /**
     * Get latest uploaded content
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestContent(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        try {
            // This would fetch latest content from repository
            return ResponseEntity.ok(Map.of(
                "content", List.of(), // Placeholder
                "message", "Latest content fetched successfully",
                "limit", limit
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch latest content: " + e.getMessage()));
        }
    }
    
    /**
     * Get content by subject area
     */
    @GetMapping("/subject/{subject}")
    public ResponseEntity<?> getContentBySubject(
            @PathVariable String subject,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        try {
            // This would fetch content by subject from repository
            return ResponseEntity.ok(Map.of(
                "content", List.of(), // Placeholder
                "subject", subject,
                "currentPage", page,
                "size", size
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch content by subject: " + e.getMessage()));
        }
    }
    
    /**
     * Bookmark content for later viewing
     */
    @PostMapping("/{contentId}/bookmark")
    public ResponseEntity<?> bookmarkContent(
            @PathVariable Long contentId,
            @RequestParam Long studentId) {
        
        try {
            // This would create a bookmark interaction
            InteractionRequest bookmarkRequest = new InteractionRequest();
            bookmarkRequest.setInteractionType("bookmark");
            
            contentService.recordInteraction(studentId, contentId, bookmarkRequest);
            
            return ResponseEntity.ok(Map.of(
                "message", "Content bookmarked successfully",
                "contentId", contentId,
                "studentId", studentId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to bookmark content: " + e.getMessage()));
        }
    }
    
    /**
     * Rate content (usefulness and accessibility)
     */
    @PostMapping("/{contentId}/rate")
    public ResponseEntity<?> rateContent(
            @PathVariable Long contentId,
            @RequestParam Long studentId,
            @RequestBody RatingRequest request) {
        
        try {
            InteractionRequest interactionRequest = new InteractionRequest();
            interactionRequest.setInteractionType("rate");
            interactionRequest.setUsefulnessRating(request.getUsefulnessRating());
            interactionRequest.setAccessibilityRating(request.getAccessibilityRating());
            interactionRequest.setDifficultyRating(request.getDifficultyRating());
            interactionRequest.setWasHelpful(request.getWasHelpful());
            interactionRequest.setWouldRecommend(request.getWouldRecommend());
            
            contentService.recordInteraction(studentId, contentId, interactionRequest);
            
            return ResponseEntity.ok(Map.of(
                "message", "Content rated successfully",
                "contentId", contentId,
                "studentId", studentId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to rate content: " + e.getMessage()));
        }
    }
    
    /**
     * Get quiz for content
     */
    @GetMapping("/{contentId}/quiz")
    public ResponseEntity<?> getQuizForContent(
            @PathVariable Long contentId,
            @RequestParam String studentEmail) {
        
        try {
            User student = userRepository.findByEmail(studentEmail);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Student not found"));
            }
            
            List<Quiz> quizzes = quizRepository.findByLearningContentId(contentId);
            if (quizzes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No quiz available for this content"));
            }
            
            // Return the first quiz (most recent)
            Quiz quiz = quizzes.get(0);
            
            return ResponseEntity.ok(Map.of(
                "quiz", quiz,
                "contentId", contentId,
                "message", "Quiz loaded successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to load quiz: " + e.getMessage()));
        }
    }
    
    /**
     * Submit quiz answers
     */
    @PostMapping("/{contentId}/quiz/submit")
    public ResponseEntity<?> submitQuiz(
            @PathVariable Long contentId,
            @RequestParam String studentEmail,
            @RequestBody QuizSubmissionRequest request) {
        
        try {
            User student = userRepository.findByEmail(studentEmail);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Student not found"));
            }
            
            List<Quiz> quizzes = quizRepository.findByLearningContentId(contentId);
            if (quizzes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Quiz not found"));
            }
            
            Quiz quiz = quizzes.get(0);
            
            // Calculate score
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
            leaderboard.setUser(student);
            leaderboard.setQuiz(quiz);
            leaderboard.setScore((int) score);
            leaderboard.setSubmittedAt(LocalDateTime.now());
            leaderboardRepository.save(leaderboard);
            
            // Record interaction
            InteractionRequest interactionRequest = new InteractionRequest();
            interactionRequest.setInteractionType("quiz_completed");
            interactionRequest.setUsefulnessRating((int) (score / 20)); // Convert to 1-5 scale
            contentService.recordInteraction(student.getId(), contentId, interactionRequest);
            
            // Record quiz completion activity and check for achievements
            activityTrackingService.recordQuizCompletion(
                student.getId(), 
                String.valueOf(quiz.getId()),
                (int) score, // Score is already a percentage (0-100)
                100, // Max possible score is 100% for percentage calculation
                5, // Estimated duration in minutes 
                null // No accessibility tools tracked for now
            );
            
            return ResponseEntity.ok(Map.of(
                "score", score,
                "passed", score >= 70,
                "correctAnswers", correctAnswers,
                "totalQuestions", quiz.getQuestions().size(),
                "message", score >= 70 ? "Quiz passed!" : "Quiz completed. You need 70% to pass."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to submit quiz: " + e.getMessage()));
        }
    }
    
    /**
     * Get quiz results/history for student
     */
    @GetMapping("/{contentId}/quiz/results")
    public ResponseEntity<?> getQuizResults(
            @PathVariable Long contentId,
            @RequestParam String studentEmail) {
        
        try {
            User student = userRepository.findByEmail(studentEmail);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Student not found"));
            }
            
            List<Quiz> quizzes = quizRepository.findByLearningContentId(contentId);
            if (quizzes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Quiz not found"));
            }
            
            Quiz quiz = quizzes.get(0);
            List<Leaderboard> results = leaderboardRepository.findByUserAndQuizOrderBySubmittedAtDesc(student, quiz);
            
            return ResponseEntity.ok(Map.of(
                "results", results,
                "hasAttempted", !results.isEmpty(),
                "bestScore", results.stream().mapToInt(Leaderboard::getScore).max().orElse(0),
                "attemptCount", results.size(),
                "message", "Quiz results retrieved successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to get quiz results: " + e.getMessage()));
        }
    }
    
    // Inner class for quiz submission
    public static class QuizSubmissionRequest {
        public Map<String, Integer> answers;
        
        public Map<String, Integer> getAnswers() { return answers; }
        public void setAnswers(Map<String, Integer> answers) { this.answers = answers; }
    }
    
    // Inner class for rating request
    public static class RatingRequest {
        private Integer usefulnessRating;
        private Integer accessibilityRating;
        private Integer difficultyRating;
        private Boolean wasHelpful;
        private Boolean wouldRecommend;
        
        // Getters and setters
        public Integer getUsefulnessRating() { return usefulnessRating; }
        public void setUsefulnessRating(Integer usefulnessRating) { this.usefulnessRating = usefulnessRating; }
        public Integer getAccessibilityRating() { return accessibilityRating; }
        public void setAccessibilityRating(Integer accessibilityRating) { this.accessibilityRating = accessibilityRating; }
        public Integer getDifficultyRating() { return difficultyRating; }
        public void setDifficultyRating(Integer difficultyRating) { this.difficultyRating = difficultyRating; }
        public Boolean getWasHelpful() { return wasHelpful; }
        public void setWasHelpful(Boolean wasHelpful) { this.wasHelpful = wasHelpful; }
        public Boolean getWouldRecommend() { return wouldRecommend; }
        public void setWouldRecommend(Boolean wouldRecommend) { this.wouldRecommend = wouldRecommend; }
    }
    
    /**
     * Helper method to enrich content with tutor information for student-friendly display
     */
    private List<Map<String, Object>> enrichContentWithTutorInfo(List<LearningContent> contentList) {
        return contentList.stream().map(content -> {
            Map<String, Object> enrichedContent = new HashMap<>();
            
            // Copy all basic content fields
            enrichedContent.put("id", content.getId());
            enrichedContent.put("title", content.getTitle());
            enrichedContent.put("description", content.getDescription());
            enrichedContent.put("subjectArea", content.getSubjectArea());
            enrichedContent.put("contentType", content.getContentType());
            enrichedContent.put("fileName", content.getFileName());
            enrichedContent.put("filePath", content.getFilePath());
            enrichedContent.put("mimeType", content.getMimeType());
            enrichedContent.put("difficultyLevel", content.getDifficultyLevel());
            enrichedContent.put("status", content.getStatus());
            enrichedContent.put("createdAt", content.getCreatedAt());
            enrichedContent.put("viewCount", content.getViewCount());
            enrichedContent.put("ratingAverage", content.getRatingAverage());
            enrichedContent.put("ratingCount", content.getRatingCount());
            
            // Accessibility flags
            enrichedContent.put("dyslexiaFriendly", content.getDyslexiaFriendly());
            enrichedContent.put("adhdFriendly", content.getAdhdFriendly());
            enrichedContent.put("autismFriendly", content.getAutismFriendly());
            enrichedContent.put("visualImpairmentFriendly", content.getVisualImpairmentFriendly());
            enrichedContent.put("hearingImpairmentFriendly", content.getHearingImpairmentFriendly());
            enrichedContent.put("motorImpairmentFriendly", content.getMotorImpairmentFriendly());
            
            // Add tutor information (force lazy loading)
            try {
                if (content.getTutor() != null) {
                    enrichedContent.put("tutorId", content.getTutor().getId());
                    enrichedContent.put("tutorName", content.getTutor().getDisplayName());
                }
            } catch (Exception e) {
                // Handle lazy loading issues gracefully
                enrichedContent.put("tutorName", "Unknown Tutor");
            }
            
            return enrichedContent;
        }).collect(Collectors.toList());
    }
    
    /**
     * Get bookmarked content for a student
     */
    @GetMapping("/favorites/{studentId}")
    public ResponseEntity<?> getFavoriteContent(@PathVariable Long studentId) {
        try {
            List<StudentContentInteraction> bookmarkedInteractions = contentService.getBookmarkedContent(studentId);
            List<Map<String, Object>> favoriteContent = bookmarkedInteractions.stream()
                .map(interaction -> {
                    LearningContent content = interaction.getContent();
                    Map<String, Object> contentMap = new HashMap<>();
                    contentMap.put("id", content.getId());
                    contentMap.put("title", content.getTitle());
                    contentMap.put("description", content.getDescription());
                    contentMap.put("subjectArea", content.getSubjectArea());
                    contentMap.put("contentType", content.getContentType());
                    contentMap.put("difficultyLevel", content.getDifficultyLevel());
                    contentMap.put("viewCount", content.getViewCount());
                    contentMap.put("bookmarkedAt", interaction.getLastAccessedAt());
                    return contentMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "favorites", favoriteContent,
                "message", "Favorite content fetched successfully",
                "count", favoriteContent.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch favorite content: " + e.getMessage()));
        }
    }
    
    /**
     * Toggle bookmark status for content
     */
    @PostMapping("/{contentId}/bookmark/{studentId}/toggle")
    public ResponseEntity<?> toggleBookmark(
            @PathVariable Long contentId,
            @PathVariable Long studentId) {
        
        try {
            boolean isBookmarked = contentService.toggleBookmark(studentId, contentId);
            
            return ResponseEntity.ok(Map.of(
                "message", isBookmarked ? "Content bookmarked successfully" : "Bookmark removed successfully",
                "contentId", contentId,
                "studentId", studentId,
                "isBookmarked", isBookmarked
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to toggle bookmark: " + e.getMessage()));
        }
    }
    
    /**
     * Check if content is bookmarked by student
     */
    @GetMapping("/{contentId}/bookmark/{studentId}/status")
    public ResponseEntity<?> getBookmarkStatus(
            @PathVariable Long contentId,
            @PathVariable Long studentId) {
        
        try {
            boolean isBookmarked = contentService.isBookmarked(studentId, contentId);
            
            return ResponseEntity.ok(Map.of(
                "contentId", contentId,
                "studentId", studentId,
                "isBookmarked", isBookmarked
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to check bookmark status: " + e.getMessage()));
        }
    }
    
    /**
     * Get student notes for specific content
     */
    @GetMapping("/{contentId}/notes")
    public ResponseEntity<?> getStudentNotes(
            @PathVariable Long contentId,
            @RequestParam Long studentId) {
        
        try {
            Optional<StudentContentInteraction> interaction = 
                    contentService.getStudentInteraction(studentId, contentId);
            
            if (interaction.isEmpty() || interaction.get().getNotes() == null || interaction.get().getNotes().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No notes found for this content"));
            }
            
            StudentContentInteraction studentInteraction = interaction.get();
            return ResponseEntity.ok(Map.of(
                "contentId", contentId,
                "studentId", studentId,
                "notes", studentInteraction.getNotes(),
                "lastUpdated", studentInteraction.getLastAccessedAt().toString(),
                "message", "Notes retrieved successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get notes: " + e.getMessage()));
        }
    }
    
    /**
     * Get AI study suggestions based on content and notes
     */
    @PostMapping("/{contentId}/ai/suggestions")
    public ResponseEntity<?> getStudySuggestions(
            @PathVariable Long contentId,
            @RequestParam Long studentId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        try {
            LearningContent content = contentService.getContentById(contentId);
            if (content == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get existing notes
            String existingNotes = "";
            Optional<StudentContentInteraction> interaction = 
                    contentService.getStudentInteraction(studentId, contentId);
            if (interaction.isPresent() && interaction.get().getNotes() != null) {
                existingNotes = interaction.get().getNotes();
            }
            
            SmartNotesAIService.AIResponse aiResponse = smartNotesAIService.generateStudySuggestions(content, existingNotes);
            
            return ResponseEntity.ok(Map.of(
                "success", aiResponse.isSuccess(),
                "message", aiResponse.getMessage(),
                "suggestions", aiResponse.getContent() != null ? aiResponse.getContent() : "",
                "contentId", contentId,
                "studentId", studentId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to generate suggestions: " + e.getMessage()));
        }
    }
    
    /**
     * Ask AI a question about the content
     */
    @PostMapping("/{contentId}/ai/ask")
    public ResponseEntity<?> askAIQuestion(
            @PathVariable Long contentId,
            @RequestParam Long studentId,
            @RequestBody Map<String, String> requestBody) {
        
        try {
            String question = requestBody.get("question");
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Question is required"));
            }
            
            LearningContent content = contentService.getContentById(contentId);
            if (content == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get existing notes for context
            String existingNotes = "";
            Optional<StudentContentInteraction> interaction = 
                    contentService.getStudentInteraction(studentId, contentId);
            if (interaction.isPresent() && interaction.get().getNotes() != null) {
                existingNotes = interaction.get().getNotes();
            }
            
            SmartNotesAIService.AIResponse aiResponse = smartNotesAIService.answerQuestion(content, question, existingNotes);
            
            return ResponseEntity.ok(Map.of(
                "success", aiResponse.isSuccess(),
                "message", aiResponse.getMessage(),
                "answer", aiResponse.getContent() != null ? aiResponse.getContent() : "",
                "question", question,
                "contentId", contentId,
                "studentId", studentId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to answer question: " + e.getMessage()));
        }
    }
    
    /**
     * Get AI suggestions for improving existing notes
     */
    @PostMapping("/{contentId}/ai/improve-notes")
    public ResponseEntity<?> getNotesImprovements(
            @PathVariable Long contentId,
            @RequestParam Long studentId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        try {
            LearningContent content = contentService.getContentById(contentId);
            if (content == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get current notes
            Optional<StudentContentInteraction> interaction = 
                    contentService.getStudentInteraction(studentId, contentId);
            
            if (interaction.isEmpty() || interaction.get().getNotes() == null || interaction.get().getNotes().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No notes found to improve. Please take some notes first."));
            }
            
            String notes = interaction.get().getNotes();
            SmartNotesAIService.AIResponse aiResponse = smartNotesAIService.suggestNoteImprovements(content, notes);
            
            return ResponseEntity.ok(Map.of(
                "success", aiResponse.isSuccess(),
                "message", aiResponse.getMessage(),
                "improvements", aiResponse.getContent() != null ? aiResponse.getContent() : "",
                "contentId", contentId,
                "studentId", studentId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to generate improvements: " + e.getMessage()));
        }
    }
    
    /**
     * Get all notes for a student - for centralized My Notes page
     */
    @GetMapping("/notes/all")
    public ResponseEntity<?> getAllStudentNotes(@RequestParam Long studentId) {
        try {
            List<StudentContentInteraction> interactions = studentInteractionRepository
                .findByStudentIdOrderByLastAccessedAtDesc(studentId);
            
            // Filter interactions that have notes and include content details
            List<Map<String, Object>> notesWithContent = interactions.stream()
                .filter(interaction -> interaction.getNotes() != null && !interaction.getNotes().trim().isEmpty())
                .map(interaction -> {
                    Map<String, Object> noteData = new HashMap<>();
                    noteData.put("id", interaction.getId());
                    noteData.put("contentId", interaction.getContent().getId());
                    noteData.put("contentTitle", interaction.getContent().getTitle());
                    noteData.put("contentDescription", interaction.getContent().getDescription());
                    noteData.put("subjectArea", interaction.getContent().getSubjectArea());
                    noteData.put("difficultyLevel", interaction.getContent().getDifficultyLevel());
                    noteData.put("notes", interaction.getNotes());
                    noteData.put("lastUpdated", interaction.getLastAccessedAt());
                    noteData.put("characterCount", interaction.getNotes().length());
                    return noteData;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notes retrieved successfully",
                "notes", notesWithContent,
                "totalNotes", notesWithContent.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve notes: " + e.getMessage()));
        }
    }
}
