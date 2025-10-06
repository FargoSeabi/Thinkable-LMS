package com.thinkable.backend.repository;

import com.thinkable.backend.model.AdaptiveUISettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdaptiveUISettingsRepository extends JpaRepository<AdaptiveUISettings, Long> {

    /**
     * Find UI settings for a specific user
     */
    Optional<AdaptiveUISettings> findByUserId(Long userId);

    /**
     * Find all users using a specific UI preset
     */
    List<AdaptiveUISettings> findByUiPreset(String uiPreset);

    /**
     * Find users with auto-applied settings
     */
    List<AdaptiveUISettings> findByAutoAppliedTrue();

    /**
     * Find users with custom (non-auto) settings
     */
    List<AdaptiveUISettings> findByAutoAppliedFalse();

    /**
     * Get preset usage statistics
     */
    @Query("SELECT aus.uiPreset, COUNT(aus) FROM AdaptiveUISettings aus GROUP BY aus.uiPreset ORDER BY COUNT(aus) DESC")
    List<Object[]> getPresetUsageStatistics();

    /**
     * Find users with high contrast settings
     */
    @Query("SELECT aus FROM AdaptiveUISettings aus WHERE aus.contrastLevel IN ('high', 'extra-high')")
    List<AdaptiveUISettings> findHighContrastUsers();

    /**
     * Find users with animations disabled
     */
    List<AdaptiveUISettings> findByAnimationsEnabledFalse();

    /**
     * Find users with specific font families
     */
    @Query("SELECT aus FROM AdaptiveUISettings aus WHERE aus.fontFamily LIKE %:fontPattern%")
    List<AdaptiveUISettings> findByFontFamilyContaining(@Param("fontPattern") String fontPattern);

    /**
     * Find users with dyslexia-friendly fonts
     */
    @Query("SELECT aus FROM AdaptiveUISettings aus WHERE " +
           "aus.fontFamily LIKE '%Comic Neue%' OR " +
           "aus.fontFamily LIKE '%OpenDyslexic%' OR " +
           "aus.fontFamily LIKE '%Lexie%'")
    List<AdaptiveUISettings> findDyslexiaFriendlyFontUsers();

    /**
     * Find users with large font sizes (accessibility needs)
     */
    @Query("SELECT aus FROM AdaptiveUISettings aus WHERE aus.fontSize >= :minSize")
    List<AdaptiveUISettings> findLargeFontUsers(@Param("minSize") Integer minSize);

    /**
     * Find users with custom break intervals
     */
    @Query("SELECT aus FROM AdaptiveUISettings aus WHERE aus.breakIntervals != 25")
    List<AdaptiveUISettings> findCustomBreakIntervalUsers();

    /**
     * Find users with ADHD-optimized timer settings
     */
    @Query("SELECT aus FROM AdaptiveUISettings aus WHERE aus.breakIntervals <= 20 AND aus.timerStyle = 'adhd'")
    List<AdaptiveUISettings> findADHDOptimizedUsers();

    /**
     * Get average font size by preset
     */
    @Query("SELECT aus.uiPreset, AVG(aus.fontSize) FROM AdaptiveUISettings aus GROUP BY aus.uiPreset")
    List<Object[]> getAverageFontSizeByPreset();

    /**
     * Get break interval distribution
     */
    @Query("SELECT aus.breakIntervals, COUNT(aus) FROM AdaptiveUISettings aus GROUP BY aus.breakIntervals ORDER BY aus.breakIntervals")
    List<Object[]> getBreakIntervalDistribution();

    /**
     * Find users needing UI updates (based on old settings)
     */
    @Query(value = "SELECT * FROM adaptive_ui_settings WHERE last_updated < CURRENT_TIMESTAMP - INTERVAL '30 days'", nativeQuery = true)
    List<AdaptiveUISettings> findOutdatedSettings();

    /**
     * Check if user has custom settings
     */
    @Query("SELECT COUNT(aus) > 0 FROM AdaptiveUISettings aus WHERE aus.userId = :userId")
    boolean hasCustomSettings(@Param("userId") Long userId);

    /**
     * Get most common font family
     */
    @Query("SELECT aus.fontFamily, COUNT(aus) as usage_count FROM AdaptiveUISettings aus " +
           "GROUP BY aus.fontFamily ORDER BY usage_count DESC")
    List<Object[]> getMostCommonFontFamily();

    /**
     * Find users with sensory-friendly settings
     */
    @Query("SELECT aus FROM AdaptiveUISettings aus WHERE " +
           "aus.animationsEnabled = false AND " +
           "aus.contrastLevel = 'normal' AND " +
           "(aus.uiPreset = 'sensory' OR aus.backgroundColor != '#ffffff')")
    List<AdaptiveUISettings> findSensoryFriendlyUsers();

    /**
     * Count users by accessibility features
     */
    @Query("SELECT " +
           "SUM(CASE WHEN aus.fontSize > 16 THEN 1 ELSE 0 END) as largeFontUsers, " +
           "SUM(CASE WHEN aus.contrastLevel IN ('high', 'extra-high') THEN 1 ELSE 0 END) as highContrastUsers, " +
           "SUM(CASE WHEN aus.animationsEnabled = false THEN 1 ELSE 0 END) as noAnimationUsers, " +
           "SUM(CASE WHEN aus.breakIntervals < 25 THEN 1 ELSE 0 END) as shortBreakUsers, " +
           "COUNT(aus) as totalUsers " +
           "FROM AdaptiveUISettings aus")
    Object[] getAccessibilityFeatureUsage();

    /**
     * Delete settings for a user (for cleanup)
     */
    void deleteByUserId(Long userId);
}
