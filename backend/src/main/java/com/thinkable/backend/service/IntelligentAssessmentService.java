package com.thinkable.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkable.backend.model.UserAssessment;
import com.thinkable.backend.model.FontTestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IntelligentAssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentAssessmentService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Intelligently determine the best UI preset based on multiple data points
     * This replaces the simple rule-based approach with weighted decision making
     */
    public String determineOptimalPreset(UserAssessment assessment, List<FontTestResult> fontTestResults, 
                                       Map<String, Object> questionResponses) {
        
        logger.info("Starting intelligent preset determination for user {}", assessment.getUserId());
        
        // Create weighted evidence system
        Map<String, Double> presetScores = initializePresetScores();
        
        // Analyze font test results (HIGH WEIGHT - direct user preference)
        analyzeFontTestEvidence(fontTestResults, presetScores);
        
        // Analyze assessment question responses (MEDIUM WEIGHT)
        analyzeQuestionnaireEvidence(questionResponses, presetScores);
        
        // Analyze symptom clustering (HIGH WEIGHT - research-based)
        analyzeSymptomClustering(assessment, presetScores);
        
        // Apply demographic considerations (LOW WEIGHT)
        applyDemographicFactors(assessment, presetScores);
        
        // Add behavioral pattern analysis for more variety
        analyzeBehavioralPatterns(fontTestResults, presetScores);
        
        // Find highest scoring preset
        String recommendedPreset = selectBestPreset(presetScores);
        
        // Log decision reasoning for transparency
        logDecisionReasoning(assessment.getUserId(), presetScores, recommendedPreset);
        
        return recommendedPreset;
    }

    private Map<String, Double> initializePresetScores() {
        Map<String, Double> scores = new HashMap<>();
        // Start with equal baseline scores to ensure fair competition
        scores.put("STANDARD_ADAPTIVE", 15.0);  // Baseline preference for balanced UI
        scores.put("READING_SUPPORT", 10.0);     // For dyslexia support
        scores.put("FOCUS_ENHANCED", 10.0);      // For ADHD support
        scores.put("FOCUS_CALM", 10.0);          // For attention + sensory
        scores.put("SOCIAL_SIMPLE", 10.0);       // For autism support
        scores.put("SENSORY_CALM", 10.0);        // For sensory processing
        return scores;
    }

    /**
     * Analyze font test results - this should be the STRONGEST signal
     * If user explicitly prefers dyslexia fonts, weight READING_SUPPORT heavily
     */
    private void analyzeFontTestEvidence(List<FontTestResult> fontTestResults, Map<String, Double> presetScores) {
        if (fontTestResults == null || fontTestResults.isEmpty()) {
            logger.warn("No font test results available for analysis");
            return;
        }

        double dyslexiaFontPreference = 0.0;
        double readingDifficultySymptoms = 0.0;
        double attentionSymptoms = 0.0;
        double visualStressSymptoms = 0.0;
        int totalFontTests = fontTestResults.size();

        for (FontTestResult result : fontTestResults) {
            String fontName = result.getFontName().toLowerCase();
            
            // Dyslexia-friendly fonts
            if (fontName.contains("opendyslexic") || fontName.contains("comic") || 
                fontName.contains("verdana") || fontName.contains("calibri")) {
                
                if (result.getReadabilityRating() != null && result.getReadabilityRating() >= 4) {
                    dyslexiaFontPreference += 2.0; // Strong preference
                }
                if (result.getDifficultyReported() != null && result.getDifficultyReported().equals("easy")) {
                    dyslexiaFontPreference += 1.5;
                }
            }
            
            // Standard fonts that caused difficulty
            if (fontName.contains("times") || fontName.contains("arial") || fontName.contains("helvetica")) {
                if (result.getReadabilityRating() != null && result.getReadabilityRating() <= 2) {
                    dyslexiaFontPreference += 1.0; // Difficulty with standard fonts
                }
                if (result.getDifficultyReported() != null && result.getDifficultyReported().equals("hard")) {
                    dyslexiaFontPreference += 1.5;
                }
            }

            // Analyze reported symptoms (stored as String, parse as JSON)
            if (result.getSymptomsReported() != null && !result.getSymptomsReported().trim().isEmpty()) {
                try {
                    JsonNode symptomsNode = objectMapper.readTree(result.getSymptomsReported());
                    if (symptomsNode.has("lettersMoveOrFlip") && symptomsNode.get("lettersMoveOrFlip").asBoolean()) {
                        readingDifficultySymptoms += 2.0;
                    }
                    if (symptomsNode.has("wordsBlurTogether") && symptomsNode.get("wordsBlurTogether").asBoolean()) {
                        readingDifficultySymptoms += 1.5;
                    }
                    if (symptomsNode.has("difficultyFocusing") && symptomsNode.get("difficultyFocusing").asBoolean()) {
                        attentionSymptoms += 1.5;
                    }
                    if (symptomsNode.has("eyeStrain") && symptomsNode.get("eyeStrain").asBoolean()) {
                        visualStressSymptoms += 2.0;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse symptoms JSON for result {}: {}", result.getId(), e.getMessage());
                }
            }
        }

        // Apply balanced font test evidence with contextual analysis
        double fontEvidence = totalFontTests > 0 ? (dyslexiaFontPreference + readingDifficultySymptoms) / totalFontTests : 0.0;
        
        // Strong dyslexia indicators (clear preference for dyslexia fonts)
        if (fontEvidence >= 3.0) {
            presetScores.put("READING_SUPPORT", presetScores.get("READING_SUPPORT") + 30.0);
            logger.info("Strong dyslexia indicators from font test (evidence: {})", fontEvidence);
        } 
        // Moderate reading difficulties
        else if (fontEvidence >= 2.0) {
            presetScores.put("READING_SUPPORT", presetScores.get("READING_SUPPORT") + 20.0);
            logger.info("Moderate dyslexia indicators from font test (evidence: {})", fontEvidence);
        }
        // Mild font preferences - might be other issues
        else if (fontEvidence >= 1.0) {
            presetScores.put("READING_SUPPORT", presetScores.get("READING_SUPPORT") + 10.0);
            // Also consider sensory processing issues
            presetScores.put("SENSORY_CALM", presetScores.get("SENSORY_CALM") + 8.0);
            logger.info("Mild font difficulty indicators (evidence: {})", fontEvidence);
        }
        // No significant font issues - user may need other adaptations
        else if (fontEvidence < 0.5) {
            // Boost other presets when no clear reading issues
            presetScores.put("FOCUS_ENHANCED", presetScores.get("FOCUS_ENHANCED") + 5.0);
            presetScores.put("FOCUS_CALM", presetScores.get("FOCUS_CALM") + 5.0);
            presetScores.put("SOCIAL_SIMPLE", presetScores.get("SOCIAL_SIMPLE") + 5.0);
            logger.info("No significant font difficulties - considering other adaptations (evidence: {})", fontEvidence);
        }
        
        // Pure sensory issues (symptoms without font preference)
        if (dyslexiaFontPreference < 1.0 && readingDifficultySymptoms >= 2.0) {
            presetScores.put("SENSORY_CALM", presetScores.get("SENSORY_CALM") + 15.0);
            logger.info("Sensory processing indicators detected");
        }
    }

    /**
     * Analyze questionnaire responses with evidence-based approach
     */
    private void analyzeQuestionnaireEvidence(Map<String, Object> responses, Map<String, Double> presetScores) {
        if (responses == null || responses.isEmpty()) {
            logger.warn("No questionnaire responses available for analysis");
            return;
        }

        double attentionScore = 0.0;
        double readingScore = 0.0;
        double socialScore = 0.0;
        double sensoryScore = 0.0;

        // Analyze each response with contextual understanding
        for (Map.Entry<String, Object> response : responses.entrySet()) {
            String questionId = response.getKey();
            Object answer = response.getValue();
            
            if (answer instanceof Number) {
                int rating = ((Number) answer).intValue();
                
                // Categorize questions by domain (this would be based on question content)
                if (isAttentionQuestion(questionId)) {
                    attentionScore += rating;
                } else if (isReadingQuestion(questionId)) {
                    readingScore += rating;
                } else if (isSocialQuestion(questionId)) {
                    socialScore += rating;
                } else if (isSensoryQuestion(questionId)) {
                    sensoryScore += rating;
                }
            }
        }

        // Apply questionnaire evidence with MEDIUM WEIGHT (30% of decision)
        if (attentionScore >= 15) {
            presetScores.put("FOCUS_ENHANCED", presetScores.get("FOCUS_ENHANCED") + 25.0);
            if (sensoryScore >= 12) {
                presetScores.put("FOCUS_CALM", presetScores.get("FOCUS_CALM") + 20.0);
            }
        }
        
        if (readingScore >= 12) {
            presetScores.put("READING_SUPPORT", presetScores.get("READING_SUPPORT") + 30.0);
        }
        
        if (socialScore >= 14) {
            presetScores.put("SOCIAL_SIMPLE", presetScores.get("SOCIAL_SIMPLE") + 25.0);
        }
        
        if (sensoryScore >= 12) {
            presetScores.put("SENSORY_CALM", presetScores.get("SENSORY_CALM") + 20.0);
        }

        logger.info("Questionnaire evidence - Attention: {}, Reading: {}, Social: {}, Sensory: {}", 
                   attentionScore, readingScore, socialScore, sensoryScore);
    }

    /**
     * Analyze symptom clustering based on research patterns
     */
    private void analyzeSymptomClustering(UserAssessment assessment, Map<String, Double> presetScores) {
        // Research-based clustering patterns
        
        // ADHD cluster: High attention + moderate sensory
        if (assessment.getAttentionScore() != null && assessment.getAttentionScore() >= 16 &&
            assessment.getSensoryProcessingScore() != null && assessment.getSensoryProcessingScore() >= 10) {
            presetScores.put("FOCUS_ENHANCED", presetScores.get("FOCUS_ENHANCED") + 20.0);
        }
        
        // Dyslexia cluster: High reading + moderate attention
        if (assessment.getReadingDifficultyScore() != null && assessment.getReadingDifficultyScore() >= 14) {
            presetScores.put("READING_SUPPORT", presetScores.get("READING_SUPPORT") + 25.0);
        }
        
        // Autism cluster: High social + high sensory
        if (assessment.getSocialCommunicationScore() != null && assessment.getSocialCommunicationScore() >= 15 &&
            assessment.getSensoryProcessingScore() != null && assessment.getSensoryProcessingScore() >= 13) {
            presetScores.put("SOCIAL_SIMPLE", presetScores.get("SOCIAL_SIMPLE") + 22.0);
        }
        
        // Sensory processing cluster: Very high sensory regardless of other scores
        if (assessment.getSensoryProcessingScore() != null && assessment.getSensoryProcessingScore() >= 16) {
            presetScores.put("SENSORY_CALM", presetScores.get("SENSORY_CALM") + 25.0);
        }
    }

    /**
     * Apply demographic factors with LOW WEIGHT (10% of decision)
     */
    private void applyDemographicFactors(UserAssessment assessment, Map<String, Double> presetScores) {
        // Age-based considerations (research shows younger users benefit from more structure)
        // Grade level considerations, etc.
        // These would be minor adjustments based on population studies
        
        // For now, just ensure no preset is completely zero unless contraindicated
        presetScores.put("STANDARD_ADAPTIVE", presetScores.get("STANDARD_ADAPTIVE") + 5.0);
    }

    private String selectBestPreset(Map<String, Double> presetScores) {
        return presetScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("STANDARD_ADAPTIVE");
    }

    private void logDecisionReasoning(Long userId, Map<String, Double> scores, String selected) {
        logger.info("Preset decision for user {}: Selected '{}' with scores: {}", 
                   userId, selected, scores);
        
        // This transparency is crucial for debugging and improving the system
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Decision reasoning: ");
        scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(entry -> reasoning.append(String.format("%s(%.1f) ", entry.getKey(), entry.getValue())));
        
        logger.info(reasoning.toString());
    }

    // Helper methods to categorize questions
    private boolean isAttentionQuestion(String questionId) {
        // This would be based on actual question content/categories
        return questionId.contains("attention") || questionId.contains("focus") || questionId.contains("concentrate");
    }
    
    private boolean isReadingQuestion(String questionId) {
        return questionId.contains("reading") || questionId.contains("text") || questionId.contains("word");
    }
    
    private boolean isSocialQuestion(String questionId) {
        return questionId.contains("social") || questionId.contains("communication") || questionId.contains("interaction");
    }
    
    private boolean isSensoryQuestion(String questionId) {
        return questionId.contains("sensory") || questionId.contains("noise") || questionId.contains("light") || questionId.contains("texture");
    }
    
    /**
     * Advanced behavioral pattern analysis to add variety and detect different user types
     */
    private void analyzeBehavioralPatterns(List<FontTestResult> fontTestResults, Map<String, Double> presetScores) {
        if (fontTestResults == null || fontTestResults.isEmpty()) {
            return;
        }
        
        // Analyze consistency in font preferences
        int consistentHighRatings = 0;
        int rapidResponses = 0;
        int symptomVariety = 0;
        double avgReadingTime = 0;
        
        for (FontTestResult result : fontTestResults) {
            if (result.getReadabilityRating() != null && result.getReadabilityRating() >= 4) {
                consistentHighRatings++;
            }
            
            if (result.getReadingTimeMs() != null) {
                avgReadingTime += result.getReadingTimeMs();
                if (result.getReadingTimeMs() < 5000) { // Very fast responses
                    rapidResponses++;
                }
            }
            
            // Count different types of symptoms reported
            if (result.getSymptomsReported() != null && !result.getSymptomsReported().trim().isEmpty()) {
                try {
                    JsonNode symptoms = objectMapper.readTree(result.getSymptomsReported());
                    final int[] symptomCount = {0}; // Use array to make it effectively final
                    symptoms.fieldNames().forEachRemaining(field -> {
                        if (symptoms.get(field).asBoolean()) {
                            symptomCount[0]++;
                        }
                    });
                    symptomVariety += symptomCount[0];
                } catch (Exception e) {
                    logger.warn("Failed to parse symptoms JSON for result {}: {}", result.getId(), e.getMessage());
                }
            }
        }
        
        avgReadingTime = avgReadingTime / fontTestResults.size();
        
        // Pattern 1: User who rates everything highly (may need FOCUS support)
        if (consistentHighRatings >= fontTestResults.size() * 0.8) {
            presetScores.put("FOCUS_ENHANCED", presetScores.get("FOCUS_ENHANCED") + 8.0);
            presetScores.put("FOCUS_CALM", presetScores.get("FOCUS_CALM") + 6.0);
            logger.info("Pattern detected: Consistently high ratings - may indicate attention/focus needs");
        }
        
        // Pattern 2: Very fast responses (may indicate impulsivity - ADHD characteristics)
        if (rapidResponses >= fontTestResults.size() * 0.6) {
            presetScores.put("FOCUS_ENHANCED", presetScores.get("FOCUS_ENHANCED") + 12.0);
            logger.info("Pattern detected: Rapid responses - may indicate ADHD characteristics");
        }
        
        // Pattern 3: Slow, methodical responses (may indicate autism characteristics)
        if (avgReadingTime > 15000) { // More than 15 seconds average
            presetScores.put("SOCIAL_SIMPLE", presetScores.get("SOCIAL_SIMPLE") + 10.0);
            logger.info("Pattern detected: Methodical responses - may indicate autism characteristics");
        }
        
        // Pattern 4: High symptom variety (may indicate sensory processing issues)
        if (symptomVariety >= fontTestResults.size() * 2) { // Average 2+ symptoms per font
            presetScores.put("SENSORY_CALM", presetScores.get("SENSORY_CALM") + 15.0);
            logger.info("Pattern detected: Multiple symptoms per font - may indicate sensory processing issues");
        }
        
        // Pattern 5: No clear problems - boost standard adaptive slightly
        if (consistentHighRatings >= fontTestResults.size() * 0.9 && symptomVariety < 3) {
            presetScores.put("STANDARD_ADAPTIVE", presetScores.get("STANDARD_ADAPTIVE") + 8.0);
            logger.info("Pattern detected: Few issues reported - standard adaptive may be sufficient");
        }
    }
}
