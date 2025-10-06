package com.thinkable.backend.controller;

import com.thinkable.backend.service.TestDataService;
import com.thinkable.backend.service.ContentRecommendationEngine;
import com.thinkable.backend.model.AssessmentQuestion;
import com.thinkable.backend.repository.AssessmentQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * REST Controller for managing test data and demonstrations
 */
@RestController
@RequestMapping("/api/test-data")
public class TestDataController {
    
    @Autowired
    private TestDataService testDataService;
    
    @Autowired
    private ContentRecommendationEngine recommendationEngine;
    
    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;
    
    /**
     * Create comprehensive test data for demonstration
     */
    @PostMapping("/create")
    public ResponseEntity<?> createTestData() {
        Map<String, Object> results = testDataService.createTestData();
        
        if (results.containsKey("error")) {
            return ResponseEntity.badRequest().body(results);
        }
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Clear all test data
     */
    @PostMapping("/clear")
    public ResponseEntity<?> clearTestData() {
        Map<String, Object> results = testDataService.clearTestData();
        
        if (results.containsKey("error")) {
            return ResponseEntity.badRequest().body(results);
        }
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Generate sample recommendations for testing
     */
    @PostMapping("/generate-recommendations/{studentId}")
    public ResponseEntity<?> generateSampleRecommendations(@PathVariable Long studentId) {
        try {
            var recommendations = recommendationEngine.generateRecommendations(studentId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Sample recommendations generated",
                "studentId", studentId,
                "recommendationsCount", recommendations.size(),
                "recommendations", recommendations
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to generate sample recommendations: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Create assessment questions for neurodivergent evaluation
     */
    @PostMapping("/create-assessment-questions")
    public ResponseEntity<?> createAssessmentQuestions() {
        try {
            // Check if questions already exist
            long existingCount = assessmentQuestionRepository.count();
            if (existingCount > 0) {
                return ResponseEntity.ok(Map.of(
                    "message", "Assessment questions already exist",
                    "existingQuestions", existingCount
                ));
            }
            
            List<AssessmentQuestion> questions = createSampleAssessmentQuestions();
            List<AssessmentQuestion> savedQuestions = assessmentQuestionRepository.saveAll(questions);
            
            return ResponseEntity.ok(Map.of(
                "message", "Assessment questions created successfully",
                "questionsCreated", savedQuestions.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create assessment questions: " + e.getMessage()
            ));
        }
    }
    
    private List<AssessmentQuestion> createSampleAssessmentQuestions() {
        List<AssessmentQuestion> questions = new ArrayList<>();
        
        // Attention/Focus Questions (ADHD indicators)
        AssessmentQuestion q1 = new AssessmentQuestion("AttentionSupport", "I often have trouble paying attention to details or make careless mistakes", "likert");
        q1.setSubcategory("Focus");
        questions.add(q1);
        
        AssessmentQuestion q2 = new AssessmentQuestion("AttentionSupport", "I find it difficult to sustain attention during tasks or activities", "likert");
        q2.setSubcategory("Concentration");
        questions.add(q2);
        
        AssessmentQuestion q3 = new AssessmentQuestion("AttentionSupport", "I have difficulty organizing tasks and activities", "likert");
        q3.setSubcategory("Organization");
        questions.add(q3);
        
        AssessmentQuestion q4 = new AssessmentQuestion("AttentionSupport", "I often fail to finish schoolwork or chores", "likert");
        q4.setSubcategory("Following Instructions");
        questions.add(q4);
        
        AssessmentQuestion q5 = new AssessmentQuestion("AttentionSupport", "I am easily distracted by external stimuli", "likert");
        q5.setSubcategory("Distraction");
        questions.add(q5);
            
        // Social Communication Questions (Autism indicators)
        AssessmentQuestion q6 = new AssessmentQuestion("SocialCommunication", "I have difficulty with back-and-forth conversation", "likert");
        q6.setSubcategory("Social Interaction");
        questions.add(q6);
        
        AssessmentQuestion q7 = new AssessmentQuestion("SocialCommunication", "I have trouble understanding nonverbal communication like facial expressions", "likert");
        q7.setSubcategory("Nonverbal Communication");
        questions.add(q7);
        
        AssessmentQuestion q8 = new AssessmentQuestion("SocialCommunication", "I find it difficult to develop and maintain peer relationships", "likert");
        q8.setSubcategory("Relationships");
        questions.add(q8);
        
        AssessmentQuestion q9 = new AssessmentQuestion("SocialCommunication", "I have difficulty understanding social situations and cues", "likert");
        q9.setSubcategory("Social Awareness");
        questions.add(q9);
        
        AssessmentQuestion q10 = new AssessmentQuestion("SocialCommunication", "I engage in repetitive motor movements or speech", "likert");
        q10.setSubcategory("Repetitive Behaviors");
        questions.add(q10);
            
        // Reading Difficulty Questions (Dyslexia indicators)
        AssessmentQuestion q11 = new AssessmentQuestion("ReadingSupport", "I read more slowly than others my age", "likert");
        q11.setSubcategory("Reading Speed");
        questions.add(q11);
        
        AssessmentQuestion q12 = new AssessmentQuestion("ReadingSupport", "I have trouble recognizing words I should know", "likert");
        q12.setSubcategory("Word Recognition");
        questions.add(q12);
        
        AssessmentQuestion q13 = new AssessmentQuestion("ReadingSupport", "I have difficulty with spelling", "likert");
        q13.setSubcategory("Spelling");
        questions.add(q13);
        
        AssessmentQuestion q14 = new AssessmentQuestion("ReadingSupport", "I have trouble understanding what I read", "likert");
        q14.setSubcategory("Reading Comprehension");
        questions.add(q14);
        
        AssessmentQuestion q15 = new AssessmentQuestion("ReadingSupport", "I sometimes confuse letters like 'b' and 'd' or 'p' and 'q'", "likert");
        q15.setSubcategory("Letter Reversal");
        questions.add(q15);
            
        // Sensory Processing Questions
        AssessmentQuestion q16 = new AssessmentQuestion("SensoryProcessing", "I am sensitive to bright lights or visual stimuli", "likert");
        q16.setSubcategory("Visual Sensitivity");
        questions.add(q16);
        
        AssessmentQuestion q17 = new AssessmentQuestion("SensoryProcessing", "I am bothered by loud or unexpected sounds", "likert");
        q17.setSubcategory("Auditory Sensitivity");
        questions.add(q17);
        
        AssessmentQuestion q18 = new AssessmentQuestion("SensoryProcessing", "I am sensitive to textures or touch", "likert");
        q18.setSubcategory("Tactile Sensitivity");
        questions.add(q18);
        
        AssessmentQuestion q19 = new AssessmentQuestion("SensoryProcessing", "I need extra time to process information", "likert");
        q19.setSubcategory("Processing Speed");
        questions.add(q19);
        
        AssessmentQuestion q20 = new AssessmentQuestion("SensoryProcessing", "I become overwhelmed in busy or noisy environments", "likert");
        q20.setSubcategory("Sensory Overload");
        questions.add(q20);
            
        return questions;
    }
    
    /**
     * Get system demonstration summary
     */
    @GetMapping("/demo-summary")
    public ResponseEntity<?> getDemoSummary() {
        return ResponseEntity.ok(Map.of(
            "title", "ThinkAble Neurodivergent Learning Platform",
            "description", "AI-powered personalized learning content marketplace",
            "features", Map.of(
                "tutorProfiles", "Verified tutors with neurodivergent specializations",
                "accessibilityTagging", "Comprehensive content accessibility metadata", 
                "smartFiltering", "AI-powered content matching based on user profiles",
                "patternRecognition", "Advanced learning pattern analysis",
                "personalizedRecommendations", "Context-aware content suggestions",
                "interactionTracking", "Detailed engagement and outcome analytics"
            ),
            "accessibilitySupport", Map.of(
                "dyslexia", "Dyslexia-friendly fonts, high contrast, visual learning",
                "adhd", "Structured content, short sessions, interactive elements",
                "autism", "Predictable structure, clear navigation, routine-based",
                "universalDesign", "Multi-modal content, assistive technology support"
            ),
            "demoEndpoints", Map.of(
                "createTestData", "POST /api/test-data/create",
                "searchContent", "GET /api/student/content/search?dyslexiaFriendly=true",
                "tutorSearch", "GET /api/tutor/profile/search?specialization=dyslexia",
                "recommendations", "GET /api/recommendations/student/{studentId}",
                "analytics", "GET /api/tutor/content/{contentId}/analytics"
            )
        ));
    }
    
    /**
     * Demonstrate accessibility filtering
     */
    @GetMapping("/demo/accessibility-filter")
    public ResponseEntity<?> demoAccessibilityFiltering() {
        return ResponseEntity.ok(Map.of(
            "description", "Content filtering based on accessibility needs",
            "examples", Map.of(
                "dyslexiaContent", Map.of(
                    "endpoint", "GET /api/student/content/search?dyslexiaFriendly=true",
                    "features", new String[]{"OpenDyslexic fonts", "High contrast", "Visual learning aids"}
                ),
                "adhdContent", Map.of(
                    "endpoint", "GET /api/student/content/search?adhdFriendly=true", 
                    "features", new String[]{"Short sessions", "Structured format", "Interactive elements"}
                ),
                "autismContent", Map.of(
                    "endpoint", "GET /api/student/content/search?autismFriendly=true",
                    "features", new String[]{"Predictable structure", "Clear navigation", "Routine-based learning"}
                )
            ),
            "combinedFiltering", Map.of(
                "endpoint", "GET /api/student/content/search?subject=math&dyslexiaFriendly=true&adhdFriendly=true",
                "description", "Find math content suitable for both dyslexia and ADHD learners"
            )
        ));
    }
    
    /**
     * Demonstrate tutor specialization search
     */
    @GetMapping("/demo/tutor-specializations")
    public ResponseEntity<?> demoTutorSpecializations() {
        return ResponseEntity.ok(Map.of(
            "description", "Finding tutors with specific neurodivergent expertise",
            "searches", Map.of(
                "dyslexiaSpecialists", Map.of(
                    "endpoint", "GET /api/tutor/profile/specialists/dyslexia",
                    "description", "Tutors specialized in dyslexia support"
                ),
                "adhdExperts", Map.of(
                    "endpoint", "GET /api/tutor/profile/specialists/adhd",
                    "description", "Tutors with ADHD and executive function expertise"
                ),
                "mathTutors", Map.of(
                    "endpoint", "GET /api/tutor/profile/subject/mathematics",
                    "description", "Mathematics tutors with accessibility knowledge"
                ),
                "topRated", Map.of(
                    "endpoint", "GET /api/tutor/profile/top-rated?minRating=4.5",
                    "description", "Highest rated accessibility-focused tutors"
                )
            )
        ));
    }
    
    /**
     * Demonstrate recommendation engine
     */
    @GetMapping("/demo/recommendations")
    public ResponseEntity<?> demoRecommendations() {
        return ResponseEntity.ok(Map.of(
            "description", "AI-powered personalized content recommendations",
            "types", Map.of(
                "profileBased", "Content matching user's neurodivergent profile",
                "collaborative", "Content that worked for similar users", 
                "contentBased", "Similar to previously helpful content",
                "accessibilityFocused", "Based on specific accessibility needs",
                "patternDriven", "Optimal timing and learning patterns",
                "trending", "Popular content with high effectiveness"
            ),
            "intelligence", Map.of(
                "contextAware", "Considers time of day, energy levels, stress",
                "adaptive", "Learns from user interactions and feedback",
                "predictive", "Forecasts learning success probability",
                "personalizable", "Adjusts to individual learning pace and style"
            ),
            "endpoints", Map.of(
                "generate", "POST /api/recommendations/generate/{studentId}",
                "getRecommendations", "GET /api/recommendations/student/{studentId}",
                "highConfidence", "GET /api/recommendations/student/{studentId}/high-confidence"
            )
        ));
    }
}
