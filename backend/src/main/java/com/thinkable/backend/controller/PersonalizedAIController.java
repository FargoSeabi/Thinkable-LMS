package com.thinkable.backend.controller;

import com.thinkable.backend.model.UserAssessment;
import com.thinkable.backend.model.User;
import com.thinkable.backend.service.AssessmentService;
import com.thinkable.backend.service.AdaptiveLearningAIService;
import com.thinkable.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/ai")
public class PersonalizedAIController {

    private static final Logger logger = LoggerFactory.getLogger(PersonalizedAIController.class);

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private AdaptiveLearningAIService aiService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Generate personalized learning path based on assessment
     */
    @GetMapping("/learning-path/{userId}")
    public ResponseEntity<?> generateLearningPath(@PathVariable Long userId) {
        try {
            logger.info("Generating personalized learning path for user: {}", userId);
            
            UserAssessment assessment = assessmentService.getLatestAssessment(userId);
            if (assessment == null || !assessment.getAssessmentCompleted()) {
                return ResponseEntity.ok(Map.of(
                    "hasAssessment", false,
                    "message", "Complete the neurodivergent assessment to get personalized learning paths"
                ));
            }
            
            Map<String, Object> learningPath = generateAdaptiveLearningPath(assessment);
            
            return ResponseEntity.ok(learningPath);
            
        } catch (Exception e) {
            logger.error("Error generating learning path for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate learning path: " + e.getMessage()));
        }
    }

    /**
     * Generate personalized content recommendations
     */
    @GetMapping("/content-recommendations/{userId}")
    public ResponseEntity<?> generateContentRecommendations(@PathVariable Long userId,
                                                          @RequestParam(required = false) String subject,
                                                          @RequestParam(required = false) String difficulty) {
        try {
            logger.info("Generating content recommendations for user: {}", userId);
            
            UserAssessment assessment = assessmentService.getLatestAssessment(userId);
            if (assessment == null || !assessment.getAssessmentCompleted()) {
                return ResponseEntity.ok(Map.of(
                    "hasAssessment", false,
                    "recommendations", generateBasicRecommendations(subject, difficulty)
                ));
            }
            
            Map<String, Object> recommendations = generatePersonalizedContentRecommendations(
                assessment, subject, difficulty);
            
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            logger.error("Error generating content recommendations for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate recommendations: " + e.getMessage()));
        }
    }

    /**
     * Adapt content based on user's neurodivergent profile
     */
    @PostMapping("/adapt-content/{userId}")
    public ResponseEntity<?> adaptContent(@PathVariable Long userId,
                                        @RequestBody Map<String, String> request) {
        try {
            logger.info("Adapting content for user: {}", userId);
            
            String originalContent = request.get("content");
            String contentType = request.getOrDefault("type", "lesson");
            
            if (originalContent == null || originalContent.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Content cannot be empty"));
            }
            
            UserAssessment assessment = assessmentService.getLatestAssessment(userId);
            Map<String, Object> adaptedContent;
            
            if (assessment == null || !assessment.getAssessmentCompleted()) {
                adaptedContent = Map.of(
                    "originalContent", originalContent,
                    "adaptedContent", originalContent,
                    "adaptations", List.of("No assessment available - using original content"),
                    "hasPersonalization", false
                );
            } else {
                adaptedContent = generateAdaptedContent(originalContent, contentType, assessment);
            }
            
            return ResponseEntity.ok(adaptedContent);
            
        } catch (Exception e) {
            logger.error("Error adapting content for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to adapt content: " + e.getMessage()));
        }
    }

    /**
     * Generate personalized study schedule
     */
    @GetMapping("/study-schedule/{userId}")
    public ResponseEntity<?> generateStudySchedule(@PathVariable Long userId,
                                                 @RequestParam(required = false) Integer hoursPerDay) {
        try {
            logger.info("Generating study schedule for user: {}", userId);
            
            UserAssessment assessment = assessmentService.getLatestAssessment(userId);
            Map<String, Object> schedule;
            
            if (assessment == null || !assessment.getAssessmentCompleted()) {
                schedule = generateBasicSchedule(hoursPerDay);
                schedule.put("hasPersonalization", false);
            } else {
                schedule = generatePersonalizedSchedule(assessment, hoursPerDay);
                schedule.put("hasPersonalization", true);
            }
            
            return ResponseEntity.ok(schedule);
            
        } catch (Exception e) {
            logger.error("Error generating study schedule for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate study schedule: " + e.getMessage()));
        }
    }

    /**
     * Get AI insights about user's learning patterns
     */
    @GetMapping("/insights/{userId}")
    public ResponseEntity<?> getLearningInsights(@PathVariable Long userId) {
        try {
            logger.info("Generating learning insights for user: {}", userId);
            
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
            }
            
            UserAssessment assessment = assessmentService.getLatestAssessment(userId);
            Map<String, Object> insights;
            
            if (assessment == null || !assessment.getAssessmentCompleted()) {
                insights = Map.of(
                    "hasAssessment", false,
                    "message", "Complete the assessment to receive personalized insights",
                    "basicInsights", generateBasicInsights(user)
                );
            } else {
                insights = generatePersonalizedInsights(assessment, user);
                insights.put("hasAssessment", true);
            }
            
            return ResponseEntity.ok(insights);
            
        } catch (Exception e) {
            logger.error("Error generating insights for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate insights: " + e.getMessage()));
        }
    }

    // Helper Methods

    private Map<String, Object> generateAdaptiveLearningPath(UserAssessment assessment) {
        Map<String, Object> learningPath = new HashMap<>();
        
        // Determine learning priorities based on assessment
        List<Map<String, Object>> priorities = new ArrayList<>();
        
        if (assessment.hasSignificantReadingNeeds()) {
            priorities.add(Map.of(
                "area", "Reading Skills",
                "priority", "high",
                "description", "Focus on reading comprehension and text processing",
                "strategies", List.of(
                    "Use dyslexia-friendly fonts",
                    "Break text into smaller chunks",
                    "Use text-to-speech tools",
                    "Practice with high-interest materials"
                )
            ));
        }
        
        if (assessment.hasSignificantAttentionNeeds()) {
            priorities.add(Map.of(
                "area", "Focus and Attention",
                "priority", "high",
                "description", "Develop sustained attention and focus strategies",
                "strategies", List.of(
                    "Use shorter study sessions (15-20 minutes)",
                    "Implement active break periods",
                    "Use visual timers and cues",
                    "Minimize environmental distractions"
                )
            ));
        }
        
        if (assessment.hasSignificantSocialNeeds()) {
            priorities.add(Map.of(
                "area", "Communication and Social Understanding",
                "priority", "medium",
                "description", "Build communication and social interaction skills",
                "strategies", List.of(
                    "Use clear, explicit instructions",
                    "Practice with structured activities",
                    "Allow processing time",
                    "Focus on routine and predictability"
                )
            ));
        }
        
        if (assessment.hasSignificantSensoryNeeds()) {
            priorities.add(Map.of(
                "area", "Sensory Processing",
                "priority", "medium",
                "description", "Manage sensory input and create optimal learning environments",
                "strategies", List.of(
                    "Control lighting and sound levels",
                    "Use noise-canceling headphones",
                    "Take sensory breaks",
                    "Organize learning spaces"
                )
            ));
        }
        
        learningPath.put("priorities", priorities);
        learningPath.put("recommendedPreset", assessment.getRecommendedPreset());
        learningPath.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        learningPath.put("assessmentDate", assessment.getAssessmentDate());
        
        return learningPath;
    }

    private Map<String, Object> generatePersonalizedContentRecommendations(
            UserAssessment assessment, String subject, String difficulty) {
        
        Map<String, Object> recommendations = new HashMap<>();
        List<Map<String, Object>> contentItems = new ArrayList<>();
        
        // Content type preferences based on assessment
        List<String> preferredTypes = new ArrayList<>();
        List<String> avoidTypes = new ArrayList<>();
        
        if (assessment.hasSignificantReadingNeeds()) {
            preferredTypes.addAll(List.of("video", "audio", "interactive", "visual"));
            avoidTypes.add("text-heavy");
        }
        
        if (assessment.hasSignificantAttentionNeeds()) {
            preferredTypes.addAll(List.of("interactive", "gamified", "short-form"));
            avoidTypes.add("long-lecture");
        }
        
        if (assessment.hasSignificantSensoryNeeds()) {
            preferredTypes.addAll(List.of("calm-visual", "minimal-sound", "structured"));
            avoidTypes.addAll(List.of("flashy", "loud", "chaotic"));
        }
        
        // Generate sample recommendations
        if ("mathematics".equalsIgnoreCase(subject)) {
            contentItems.add(Map.of(
                "title", "Visual Math Concepts",
                "type", "interactive",
                "difficulty", "adaptive",
                "description", "Math concepts explained through visual diagrams and interactive exercises",
                "personalizedFor", getPersonalizationReasons(assessment)
            ));
        }
        
        recommendations.put("contentItems", contentItems);
        recommendations.put("preferredTypes", preferredTypes);
        recommendations.put("typesToAvoid", avoidTypes);
        recommendations.put("personalizedFor", assessment.getRecommendedPreset());
        
        return recommendations;
    }

    private Map<String, Object> generateAdaptedContent(String originalContent, 
                                                     String contentType, 
                                                     UserAssessment assessment) {
        
        Map<String, Object> result = new HashMap<>();
        List<String> adaptations = new ArrayList<>();
        StringBuilder adaptedContent = new StringBuilder();
        
        // Apply reading adaptations
        if (assessment.hasSignificantReadingNeeds()) {
            adaptations.add("Simplified vocabulary and sentence structure");
            adaptations.add("Added paragraph breaks for better readability");
            adaptations.add("Included phonetic guides for complex words");
            
            // Simulate content adaptation
            String[] paragraphs = originalContent.split("\n\n");
            for (String paragraph : paragraphs) {
                // Break long sentences
                String adapted = paragraph.replaceAll("([.!?])\\s*", "$1\n\n");
                adaptedContent.append(adapted).append("\n\n");
            }
        } else {
            adaptedContent.append(originalContent);
        }
        
        // Apply attention adaptations
        if (assessment.hasSignificantAttentionNeeds()) {
            adaptations.add("Added clear headings and bullet points");
            adaptations.add("Highlighted key concepts");
            adaptations.add("Included progress indicators");
        }
        
        // Apply sensory adaptations
        if (assessment.hasSignificantSensoryNeeds()) {
            adaptations.add("Reduced visual complexity");
            adaptations.add("Used calming language patterns");
            adaptations.add("Minimized overwhelming descriptions");
        }
        
        result.put("originalContent", originalContent);
        result.put("adaptedContent", adaptedContent.toString());
        result.put("adaptations", adaptations);
        result.put("hasPersonalization", true);
        result.put("personalizedFor", assessment.getRecommendedPreset());
        
        return result;
    }

    private Map<String, Object> generatePersonalizedSchedule(UserAssessment assessment, Integer hoursPerDay) {
        Map<String, Object> schedule = new HashMap<>();
        
        // Determine optimal session parameters
        int sessionLength = 25; // Default Pomodoro
        int shortBreak = 5;
        int longBreak = 15;
        
        if (assessment.hasSignificantAttentionNeeds()) {
            sessionLength = 15; // Shorter sessions
            shortBreak = 5;
            longBreak = 20; // Longer breaks
        }
        
        if (assessment.hasSignificantSensoryNeeds()) {
            sessionLength = 20;
            shortBreak = 10; // Longer recovery time
            longBreak = 25;
        }
        
        // Calculate daily structure
        int targetHours = hoursPerDay != null ? hoursPerDay : 2;
        int totalMinutes = targetHours * 60;
        int sessionsNeeded = totalMinutes / sessionLength;
        
        schedule.put("sessionLength", sessionLength);
        schedule.put("shortBreakLength", shortBreak);
        schedule.put("longBreakLength", longBreak);
        schedule.put("recommendedSessions", sessionsNeeded);
        schedule.put("totalStudyTime", targetHours + " hours");
        
        // Personalized tips
        List<String> tips = new ArrayList<>();
        if (assessment.hasSignificantAttentionNeeds()) {
            tips.add("Use a visual timer to track session progress");
            tips.add("Take movement breaks between sessions");
            tips.add("Study in a distraction-free environment");
        }
        
        if (assessment.hasSignificantReadingNeeds()) {
            tips.add("Allow extra time for reading-heavy subjects");
            tips.add("Use audio materials when possible");
            tips.add("Take frequent breaks during reading sessions");
        }
        
        schedule.put("personalizedTips", tips);
        schedule.put("adaptedFor", getPersonalizationReasons(assessment));
        
        return schedule;
    }

    private Map<String, Object> generatePersonalizedInsights(UserAssessment assessment, User user) {
        Map<String, Object> insights = new HashMap<>();
        
        // Learning strengths
        List<String> strengths = new ArrayList<>();
        if (!assessment.hasSignificantReadingNeeds()) {
            strengths.add("Strong text processing abilities");
        }
        if (!assessment.hasSignificantAttentionNeeds()) {
            strengths.add("Good sustained attention capacity");
        }
        if (!assessment.hasSignificantSocialNeeds()) {
            strengths.add("Effective communication and social skills");
        }
        if (!assessment.hasSignificantSensoryNeeds()) {
            strengths.add("Good sensory processing and environmental adaptation");
        }
        
        // Areas for support
        List<String> supportAreas = new ArrayList<>();
        if (assessment.hasSignificantReadingNeeds()) {
            supportAreas.add("Reading comprehension and text processing");
        }
        if (assessment.hasSignificantAttentionNeeds()) {
            supportAreas.add("Sustained attention and focus");
        }
        if (assessment.hasSignificantSocialNeeds()) {
            supportAreas.add("Communication and social interaction");
        }
        if (assessment.hasSignificantSensoryNeeds()) {
            supportAreas.add("Sensory processing and environmental sensitivity");
        }
        
        // Personalized learning approach
        String learningApproach = generateLearningApproachDescription(assessment);
        
        insights.put("learningStrengths", strengths);
        insights.put("supportAreas", supportAreas);
        insights.put("recommendedApproach", learningApproach);
        insights.put("uiPreset", assessment.getRecommendedPreset());
        insights.put("assessmentScores", Map.of(
            "attention", assessment.getAttentionScore(),
            "reading", assessment.getReadingDifficultyScore(),
            "social", assessment.getSocialCommunicationScore(),
            "sensory", assessment.getSensoryProcessingScore()
        ));
        
        return insights;
    }

    private List<String> getPersonalizationReasons(UserAssessment assessment) {
        List<String> reasons = new ArrayList<>();
        
        if (assessment.hasSignificantAttentionNeeds()) {
            reasons.add("ADHD/Attention support");
        }
        if (assessment.hasSignificantReadingNeeds()) {
            reasons.add("Dyslexia/Reading support");
        }
        if (assessment.hasSignificantSocialNeeds()) {
            reasons.add("Autism/Communication support");
        }
        if (assessment.hasSignificantSensoryNeeds()) {
            reasons.add("Sensory processing support");
        }
        
        return reasons;
    }

    private String generateLearningApproachDescription(UserAssessment assessment) {
        StringBuilder approach = new StringBuilder();
        
        approach.append("Your personalized learning approach combines ");
        
        List<String> strategies = new ArrayList<>();
        if (assessment.hasSignificantReadingNeeds()) {
            strategies.add("multi-modal content delivery");
        }
        if (assessment.hasSignificantAttentionNeeds()) {
            strategies.add("focused micro-sessions");
        }
        if (assessment.hasSignificantSocialNeeds()) {
            strategies.add("structured, explicit instruction");
        }
        if (assessment.hasSignificantSensoryNeeds()) {
            strategies.add("sensory-aware environments");
        }
        
        if (strategies.isEmpty()) {
            return "Standard adaptive learning approach optimized for your preferences";
        }
        
        for (int i = 0; i < strategies.size(); i++) {
            if (i == strategies.size() - 1 && strategies.size() > 1) {
                approach.append(" and ");
            } else if (i > 0) {
                approach.append(", ");
            }
            approach.append(strategies.get(i));
        }
        
        approach.append(" to maximize your learning potential.");
        
        return approach.toString();
    }

    private Map<String, Object> generateBasicRecommendations(String subject, String difficulty) {
        return Map.of(
            "message", "Complete your neurodivergent assessment to receive personalized recommendations",
            "generalTips", List.of(
                "Take regular breaks during study sessions",
                "Find a comfortable, quiet study environment",
                "Use a mix of different learning materials",
                "Set achievable daily goals"
            )
        );
    }

    private Map<String, Object> generateBasicSchedule(Integer hoursPerDay) {
        int targetHours = hoursPerDay != null ? hoursPerDay : 2;
        return Map.of(
            "sessionLength", 25,
            "shortBreakLength", 5,
            "longBreakLength", 15,
            "recommendedSessions", (targetHours * 60) / 25,
            "totalStudyTime", targetHours + " hours",
            "hasPersonalization", false,
            "message", "Complete your assessment for a personalized schedule"
        );
    }

    private Map<String, Object> generateBasicInsights(User user) {
        return Map.of(
            "message", "Complete the neurodivergent assessment to receive personalized learning insights",
            "generalStrengths", List.of(
                "Motivated to learn and improve",
                "Willing to try new approaches",
                "Engaged with the learning platform"
            ),
            "nextSteps", List.of(
                "Complete the accessibility assessment",
                "Explore different content types",
                "Establish a regular study routine"
            )
        );
    }
}
