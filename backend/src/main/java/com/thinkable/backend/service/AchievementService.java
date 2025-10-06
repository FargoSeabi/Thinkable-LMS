package com.thinkable.backend.service;

import com.thinkable.backend.entity.Achievement;
import com.thinkable.backend.entity.UserAchievement;
import com.thinkable.backend.repository.AchievementRepository;
import com.thinkable.backend.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AchievementService {
    
    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);
    
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    
    /**
     * Check and award new achievements for a user based on their current progress
     */
    public List<UserAchievement> checkAndAwardAchievements(Long userId, Map<String, Integer> userMetrics) {
        logger.info("Checking achievements for user {}", userId);
        
        List<Achievement> allAchievements = achievementRepository.findByIsActiveTrueOrderByCreatedAtAsc();
        List<UserAchievement> earnedAchievements = userAchievementRepository.findByUserIdOrderByEarnedAtDesc(userId);
        
        // Get IDs of already earned achievements
        Set<Long> earnedAchievementIds = earnedAchievements.stream()
            .map(ua -> ua.getAchievement().getId())
            .collect(Collectors.toSet());
        
        List<UserAchievement> newlyEarned = new ArrayList<>();
        
        for (Achievement achievement : allAchievements) {
            // Skip if already earned
            if (earnedAchievementIds.contains(achievement.getId())) {
                continue;
            }
            
            // Check if user qualifies for this achievement
            if (qualifiesForAchievement(achievement, userMetrics)) {
                UserAchievement userAchievement = awardAchievement(userId, achievement, userMetrics);
                newlyEarned.add(userAchievement);
                logger.info("Awarded achievement '{}' to user {}", achievement.getName(), userId);
            }
        }
        
        return newlyEarned;
    }
    
    /**
     * Check if user qualifies for a specific achievement
     */
    private boolean qualifiesForAchievement(Achievement achievement, Map<String, Integer> userMetrics) {
        String requirementType = achievement.getRequirementType();
        Integer requiredValue = achievement.getRequirementValue();
        Integer userValue = userMetrics.getOrDefault(requirementType, 0);
        
        return userValue >= requiredValue;
    }
    
    /**
     * Award an achievement to a user
     */
    private UserAchievement awardAchievement(Long userId, Achievement achievement, Map<String, Integer> userMetrics) {
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUserId(userId);
        userAchievement.setAchievement(achievement);
        userAchievement.setProgressValue(userMetrics.getOrDefault(achievement.getRequirementType(), 0));
        userAchievement.setEarnedAt(LocalDateTime.now());
        userAchievement.setIsNew(true);
        userAchievement.setNotificationSent(false);
        
        return userAchievementRepository.save(userAchievement);
    }
    
    /**
     * Get all achievements earned by a user
     */
    @Transactional(readOnly = true)
    public List<UserAchievement> getUserAchievements(Long userId) {
        return userAchievementRepository.findByUserIdOrderByEarnedAtDesc(userId);
    }
    
    /**
     * Get new achievements for a user (not yet viewed)
     */
    @Transactional(readOnly = true)
    public List<UserAchievement> getNewAchievements(Long userId) {
        return userAchievementRepository.findByUserIdAndIsNewTrueOrderByEarnedAtDesc(userId);
    }
    
    /**
     * Mark achievements as viewed
     */
    public void markAchievementsAsViewed(Long userId, List<Long> achievementIds) {
        List<UserAchievement> achievements = userAchievementRepository.findByUserIdOrderByEarnedAtDesc(userId);
        
        for (UserAchievement ua : achievements) {
            if (achievementIds.contains(ua.getId()) && ua.getIsNew()) {
                ua.markAsViewed();
                userAchievementRepository.save(ua);
            }
        }
    }
    
    /**
     * Get achievement progress for a user (shows locked achievements with progress)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAchievementProgress(Long userId, Map<String, Integer> userMetrics) {
        List<Achievement> allAchievements = achievementRepository.findByIsActiveTrueAndIsHiddenFalseOrderByPointsValueAsc();
        List<UserAchievement> earnedAchievements = userAchievementRepository.findByUserIdOrderByEarnedAtDesc(userId);
        
        Set<Long> earnedIds = earnedAchievements.stream()
            .map(ua -> ua.getAchievement().getId())
            .collect(Collectors.toSet());
        
        List<Map<String, Object>> progressList = new ArrayList<>();
        
        for (Achievement achievement : allAchievements) {
            Map<String, Object> progress = new HashMap<>();
            progress.put("achievement", achievement);
            progress.put("isEarned", earnedIds.contains(achievement.getId()));
            
            if (earnedIds.contains(achievement.getId())) {
                // Find the earned achievement for timestamp
                earnedAchievements.stream()
                    .filter(ua -> ua.getAchievement().getId().equals(achievement.getId()))
                    .findFirst()
                    .ifPresent(ua -> progress.put("earnedAt", ua.getEarnedAt()));
                progress.put("progress", 100);
            } else {
                // Calculate progress towards this achievement
                Integer userValue = userMetrics.getOrDefault(achievement.getRequirementType(), 0);
                Integer requiredValue = achievement.getRequirementValue();
                int progressPercent = Math.min(100, (userValue * 100) / Math.max(1, requiredValue));
                progress.put("progress", progressPercent);
                progress.put("currentValue", userValue);
                progress.put("requiredValue", requiredValue);
            }
            
            progressList.add(progress);
        }
        
        return progressList;
    }
    
    /**
     * Get user's achievement statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAchievementStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        Long totalEarned = userAchievementRepository.countByUserId(userId);
        Long totalPoints = userAchievementRepository.getTotalPointsByUserId(userId);
        Long totalAvailable = (long) achievementRepository.findByIsActiveTrueOrderByCreatedAtAsc().size();
        
        stats.put("totalEarned", totalEarned != null ? totalEarned : 0);
        stats.put("totalPoints", totalPoints != null ? totalPoints : 0);
        stats.put("totalAvailable", totalAvailable);
        stats.put("completionPercentage", totalAvailable > 0 ? (totalEarned * 100 / totalAvailable) : 0);
        
        // Get recent achievements (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<UserAchievement> recentAchievements = userAchievementRepository.findRecentAchievements(userId, sevenDaysAgo);
        stats.put("recentAchievements", recentAchievements);
        
        return stats;
    }
    
    /**
     * Initialize default achievements (called once during app startup)
     */
    @Transactional
    public void initializeDefaultAchievements() {
        if (achievementRepository.count() > 0) {
            return; // Already initialized
        }
        
        logger.info("Initializing default achievements");
        
        // Learning Milestones
        createAchievement("First Steps", "Complete your first lesson!", "🌟", "LEARNING", 10, "LESSONS_COMPLETED", 1, "COMMON", "KIDS");
        createAchievement("Getting Started", "Complete 5 lessons", "⭐", "LEARNING", 25, "LESSONS_COMPLETED", 5, "COMMON", "KIDS");
        createAchievement("Learning Hero", "Complete 10 lessons", "🏆", "LEARNING", 50, "LESSONS_COMPLETED", 10, "RARE", "KIDS");
        createAchievement("Knowledge Master", "Complete 25 lessons", "👑", "LEARNING", 100, "LESSONS_COMPLETED", 25, "EPIC", "KIDS");
        createAchievement("Super Learner", "Complete 50 lessons", "💎", "LEARNING", 200, "LESSONS_COMPLETED", 50, "LEGENDARY", "KIDS");
        
        // Study Streak Badges
        createAchievement("Day One", "Study for 1 day", "📚", "STREAK", 5, "DAYS_STREAK", 1, "COMMON", "KIDS");
        createAchievement("Three's a Charm", "Study for 3 days in a row", "🔥", "STREAK", 15, "DAYS_STREAK", 3, "COMMON", "KIDS");
        createAchievement("Week Warrior", "Study for 7 days in a row", "⚡", "STREAK", 35, "DAYS_STREAK", 7, "RARE", "KIDS");
        createAchievement("Unstoppable", "Study for 14 days in a row", "💪", "STREAK", 75, "DAYS_STREAK", 14, "EPIC", "KIDS");
        createAchievement("Legend", "Study for 30 days in a row", "👑", "STREAK", 150, "DAYS_STREAK", 30, "LEGENDARY", "KIDS");
        
        // Quiz Performance
        createAchievement("Quiz Starter", "Take your first quiz", "🎯", "MILESTONE", 10, "QUIZZES_COMPLETED", 1, "COMMON", "KIDS");
        createAchievement("Smart Cookie", "Get 80% or higher on a quiz", "🍪", "MILESTONE", 20, "QUIZ_SCORE", 80, "COMMON", "KIDS");
        createAchievement("Brilliant Mind", "Get 90% or higher on a quiz", "🧠", "MILESTONE", 30, "QUIZ_SCORE", 90, "RARE", "KIDS");
        createAchievement("Perfect Score", "Get 100% on a quiz", "🌟", "MILESTONE", 50, "QUIZ_SCORE", 100, "EPIC", "KIDS");
        
        // Study Time
        createAchievement("Quick Learner", "Study for 30 minutes total", "⏰", "MILESTONE", 10, "TOTAL_STUDY_TIME", 30, "COMMON", "KIDS");
        createAchievement("Dedicated Student", "Study for 2 hours total", "📖", "MILESTONE", 25, "TOTAL_STUDY_TIME", 120, "COMMON", "KIDS");
        createAchievement("Study Champion", "Study for 5 hours total", "🎓", "MILESTONE", 50, "TOTAL_STUDY_TIME", 300, "RARE", "KIDS");
        
        // Accessibility Champions
        createAchievement("Tool Explorer", "Use 3 different accessibility tools", "🔧", "ACCESSIBILITY", 15, "TOOLS_USED_COUNT", 3, "COMMON", "KIDS");
        createAchievement("Accessibility Hero", "Use 5 different accessibility tools", "🦸", "ACCESSIBILITY", 30, "TOOLS_USED_COUNT", 5, "RARE", "KIDS");
        
        logger.info("Initialized {} default achievements", achievementRepository.count());
    }
    
    private void createAchievement(String name, String description, String icon, String category, 
                                 Integer points, String requirementType, Integer requirementValue, 
                                 String rarity, String ageGroup) {
        Achievement achievement = new Achievement();
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setIcon(icon);
        achievement.setCategory(category);
        achievement.setPointsValue(points);
        achievement.setRequirementType(requirementType);
        achievement.setRequirementValue(requirementValue);
        achievement.setIsActive(true);
        achievement.setIsHidden(false);
        achievement.setRarity(rarity);
        achievement.setAgeGroup(ageGroup);
        achievement.setCreatedAt(LocalDateTime.now());
        
        achievementRepository.save(achievement);
    }
}
