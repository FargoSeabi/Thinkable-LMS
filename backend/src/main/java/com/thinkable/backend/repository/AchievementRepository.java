package com.thinkable.backend.repository;

import com.thinkable.backend.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    
    /**
     * Find all active achievements
     */
    List<Achievement> findByIsActiveTrueOrderByCreatedAtAsc();
    
    /**
     * Find achievements by category
     */
    List<Achievement> findByCategoryAndIsActiveTrueOrderByPointsValueAsc(String category);
    
    /**
     * Find achievements by requirement type
     */
    List<Achievement> findByRequirementTypeAndIsActiveTrueOrderByRequirementValueAsc(String requirementType);
    
    /**
     * Find achievements suitable for age group
     */
    @Query("SELECT a FROM Achievement a WHERE a.isActive = true AND (a.ageGroup = :ageGroup OR a.ageGroup = 'ALL') ORDER BY a.pointsValue ASC")
    List<Achievement> findByAgeGroupAndIsActiveTrue(@Param("ageGroup") String ageGroup);
    
    /**
     * Find non-hidden achievements for discovery
     */
    List<Achievement> findByIsActiveTrueAndIsHiddenFalseOrderByPointsValueAsc();
}
