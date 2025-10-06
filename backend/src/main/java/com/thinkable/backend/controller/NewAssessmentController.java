package com.thinkable.backend.controller;

import com.thinkable.backend.model.UserAssessment;
import com.thinkable.backend.model.FontTestResult;
import com.thinkable.backend.model.AssessmentQuestion;
import com.thinkable.backend.model.AdaptiveUISettings;
import com.thinkable.backend.service.AssessmentService;
import com.thinkable.backend.service.AdaptiveLearningAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assessment")
public class NewAssessmentController {

    private static final Logger logger = LoggerFactory.getLogger(NewAssessmentController.class);

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private AdaptiveLearningAIService aiService;

    /**
     * Start a new assessment session for a user
     */
    @PostMapping("/start/{userId}")
    public ResponseEntity<?> startAssessment(@PathVariable Long userId) {
        try {
            logger.info("Starting assessment for user: {}", userId);
            
            // Get age-appropriate questions
            List<AssessmentQuestion> questions = assessmentService.getAssessmentQuestionsForUser(userId);
            
            // Create or get existing assessment
            UserAssessment assessment = assessmentService.getOrCreateAssessment(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assessmentId", assessment.getId());
            response.put("questions", questions);
            response.put("totalQuestions", questions.size());
            response.put("estimatedTimeMinutes", questions.size() * 0.5); // 30 seconds per question average
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error starting assessment for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to start assessment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Conduct font readability test
     */
    @PostMapping("/font-test/{userId}")
    public ResponseEntity<?> conductFontTest(@PathVariable Long userId, @RequestBody FontTestRequest request) {
        try {
            logger.info("Processing font test for user: {}", userId);
            
            List<FontTestResult> results = assessmentService.processFontTest(userId, request);
            FontTestAnalysis analysis = assessmentService.analyzeFontTestResults(userId, results);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("analysis", analysis);
            response.put("dyslexiaIndicators", analysis.getDyslexiaIndicators());
            response.put("recommendedFonts", analysis.getRecommendedFonts());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing font test for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to process font test: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Submit assessment responses
     */
    @PostMapping("/submit/{userId}")
    public ResponseEntity<?> submitAssessment(@PathVariable Long userId, @RequestBody AssessmentSubmissionRequest request) {
        try {
            logger.info("Processing assessment submission for user: {}", userId);
            
            // Calculate assessment scores
            UserAssessment results = assessmentService.calculateAssessmentResults(userId, request);
            
            // Generate AI-powered recommendations
            if (results.getAssessmentCompleted()) {
                try {
                    String aiRecommendations = aiService.generatePersonalizedRecommendations(results);
                    // Store AI recommendations in user assessment
                    assessmentService.updateAIRecommendations(userId, aiRecommendations);
                } catch (Exception aiError) {
                    logger.warn("AI recommendations generation failed for user {}: {}", userId, aiError.getMessage());
                }
            }
            
            // Create adaptive UI settings based on results
            AdaptiveUISettings uiSettings = assessmentService.createAdaptiveUISettings(userId, results);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assessment", results);
            response.put("uiSettings", uiSettings);
            response.put("recommendations", getAccessibilityRecommendations(results));
            response.put("nextSteps", getNextSteps(results));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error submitting assessment for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to submit assessment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get user's learning profile
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        try {
            UserAssessment assessment = assessmentService.getLatestAssessment(userId);
            AdaptiveUISettings uiSettings = assessmentService.getUISettings(userId);
            
            if (assessment == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("hasAssessment", false);
                response.put("assessmentCompleted", false);
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("hasAssessment", true);
            profile.put("assessmentCompleted", assessment.getAssessmentCompleted());
            profile.put("assessment", assessment);
            profile.put("uiSettings", uiSettings);
            profile.put("recommendedPreset", assessment.getRecommendedPreset());
            profile.put("traits", getTraitSummary(assessment));
            
            return ResponseEntity.ok(profile);
            
        } catch (Exception e) {
            logger.error("Error getting profile for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get user profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Helper methods
    private Map<String, Object> getAccessibilityRecommendations(UserAssessment assessment) {
        Map<String, Object> recommendations = new HashMap<>();
        
        if (assessment.hasSignificantAttentionNeeds()) {
            recommendations.put("attention", List.of(
                "Use shorter study sessions (15-20 minutes)",
                "Take frequent breaks",
                "Use visual progress indicators",
                "Minimize distractions in study environment"
            ));
        }
        
        if (assessment.hasSignificantReadingNeeds()) {
            recommendations.put("reading", List.of(
                "Use dyslexia-friendly fonts",
                "Increase line spacing",
                "Use text-to-speech features",
                "Break text into smaller chunks"
            ));
        }
        
        if (assessment.hasSignificantSocialNeeds()) {
            recommendations.put("social", List.of(
                "Provide clear, explicit instructions",
                "Use predictable layouts and routines",
                "Avoid metaphors and implied meanings",
                "Offer structured learning paths"
            ));
        }
        
        if (assessment.hasSignificantSensoryNeeds()) {
            recommendations.put("sensory", List.of(
                "Reduce visual clutter",
                "Control brightness and contrast",
                "Minimize animations and movement",
                "Provide quiet study environments"
            ));
        }
        
        return recommendations;
    }
    
    private Map<String, String> getNextSteps(UserAssessment assessment) {
        Map<String, String> nextSteps = new HashMap<>();
        
        if (assessment.getAssessmentCompleted()) {
            nextSteps.put("primary", "Your personalized learning environment is ready!");
            nextSteps.put("secondary", "Explore lessons with your optimized settings");
            nextSteps.put("action", "Start Learning");
            nextSteps.put("actionUrl", "/lessons.html");
        } else {
            nextSteps.put("primary", "Complete your assessment");
            nextSteps.put("secondary", "A few more questions to personalize your experience");
            nextSteps.put("action", "Continue Assessment");
            nextSteps.put("actionUrl", "/assessment.html");
        }
        
        return nextSteps;
    }
    
    private Map<String, Object> getTraitSummary(UserAssessment assessment) {
        Map<String, Object> traits = new HashMap<>();
        
        traits.put("attention", Map.of(
            "score", assessment.getAttentionScore(),
            "level", getScoreLevel(assessment.getAttentionScore(), 18),
            "description", getAttentionDescription(assessment.getAttentionScore())
        ));
        
        traits.put("reading", Map.of(
            "score", assessment.getReadingDifficultyScore(),
            "level", getScoreLevel(assessment.getReadingDifficultyScore(), 15),
            "description", getReadingDescription(assessment.getReadingDifficultyScore())
        ));
        
        traits.put("social", Map.of(
            "score", assessment.getSocialCommunicationScore(),
            "level", getScoreLevel(assessment.getSocialCommunicationScore(), 16),
            "description", getSocialDescription(assessment.getSocialCommunicationScore())
        ));
        
        traits.put("sensory", Map.of(
            "score", assessment.getSensoryProcessingScore(),
            "level", getScoreLevel(assessment.getSensoryProcessingScore(), 14),
            "description", getSensoryDescription(assessment.getSensoryProcessingScore())
        ));
        
        return traits;
    }
    
    private String getScoreLevel(Integer score, int threshold) {
        if (score == null) return "unknown";
        if (score >= threshold) return "high";
        if (score >= threshold * 0.7) return "moderate";
        return "low";
    }
    
    private String getAttentionDescription(Integer score) {
        if (score == null) return "Not assessed";
        if (score >= 18) return "Benefits from attention support strategies";
        if (score >= 12) return "May benefit from occasional attention supports";
        return "Good attention regulation";
    }
    
    private String getReadingDescription(Integer score) {
        if (score == null) return "Not assessed";
        if (score >= 15) return "Benefits from reading support tools";
        if (score >= 10) return "May benefit from reading accommodations";
        return "Good reading skills";
    }
    
    private String getSocialDescription(Integer score) {
        if (score == null) return "Not assessed";
        if (score >= 16) return "Benefits from clear, explicit communication";
        if (score >= 11) return "May benefit from structured interactions";
        return "Good social communication";
    }
    
    private String getSensoryDescription(Integer score) {
        if (score == null) return "Not assessed";
        if (score >= 14) return "Benefits from sensory-friendly environments";
        if (score >= 10) return "May benefit from sensory considerations";
        return "Good sensory processing";
    }

    // Request/Response DTOs
    public static class FontTestRequest {
        private List<FontTestResponse> fontResponses;
        
        public List<FontTestResponse> getFontResponses() { return fontResponses; }
        public void setFontResponses(List<FontTestResponse> fontResponses) { this.fontResponses = fontResponses; }
    }
    
    public static class FontTestResponse {
        private String fontName;
        private Integer rating;
        private String difficulty;
        private Map<String, Boolean> symptoms;
        
        public String getFontName() { return fontName; }
        public void setFontName(String fontName) { this.fontName = fontName; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public Map<String, Boolean> getSymptoms() { return symptoms; }
        public void setSymptoms(Map<String, Boolean> symptoms) { this.symptoms = symptoms; }
    }
    
    public static class AssessmentSubmissionRequest {
        private Map<String, Object> responses;
        private String sessionId;
        
        public Map<String, Object> getResponses() { return responses; }
        public void setResponses(Map<String, Object> responses) { this.responses = responses; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
    
    public static class FontTestAnalysis {
        private Map<String, Boolean> dyslexiaIndicators;
        private List<String> recommendedFonts;
        private String analysis;
        
        public Map<String, Boolean> getDyslexiaIndicators() { return dyslexiaIndicators; }
        public void setDyslexiaIndicators(Map<String, Boolean> dyslexiaIndicators) { this.dyslexiaIndicators = dyslexiaIndicators; }
        public List<String> getRecommendedFonts() { return recommendedFonts; }
        public void setRecommendedFonts(List<String> recommendedFonts) { this.recommendedFonts = recommendedFonts; }
        public String getAnalysis() { return analysis; }
        public void setAnalysis(String analysis) { this.analysis = analysis; }
    }
}
