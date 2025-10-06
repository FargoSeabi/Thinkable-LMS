package com.thinkable.backend.repository;

import com.thinkable.backend.entity.UserNeurodivergentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository interface for UserNeurodivergentProfile entity
 * Handles database operations for individual neurodivergent learning profiles
 */
@Repository
public interface UserNeurodivergentProfileRepository extends JpaRepository<UserNeurodivergentProfile, Long> {
    
    /**
     * Find a neurodivergent profile by user ID
     */
    Optional<UserNeurodivergentProfile> findByUserId(Long userId);
    
    /**
     * Check if a user has a neurodivergent profile
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Find profiles with high hyperfocus intensity for targeted tool recommendations
     */
    @Query("SELECT p FROM UserNeurodivergentProfile p WHERE p.hyperfocusIntensity > :threshold")
    List<UserNeurodivergentProfile> findByHighHyperfocusIntensity(@Param("threshold") Integer threshold);
    
    /**
     * Find profiles that need executive function support
     */
    @Query("SELECT p FROM UserNeurodivergentProfile p WHERE p.executiveFunction < :threshold")
    List<UserNeurodivergentProfile> findByLowExecutiveFunction(@Param("threshold") Integer threshold);
    
    /**
     * Find profiles with high sensory processing needs
     */
    @Query("SELECT p FROM UserNeurodivergentProfile p WHERE p.sensoryProcessing > :threshold")
    List<UserNeurodivergentProfile> findByHighSensoryProcessing(@Param("threshold") Integer threshold);
    
    /**
     * Find profiles by natural rhythm preference for scheduling insights
     */
    List<UserNeurodivergentProfile> findByNaturalRhythm(String naturalRhythm);
    
    /**
     * Find profiles by primary learning style
     */
    List<UserNeurodivergentProfile> findByPrimaryLearningStyle(String learningStyle);
    
    /**
     * Find profiles with similar trait combinations for peer insights
     */
    @Query("SELECT p FROM UserNeurodivergentProfile p WHERE " +
           "ABS(p.hyperfocusIntensity - :hyperfocus) <= 2 AND " +
           "ABS(p.attentionFlexibility - :flexibility) <= 2 AND " +
           "ABS(p.sensoryProcessing - :sensory) <= 2 AND " +
           "p.userId != :excludeUserId")
    List<UserNeurodivergentProfile> findSimilarProfiles(
        @Param("hyperfocus") Integer hyperfocusIntensity,
        @Param("flexibility") Integer attentionFlexibility,
        @Param("sensory") Integer sensoryProcessing,
        @Param("excludeUserId") Long excludeUserId
    );
    
    /**
     * Find profiles needing emotional regulation support
     */
    @Query("SELECT p FROM UserNeurodivergentProfile p WHERE p.emotionalRegulation < :threshold")
    List<UserNeurodivergentProfile> findByLowEmotionalRegulation(@Param("threshold") Integer threshold);
    
    /**
     * Find profiles with high structure preference
     */
    @Query("SELECT p FROM UserNeurodivergentProfile p WHERE p.structurePreference > :threshold")
    List<UserNeurodivergentProfile> findByHighStructurePreference(@Param("threshold") Integer threshold);
    
    /**
     * Find profiles by optimal session length range for group recommendations
     */
    @Query("SELECT p FROM UserNeurodivergentProfile p WHERE " +
           "p.optimalSessionLength BETWEEN :minLength AND :maxLength")
    List<UserNeurodivergentProfile> findByOptimalSessionLengthRange(
        @Param("minLength") Integer minLength,
        @Param("maxLength") Integer maxLength
    );
    
    /**
     * Count profiles by version for migration tracking
     */
    @Query("SELECT COUNT(p) FROM UserNeurodivergentProfile p WHERE p.version = :version")
    Long countByVersion(@Param("version") String version);
    
    /**
     * Find profiles that need profile updates (older versions)
     */
    @Query("SELECT p FROM UserNeurodivergentProfile p WHERE p.version != :currentVersion")
    List<UserNeurodivergentProfile> findProfilesNeedingUpdate(@Param("currentVersion") String currentVersion);
}
