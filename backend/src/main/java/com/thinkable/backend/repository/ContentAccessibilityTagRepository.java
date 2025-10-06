package com.thinkable.backend.repository;

import com.thinkable.backend.entity.ContentAccessibilityTag;
import com.thinkable.backend.entity.LearningContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.math.BigDecimal;

@Repository
public interface ContentAccessibilityTagRepository extends JpaRepository<ContentAccessibilityTag, Long> {
    
    List<ContentAccessibilityTag> findByContent(LearningContent content);
    
    List<ContentAccessibilityTag> findByContentAndTagCategory(LearningContent content, String tagCategory);
    
    List<ContentAccessibilityTag> findByTagCategoryAndTagName(String tagCategory, String tagName);
    
    @Query("SELECT t FROM ContentAccessibilityTag t WHERE t.content.id = :contentId AND " +
           "t.compatibilityScore >= :minScore ORDER BY t.compatibilityScore DESC")
    List<ContentAccessibilityTag> findHighCompatibilityTags(@Param("contentId") Long contentId, 
                                                           @Param("minScore") BigDecimal minScore);
    
    @Query("SELECT t FROM ContentAccessibilityTag t WHERE t.tagCategory = :category AND " +
           "t.isRequired = true")
    List<ContentAccessibilityTag> findRequiredTagsByCategory(@Param("category") String category);
    
    @Query("SELECT t FROM ContentAccessibilityTag t WHERE t.evidenceBased = true OR t.verifiedByExpert = true")
    List<ContentAccessibilityTag> findTrustedTags();
    
    @Query("SELECT t.tagCategory, COUNT(t) FROM ContentAccessibilityTag t " +
           "GROUP BY t.tagCategory ORDER BY COUNT(t) DESC")
    List<Object[]> getTagCategoryStats();
    
    @Modifying
    @Query("DELETE FROM ContentAccessibilityTag t WHERE t.content.id = :contentId")
    void deleteByContentId(@Param("contentId") Long contentId);
}
