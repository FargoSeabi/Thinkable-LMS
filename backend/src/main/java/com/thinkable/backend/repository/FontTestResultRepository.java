package com.thinkable.backend.repository;

import com.thinkable.backend.model.FontTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FontTestResultRepository extends JpaRepository<FontTestResult, Long> {

    /**
     * Find all font test results for a user
     */
    List<FontTestResult> findByUserIdOrderByTestDateDesc(Long userId);

    /**
     * Find font test results for a specific font
     */
    List<FontTestResult> findByFontName(String fontName);

    /**
     * Find user's font test results for a specific font
     */
    List<FontTestResult> findByUserIdAndFontName(Long userId, String fontName);

    /**
     * Find users who prefer a specific font
     */
    @Query("SELECT ftr FROM FontTestResult ftr WHERE ftr.fontName = :fontName AND ftr.difficultyReported = 'easy'")
    List<FontTestResult> findUsersWhoPreferFont(@Param("fontName") String fontName);

    /**
     * Find users who have difficulty with a specific font
     */
    @Query("SELECT ftr FROM FontTestResult ftr WHERE ftr.fontName = :fontName AND ftr.difficultyReported = 'hard'")
    List<FontTestResult> findUsersWithFontDifficulty(@Param("fontName") String fontName);

    /**
     * Find users with potential dyslexia indicators
     */
    @Query("SELECT DISTINCT ftr.userId FROM FontTestResult ftr WHERE " +
           "(ftr.fontName IN ('Times New Roman', 'Georgia') AND ftr.difficultyReported = 'hard') OR " +
           "(ftr.fontName IN ('Comic Neue', 'OpenDyslexic') AND ftr.difficultyReported = 'easy')")
    List<Long> findUsersWithDyslexiaIndicators();

    /**
     * Find users reporting movement symptoms
     */
    @Query(value = "SELECT * FROM font_test_results WHERE symptoms_reported->>'lettersMove' = 'true'", nativeQuery = true)
    List<FontTestResult> findUsersWithMovementSymptoms();

    /**
     * Find users reporting eye strain
     */
    @Query(value = "SELECT * FROM font_test_results WHERE symptoms_reported->>'eyeStrain' = 'true'", nativeQuery = true)
    List<FontTestResult> findUsersWithEyeStrain();

    /**
     * Get font preference statistics
     */
    @Query("SELECT ftr.fontName, ftr.difficultyReported, COUNT(ftr) FROM FontTestResult ftr " +
           "GROUP BY ftr.fontName, ftr.difficultyReported ORDER BY ftr.fontName")
    List<Object[]> getFontPreferenceStatistics();

    /**
     * Get average readability ratings by font
     */
    @Query("SELECT ftr.fontName, AVG(ftr.readabilityRating) FROM FontTestResult ftr " +
           "WHERE ftr.readabilityRating IS NOT NULL GROUP BY ftr.fontName ORDER BY AVG(ftr.readabilityRating) DESC")
    List<Object[]> getAverageReadabilityByFont();

    /**
     * Find the most preferred font for a user
     */
    @Query("SELECT ftr FROM FontTestResult ftr WHERE ftr.userId = :userId " +
           "ORDER BY ftr.readabilityRating DESC")
    List<FontTestResult> findByUserIdOrderByReadabilityRatingDesc(@Param("userId") Long userId);

    /**
     * Count users who completed font testing
     */
    @Query("SELECT COUNT(DISTINCT ftr.userId) FROM FontTestResult ftr")
    long countUsersWithFontTests();

    /**
     * Find users with specific symptom patterns
     */
    @Query(value = "SELECT DISTINCT user_id FROM font_test_results WHERE " +
           "symptoms_reported->>'lettersMove' = 'true' AND " +
           "symptoms_reported->>'eyeStrain' = 'true'", nativeQuery = true)
    List<Long> findUsersWithMultipleSymptoms();

    /**
     * Get symptom frequency across all users
     */
    @Query(value = "SELECT " +
           "SUM(CASE WHEN symptoms_reported->>'lettersMove' = 'true' THEN 1 ELSE 0 END) as lettersMove, " +
           "SUM(CASE WHEN symptoms_reported->>'eyeStrain' = 'true' THEN 1 ELSE 0 END) as eyeStrain, " +
           "SUM(CASE WHEN symptoms_reported->>'slowReading' = 'true' THEN 1 ELSE 0 END) as slowReading, " +
           "COUNT(*) as total " +
           "FROM font_test_results", nativeQuery = true)
    Object[] getSymptomFrequency();
}
