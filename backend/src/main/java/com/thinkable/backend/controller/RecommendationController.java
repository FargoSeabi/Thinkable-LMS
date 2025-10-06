package com.thinkable.backend.controller;

import java.util.stream.Collectors;

import com.thinkable.backend.entity.ContentRecommendation;
import com.thinkable.backend.service.ContentRecommendationEngine;
import com.thinkable.backend.repository.ContentRecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for AI-powered content recommendations
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    
    @Autowired
    private ContentRecommendationEngine recommendationEngine;
    
    @Autowired
    private ContentRecommendationRepository recommendationRepository;
    
    /**
     * Generate personalized recommendations for a student
     */
    @PostMapping("/generate/{studentId}")
    public ResponseEntity<?> generateRecommendations(@PathVariable Long studentId) {
        try {
            List<ContentRecommendation> recommendations = recommendationEngine.generateRecommendations(studentId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Recommendations generated successfully",
                "studentId", studentId,
                "recommendationsCount", recommendations.size(),
                "recommendations", recommendations.stream().limit(10).collect(Collectors.toList()), // Return top 10
                "generatedAt", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to generate recommendations: " + e.getMessage()));
        }
    }
    
    /**
     * Get active recommendations for a student
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentRecommendations(@PathVariable Long studentId,
                                                      @RequestParam(value = "includePresented", defaultValue = "false") Boolean includePresented) {
        try {
            List<ContentRecommendation> recommendations;
            
            if (includePresented) {
                recommendations = recommendationRepository.findByStudentIdAndIsActiveTrueOrderByConfidenceScoreDesc(studentId);
            } else {
                recommendations = recommendationRepository.findByStudentIdAndIsActiveTrueAndPresentedToStudentFalseOrderByConfidenceScoreDesc(studentId);
            }
            
            return ResponseEntity.ok(Map.of(
                "recommendations", recommendations,
                "count", recommendations.size(),
                "studentId", studentId,
                "includePresented", includePresented
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get recommendations: " + e.getMessage()));
        }
    }
    
    /**
     * Get high-confidence recommendations
     */
    @GetMapping("/student/{studentId}/high-confidence")
    public ResponseEntity<?> getHighConfidenceRecommendations(@PathVariable Long studentId,
                                                             @RequestParam(value = "minConfidence", defaultValue = "0.8") Double minConfidence) {
        try {
            List<ContentRecommendation> recommendations = recommendationRepository
                    .findHighConfidenceRecommendations(studentId, BigDecimal.valueOf(minConfidence));
            
            return ResponseEntity.ok(Map.of(
                "recommendations", recommendations,
                "count", recommendations.size(),
                "minConfidence", minConfidence,
                "message", "High-confidence recommendations most likely to be helpful"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get high-confidence recommendations: " + e.getMessage()));
        }
    }
    
    /**
     * Get recommendations by type
     */
    @GetMapping("/student/{studentId}/type/{type}")
    public ResponseEntity<?> getRecommendationsByType(@PathVariable Long studentId,
                                                     @PathVariable String type) {
        try {
            List<ContentRecommendation> recommendations = recommendationRepository
                    .findByStudentIdAndRecommendationTypeAndIsActiveTrue(studentId, type);
            
            String typeDescription = getTypeDescription(type);
            
            return ResponseEntity.ok(Map.of(
                "recommendations", recommendations,
                "count", recommendations.size(),
                "type", type,
                "description", typeDescription
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get recommendations by type: " + e.getMessage()));
        }
    }
    
    /**
     * Mark recommendation as presented
     */
    @PostMapping("/{recommendationId}/present")
    public ResponseEntity<?> markRecommendationPresented(@PathVariable Long recommendationId) {
        try {
            ContentRecommendation recommendation = recommendationRepository.findById(recommendationId)
                    .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
            
            recommendation.markPresented();
            recommendationRepository.save(recommendation);
            
            return ResponseEntity.ok(Map.of(
                "message", "Recommendation marked as presented",
                "recommendationId", recommendationId,
                "presentedAt", recommendation.getPresentedAt()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to mark recommendation as presented: " + e.getMessage()));
        }
    }
    
    /**
     * Record student response to recommendation
     */
    @PostMapping("/{recommendationId}/respond")
    public ResponseEntity<?> recordStudentResponse(@PathVariable Long recommendationId,
                                                  @RequestParam String response) {
        try {
            ContentRecommendation recommendation = recommendationRepository.findById(recommendationId)
                    .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
            
            recommendation.recordResponse(response);
            recommendationRepository.save(recommendation);
            
            return ResponseEntity.ok(Map.of(
                "message", "Student response recorded",
                "recommendationId", recommendationId,
                "response", response,
                "respondedAt", recommendation.getRespondedAt()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to record response: " + e.getMessage()));
        }
    }
    
    /**
     * Record recommendation feedback
     */
    @PostMapping("/{recommendationId}/feedback")
    public ResponseEntity<?> recordRecommendationFeedback(@PathVariable Long recommendationId,
                                                         @RequestParam Integer rating,
                                                         @RequestParam Boolean helpful) {
        try {
            ContentRecommendation recommendation = recommendationRepository.findById(recommendationId)
                    .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
            
            recommendation.recordFeedback(rating, helpful);
            recommendationRepository.save(recommendation);
            
            return ResponseEntity.ok(Map.of(
                "message", "Feedback recorded successfully",
                "recommendationId", recommendationId,
                "rating", rating,
                "helpful", helpful
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to record feedback: " + e.getMessage()));
        }
    }
    
    /**
     * Get recommendation analytics
     */
    @GetMapping("/analytics/{studentId}")
    public ResponseEntity<?> getRecommendationAnalytics(@PathVariable Long studentId) {
        try {
            Long completedRecommendations = recommendationRepository.countCompletedRecommendations(studentId);
            Long ignoredRecommendations = recommendationRepository.countIgnoredRecommendations(studentId);
            List<Object[]> typeStats = recommendationRepository.getRecommendationTypeStats(studentId);
            
            return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "completedRecommendations", completedRecommendations,
                "ignoredRecommendations", ignoredRecommendations,
                "typeStats", typeStats,
                "engagementRate", calculateEngagementRate(completedRecommendations, ignoredRecommendations)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get analytics: " + e.getMessage()));
        }
    }
    
    /**
     * Clean up expired recommendations
     */
    @PostMapping("/cleanup/expired")
    public ResponseEntity<?> cleanupExpiredRecommendations() {
        try {
            List<ContentRecommendation> expiredRecommendations = recommendationRepository
                    .findExpiredRecommendations(LocalDateTime.now());
            
            for (ContentRecommendation recommendation : expiredRecommendations) {
                recommendation.expire();
            }
            
            recommendationRepository.saveAll(expiredRecommendations);
            
            return ResponseEntity.ok(Map.of(
                "message", "Expired recommendations cleaned up",
                "expiredCount", expiredRecommendations.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to cleanup expired recommendations: " + e.getMessage()));
        }
    }
    
    /**
     * Get algorithm performance statistics
     */
    @GetMapping("/statistics/algorithm/{version}")
    public ResponseEntity<?> getAlgorithmStatistics(@PathVariable String version) {
        try {
            Double averageFeedback = recommendationRepository.getAverageFeedbackRatingForAlgorithm(version);
            
            return ResponseEntity.ok(Map.of(
                "algorithmVersion", version,
                "averageFeedbackRating", averageFeedback != null ? averageFeedback : 0.0,
                "message", String.format("Performance statistics for algorithm version %s", version)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get algorithm statistics: " + e.getMessage()));
        }
    }
    
    // Helper methods
    
    private String getTypeDescription(String type) {
        switch (type.toLowerCase()) {
            case "personalized":
                return "Content tailored to your learning profile and preferences";
            case "similar_content":
                return "Content similar to what you've previously found helpful";
            case "accessibility_match":
                return "Content designed for your specific accessibility needs";
            case "trending":
                return "Popular content with high ratings from learners like you";
            case "optimal_timing":
                return "Content recommended for your peak learning times";
            default:
                return "Recommended content based on various factors";
        }
    }
    
    private double calculateEngagementRate(Long completed, Long ignored) {
        long total = completed + ignored;
        if (total == 0) return 0.0;
        return (completed.doubleValue() / total) * 100.0;
    }
}
