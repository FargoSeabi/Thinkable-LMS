package com.thinkable.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thinkable.backend.controller.NewAssessmentController.AssessmentSubmissionRequest;
import com.thinkable.backend.controller.NewAssessmentController.FontTestAnalysis;
import com.thinkable.backend.controller.NewAssessmentController.FontTestRequest;
import com.thinkable.backend.controller.NewAssessmentController.FontTestResponse;
import com.thinkable.backend.model.*;
import com.thinkable.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentService.class);

    @Autowired
    private UserAssessmentRepository userAssessmentRepository;

    @Autowired
    private FontTestResultRepository fontTestResultRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AdaptiveUISettingsRepository adaptiveUISettingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IntelligentAssessmentService intelligentAssessmentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Assessment thresholds for trait determination
    private static final int ATTENTION_THRESHOLD = 18;
    private static final int READING_THRESHOLD = 15;
    private static final int SOCIAL_THRESHOLD = 16;
    private static final int SENSORY_THRESHOLD = 14;
    private static final int MOTOR_THRESHOLD = 12;

    /**
     * Get or create assessment for user
     */
    public UserAssessment getOrCreateAssessment(Long userId) {
        Optional<UserAssessment> existingAssessment = userAssessmentRepository
            .findTopByUserIdOrderByAssessmentDateDesc(userId);
        
        if (existingAssessment.isPresent() && !existingAssessment.get().getAssessmentCompleted()) {
            return existingAssessment.get();
        }
        
        // Create new assessment
        UserAssessment newAssessment = new UserAssessment(userId);
        return userAssessmentRepository.save(newAssessment);
    }

    /**
     * Get age-appropriate assessment questions for user
     */
    public List<AssessmentQuestion> getAssessmentQuestionsForUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        Integer userAge = calculateUserAge(user);
        
        // Get questions for all categories (using actual category names from database)
        List<String> categories = Arrays.asList("AttentionSupport", "ReadingSupport", "SocialCommunication", "SensoryProcessing", "MotorSkills", "EmotionalRegulation");
        List<AssessmentQuestion> questions = new ArrayList<>();
        
        for (String category : categories) {
            List<AssessmentQuestion> categoryQuestions = assessmentQuestionRepository
                .findQuestionsByCategoryForAge(category, userAge);
            questions.addAll(categoryQuestions);
        }
        
        // Shuffle questions to prevent order bias
        Collections.shuffle(questions);
        
        logger.info("Generated {} assessment questions for user {} (age {})", 
                   questions.size(), userId, userAge);
        
        return questions;
    }

    /**
     * Process font test results
     */
    public List<FontTestResult> processFontTest(Long userId, FontTestRequest request) {
        List<FontTestResult> results = new ArrayList<>();
        
        for (FontTestResponse response : request.getFontResponses()) {
            FontTestResult result = new FontTestResult();
            result.setUserId(userId);
            result.setFontName(response.getFontName());
            result.setReadabilityRating(response.getRating());
            result.setDifficultyReported(response.getDifficulty());
            
            // Convert symptoms map to JSON
            if (response.getSymptoms() != null) {
                ObjectNode symptomsNode = objectMapper.createObjectNode();
                response.getSymptoms().forEach(symptomsNode::put);
                result.setSymptomsReported(symptomsNode);
            }
            
            FontTestResult savedResult = fontTestResultRepository.save(result);
            results.add(savedResult);
        }
        
        logger.info("Processed font test results for user {}: {} fonts tested", userId, results.size());
        return results;
    }

    /**
     * Analyze font test results for dyslexia indicators
     */
    public FontTestAnalysis analyzeFontTestResults(Long userId, List<FontTestResult> results) {
        FontTestAnalysis analysis = new FontTestAnalysis();
        Map<String, Boolean> indicators = new HashMap<>();
        List<String> recommendedFonts = new ArrayList<>();
        
        // Check for serif font difficulties
        boolean serifDifficulty = results.stream()
            .anyMatch(r -> isSerifFont(r.getFontName()) && "hard".equals(r.getDifficultyReported()));
        
        // Check for dyslexia-friendly font preferences
        boolean dyslexiaFontPreference = results.stream()
            .anyMatch(r -> isDyslexiaFriendlyFont(r.getFontName()) && "easy".equals(r.getDifficultyReported()));
        
        // Check for movement symptoms
        boolean hasMovementSymptoms = results.stream()
            .anyMatch(FontTestResult::hasMovementSymptoms);
        
        // Check for eye strain
        boolean hasEyeStrain = results.stream()
            .anyMatch(FontTestResult::hasEyeStrain);
        
        // Determine dyslexia likelihood
        boolean likelyDyslexia = (serifDifficulty && dyslexiaFontPreference) || 
                                (hasMovementSymptoms && hasEyeStrain);
        
        indicators.put("serifDifficulty", serifDifficulty);
        indicators.put("dyslexiaFontPreference", dyslexiaFontPreference);
        indicators.put("hasMovementSymptoms", hasMovementSymptoms);
        indicators.put("hasEyeStrain", hasEyeStrain);
        indicators.put("likelyDyslexia", likelyDyslexia);
        
        // Generate font recommendations
        recommendedFonts = generateFontRecommendations(results, likelyDyslexia);
        
        // Generate analysis text
        String analysisText = generateFontAnalysisText(indicators, recommendedFonts);
        
        analysis.setDyslexiaIndicators(indicators);
        analysis.setRecommendedFonts(recommendedFonts);
        analysis.setAnalysis(analysisText);
        
        return analysis;
    }

    /**
     * Calculate assessment results using INTELLIGENT evidence-based analysis
     * This replaces the broken rule-based approach with proper multi-modal assessment
     */
    public UserAssessment calculateAssessmentResults(Long userId, AssessmentSubmissionRequest request) {
        logger.info("Starting INTELLIGENT assessment calculation for user {}", userId);
        
        UserAssessment assessment = getOrCreateAssessment(userId);
        
        // Calculate scores by category (still needed for individual metrics)
        Map<String, Integer> categoryScores = calculateCategoryScores(request.getResponses());
        
        // Update assessment with calculated scores (map database categories to expected fields)
        assessment.setAttentionScore(categoryScores.getOrDefault("AttentionSupport", 0));
        assessment.setSocialCommunicationScore(categoryScores.getOrDefault("SocialCommunication", 0));
        assessment.setSensoryProcessingScore(categoryScores.getOrDefault("SensoryProcessing", 0));
        assessment.setReadingDifficultyScore(categoryScores.getOrDefault("ReadingSupport", 0));
        assessment.setMotorSkillsScore(categoryScores.getOrDefault("MotorSkills", 0));
        
        // Get user's font test results (CRITICAL for dyslexia detection)
        List<FontTestResult> fontTestResults = fontTestResultRepository.findByUserIdOrderByTestDateDesc(userId);
        
        // Use INTELLIGENT ASSESSMENT SERVICE instead of broken rules
        String recommendedPreset = intelligentAssessmentService.determineOptimalPreset(
            assessment, 
            fontTestResults, 
            request.getResponses()
        );
        
        logger.info("INTELLIGENT assessment determined preset '{}' for user {} (old system would have chosen '{}')", 
                   recommendedPreset, userId, determineUIPreset(assessment));
        
        assessment.setRecommendedPreset(recommendedPreset);
        
        // Mark assessment as completed
        assessment.setAssessmentCompleted(true);
        assessment.setAssessmentDate(LocalDateTime.now());
        
        // Store additional assessment data with intelligent reasoning
        ObjectNode adaptationsNode = objectMapper.createObjectNode();
        adaptationsNode.put("preset", recommendedPreset);
        adaptationsNode.put("intelligentAnalysis", true);
        adaptationsNode.put("fontTestConsidered", fontTestResults != null && !fontTestResults.isEmpty());
        adaptationsNode.set("traits", createTraitsNode(assessment));
        assessment.setUiAdaptations(adaptationsNode);
        
        UserAssessment savedAssessment = userAssessmentRepository.save(assessment);
        
        logger.info("COMPLETED INTELLIGENT assessment for user {}: preset = {}, scores = {}, fontTests = {}", 
                   userId, recommendedPreset, categoryScores, fontTestResults.size());
        
        return savedAssessment;
    }

    /**
     * Create adaptive UI settings based on assessment results
     */
    public AdaptiveUISettings createAdaptiveUISettings(Long userId, UserAssessment assessment) {
        // Check if user already has settings
        Optional<AdaptiveUISettings> existingSettings = adaptiveUISettingsRepository.findByUserId(userId);
        
        AdaptiveUISettings settings;
        if (existingSettings.isPresent()) {
            settings = existingSettings.get();
        } else {
            settings = new AdaptiveUISettings(userId);
        }
        
        // Apply preset-based defaults
        settings.setUiPreset(assessment.getRecommendedPreset());
        settings.applyPresetDefaults(assessment.getRecommendedPreset());
        
        // Customize based on font test results if available
        customizeSettingsFromFontTest(userId, settings);
        
        // Mark as auto-applied
        settings.setAutoApplied(true);
        
        return adaptiveUISettingsRepository.save(settings);
    }

    /**
     * Get latest assessment for user
     */
    public UserAssessment getLatestAssessment(Long userId) {
        return userAssessmentRepository.findTopByUserIdOrderByAssessmentDateDesc(userId)
            .orElse(null);
    }

    /**
     * Get UI settings for user
     */
    public AdaptiveUISettings getUISettings(Long userId) {
        return adaptiveUISettingsRepository.findByUserId(userId).orElse(null);
    }

    /**
     * Save UI settings
     */
    public AdaptiveUISettings saveUISettings(AdaptiveUISettings settings) {
        return adaptiveUISettingsRepository.save(settings);
    }

    /**
     * Update AI recommendations for user
     */
    public void updateAIRecommendations(Long userId, String recommendations) {
        UserAssessment assessment = getLatestAssessment(userId);
        if (assessment != null) {
            ObjectNode adaptationsNode = (ObjectNode) assessment.getUiAdaptations();
            if (adaptationsNode == null) {
                adaptationsNode = objectMapper.createObjectNode();
            }
            adaptationsNode.put("aiRecommendations", recommendations);
            assessment.setUiAdaptations(adaptationsNode);
            userAssessmentRepository.save(assessment);
        }
    }

    /**
     * Get assessment statistics for dashboard
     */
    public Map<String, Object> getAssessmentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        long totalAssessments = userAssessmentRepository.count();
        long completedAssessments = userAssessmentRepository.countCompletedAssessments();
        long fontTestUsers = fontTestResultRepository.countUsersWithFontTests();
        
        // Preset distribution
        List<Object[]> presetDistribution = userAssessmentRepository.getPresetDistribution();
        Map<String, Long> presetCounts = new HashMap<>();
        for (Object[] row : presetDistribution) {
            presetCounts.put((String) row[0], ((Number) row[1]).longValue());
        }
        
        // Average scores
        Object[] averageScores = userAssessmentRepository.getAverageScores();
        Map<String, Double> avgScores = new HashMap<>();
        if (averageScores.length >= 5) {
            avgScores.put("attention", ((Number) averageScores[0]).doubleValue());
            avgScores.put("social", ((Number) averageScores[1]).doubleValue());
            avgScores.put("sensory", ((Number) averageScores[2]).doubleValue());
            avgScores.put("reading", ((Number) averageScores[3]).doubleValue());
            avgScores.put("motor", ((Number) averageScores[4]).doubleValue());
        }
        
        // Accessibility feature usage
        Object[] accessibilityUsage = adaptiveUISettingsRepository.getAccessibilityFeatureUsage();
        Map<String, Long> featureUsage = new HashMap<>();
        if (accessibilityUsage.length >= 5) {
            featureUsage.put("largeFontUsers", ((Number) accessibilityUsage[0]).longValue());
            featureUsage.put("highContrastUsers", ((Number) accessibilityUsage[1]).longValue());
            featureUsage.put("noAnimationUsers", ((Number) accessibilityUsage[2]).longValue());
            featureUsage.put("shortBreakUsers", ((Number) accessibilityUsage[3]).longValue());
            featureUsage.put("totalUIUsers", ((Number) accessibilityUsage[4]).longValue());
        }
        
        stats.put("totalAssessments", totalAssessments);
        stats.put("completedAssessments", completedAssessments);
        stats.put("completionRate", totalAssessments > 0 ? (double) completedAssessments / totalAssessments : 0.0);
        stats.put("fontTestUsers", fontTestUsers);
        stats.put("presetDistribution", presetCounts);
        stats.put("averageScores", avgScores);
        stats.put("accessibilityFeatureUsage", featureUsage);
        
        return stats;
    }

    // Helper methods
    private Integer calculateUserAge(User user) {
        // Try to get age from age range or calculate from birth date
        String ageRange = user.getAgeRange();
        if (ageRange != null) {
            return parseAgeFromRange(ageRange);
        }
        
        // Default to young adult if no age info
        return 16;
    }

    private Integer parseAgeFromRange(String ageRange) {
        switch (ageRange) {
            case "5-8": return 7;
            case "9-12": return 11;
            case "13-16": return 15;
            case "17+": return 18;
            default: return 16;
        }
    }

    private Map<String, Integer> calculateCategoryScores(Map<String, Object> responses) {
        Map<String, Integer> categoryScores = new HashMap<>();
        Map<String, List<Integer>> categoryResponses = new HashMap<>();
        
        // Group responses by category
        for (Map.Entry<String, Object> entry : responses.entrySet()) {
            String questionId = entry.getKey();
            Object responseValue = entry.getValue();
            
            // Get question to determine category
            try {
                Long qId = Long.parseLong(questionId);
                Optional<AssessmentQuestion> question = assessmentQuestionRepository.findById(qId);
                
                if (question.isPresent()) {
                    String category = question.get().getCategory();
                    Integer score = convertResponseToScore(responseValue, question.get());
                    
                    categoryResponses.computeIfAbsent(category, k -> new ArrayList<>()).add(score);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid question ID format: {}", questionId);
            }
        }
        
        // Calculate category totals
        for (Map.Entry<String, List<Integer>> entry : categoryResponses.entrySet()) {
            String category = entry.getKey();
            List<Integer> scores = entry.getValue();
            Integer total = scores.stream().mapToInt(Integer::intValue).sum();
            categoryScores.put(category, total);
        }
        
        return categoryScores;
    }

    private Integer convertResponseToScore(Object responseValue, AssessmentQuestion question) {
        if (responseValue instanceof Number) {
            return ((Number) responseValue).intValue();
        } else if (responseValue instanceof String) {
            String response = (String) responseValue;
            
            if (question.isBinary()) {
                return "Yes".equalsIgnoreCase(response) ? 1 : 0;
            } else if (question.isLikertScale()) {
                // Convert text responses to numeric scores
                switch (response.toLowerCase()) {
                    case "never": case "not at all": case "very easy": return 1;
                    case "rarely": case "slightly": case "easy": return 2;
                    case "sometimes": case "moderate": case "neutral": return 3;
                    case "often": case "difficult": case "uncomfortable": return 4;
                    case "always": case "very difficult": case "very uncomfortable": return 5;
                    default: return 3; // Default to middle score
                }
            }
        }
        
        return 0; // Default score
    }

    private String determineUIPreset(UserAssessment assessment) {
        List<String> significantTraits = new ArrayList<>();
        
        if (assessment.hasSignificantAttentionNeeds()) {
            significantTraits.add("adhd");
        }
        if (assessment.hasSignificantReadingNeeds()) {
            significantTraits.add("dyslexia");
        }
        if (assessment.hasSignificantSocialNeeds()) {
            significantTraits.add("autism");
        }
        if (assessment.hasSignificantSensoryNeeds()) {
            significantTraits.add("sensory");
        }
        
        // Determine preset based on trait combinations
        if (significantTraits.isEmpty()) {
            return "standard";
        } else if (significantTraits.size() == 1) {
            return significantTraits.get(0);
        } else if (significantTraits.contains("dyslexia") && significantTraits.contains("adhd")) {
            return "dyslexia-adhd";
        } else {
            // Return the trait with the highest score
            return getHighestScoringTrait(assessment);
        }
    }

    private String getHighestScoringTrait(UserAssessment assessment) {
        Map<String, Integer> traitScores = new HashMap<>();
        traitScores.put("adhd", assessment.getAttentionScore());
        traitScores.put("dyslexia", assessment.getReadingDifficultyScore());
        traitScores.put("autism", assessment.getSocialCommunicationScore());
        traitScores.put("sensory", assessment.getSensoryProcessingScore());
        
        return traitScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("standard");
    }

    private JsonNode createTraitsNode(UserAssessment assessment) {
        ObjectNode traitsNode = objectMapper.createObjectNode();
        
        traitsNode.put("attention", assessment.hasSignificantAttentionNeeds());
        traitsNode.put("reading", assessment.hasSignificantReadingNeeds());
        traitsNode.put("social", assessment.hasSignificantSocialNeeds());
        traitsNode.put("sensory", assessment.hasSignificantSensoryNeeds());
        
        return traitsNode;
    }

    private void customizeSettingsFromFontTest(Long userId, AdaptiveUISettings settings) {
        List<FontTestResult> fontResults = fontTestResultRepository.findByUserIdOrderByTestDateDesc(userId);
        
        if (!fontResults.isEmpty()) {
            // Find preferred font
            Optional<FontTestResult> preferredFont = fontResults.stream()
                .filter(r -> "easy".equals(r.getDifficultyReported()))
                .findFirst();
            
            if (preferredFont.isPresent()) {
                String fontName = preferredFont.get().getFontName();
                settings.setFontFamily(mapFontNameToCSS(fontName));
                
                // If dyslexia-friendly font preferred, enhance other settings
                if (isDyslexiaFriendlyFont(fontName)) {
                    settings.setLineHeight(settings.getLineHeight().add(new java.math.BigDecimal("0.2")));
                    settings.setBackgroundColor("#fffef7"); // Cream background
                }
            }
        }
    }

    private boolean isSerifFont(String fontName) {
        return Arrays.asList("Times New Roman", "Georgia", "Times", "serif").contains(fontName);
    }

    private boolean isDyslexiaFriendlyFont(String fontName) {
        return Arrays.asList("Comic Neue", "OpenDyslexic", "Lexie Readable", "Dyslexie").contains(fontName);
    }

    private String mapFontNameToCSS(String fontName) {
        switch (fontName) {
            case "Comic Neue": return "Comic Neue, cursive";
            case "OpenDyslexic": return "OpenDyslexic, monospace";
            case "Times New Roman": return "Times New Roman, serif";
            case "Arial": return "Arial, sans-serif";
            case "Verdana": return "Verdana, sans-serif";
            default: return fontName + ", sans-serif";
        }
    }

    private List<String> generateFontRecommendations(List<FontTestResult> results, boolean likelyDyslexia) {
        List<String> recommendations = new ArrayList<>();
        
        if (likelyDyslexia) {
            recommendations.add("Comic Neue");
            recommendations.add("OpenDyslexic");
            recommendations.add("Lexie Readable");
        } else {
            // Find fonts rated as "easy"
            Set<String> easyFonts = results.stream()
                .filter(r -> "easy".equals(r.getDifficultyReported()))
                .map(FontTestResult::getFontName)
                .collect(Collectors.toSet());
            
            if (!easyFonts.isEmpty()) {
                recommendations.addAll(easyFonts);
            } else {
                recommendations.add("Arial");
                recommendations.add("Verdana");
            }
        }
        
        return recommendations;
    }

    private String generateFontAnalysisText(Map<String, Boolean> indicators, List<String> recommendedFonts) {
        StringBuilder analysis = new StringBuilder();
        
        if (indicators.get("likelyDyslexia")) {
            analysis.append("Assessment suggests potential reading support benefits. ");
            analysis.append("Dyslexia-friendly fonts and increased spacing may improve reading comfort. ");
        } else {
            analysis.append("Good font flexibility observed. ");
            analysis.append("Standard fonts work well with possible customization options. ");
        }
        
        analysis.append("Recommended fonts: ").append(String.join(", ", recommendedFonts));
        
        return analysis.toString();
    }

    public int getTotalQuestionsForUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Integer userAge = calculateUserAge(user);
        return assessmentQuestionRepository.getFullAssessmentForAge(userAge).size();
    }

    public int getAnsweredQuestionsCount(Long userId) {
        // This would need to be implemented based on how you track question responses
        // For now, return 0 as placeholder
        return 0;
    }
}
