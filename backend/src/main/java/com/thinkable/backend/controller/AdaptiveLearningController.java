package com.thinkable.backend.controller;

import com.thinkable.backend.model.User;
import com.thinkable.backend.repository.UserRepository;
import com.thinkable.backend.service.AdaptiveLearningAIService;
import com.thinkable.backend.service.AdaptiveLearningAIService.LearningProfile;
import com.thinkable.backend.service.AdaptiveLearningAIService.LearningRecommendations;
import com.thinkable.backend.service.AdaptiveLearningAIService.DifficultyLevel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AdaptiveLearningController {

    @Autowired
    private AdaptiveLearningAIService aiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get comprehensive learning profile analysis for a user
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getLearningProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }

            LearningProfile profile = aiService.analyzeLearningProfile(user);
            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            System.err.println("Error analyzing learning profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResponse("Error analyzing learning profile: " + e.getMessage()));
        }
    }

    /**
     * Get personalized learning recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }

            LearningRecommendations recommendations = aiService.generateRecommendations(user);
            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            System.err.println("Error generating recommendations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResponse("Error generating recommendations: " + e.getMessage()));
        }
    }

    /**
     * Get adaptive difficulty recommendation based on performance
     */
    @PostMapping("/adapt-difficulty")
    public ResponseEntity<?> adaptDifficulty(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody DifficultyAdaptationRequest request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }

            DifficultyLevel adaptedDifficulty = aiService.adaptDifficultyBasedOnPerformance(
                user, request.getRecentPerformance()
            );
            
            return ResponseEntity.ok(new DifficultyAdaptationResponse(adaptedDifficulty, 
                generateDifficultyExplanation(adaptedDifficulty, request.getRecentPerformance())));

        } catch (Exception e) {
            System.err.println("Error adapting difficulty: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResponse("Error adapting difficulty: " + e.getMessage()));
        }
    }

    /**
     * Get AI-powered study insights
     */
    @GetMapping("/insights")
    public ResponseEntity<?> getStudyInsights(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
            }

            LearningProfile profile = aiService.analyzeLearningProfile(user);
            StudyInsights insights = generateStudyInsights(profile);
            
            return ResponseEntity.ok(insights);

        } catch (Exception e) {
            System.err.println("Error generating study insights: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResponse("Error generating study insights: " + e.getMessage()));
        }
    }

    // Helper methods
    
    private String generateDifficultyExplanation(DifficultyLevel difficulty, double performance) {
        if (performance >= 85) {
            return "Great job! 🎉 Your performance shows you're ready for " + 
                   difficulty.toString().toLowerCase() + " level challenges.";
        } else if (performance < 60) {
            return "Let's take a step back and build confidence with " + 
                   difficulty.toString().toLowerCase() + " level content. You've got this! 💪";
        } else {
            return "You're doing well! Continuing with " + 
                   difficulty.toString().toLowerCase() + " level is perfect for your current progress.";
        }
    }

    private StudyInsights generateStudyInsights(LearningProfile profile) {
        StudyInsights insights = new StudyInsights();
        
        // Neurodivergent-specific insights
        if (profile.getNeuroProfile().getAdhdIndicators() > 0.5) {
            insights.addInsight("ADHD Support", 
                "Your assessment suggests you thrive with short, focused study sessions. " +
                "Try studying for " + profile.getSessionParameters().getOptimalSessionLength() + 
                " minutes at a time with " + profile.getSessionParameters().getOptimalBreakInterval() + 
                " minute breaks.");
        }

        if (profile.getNeuroProfile().getAutismSpectrumIndicators() > 0.5) {
            insights.addInsight("Structure & Routine", 
                "You benefit from structured learning with clear expectations. " +
                "Consider following the same study routine each day for optimal results.");
        }

        // Learning style insights
        String modality = profile.getLearningStyle().getPrimaryModality();
        switch (modality) {
            case "VISUAL":
                insights.addInsight("Visual Learning", 
                    "You're a visual learner! 👁️ Focus on lessons with videos, diagrams, and visual content.");
                break;
            case "AUDITORY":
                insights.addInsight("Auditory Learning", 
                    "You learn best through listening! 👂 Try reading content aloud or using text-to-speech features.");
                break;
            case "KINESTHETIC":
                insights.addInsight("Hands-On Learning", 
                    "You learn by doing! ✋ Take notes, use interactive features, and engage actively with content.");
                break;
        }

        // Performance insights
        if (profile.getPerformanceMetrics().getAverageScore() >= 80) {
            insights.addInsight("High Performance", 
                "Excellent work! 🌟 Your consistent high scores show you're mastering the material.");
        } else if (profile.getPerformanceMetrics().getAverageScore() < 60) {
            insights.addInsight("Growth Opportunity", 
                "Every expert was once a beginner! 🌱 Focus on understanding concepts rather than speed.");
        }

        return insights;
    }

    // Response classes

    public static class DifficultyAdaptationRequest {
        private double recentPerformance;

        public double getRecentPerformance() { return recentPerformance; }
        public void setRecentPerformance(double recentPerformance) { this.recentPerformance = recentPerformance; }
    }

    public static class DifficultyAdaptationResponse {
        private DifficultyLevel recommendedDifficulty;
        private String explanation;

        public DifficultyAdaptationResponse(DifficultyLevel difficulty, String explanation) {
            this.recommendedDifficulty = difficulty;
            this.explanation = explanation;
        }

        public DifficultyLevel getRecommendedDifficulty() { return recommendedDifficulty; }
        public void setRecommendedDifficulty(DifficultyLevel recommendedDifficulty) { this.recommendedDifficulty = recommendedDifficulty; }
        
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    public static class StudyInsights {
        private java.util.Map<String, String> insights = new java.util.HashMap<>();

        public void addInsight(String category, String insight) {
            insights.put(category, insight);
        }

        public java.util.Map<String, String> getInsights() { return insights; }
        public void setInsights(java.util.Map<String, String> insights) { this.insights = insights; }
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
