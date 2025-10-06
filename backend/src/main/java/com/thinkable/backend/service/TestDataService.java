package com.thinkable.backend.service;

import java.util.stream.Collectors;

import com.thinkable.backend.entity.*;
import com.thinkable.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for creating test data for the tutor-student content system
 */
@Service
@Transactional
public class TestDataService {
    
    @Autowired
    private TutorProfileRepository tutorRepository;
    
    @Autowired
    private LearningContentRepository contentRepository;
    
    @Autowired
    private ContentAccessibilityTagRepository accessibilityTagRepository;
    
    @Autowired
    private StudentContentInteractionRepository interactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Create comprehensive test data for the tutor-student system
     */
    public Map<String, Object> createTestData() {
        Map<String, Object> results = new HashMap<>();
        
        try {
            // 1. Create sample tutors
            List<TutorProfile> tutors = createSampleTutors();
            results.put("tutorsCreated", tutors.size());
            
            // 2. Create sample learning content
            List<LearningContent> content = createSampleContent(tutors);
            results.put("contentCreated", content.size());
            
            // 3. Create sample accessibility tags
            int tagsCreated = createSampleAccessibilityTags(content);
            results.put("accessibilityTagsCreated", tagsCreated);
            
            // 4. Create sample student interactions
            int interactionsCreated = createSampleInteractions(content);
            results.put("interactionsCreated", interactionsCreated);
            
            results.put("message", "Test data created successfully");
            results.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            results.put("error", "Failed to create test data: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Create sample tutor profiles with diverse specializations
     */
    private List<TutorProfile> createSampleTutors() throws Exception {
        List<TutorProfile> tutors = new ArrayList<>();
        
        // Tutor 1: Dyslexia specialist in Mathematics
        TutorProfile tutor1 = new TutorProfile();
        tutor1.setUserId(1001L);
        tutor1.setDisplayName("Dr. Sarah Chen");
        tutor1.setBio("Specialized in dyslexia-friendly mathematics instruction with 8 years experience");
        tutor1.setQualifications("PhD in Educational Psychology, Certified Dyslexia Therapist");
        tutor1.setTeachingExperienceYears(8);
        tutor1.setNeurodivergentSpecialization(objectMapper.writeValueAsString(
                List.of("dyslexia", "dyscalculia", "learning_disabilities")));
        tutor1.setSubjectExpertise(objectMapper.writeValueAsString(
                List.of("mathematics", "algebra", "geometry", "basic_arithmetic")));
        tutor1.setAccessibilityExpertise(objectMapper.writeValueAsString(
                List.of("dyslexia_friendly_fonts", "visual_learning", "multi_sensory_teaching")));
        tutor1.setVerificationStatus("verified");
        tutor1.setRatingAverage(4.8);
        tutor1.setTotalStudents(45);
        tutor1.setContentCount(0);
        tutor1.setIsActive(true);
        tutors.add(tutor1);
        
        // Tutor 2: ADHD specialist in Science
        TutorProfile tutor2 = new TutorProfile();
        tutor2.setUserId(1002L);
        tutor2.setDisplayName("Mark Thompson");
        tutor2.setBio("Expert in ADHD-friendly science education with structured, engaging content");
        tutor2.setQualifications("MSc in Biology, ADHD Coaching Certification");
        tutor2.setTeachingExperienceYears(6);
        tutor2.setNeurodivergentSpecialization(objectMapper.writeValueAsString(
                List.of("adhd", "executive_function", "attention_difficulties")));
        tutor2.setSubjectExpertise(objectMapper.writeValueAsString(
                List.of("biology", "chemistry", "physics", "environmental_science")));
        tutor2.setAccessibilityExpertise(objectMapper.writeValueAsString(
                List.of("structured_content", "short_sessions", "interactive_learning")));
        tutor2.setVerificationStatus("verified");
        tutor2.setRatingAverage(4.6);
        tutor2.setTotalStudents(38);
        tutor2.setContentCount(0);
        tutor2.setIsActive(true);
        tutors.add(tutor2);
        
        // Tutor 3: Autism specialist in Languages
        TutorProfile tutor3 = new TutorProfile();
        tutor3.setUserId(1003L);
        tutor3.setDisplayName("Lisa Rodriguez");
        tutor3.setBio("Autism-friendly language instruction focusing on clear structure and predictable patterns");
        tutor3.setQualifications("MA in Applied Linguistics, Autism Spectrum Disorder Specialist");
        tutor3.setTeachingExperienceYears(10);
        tutor3.setNeurodivergentSpecialization(objectMapper.writeValueAsString(
                List.of("autism", "aspergers", "sensory_processing")));
        tutor3.setSubjectExpertise(objectMapper.writeValueAsString(
                List.of("english", "spanish", "language_arts", "grammar", "vocabulary")));
        tutor3.setAccessibilityExpertise(objectMapper.writeValueAsString(
                List.of("clear_structure", "visual_supports", "routine_based_learning")));
        tutor3.setVerificationStatus("verified");
        tutor3.setRatingAverage(4.9);
        tutor3.setTotalStudents(52);
        tutor3.setContentCount(0);
        tutor3.setIsActive(true);
        tutors.add(tutor3);
        
        // Tutor 4: General accessibility specialist
        TutorProfile tutor4 = new TutorProfile();
        tutor4.setUserId(1004L);
        tutor4.setDisplayName("Professor James Wilson");
        tutor4.setBio("Universal Design for Learning expert with experience across multiple neurodivergent conditions");
        tutor4.setQualifications("PhD in Special Education, Universal Design for Learning Certificate");
        tutor4.setTeachingExperienceYears(15);
        tutor4.setNeurodivergentSpecialization(objectMapper.writeValueAsString(
                List.of("universal_design", "multiple_disabilities", "inclusive_education")));
        tutor4.setSubjectExpertise(objectMapper.writeValueAsString(
                List.of("history", "social_studies", "life_skills", "study_strategies")));
        tutor4.setAccessibilityExpertise(objectMapper.writeValueAsString(
                List.of("universal_design", "multi_modal_content", "assistive_technology")));
        tutor4.setVerificationStatus("verified");
        tutor4.setRatingAverage(4.7);
        tutor4.setTotalStudents(67);
        tutor4.setContentCount(0);
        tutor4.setIsActive(true);
        tutors.add(tutor4);
        
        return tutorRepository.saveAll(tutors);
    }
    
    /**
     * Create sample learning content with accessibility features
     */
    private List<LearningContent> createSampleContent(List<TutorProfile> tutors) {
        List<LearningContent> contentList = new ArrayList<>();
        
        // Content 1: Dyslexia-friendly algebra video
        LearningContent content1 = new LearningContent();
        content1.setTutor(tutors.get(0)); // Dr. Sarah Chen
        content1.setTitle("Algebra Basics with Dyslexia-Friendly Fonts");
        content1.setDescription("Introduction to algebraic concepts using clear, dyslexia-friendly typography and visual aids");
        content1.setContentType("video");
        content1.setSubjectArea("mathematics");
        content1.setDifficultyLevel("beginner");
        content1.setTargetAgeMin(12);
        content1.setTargetAgeMax(16);
        content1.setEstimatedDurationMinutes(25);
        content1.setDyslexiaFriendly(true);
        content1.setAdhdFriendly(false);
        content1.setAutismFriendly(true);
        content1.setFontType("OpenDyslexic");
        content1.setColorContrastRatio(BigDecimal.valueOf(4.5));
        content1.setReadingLevel("grade_6");
        content1.setHasSubtitles(true);
        content1.setCognitiveLoadLevel("low");
        content1.setStatus("published");
        content1.setIsPublic(true);
        content1.setRatingAverage(BigDecimal.valueOf(4.6));
        content1.setRatingCount(23);
        content1.setViewCount(156);
        content1.setSuccessRate(BigDecimal.valueOf(82.5));
        contentList.add(content1);
        
        // Content 2: ADHD-friendly chemistry experiment
        LearningContent content2 = new LearningContent();
        content2.setTutor(tutors.get(1)); // Mark Thompson
        content2.setTitle("Quick Chemistry: 5-Minute Experiments");
        content2.setDescription("Short, structured chemistry experiments designed for ADHD learners");
        content2.setContentType("interactive");
        content2.setSubjectArea("chemistry");
        content2.setDifficultyLevel("intermediate");
        content2.setTargetAgeMin(14);
        content2.setTargetAgeMax(18);
        content2.setEstimatedDurationMinutes(15);
        content2.setDyslexiaFriendly(true);
        content2.setAdhdFriendly(true);
        content2.setAutismFriendly(false);
        content2.setCognitiveLoadLevel("medium");
        content2.setInteractionType("interactive");
        content2.setStatus("published");
        content2.setIsPublic(true);
        content2.setRatingAverage(BigDecimal.valueOf(4.4));
        content2.setRatingCount(31);
        content2.setViewCount(203);
        content2.setSuccessRate(BigDecimal.valueOf(76.8));
        contentList.add(content2);
        
        // Content 3: Autism-friendly grammar guide
        LearningContent content3 = new LearningContent();
        content3.setTutor(tutors.get(2)); // Lisa Rodriguez
        content3.setTitle("Grammar Rules: A Structured Approach");
        content3.setDescription("Clear, rule-based grammar instruction with predictable patterns for autism learners");
        content3.setContentType("document");
        content3.setSubjectArea("english");
        content3.setDifficultyLevel("beginner");
        content3.setTargetAgeMin(10);
        content3.setTargetAgeMax(15);
        content3.setEstimatedDurationMinutes(30);
        content3.setDyslexiaFriendly(false);
        content3.setAdhdFriendly(false);
        content3.setAutismFriendly(true);
        content3.setReadingLevel("grade_7");
        content3.setCognitiveLoadLevel("low");
        content3.setInteractionType("passive");
        content3.setStatus("published");
        content3.setIsPublic(true);
        content3.setRatingAverage(BigDecimal.valueOf(4.8));
        content3.setRatingCount(18);
        content3.setViewCount(134);
        content3.setSuccessRate(BigDecimal.valueOf(89.2));
        contentList.add(content3);
        
        // Content 4: Multi-modal history lesson
        LearningContent content4 = new LearningContent();
        content4.setTutor(tutors.get(3)); // Professor James Wilson
        content4.setTitle("World War II: Multiple Perspectives");
        content4.setDescription("Universal Design history lesson with audio, visual, and text components");
        content4.setContentType("video");
        content4.setSubjectArea("history");
        content4.setDifficultyLevel("advanced");
        content4.setTargetAgeMin(16);
        content4.setTargetAgeMax(20);
        content4.setEstimatedDurationMinutes(45);
        content4.setDyslexiaFriendly(true);
        content4.setAdhdFriendly(true);
        content4.setAutismFriendly(true);
        content4.setVisualImpairmentFriendly(true);
        content4.setHearingImpairmentFriendly(true);
        content4.setHasAudioDescription(true);
        content4.setHasSubtitles(true);
        content4.setCognitiveLoadLevel("high");
        content4.setInteractionType("collaborative");
        content4.setStatus("published");
        content4.setIsPublic(true);
        content4.setRatingAverage(BigDecimal.valueOf(4.9));
        content4.setRatingCount(42);
        content4.setViewCount(278);
        content4.setSuccessRate(BigDecimal.valueOf(91.3));
        contentList.add(content4);
        
        // Content 5: Basic math with visual supports
        LearningContent content5 = new LearningContent();
        content5.setTutor(tutors.get(0)); // Dr. Sarah Chen
        content5.setTitle("Visual Math: Numbers and Shapes");
        content5.setDescription("Mathematics concepts explained through visual representations and manipulatives");
        content5.setContentType("interactive");
        content5.setSubjectArea("mathematics");
        content5.setDifficultyLevel("beginner");
        content5.setTargetAgeMin(8);
        content5.setTargetAgeMax(12);
        content5.setEstimatedDurationMinutes(20);
        content5.setDyslexiaFriendly(true);
        content5.setAdhdFriendly(true);
        content5.setAutismFriendly(true);
        content5.setVisualImpairmentFriendly(false);
        content5.setCognitiveLoadLevel("low");
        content5.setInteractionType("interactive");
        content5.setStatus("published");
        content5.setIsPublic(true);
        content5.setRatingAverage(BigDecimal.valueOf(4.7));
        content5.setRatingCount(35);
        content5.setViewCount(189);
        content5.setSuccessRate(BigDecimal.valueOf(85.1));
        contentList.add(content5);
        
        List<LearningContent> savedContent = contentRepository.saveAll(contentList);
        
        // Update tutor content counts
        for (TutorProfile tutor : tutors) {
            long contentCount = savedContent.stream()
                    .mapToLong(content -> content.getTutor().getId().equals(tutor.getId()) ? 1 : 0)
                    .sum();
            tutor.setContentCount((int) contentCount);
        }
        tutorRepository.saveAll(tutors);
        
        return savedContent;
    }
    
    /**
     * Create sample accessibility tags for content
     */
    private int createSampleAccessibilityTags(List<LearningContent> contentList) {
        List<ContentAccessibilityTag> tags = new ArrayList<>();
        
        for (LearningContent content : contentList) {
            // Add dyslexia tags
            if (Boolean.TRUE.equals(content.getDyslexiaFriendly())) {
                tags.add(ContentAccessibilityTag.createDyslexiaTag(content, "dyslexia_friendly_font", BigDecimal.valueOf(0.9)));
                tags.add(ContentAccessibilityTag.createDyslexiaTag(content, "high_contrast", BigDecimal.valueOf(0.85)));
            }
            
            // Add ADHD tags
            if (Boolean.TRUE.equals(content.getAdhdFriendly())) {
                tags.add(ContentAccessibilityTag.createADHDTag(content, "structured_content", BigDecimal.valueOf(0.88)));
                tags.add(ContentAccessibilityTag.createADHDTag(content, "short_duration", BigDecimal.valueOf(0.82)));
            }
            
            // Add autism tags
            if (Boolean.TRUE.equals(content.getAutismFriendly())) {
                tags.add(ContentAccessibilityTag.createAutismTag(content, "predictable_structure", BigDecimal.valueOf(0.91)));
                tags.add(ContentAccessibilityTag.createAutismTag(content, "clear_navigation", BigDecimal.valueOf(0.87)));
            }
        }
        
        accessibilityTagRepository.saveAll(tags);
        return tags.size();
    }
    
    /**
     * Create sample student interactions
     */
    private int createSampleInteractions(List<LearningContent> contentList) {
        List<StudentContentInteraction> interactions = new ArrayList<>();
        Random random = new Random();
        
        // Simulate interactions for student IDs 1-10
        for (int studentId = 1; studentId <= 10; studentId++) {
            // Each student interacts with 2-4 pieces of content
            Collections.shuffle(contentList);
            int interactionCount = 2 + random.nextInt(3);
            
            for (int i = 0; i < interactionCount && i < contentList.size(); i++) {
                LearningContent content = contentList.get(i);
                
                StudentContentInteraction interaction = new StudentContentInteraction();
                interaction.setStudentId((long) studentId);
                interaction.setContent(content);
                interaction.setInteractionType("complete");
                interaction.setTimeSpentMinutes(10 + random.nextInt(40));
                interaction.setCompletionPercentage(60 + random.nextInt(41));
                interaction.setEngagementScore(BigDecimal.valueOf(0.6 + random.nextDouble() * 0.4));
                interaction.setComprehensionScore(BigDecimal.valueOf(0.5 + random.nextDouble() * 0.5));
                interaction.setDifficultyRating(2 + random.nextInt(4));
                interaction.setUsefulnessRating(3 + random.nextInt(3));
                interaction.setAccessibilityRating(3 + random.nextInt(3));
                interaction.setEnergyLevelBefore(4 + random.nextInt(4));
                interaction.setEnergyLevelAfter(5 + random.nextInt(4));
                interaction.setFocusLevel(5 + random.nextInt(5));
                interaction.setStressLevel(2 + random.nextInt(4));
                interaction.setDeviceType(random.nextBoolean() ? "desktop" : "tablet");
                interaction.setWasHelpful(random.nextDouble() > 0.3);
                interaction.setWouldRecommend(random.nextDouble() > 0.4);
                interaction.setStartedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
                interaction.setCompletedAt(LocalDateTime.now().minusDays(random.nextInt(25)));
                interaction.setLastAccessedAt(LocalDateTime.now().minusDays(random.nextInt(7)));
                
                interactions.add(interaction);
            }
        }
        
        interactionRepository.saveAll(interactions);
        return interactions.size();
    }
    
    /**
     * Clear all test data
     */
    public Map<String, Object> clearTestData() {
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Clear in correct order due to foreign key constraints
            interactionRepository.deleteAll();
            accessibilityTagRepository.deleteAll();
            contentRepository.deleteAll();
            
            // Only delete test tutor profiles (user IDs 1001-1004)
            List<TutorProfile> testTutors = tutorRepository.findAll().stream()
                    .filter(tutor -> tutor.getUserId() >= 1001L && tutor.getUserId() <= 1004L)
                    .collect(Collectors.toList());
            tutorRepository.deleteAll(testTutors);
            
            results.put("message", "Test data cleared successfully");
            results.put("tutorsDeleted", testTutors.size());
            results.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            results.put("error", "Failed to clear test data: " + e.getMessage());
        }
        
        return results;
    }
}
