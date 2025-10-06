package com.thinkable.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkable.backend.model.User;
import com.thinkable.backend.model.Lesson;
import com.thinkable.backend.model.UserAssessment;
import com.thinkable.backend.repository.UserRepository;
import com.thinkable.backend.repository.LessonRepository;
import com.thinkable.backend.repository.LeaderboardRepository;
import com.thinkable.backend.repository.LessonProgressRepository;
import com.thinkable.backend.repository.UserAssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdaptiveLearningAIService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private UserAssessmentRepository userAssessmentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analyzes user's assessment data and creates a comprehensive learning profile
     */
    public LearningProfile analyzeLearningProfile(User user) {
        LearningProfile profile = new LearningProfile();
        profile.setUserId(user.getId());
        profile.setUserEmail(user.getEmail());

        // Parse assessment scores
        Map<String, Object> assessmentScores = parseAssessmentScores(user.getAssessmentScores());
        
        // Analyze neurodivergent traits
        NeuroProfile neuroProfile = analyzeNeuroProfile(assessmentScores);
        profile.setNeuroProfile(neuroProfile);

        // Determine learning style
        LearningStyle learningStyle = determineLearningStyle(user, assessmentScores);
        profile.setLearningStyle(learningStyle);

        // Calculate optimal session parameters
        SessionParameters sessionParams = calculateOptimalSessionParameters(neuroProfile);
        profile.setSessionParameters(sessionParams);

        // Analyze performance data
        PerformanceMetrics performance = analyzePerformanceMetrics(user);
        profile.setPerformanceMetrics(performance);

        return profile;
    }

    /**
     * Generates personalized learning recommendations based on AI analysis
     */
    public LearningRecommendations generateRecommendations(User user) {
        LearningProfile profile = analyzeLearningProfile(user);
        LearningRecommendations recommendations = new LearningRecommendations();

        // Get all available lessons
        List<Lesson> allLessons = lessonRepository.findAll();

        // Prioritize lessons based on learning profile
        List<Lesson> prioritizedLessons = prioritizeLessons(allLessons, profile);
        recommendations.setRecommendedLessons(prioritizedLessons.subList(0, Math.min(5, prioritizedLessons.size())));

        // Generate study schedule
        StudySchedule schedule = generateOptimalStudySchedule(profile);
        recommendations.setStudySchedule(schedule);

        // Create motivational messages
        List<String> motivationalMessages = generateMotivationalMessages(profile);
        recommendations.setMotivationalMessages(motivationalMessages);

        // Suggest break intervals
        recommendations.setRecommendedBreakInterval(profile.getSessionParameters().getOptimalBreakInterval());

        // Suggest difficulty level
        recommendations.setRecommendedDifficulty(calculateOptimalDifficulty(profile));

        return recommendations;
    }

    /**
     * Adapts lesson difficulty based on real-time performance
     */
    public DifficultyLevel adaptDifficultyBasedOnPerformance(User user, double recentPerformance) {
        LearningProfile profile = analyzeLearningProfile(user);
        
        // Base difficulty on learning profile
        DifficultyLevel baseDifficulty = profile.getPerformanceMetrics().getAverageDifficulty();
        
        // Adjust based on recent performance
        if (recentPerformance >= 85) {
            // High performance - increase difficulty
            return increaseDifficulty(baseDifficulty);
        } else if (recentPerformance < 60) {
            // Low performance - decrease difficulty
            return decreaseDifficulty(baseDifficulty);
        }
        
        return baseDifficulty;
    }

    /**
     * Generate personalized recommendations based on new assessment system
     */
    public String generatePersonalizedRecommendations(UserAssessment assessment) {
        StringBuilder recommendations = new StringBuilder();
        
        recommendations.append("Based on your assessment results:\n\n");
        
        // ADHD/Attention recommendations
        if (assessment.hasSignificantAttentionNeeds()) {
            recommendations.append("🎯 ATTENTION SUPPORT:\n");
            recommendations.append("- Study in 15-20 minute focused sessions\n");
            recommendations.append("- Take 5-minute movement breaks between sessions\n");
            recommendations.append("- Use timers and visual progress indicators\n");
            recommendations.append("- Find a quiet, distraction-free environment\n");
            recommendations.append("- Try fidget tools if they help you focus\n\n");
        }
        
        // Reading/Dyslexia recommendations
        if (assessment.hasSignificantReadingNeeds()) {
            recommendations.append("📚 READING SUPPORT:\n");
            recommendations.append("- Use dyslexia-friendly fonts (Comic Neue, OpenDyslexic)\n");
            recommendations.append("- Increase text size and line spacing for comfort\n");
            recommendations.append("- Use text-to-speech features when available\n");
            recommendations.append("- Read in well-lit environments\n");
            recommendations.append("- Break large texts into smaller sections\n\n");
        }
        
        // Social/Autism recommendations
        if (assessment.hasSignificantSocialNeeds()) {
            recommendations.append("🧩 COMMUNICATION SUPPORT:\n");
            recommendations.append("- Look for clear, step-by-step instructions\n");
            recommendations.append("- Create consistent learning routines\n");
            recommendations.append("- Use structured lesson formats\n");
            recommendations.append("- Take time to process new information\n");
            recommendations.append("- Focus on one concept at a time\n\n");
        }
        
        // Sensory recommendations
        if (assessment.hasSignificantSensoryNeeds()) {
            recommendations.append("🌟 SENSORY SUPPORT:\n");
            recommendations.append("- Adjust screen brightness to comfortable levels\n");
            recommendations.append("- Use noise-canceling headphones if needed\n");
            recommendations.append("- Take sensory breaks when overwhelmed\n");
            recommendations.append("- Choose calm, organized study spaces\n");
            recommendations.append("- Minimize visual clutter on your screen\n\n");
        }
        
        // General recommendations based on preset
        recommendations.append("🚀 YOUR PERSONALIZED SETUP:\n");
        recommendations.append("UI Mode: ").append(getPresetDescription(assessment.getRecommendedPreset())).append("\n");
        recommendations.append("This mode automatically optimizes fonts, colors, spacing, and navigation for your needs.\n\n");
        
        recommendations.append("💡 STUDY TIPS:\n");
        recommendations.append("- Start with easier topics to build confidence\n");
        recommendations.append("- Celebrate small wins along the way\n");
        recommendations.append("- Don't hesitate to repeat lessons if needed\n");
        recommendations.append("- Use the built-in study timer for optimal focus\n");
        recommendations.append("- Take regular breaks to maintain performance\n");
        
        return recommendations.toString();
    }

    /**
     * Generate adaptive content based on assessment profile
     */
    public String generateAdaptiveContent(String originalContent, UserAssessment assessment) {
        StringBuilder adaptedContent = new StringBuilder();
        
        // Add content adaptation prompt for AI
        adaptedContent.append("Please adapt the following content for a learner with these characteristics:\n\n");
        
        if (assessment.hasSignificantAttentionNeeds()) {
            adaptedContent.append("- ATTENTION: Use short paragraphs, bullet points, clear headings\n");
        }
        
        if (assessment.hasSignificantReadingNeeds()) {
            adaptedContent.append("- READING: Use simple vocabulary, shorter sentences, clear structure\n");
        }
        
        if (assessment.hasSignificantSocialNeeds()) {
            adaptedContent.append("- COMMUNICATION: Be explicit and literal, avoid metaphors, use step-by-step instructions\n");
        }
        
        if (assessment.hasSignificantSensoryNeeds()) {
            adaptedContent.append("- SENSORY: Use calm language, minimize overwhelming descriptions\n");
        }
        
        adaptedContent.append("\nOriginal content:\n").append(originalContent);
        adaptedContent.append("\n\nAdapted content (maintain educational value while improving accessibility):");
        
        return adaptedContent.toString();
    }

    /**
     * Generate study schedule recommendations based on assessment
     */
    public Map<String, Object> generateStudyScheduleRecommendations(UserAssessment assessment) {
        Map<String, Object> schedule = new HashMap<>();
        
        // Determine optimal session length
        int sessionLength;
        int breakLength;
        
        if (assessment.hasSignificantAttentionNeeds()) {
            sessionLength = 15; // Shorter sessions for ADHD
            breakLength = 5;
        } else if (assessment.hasSignificantSensoryNeeds()) {
            sessionLength = 20; // Moderate sessions with longer breaks
            breakLength = 10;
        } else {
            sessionLength = 25; // Standard Pomodoro
            breakLength = 5;
        }
        
        schedule.put("sessionLength", sessionLength);
        schedule.put("breakLength", breakLength);
        schedule.put("dailySessions", calculateOptimalDailySessions(assessment));
        schedule.put("bestTimes", generateOptimalStudyTimes(assessment));
        schedule.put("tips", generateScheduleTips(assessment));
        
        return schedule;
    }

    /**
     * Enhanced learning recommendations using new assessment data
     */
    public LearningRecommendations generateEnhancedRecommendations(UserAssessment assessment) {
        LearningRecommendations recommendations = new LearningRecommendations();
        
        // Get user for existing functionality
        User user = userRepository.findById(assessment.getUserId()).orElse(null);
        if (user == null) {
            return recommendations; // Return empty if user not found
        }
        
        // Get lessons prioritized for this assessment profile
        List<Lesson> allLessons = lessonRepository.findAll();
        List<Lesson> prioritizedLessons = prioritizeLessonsForAssessment(allLessons, assessment);
        recommendations.setRecommendedLessons(prioritizedLessons.subList(0, Math.min(5, prioritizedLessons.size())));
        
        // Generate study schedule based on assessment
        StudySchedule schedule = generateAssessmentBasedSchedule(assessment);
        recommendations.setStudySchedule(schedule);
        
        // Generate motivational messages based on assessment traits
        List<String> motivationalMessages = generateAssessmentBasedMotivation(assessment);
        recommendations.setMotivationalMessages(motivationalMessages);
        
        // Set break interval based on attention needs
        if (assessment.hasSignificantAttentionNeeds()) {
            recommendations.setRecommendedBreakInterval(15); // Shorter sessions
        } else if (assessment.hasSignificantSensoryNeeds()) {
            recommendations.setRecommendedBreakInterval(20); // Longer breaks
        } else {
            recommendations.setRecommendedBreakInterval(25); // Standard
        }
        
        // Set difficulty based on overall assessment profile
        recommendations.setRecommendedDifficulty(calculateDifficultyFromAssessment(assessment));
        
        return recommendations;
    }

    // Private helper methods

    private Map<String, Object> parseAssessmentScores(String assessmentScoresJson) {
        if (assessmentScoresJson == null || assessmentScoresJson.isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(assessmentScoresJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            System.err.println("Error parsing assessment scores: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private NeuroProfile analyzeNeuroProfile(Map<String, Object> assessmentScores) {
        NeuroProfile profile = new NeuroProfile();
        
        // Analyze ADHD indicators
        double adhdScore = calculateAdhdScore(assessmentScores);
        profile.setAdhdIndicators(adhdScore);
        
        // Analyze Autism Spectrum indicators
        double autismScore = calculateAutismScore(assessmentScores);
        profile.setAutismSpectrumIndicators(autismScore);
        
        // Analyze processing speed
        double processingSpeed = calculateProcessingSpeed(assessmentScores);
        profile.setProcessingSpeedIndicators(processingSpeed);
        
        // Analyze attention patterns
        AttentionPattern attentionPattern = analyzeAttentionPattern(assessmentScores);
        profile.setAttentionPattern(attentionPattern);
        
        return profile;
    }

    private double calculateAdhdScore(Map<String, Object> scores) {
        // Focus Game analysis - high variability indicates ADHD
        Object focusData = scores.get("focusGame");
        if (focusData instanceof Map) {
            Map<String, Object> focusMap = (Map<String, Object>) focusData;
            Double consistency = convertToDouble(focusMap.get("consistency"));
            Double attentionVariability = convertToDouble(focusMap.get("attentionVariability"));
            
            if (consistency != null && attentionVariability != null) {
                // Higher variability and lower consistency suggest ADHD traits
                return Math.max(0, (attentionVariability - 20) / 30.0 + (80 - consistency) / 80.0) / 2.0;
            }
        }
        return 0.0;
    }

    private double calculateAutismScore(Map<String, Object> scores) {
        // Pattern Recognition and Executive Function analysis
        Object patternData = scores.get("patternGame");
        Object execData = scores.get("executiveFunctionGame");
        
        double patternScore = 0.0;
        double execScore = 0.0;
        
        if (patternData instanceof Map) {
            Map<String, Object> patternMap = (Map<String, Object>) patternData;
            Double score = convertToDouble(patternMap.get("score"));
            if (score != null && score > 85) {
                patternScore = 0.3; // High pattern recognition
            }
        }
        
        if (execData instanceof Map) {
            Map<String, Object> execMap = (Map<String, Object>) execData;
            Double taskSwitchingCost = convertToDouble(execMap.get("taskSwitchingCost"));
            if (taskSwitchingCost != null && taskSwitchingCost > 1000) {
                execScore = 0.4; // High task switching difficulty
            }
        }
        
        return Math.min(1.0, patternScore + execScore);
    }

    private double calculateProcessingSpeed(Map<String, Object> scores) {
        // Analyze response times across all games
        double totalResponseTime = 0;
        int gameCount = 0;
        
        for (Map.Entry<String, Object> entry : scores.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, Object> gameData = (Map<String, Object>) entry.getValue();
                Double avgResponseTime = convertToDouble(gameData.get("averageResponseTime"));
                if (avgResponseTime != null) {
                    totalResponseTime += avgResponseTime;
                    gameCount++;
                }
            }
        }
        
        if (gameCount > 0) {
            double avgResponseTime = totalResponseTime / gameCount;
            // Normalize to 0-1 scale (lower is better for processing speed)
            return Math.max(0, Math.min(1, (3000 - avgResponseTime) / 3000.0));
        }
        
        return 0.5; // Default middle value
    }

    private AttentionPattern analyzeAttentionPattern(Map<String, Object> scores) {
        double adhdScore = calculateAdhdScore(scores);
        
        if (adhdScore > 0.6) {
            return AttentionPattern.HIGHLY_VARIABLE;
        } else if (adhdScore > 0.3) {
            return AttentionPattern.MODERATELY_VARIABLE;
        } else {
            return AttentionPattern.CONSISTENT;
        }
    }

    private LearningStyle determineLearningStyle(User user, Map<String, Object> assessmentScores) {
        LearningStyle style = new LearningStyle();
        
        // Parse learning preferences from user data
        Map<String, Object> preferences = parsePreferences(user.getLearningPreferences());
        
        // Determine primary learning modality
        String preferredModality = determinePreferredModality(preferences, assessmentScores);
        style.setPrimaryModality(preferredModality);
        
        // Determine pacing preference
        String pacingPreference = determinePacingPreference(assessmentScores);
        style.setPacingPreference(pacingPreference);
        
        // Determine feedback preference
        String feedbackPreference = determineFeedbackPreference(assessmentScores);
        style.setFeedbackPreference(feedbackPreference);
        
        return style;
    }

    private Map<String, Object> parsePreferences(String preferencesJson) {
        if (preferencesJson == null || preferencesJson.isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(preferencesJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String determinePreferredModality(Map<String, Object> preferences, Map<String, Object> scores) {
        // Check explicit preferences first
        String explicitPreference = (String) preferences.get("preferredLearningStyle");
        if (explicitPreference != null) {
            return explicitPreference;
        }
        
        // Infer from assessment performance
        double visualScore = calculateVisualLearningScore(scores);
        double auditoryScore = calculateAuditoryLearningScore(scores);
        double kinestheticScore = calculateKinestheticLearningScore(scores);
        
        if (visualScore >= auditoryScore && visualScore >= kinestheticScore) {
            return "VISUAL";
        } else if (auditoryScore >= kinestheticScore) {
            return "AUDITORY";
        } else {
            return "KINESTHETIC";
        }
    }

    private double calculateVisualLearningScore(Map<String, Object> scores) {
        // Pattern recognition and visual processing games
        Object patternData = scores.get("patternGame");
        if (patternData instanceof Map) {
            Map<String, Object> patternMap = (Map<String, Object>) patternData;
            Double score = convertToDouble(patternMap.get("score"));
            return score != null ? score / 100.0 : 0.5;
        }
        return 0.5;
    }

    private double calculateAuditoryLearningScore(Map<String, Object> scores) {
        // For now, return moderate score - would need auditory assessment
        return 0.5;
    }

    private double calculateKinestheticLearningScore(Map<String, Object> scores) {
        // Reaction time and interactive game performance
        Object reactionData = scores.get("reactionGame");
        if (reactionData instanceof Map) {
            Map<String, Object> reactionMap = (Map<String, Object>) reactionData;
            Double avgTime = convertToDouble(reactionMap.get("averageTime"));
            if (avgTime != null && avgTime < 800) {
                return 0.8; // Good reaction time suggests kinesthetic learning
            }
        }
        return 0.5;
    }

    private String determinePacingPreference(Map<String, Object> scores) {
        double adhdScore = calculateAdhdScore(scores);
        if (adhdScore > 0.5) {
            return "SHORT_BURSTS"; // ADHD students benefit from shorter sessions
        } else {
            return "STEADY_PACE";
        }
    }

    private String determineFeedbackPreference(Map<String, Object> scores) {
        double autismScore = calculateAutismScore(scores);
        if (autismScore > 0.5) {
            return "DETAILED_STRUCTURED"; // Autism spectrum students prefer detailed feedback
        } else {
            return "ENCOURAGING_BRIEF";
        }
    }

    private SessionParameters calculateOptimalSessionParameters(NeuroProfile neuroProfile) {
        SessionParameters params = new SessionParameters();
        
        // Calculate optimal session length based on ADHD indicators
        if (neuroProfile.getAdhdIndicators() > 0.6) {
            params.setOptimalSessionLength(15); // 15 minutes for high ADHD
            params.setOptimalBreakInterval(5);  // 5 minute breaks
        } else if (neuroProfile.getAdhdIndicators() > 0.3) {
            params.setOptimalSessionLength(25); // 25 minutes for moderate ADHD
            params.setOptimalBreakInterval(10); // 10 minute breaks
        } else {
            params.setOptimalSessionLength(45); // 45 minutes for typical attention
            params.setOptimalBreakInterval(15); // 15 minute breaks
        }
        
        // Adjust for processing speed
        if (neuroProfile.getProcessingSpeedIndicators() < 0.4) {
            params.setOptimalSessionLength((int) (params.getOptimalSessionLength() * 0.8));
        }
        
        return params;
    }

    private PerformanceMetrics analyzePerformanceMetrics(User user) {
        PerformanceMetrics metrics = new PerformanceMetrics();
        
        // Get comprehensive performance data from lesson progress
        Long completedQuizzes = leaderboardRepository.countByUserId(user.getId());
        metrics.setCompletedQuizzes(completedQuizzes.intValue());
        
        // Get average quiz score from lesson progress
        Double averageQuizScore = lessonProgressRepository.getAverageQuizScore(user);
        if (averageQuizScore != null) {
            metrics.setAverageScore(averageQuizScore);
        } else {
            metrics.setAverageScore(0.0);
        }
        
        // Estimate difficulty level based on performance
        if (metrics.getAverageScore() >= 85) {
            metrics.setAverageDifficulty(DifficultyLevel.ADVANCED);
        } else if (metrics.getAverageScore() >= 70) {
            metrics.setAverageDifficulty(DifficultyLevel.INTERMEDIATE);
        } else {
            metrics.setAverageDifficulty(DifficultyLevel.BEGINNER);
        }
        
        return metrics;
    }

    private List<Lesson> prioritizeLessons(List<Lesson> lessons, LearningProfile profile) {
        return lessons.stream()
                .sorted((l1, l2) -> calculateLessonScore(l2, profile) - calculateLessonScore(l1, profile))
                .collect(Collectors.toList());
    }

    private Integer calculateLessonScore(Lesson lesson, LearningProfile profile) {
        int score = 0;
        
        // Base score
        score += 50;
        
        // Adjust for difficulty preference
        // (In a real implementation, lessons would have difficulty levels)
        score += 20;
        
        // Adjust for learning style
        if (profile.getLearningStyle().getPrimaryModality().equals("VISUAL") && 
            lesson.getYoutubeUrl() != null && !lesson.getYoutubeUrl().isEmpty()) {
            score += 30; // Video content for visual learners
        }
        
        // Prioritize shorter content for ADHD students
        if (profile.getNeuroProfile().getAdhdIndicators() > 0.5) {
            score += 25; // Assume this lesson is suitable for ADHD
        }
        
        return score;
    }

    private StudySchedule generateOptimalStudySchedule(LearningProfile profile) {
        StudySchedule schedule = new StudySchedule();
        
        // Generate study times based on attention pattern
        List<String> optimalTimes = new ArrayList<>();
        
        if (profile.getNeuroProfile().getAttentionPattern() == AttentionPattern.HIGHLY_VARIABLE) {
            // Multiple short sessions for ADHD
            optimalTimes.add("9:00 AM - 9:15 AM");
            optimalTimes.add("11:00 AM - 11:15 AM");
            optimalTimes.add("2:00 PM - 2:15 PM");
            optimalTimes.add("4:00 PM - 4:15 PM");
        } else {
            // Longer sessions for consistent attention
            optimalTimes.add("9:00 AM - 9:45 AM");
            optimalTimes.add("2:00 PM - 2:45 PM");
        }
        
        schedule.setOptimalStudyTimes(optimalTimes);
        schedule.setSessionLength(profile.getSessionParameters().getOptimalSessionLength());
        schedule.setBreakDuration(profile.getSessionParameters().getOptimalBreakInterval());
        
        return schedule;
    }

    private List<String> generateMotivationalMessages(LearningProfile profile) {
        List<String> messages = new ArrayList<>();
        
        // Get current context
        String primaryModality = profile.getLearningStyle().getPrimaryModality();
        double adhdScore = profile.getNeuroProfile().getAdhdIndicators();
        double autismScore = profile.getNeuroProfile().getAutismSpectrumIndicators();
        int completedQuizzes = profile.getPerformanceMetrics().getCompletedQuizzes();
        double avgScore = profile.getPerformanceMetrics().getAverageScore();
        
        // Time-based messages
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) {
            messages.add("🌅 Good morning! Your brain is fresh and ready to learn something amazing today!");
        } else if (hour < 17) {
            messages.add("☀️ Afternoon focus time! You're doing great - keep that momentum going!");
        } else {
            messages.add("🌆 Evening learning session! Perfect time to review and consolidate what you've learned!");
        }
        
        // Performance-based dynamic messages
        if (avgScore >= 85) {
            messages.add("🔥 You're on fire! " + (int)avgScore + "% average score shows you're mastering this material!");
            messages.add("🎯 Your high performance proves your learning strategy is working perfectly!");
        } else if (avgScore >= 70) {
            messages.add("📈 Steady progress at " + (int)avgScore + "% average - you're building solid foundations!");
            messages.add("💪 You're in the learning sweet spot - keep this consistent pace going!");
        } else if (avgScore > 0) {
            messages.add("🌱 Every expert was once a beginner - you're growing stronger with each attempt!");
            messages.add("💡 Focus on understanding over speed - you're developing deep learning skills!");
        }
        
        // Progress-based milestone messages
        if (completedQuizzes == 0) {
            messages.add("🚀 Ready for your first quiz? You've got this - take your time and trust your knowledge!");
        } else if (completedQuizzes == 1) {
            messages.add("🎉 One quiz completed! You're building confidence with each step forward!");
        } else if (completedQuizzes < 5) {
            messages.add("🏃‍♀️ " + completedQuizzes + " quizzes down - you're developing a great learning rhythm!");
        } else if (completedQuizzes < 10) {
            messages.add("⭐ " + completedQuizzes + " quizzes completed! You're becoming a learning champion!");
        } else {
            messages.add("🏆 " + completedQuizzes + " quizzes conquered! You're a true learning superstar!");
        }
        
        // Neurodivergent-specific dynamic support
        if (adhdScore > 0.6) {
            messages.add("⚡ Your ADHD brain thrives on variety - mix up your study activities today!");
            messages.add("🎯 Short focused bursts are your superpower - use that natural intensity!");
            if (completedQuizzes > 3) {
                messages.add("🔋 You've proven you can maintain focus when it matters - trust your abilities!");
            }
        } else if (adhdScore > 0.3) {
            messages.add("🌊 You have great focus when engaged - find what sparks your curiosity today!");
        }
        
        if (autismScore > 0.5) {
            messages.add("🧩 Your attention to detail is incredible - use that precision to master concepts!");
            messages.add("📚 Structure and routine work for you - you're building excellent learning habits!");
            if (avgScore > 80) {
                messages.add("🎯 Your systematic approach to learning is paying off beautifully!");
            }
        }
        
        // Learning style encouragement
        if (primaryModality.equals("VISUAL")) {
            messages.add("👁️ Your visual processing skills are amazing - those video lessons are perfect for you!");
            if (completedQuizzes > 2) {
                messages.add("🎨 You're proving that visual learners can excel - keep using those strengths!");
            }
        } else if (primaryModality.equals("KINESTHETIC")) {
            messages.add("🏃‍♂️ Your hands-on learning style is powerful - engage actively with every lesson!");
            messages.add("✋ You learn by doing - trust your instincts and dive into interactive content!");
        } else if (primaryModality.equals("AUDITORY")) {
            messages.add("👂 Your listening skills are your secret weapon - absorb those explanations fully!");
            messages.add("🎵 You process information beautifully through sound - use that gift!");
        }
        
        // Seasonal/contextual motivation
        String dayOfWeek = java.time.LocalDate.now().getDayOfWeek().toString();
        if (dayOfWeek.equals("MONDAY")) {
            messages.add("💪 Fresh week, fresh opportunities - let's make this Monday amazing!");
        } else if (dayOfWeek.equals("FRIDAY")) {
            messages.add("🎉 Friday energy! End the week strong with some focused learning!");
        } else if (dayOfWeek.equals("SATURDAY") || dayOfWeek.equals("SUNDAY")) {
            messages.add("🌟 Weekend learning time - you're truly dedicated to your growth!");
        }
        
        // Encouraging baseline messages (always include some)
        messages.add("🌟 Your unique learning journey is exactly what makes you special!");
        messages.add("💎 You're not just learning facts - you're developing your amazing mind!");
        messages.add("🚀 Every minute you spend learning is an investment in your incredible future!");
        
        return messages;
    }

    private DifficultyLevel calculateOptimalDifficulty(LearningProfile profile) {
        double avgScore = profile.getPerformanceMetrics().getAverageScore();
        double processingSpeed = profile.getNeuroProfile().getProcessingSpeedIndicators();
        
        // Adjust difficulty based on both performance and processing speed
        if (avgScore >= 85 && processingSpeed > 0.6) {
            return DifficultyLevel.ADVANCED;
        } else if (avgScore >= 70 && processingSpeed > 0.4) {
            return DifficultyLevel.INTERMEDIATE;
        } else {
            return DifficultyLevel.BEGINNER;
        }
    }

    private DifficultyLevel increaseDifficulty(DifficultyLevel current) {
        switch (current) {
            case BEGINNER:
                return DifficultyLevel.INTERMEDIATE;
            case INTERMEDIATE:
                return DifficultyLevel.ADVANCED;
            default:
                return current;
        }
    }

    private DifficultyLevel decreaseDifficulty(DifficultyLevel current) {
        switch (current) {
            case ADVANCED:
                return DifficultyLevel.INTERMEDIATE;
            case INTERMEDIATE:
                return DifficultyLevel.BEGINNER;
            default:
                return current;
        }
    }

    // New helper methods for assessment-based functionality
    
    private String getPresetDescription(String preset) {
        switch (preset.toLowerCase()) {
            case "adhd": return "ADHD Support Mode - Optimized for attention and focus";
            case "dyslexia": return "Dyslexia Support Mode - Enhanced readability and text support";
            case "autism": return "Autism Support Mode - Structured and predictable interface";
            case "sensory": return "Sensory-Friendly Mode - Reduced stimulation and calm design";
            case "dyslexia-adhd": return "Combined Support Mode - ADHD + Dyslexia optimizations";
            default: return "Standard Mode - Balanced learning environment";
        }
    }
    
    private List<Integer> calculateOptimalDailySessions(UserAssessment assessment) {
        List<Integer> sessions = new ArrayList<>();
        
        if (assessment.hasSignificantAttentionNeeds()) {
            // More frequent, shorter sessions for ADHD
            sessions.addAll(Arrays.asList(3, 4, 5, 4, 3, 2, 1)); // Mon-Sun
        } else if (assessment.hasSignificantSensoryNeeds()) {
            // Moderate sessions with recovery time
            sessions.addAll(Arrays.asList(2, 3, 2, 3, 2, 2, 1)); // Mon-Sun
        } else {
            // Standard schedule
            sessions.addAll(Arrays.asList(2, 3, 3, 3, 2, 2, 1)); // Mon-Sun
        }
        
        return sessions;
    }
    
    private List<String> generateOptimalStudyTimes(UserAssessment assessment) {
        List<String> times = new ArrayList<>();
        
        if (assessment.hasSignificantAttentionNeeds()) {
            // Multiple short sessions throughout the day
            times.add("9:00 AM - 9:15 AM (Morning focus peak)");
            times.add("10:30 AM - 10:45 AM (Mid-morning burst)");
            times.add("2:00 PM - 2:15 PM (Afternoon reset)");
            times.add("4:00 PM - 4:15 PM (Late afternoon focus)");
        } else if (assessment.hasSignificantSensoryNeeds()) {
            // Quieter times of day
            times.add("8:00 AM - 8:20 AM (Quiet morning)");
            times.add("1:00 PM - 1:20 PM (Lunch break calm)");
            times.add("7:00 PM - 7:20 PM (Evening quiet)");
        } else {
            // Standard optimal times
            times.add("9:00 AM - 9:25 AM (Morning peak)");
            times.add("2:00 PM - 2:25 PM (Afternoon focus)");
            times.add("7:00 PM - 7:25 PM (Evening review)");
        }
        
        return times;
    }
    
    private List<String> generateScheduleTips(UserAssessment assessment) {
        List<String> tips = new ArrayList<>();
        
        if (assessment.hasSignificantAttentionNeeds()) {
            tips.add("Use a timer to stay on track during sessions");
            tips.add("Take movement breaks between study periods");
            tips.add("Study when you feel most alert and focused");
            tips.add("Minimize distractions in your study environment");
        }
        
        if (assessment.hasSignificantReadingNeeds()) {
            tips.add("Read material aloud or use text-to-speech");
            tips.add("Take frequent breaks when reading heavy content");
            tips.add("Use highlighting to track your reading progress");
        }
        
        if (assessment.hasSignificantSocialNeeds()) {
            tips.add("Follow the same study routine each day");
            tips.add("Break complex topics into smaller, clear steps");
            tips.add("Review instructions carefully before starting");
        }
        
        if (assessment.hasSignificantSensoryNeeds()) {
            tips.add("Study in a calm, organized environment");
            tips.add("Adjust lighting and screen brightness for comfort");
            tips.add("Take sensory breaks when feeling overwhelmed");
        }
        
        // General tips
        tips.add("Start with easier topics to build confidence");
        tips.add("Celebrate completing each study session");
        
        return tips;
    }
    
    private List<Lesson> prioritizeLessonsForAssessment(List<Lesson> lessons, UserAssessment assessment) {
        return lessons.stream()
                .sorted((l1, l2) -> calculateAssessmentLessonScore(l2, assessment) - calculateAssessmentLessonScore(l1, assessment))
                .collect(Collectors.toList());
    }
    
    private Integer calculateAssessmentLessonScore(Lesson lesson, UserAssessment assessment) {
        int score = 50; // Base score
        
        // Prioritize video content for students with reading difficulties
        if (assessment.hasSignificantReadingNeeds() && 
            lesson.getYoutubeUrl() != null && !lesson.getYoutubeUrl().isEmpty()) {
            score += 30;
        }
        
        // Prioritize structured content for students with social communication needs
        if (assessment.hasSignificantSocialNeeds()) {
            score += 20; // Assume lessons have structured format
        }
        
        // Prioritize shorter content for attention needs
        if (assessment.hasSignificantAttentionNeeds()) {
            score += 25; // Assume lesson is appropriately sized
        }
        
        return score;
    }
    
    private StudySchedule generateAssessmentBasedSchedule(UserAssessment assessment) {
        StudySchedule schedule = new StudySchedule();
        
        List<String> optimalTimes = generateOptimalStudyTimes(assessment);
        schedule.setOptimalStudyTimes(optimalTimes);
        
        if (assessment.hasSignificantAttentionNeeds()) {
            schedule.setSessionLength(15);
            schedule.setBreakDuration(5);
        } else if (assessment.hasSignificantSensoryNeeds()) {
            schedule.setSessionLength(20);
            schedule.setBreakDuration(10);
        } else {
            schedule.setSessionLength(25);
            schedule.setBreakDuration(5);
        }
        
        return schedule;
    }
    
    private List<String> generateAssessmentBasedMotivation(UserAssessment assessment) {
        List<String> messages = new ArrayList<>();
        
        // Trait-specific encouragement
        if (assessment.hasSignificantAttentionNeeds()) {
            messages.add("🎯 Your focused energy is a superpower - use it in short, intense bursts!");
            messages.add("⚡ ADHD brains thrive on variety - mix up your learning activities!");
            messages.add("🏃‍♀️ Movement breaks help your brain process information better!");
        }
        
        if (assessment.hasSignificantReadingNeeds()) {
            messages.add("📚 Every reading challenge you overcome makes you stronger!");
            messages.add("🎧 Audio learning can be just as powerful as reading - use all your tools!");
            messages.add("📖 Take your time with text - understanding is more important than speed!");
        }
        
        if (assessment.hasSignificantSocialNeeds()) {
            messages.add("🧩 Your attention to detail and systematic thinking are incredible strengths!");
            messages.add("📋 Structure and routine help you excel - you're building great habits!");
            messages.add("🎯 Your methodical approach to learning leads to deep understanding!");
        }
        
        if (assessment.hasSignificantSensoryNeeds()) {
            messages.add("🌟 Creating the right environment for learning shows great self-awareness!");
            messages.add("🎨 Your sensitivity to your surroundings helps you learn more effectively!");
            messages.add("💡 Taking sensory breaks is a smart learning strategy!");
        }
        
        // General encouragement
        messages.add("🚀 Your unique learning style is exactly what makes you special!");
        messages.add("💎 You're not just learning - you're developing your amazing mind!");
        messages.add("🌈 Every step forward is progress worth celebrating!");
        
        return messages;
    }
    
    private DifficultyLevel calculateDifficultyFromAssessment(UserAssessment assessment) {
        // Start with beginner level for new assessment users
        int totalScore = assessment.getTotalScore();
        
        // Adjust based on trait combinations
        if (totalScore < 30) {
            return DifficultyLevel.BEGINNER;
        } else if (totalScore < 60) {
            return DifficultyLevel.INTERMEDIATE;
        } else {
            // High scores might indicate specific needs rather than difficulty
            return DifficultyLevel.INTERMEDIATE; // Conservative approach
        }
    }

    // Helper method to safely convert objects to Double
    private Double convertToDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // Inner classes for data structures

    public static class LearningProfile {
        private Long userId;
        private String userEmail;
        private NeuroProfile neuroProfile;
        private LearningStyle learningStyle;
        private SessionParameters sessionParameters;
        private PerformanceMetrics performanceMetrics;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        
        public NeuroProfile getNeuroProfile() { return neuroProfile; }
        public void setNeuroProfile(NeuroProfile neuroProfile) { this.neuroProfile = neuroProfile; }
        
        public LearningStyle getLearningStyle() { return learningStyle; }
        public void setLearningStyle(LearningStyle learningStyle) { this.learningStyle = learningStyle; }
        
        public SessionParameters getSessionParameters() { return sessionParameters; }
        public void setSessionParameters(SessionParameters sessionParameters) { this.sessionParameters = sessionParameters; }
        
        public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }
        public void setPerformanceMetrics(PerformanceMetrics performanceMetrics) { this.performanceMetrics = performanceMetrics; }
    }

    public static class NeuroProfile {
        private double adhdIndicators;
        private double autismSpectrumIndicators;
        private double processingSpeedIndicators;
        private AttentionPattern attentionPattern;

        // Getters and setters
        public double getAdhdIndicators() { return adhdIndicators; }
        public void setAdhdIndicators(double adhdIndicators) { this.adhdIndicators = adhdIndicators; }
        
        public double getAutismSpectrumIndicators() { return autismSpectrumIndicators; }
        public void setAutismSpectrumIndicators(double autismSpectrumIndicators) { this.autismSpectrumIndicators = autismSpectrumIndicators; }
        
        public double getProcessingSpeedIndicators() { return processingSpeedIndicators; }
        public void setProcessingSpeedIndicators(double processingSpeedIndicators) { this.processingSpeedIndicators = processingSpeedIndicators; }
        
        public AttentionPattern getAttentionPattern() { return attentionPattern; }
        public void setAttentionPattern(AttentionPattern attentionPattern) { this.attentionPattern = attentionPattern; }
    }

    public enum AttentionPattern {
        CONSISTENT, MODERATELY_VARIABLE, HIGHLY_VARIABLE
    }

    public static class LearningStyle {
        private String primaryModality; // VISUAL, AUDITORY, KINESTHETIC
        private String pacingPreference; // SHORT_BURSTS, STEADY_PACE
        private String feedbackPreference; // DETAILED_STRUCTURED, ENCOURAGING_BRIEF

        // Getters and setters
        public String getPrimaryModality() { return primaryModality; }
        public void setPrimaryModality(String primaryModality) { this.primaryModality = primaryModality; }
        
        public String getPacingPreference() { return pacingPreference; }
        public void setPacingPreference(String pacingPreference) { this.pacingPreference = pacingPreference; }
        
        public String getFeedbackPreference() { return feedbackPreference; }
        public void setFeedbackPreference(String feedbackPreference) { this.feedbackPreference = feedbackPreference; }
    }

    public static class SessionParameters {
        private int optimalSessionLength; // in minutes
        private int optimalBreakInterval; // in minutes

        // Getters and setters
        public int getOptimalSessionLength() { return optimalSessionLength; }
        public void setOptimalSessionLength(int optimalSessionLength) { this.optimalSessionLength = optimalSessionLength; }
        
        public int getOptimalBreakInterval() { return optimalBreakInterval; }
        public void setOptimalBreakInterval(int optimalBreakInterval) { this.optimalBreakInterval = optimalBreakInterval; }
    }

    public static class PerformanceMetrics {
        private int completedQuizzes;
        private double averageScore;
        private DifficultyLevel averageDifficulty;

        // Getters and setters
        public int getCompletedQuizzes() { return completedQuizzes; }
        public void setCompletedQuizzes(int completedQuizzes) { this.completedQuizzes = completedQuizzes; }
        
        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
        
        public DifficultyLevel getAverageDifficulty() { return averageDifficulty; }
        public void setAverageDifficulty(DifficultyLevel averageDifficulty) { this.averageDifficulty = averageDifficulty; }
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    public static class LearningRecommendations {
        private List<Lesson> recommendedLessons;
        private StudySchedule studySchedule;
        private List<String> motivationalMessages;
        private int recommendedBreakInterval;
        private DifficultyLevel recommendedDifficulty;

        // Getters and setters
        public List<Lesson> getRecommendedLessons() { return recommendedLessons; }
        public void setRecommendedLessons(List<Lesson> recommendedLessons) { this.recommendedLessons = recommendedLessons; }
        
        public StudySchedule getStudySchedule() { return studySchedule; }
        public void setStudySchedule(StudySchedule studySchedule) { this.studySchedule = studySchedule; }
        
        public List<String> getMotivationalMessages() { return motivationalMessages; }
        public void setMotivationalMessages(List<String> motivationalMessages) { this.motivationalMessages = motivationalMessages; }
        
        public int getRecommendedBreakInterval() { return recommendedBreakInterval; }
        public void setRecommendedBreakInterval(int recommendedBreakInterval) { this.recommendedBreakInterval = recommendedBreakInterval; }
        
        public DifficultyLevel getRecommendedDifficulty() { return recommendedDifficulty; }
        public void setRecommendedDifficulty(DifficultyLevel recommendedDifficulty) { this.recommendedDifficulty = recommendedDifficulty; }
    }

    public static class StudySchedule {
        private List<String> optimalStudyTimes;
        private int sessionLength;
        private int breakDuration;

        // Getters and setters
        public List<String> getOptimalStudyTimes() { return optimalStudyTimes; }
        public void setOptimalStudyTimes(List<String> optimalStudyTimes) { this.optimalStudyTimes = optimalStudyTimes; }
        
        public int getSessionLength() { return sessionLength; }
        public void setSessionLength(int sessionLength) { this.sessionLength = sessionLength; }
        
        public int getBreakDuration() { return breakDuration; }
        public void setBreakDuration(int breakDuration) { this.breakDuration = breakDuration; }
    }
}
