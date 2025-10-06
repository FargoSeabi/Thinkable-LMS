package com.thinkable.backend.service;

import com.thinkable.backend.entity.*;
import com.thinkable.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * AI-Powered Content Recommendation Engine
 * Generates personalized learning content recommendations based on user profiles, 
 * learning patterns, and accessibility needs
 */
@Service
@Transactional
public class ContentRecommendationEngine {
    
    @Autowired
    private ContentRecommendationRepository recommendationRepository;
    
    @Autowired
    private LearningContentRepository contentRepository;
    
    @Autowired
    private StudentContentInteractionRepository interactionRepository;
    
    @Autowired
    private UserNeurodivergentProfileService profileService;
    
    @Autowired
    private ContentAccessibilityTagRepository accessibilityTagRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Generate comprehensive personalized recommendations for a student
     */
    public List<ContentRecommendation> generateRecommendations(Long studentId) {
        UserNeurodivergentProfile profile = profileService.getOrCreateProfile(studentId);
        List<StudentContentInteraction> history = interactionRepository.findByStudentIdOrderByLastAccessedAtDesc(studentId);
        
        List<ContentRecommendation> recommendations = new ArrayList<>();
        
        // 1. Profile-based recommendations (collaborative filtering)
        recommendations.addAll(generateProfileBasedRecommendations(studentId, profile));
        
        // 2. Content-based recommendations (similar content)
        recommendations.addAll(generateContentBasedRecommendations(studentId, history));
        
        // 3. Learning pattern recommendations
        recommendations.addAll(generatePatternBasedRecommendations(studentId, history, profile));
        
        // 4. Accessibility-focused recommendations
        recommendations.addAll(generateAccessibilityBasedRecommendations(studentId, profile));
        
        // 5. Trending content recommendations
        recommendations.addAll(generateTrendingRecommendations(studentId, profile));
        
        // Rank and filter recommendations
        List<ContentRecommendation> finalRecommendations = rankAndFilterRecommendations(recommendations, profile);
        
        // Save to database
        return recommendationRepository.saveAll(finalRecommendations);
    }
    
    /**
     * Generate recommendations based on similar user profiles (collaborative filtering)
     */
    private List<ContentRecommendation> generateProfileBasedRecommendations(Long studentId, UserNeurodivergentProfile profile) {
        List<ContentRecommendation> recommendations = new ArrayList<>();
        
        // Find similar users based on neurodivergent traits
        List<UserNeurodivergentProfile> similarProfiles = findSimilarProfiles(profile);
        
        // Get content that worked well for similar users
        for (UserNeurodivergentProfile similarProfile : similarProfiles.stream().limit(5).collect(Collectors.toList())) {
            List<StudentContentInteraction> successfulInteractions = interactionRepository
                    .findHelpfulContent(similarProfile.getUserId())
                    .stream()
                    .filter(interaction -> interaction.hadPositiveOutcome())
                    .limit(3)
                    .collect(Collectors.toList());
            
            for (StudentContentInteraction interaction : successfulInteractions) {
                // Check if student hasn't already interacted with this content
                if (!hasInteractedWith(studentId, interaction.getContent().getId())) {
                    ContentRecommendation recommendation = createRecommendation(
                        studentId,
                        interaction.getContent(),
                        "personalized",
                        calculateCollaborativeScore(profile, similarProfile, interaction),
                        "Content that worked well for users with similar learning profiles",
                        List.of("collaborative_filtering", "similar_users", similarProfile.getId().toString())
                    );
                    recommendations.add(recommendation);
                }
            }
        }
        
        return recommendations;
    }
    
    /**
     * Generate recommendations based on content similarity to previously consumed content
     */
    private List<ContentRecommendation> generateContentBasedRecommendations(Long studentId, List<StudentContentInteraction> history) {
        List<ContentRecommendation> recommendations = new ArrayList<>();
        
        // Get content user liked/found helpful
        List<LearningContent> likedContent = history.stream()
                .filter(StudentContentInteraction::hadPositiveOutcome)
                .map(StudentContentInteraction::getContent)
                .limit(5)
                .collect(Collectors.toList());
        
        for (LearningContent content : likedContent) {
            // Find similar content by subject, difficulty, and accessibility features
            List<LearningContent> similarContent = findSimilarContent(content, studentId);
            
            for (LearningContent similar : similarContent.stream().limit(2).collect(Collectors.toList())) {
                ContentRecommendation recommendation = createRecommendation(
                    studentId,
                    similar,
                    "similar_content",
                    calculateContentSimilarityScore(content, similar),
                    String.format("Similar to \"%s\" which you found helpful", content.getTitle()),
                    List.of("content_based", "similar_to:" + content.getId(), similar.getSubjectArea())
                );
                recommendations.add(recommendation);
            }
        }
        
        return recommendations;
    }
    
    /**
     * Generate recommendations based on learning patterns and optimal timing
     */
    private List<ContentRecommendation> generatePatternBasedRecommendations(Long studentId, 
                                                                           List<StudentContentInteraction> history,
                                                                           UserNeurodivergentProfile profile) {
        List<ContentRecommendation> recommendations = new ArrayList<>();
        
        // Analyze when user is most successful
        Map<Integer, Double> hourlySuccess = calculateHourlySuccessRates(history);
        int optimalHour = hourlySuccess.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(10); // Default to 10 AM
        
        // Find content suitable for user's optimal learning time
        List<LearningContent> timeAppropriateContent = contentRepository.findByStatusAndIsPublicTrue("published")
                .stream()
                .filter(content -> isAppropriateForTime(content, optimalHour, profile))
                .filter(content -> !hasInteractedWith(studentId, content.getId()))
                .limit(5)
                .collect(Collectors.toList());
        
        for (LearningContent content : timeAppropriateContent) {
            BigDecimal patternScore = calculatePatternMatchScore(content, profile, hourlySuccess);
            
            ContentRecommendation recommendation = createRecommendation(
                studentId,
                content,
                "optimal_timing",
                patternScore,
                String.format("Best consumed during your peak performance time (%d:00)", optimalHour),
                List.of("pattern_based", "optimal_hour:" + optimalHour, "peak_performance")
            );
            recommendation.setOptimalTiming(String.format("%d:00-%d:00", optimalHour, optimalHour + 1));
            recommendations.add(recommendation);
        }
        
        return recommendations;
    }
    
    /**
     * Generate recommendations focused on accessibility compatibility
     */
    private List<ContentRecommendation> generateAccessibilityBasedRecommendations(Long studentId, UserNeurodivergentProfile profile) {
        List<ContentRecommendation> recommendations = new ArrayList<>();
        
        // Get content with high accessibility compatibility for user's needs
        List<LearningContent> accessibleContent = new ArrayList<>();
        
        // Dyslexia-friendly content
        if (profile.getHyperfocusIntensity() != null && profile.getHyperfocusIntensity() > 6) {
            accessibleContent.addAll(contentRepository.findDyslexiaFriendlyContent(true));
        }
        
        // ADHD-friendly content
        if (profile.needsExecutiveSupport()) {
            accessibleContent.addAll(contentRepository.findADHDFriendlyContent(true));
        }
        
        // Autism-friendly content
        if (profile.isSensoryProcessingHigh()) {
            accessibleContent.addAll(contentRepository.findAutismFriendlyContent(true));
        }
        
        // Filter out already interacted content and limit results
        accessibleContent = accessibleContent.stream()
                .distinct()
                .filter(content -> !hasInteractedWith(studentId, content.getId()))
                .limit(8)
                .collect(Collectors.toList());
        
        for (LearningContent content : accessibleContent) {
            BigDecimal accessibilityScore = calculateAccessibilityMatchScore(content, profile);
            
            ContentRecommendation recommendation = createRecommendation(
                studentId,
                content,
                "accessibility_match",
                accessibilityScore,
                "Designed specifically for your accessibility needs",
                List.of("accessibility_focused", getAccessibilityTags(content), "high_compatibility")
            );
            recommendations.add(recommendation);
        }
        
        return recommendations;
    }
    
    /**
     * Generate recommendations for trending/popular content
     */
    private List<ContentRecommendation> generateTrendingRecommendations(Long studentId, UserNeurodivergentProfile profile) {
        List<ContentRecommendation> recommendations = new ArrayList<>();
        
        // Get popular content that matches user's accessibility needs
        List<LearningContent> trendingContent = contentRepository.findHighRatedContent(BigDecimal.valueOf(4.0))
                .stream()
                .filter(content -> content.isPopular())
                .filter(content -> isAccessibleFor(content, profile))
                .filter(content -> !hasInteractedWith(studentId, content.getId()))
                .limit(3)
                .collect(Collectors.toList());
        
        for (LearningContent content : trendingContent) {
            BigDecimal trendingScore = BigDecimal.valueOf(0.75)
                    .add(BigDecimal.valueOf(content.getViewCount() / 1000.0).min(BigDecimal.valueOf(0.2)));
            
            ContentRecommendation recommendation = createRecommendation(
                studentId,
                content,
                "trending",
                trendingScore,
                "Popular content with high ratings from learners like you",
                List.of("trending", "popular", "high_rated")
            );
            recommendations.add(recommendation);
        }
        
        return recommendations;
    }
    
    /**
     * Rank recommendations by overall score and filter duplicates
     */
    private List<ContentRecommendation> rankAndFilterRecommendations(List<ContentRecommendation> recommendations, 
                                                                    UserNeurodivergentProfile profile) {
        // Remove duplicates based on content ID
        Map<Long, ContentRecommendation> uniqueRecommendations = new HashMap<>();
        
        for (ContentRecommendation rec : recommendations) {
            Long contentId = rec.getContent().getId();
            if (!uniqueRecommendations.containsKey(contentId) || 
                rec.getConfidenceScore().compareTo(uniqueRecommendations.get(contentId).getConfidenceScore()) > 0) {
                uniqueRecommendations.put(contentId, rec);
            }
        }
        
        // Calculate final scores and rank
        return uniqueRecommendations.values().stream()
                .peek(this::calculateFinalScores)
                .sorted((r1, r2) -> r2.calculateOverallScore().compareTo(r1.calculateOverallScore()))
                .limit(15) // Top 15 recommendations
                .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private List<UserNeurodivergentProfile> findSimilarProfiles(UserNeurodivergentProfile profile) {
        // This would implement profile similarity algorithm
        // For now, return empty list - would integrate with profile service
        return List.of();
    }
    
    private boolean hasInteractedWith(Long studentId, Long contentId) {
        return interactionRepository.findByStudentIdAndContentId(studentId, contentId).isPresent();
    }
    
    private BigDecimal calculateCollaborativeScore(UserNeurodivergentProfile profile, 
                                                 UserNeurodivergentProfile similarProfile,
                                                 StudentContentInteraction interaction) {
        // Calculate similarity between profiles (0.5-1.0)
        double profileSimilarity = 0.8; // Placeholder
        
        // Factor in interaction success
        double interactionSuccess = interaction.calculateOverallSuccess().doubleValue();
        
        return BigDecimal.valueOf(profileSimilarity * 0.6 + interactionSuccess * 0.4)
                .setScale(3, RoundingMode.HALF_UP);
    }
    
    private List<LearningContent> findSimilarContent(LearningContent content, Long studentId) {
        // Find content with same subject area and similar accessibility features
        return contentRepository.findBySubjectAreaAndStatusAndIsPublicTrue(
                content.getSubjectArea(), "published")
                .stream()
                .filter(c -> !c.getId().equals(content.getId()))
                .filter(c -> !hasInteractedWith(studentId, c.getId()))
                .filter(c -> hasSimilarAccessibilityFeatures(content, c))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    private BigDecimal calculateContentSimilarityScore(LearningContent content1, LearningContent content2) {
        double score = 0.5; // Base similarity
        
        // Same subject area
        if (content1.getSubjectArea().equals(content2.getSubjectArea())) {
            score += 0.2;
        }
        
        // Similar difficulty
        if (Objects.equals(content1.getDifficultyLevel(), content2.getDifficultyLevel())) {
            score += 0.15;
        }
        
        // Similar accessibility features
        if (hasSimilarAccessibilityFeatures(content1, content2)) {
            score += 0.15;
        }
        
        return BigDecimal.valueOf(score).setScale(3, RoundingMode.HALF_UP);
    }
    
    private Map<Integer, Double> calculateHourlySuccessRates(List<StudentContentInteraction> history) {
        return history.stream()
                .filter(i -> i.getStartedAt() != null && i.hadPositiveOutcome())
                .collect(Collectors.groupingBy(
                    i -> i.getStartedAt().getHour(),
                    Collectors.averagingDouble(i -> i.calculateOverallSuccess().doubleValue())
                ));
    }
    
    private boolean isAppropriateForTime(LearningContent content, int hour, UserNeurodivergentProfile profile) {
        // Check if content difficulty matches user's energy levels at this time
        if (hour < 9 || hour > 17) { // Early morning or evening
            return "beginner".equals(content.getDifficultyLevel());
        }
        return true; // Daytime is good for any difficulty
    }
    
    private BigDecimal calculatePatternMatchScore(LearningContent content, 
                                                UserNeurodivergentProfile profile,
                                                Map<Integer, Double> hourlySuccess) {
        double score = 0.7; // Base score
        
        // Boost score if content matches user's learning patterns
        if (content.getEstimatedDurationMinutes() != null && 
            content.getEstimatedDurationMinutes() <= profile.getOptimalSessionLength() + 10) {
            score += 0.1;
        }
        
        // Factor in user's peak performance data
        double avgSuccess = hourlySuccess.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.7);
        score = score * (0.8 + avgSuccess * 0.2);
        
        return BigDecimal.valueOf(score).setScale(3, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateAccessibilityMatchScore(LearningContent content, UserNeurodivergentProfile profile) {
        double score = 0.0;
        int matchCount = 0;
        int totalChecks = 0;
        
        // Check dyslexia compatibility
        if (profile.getHyperfocusIntensity() != null && profile.getHyperfocusIntensity() > 6) {
            totalChecks++;
            if (Boolean.TRUE.equals(content.getDyslexiaFriendly())) {
                matchCount++;
                score += 0.25;
            }
        }
        
        // Check ADHD compatibility
        if (profile.needsExecutiveSupport()) {
            totalChecks++;
            if (Boolean.TRUE.equals(content.getAdhdFriendly())) {
                matchCount++;
                score += 0.25;
            }
        }
        
        // Check autism compatibility
        if (profile.isSensoryProcessingHigh()) {
            totalChecks++;
            if (Boolean.TRUE.equals(content.getAutismFriendly())) {
                matchCount++;
                score += 0.25;
            }
        }
        
        // Check sensory processing compatibility
        if (profile.getSensoryProcessing() != null && profile.getSensoryProcessing() > 7) {
            totalChecks++;
            if (Boolean.TRUE.equals(content.getVisualImpairmentFriendly()) ||
                Boolean.TRUE.equals(content.getHearingImpairmentFriendly())) {
                matchCount++;
                score += 0.25;
            }
        }
        
        // If no specific needs, give moderate score
        if (totalChecks == 0) {
            score = 0.6;
        } else {
            score = Math.max(score, 0.3); // Minimum score for any content
        }
        
        return BigDecimal.valueOf(score).setScale(3, RoundingMode.HALF_UP);
    }
    
    private boolean isAccessibleFor(LearningContent content, UserNeurodivergentProfile profile) {
        if (profile.getHyperfocusIntensity() != null && profile.getHyperfocusIntensity() > 6) {
            return Boolean.TRUE.equals(content.getDyslexiaFriendly());
        }
        if (profile.needsExecutiveSupport()) {
            return Boolean.TRUE.equals(content.getAdhdFriendly());
        }
        if (profile.isSensoryProcessingHigh()) {
            return Boolean.TRUE.equals(content.getAutismFriendly());
        }
        return true; // No specific requirements
    }
    
    private String getAccessibilityTags(LearningContent content) {
        List<String> tags = new ArrayList<>();
        if (Boolean.TRUE.equals(content.getDyslexiaFriendly())) tags.add("dyslexia_friendly");
        if (Boolean.TRUE.equals(content.getAdhdFriendly())) tags.add("adhd_friendly");
        if (Boolean.TRUE.equals(content.getAutismFriendly())) tags.add("autism_friendly");
        return String.join(",", tags);
    }
    
    private boolean hasSimilarAccessibilityFeatures(LearningContent content1, LearningContent content2) {
        int matches = 0;
        if (Objects.equals(content1.getDyslexiaFriendly(), content2.getDyslexiaFriendly())) matches++;
        if (Objects.equals(content1.getAdhdFriendly(), content2.getAdhdFriendly())) matches++;
        if (Objects.equals(content1.getAutismFriendly(), content2.getAutismFriendly())) matches++;
        return matches >= 2; // At least 2 matching accessibility features
    }
    
    private ContentRecommendation createRecommendation(Long studentId, LearningContent content, 
                                                     String type, BigDecimal confidenceScore,
                                                     String reasoning, List<String> factors) {
        ContentRecommendation recommendation = new ContentRecommendation();
        recommendation.setStudentId(studentId);
        recommendation.setContent(content);
        recommendation.setRecommendationType(type);
        recommendation.setConfidenceScore(confidenceScore);
        recommendation.setReasoning(reasoning);
        recommendation.setRecommendationSource("hybrid_ml_engine");
        recommendation.setAlgorithmVersion("2.0");
        
        try {
            recommendation.setMatchingFactors(objectMapper.writeValueAsString(factors));
        } catch (JsonProcessingException e) {
            recommendation.setMatchingFactors("[]");
        }
        
        // Set expiry (recommendations valid for 7 days)
        recommendation.setExpiresAt(LocalDateTime.now().plusDays(7));
        
        return recommendation;
    }
    
    private void calculateFinalScores(ContentRecommendation recommendation) {
        LearningContent content = recommendation.getContent();
        
        // Set relevance score based on content quality
        BigDecimal relevanceScore = BigDecimal.valueOf(0.7);
        if (content.getRatingAverage() != null) {
            relevanceScore = content.getRatingAverage().divide(BigDecimal.valueOf(5), 3, RoundingMode.HALF_UP);
        }
        recommendation.setRelevanceScore(relevanceScore);
        
        // Set success prediction based on content effectiveness
        BigDecimal successPrediction = BigDecimal.valueOf(0.75);
        if (content.getSuccessRate() != null && content.getSuccessRate().compareTo(BigDecimal.ZERO) > 0) {
            successPrediction = content.getSuccessRate().divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);
        }
        recommendation.setSuccessPredictionScore(successPrediction);
        
        // Set priority based on confidence score
        if (recommendation.getConfidenceScore().compareTo(BigDecimal.valueOf(0.85)) >= 0) {
            recommendation.setPriorityLevel("high");
        } else if (recommendation.getConfidenceScore().compareTo(BigDecimal.valueOf(0.65)) >= 0) {
            recommendation.setPriorityLevel("medium");
        } else {
            recommendation.setPriorityLevel("low");
        }
    }
}
