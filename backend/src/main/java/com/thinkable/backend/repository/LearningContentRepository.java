package com.thinkable.backend.repository;

import com.thinkable.backend.entity.LearningContent;
import com.thinkable.backend.entity.TutorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.math.BigDecimal;

@Repository
public interface LearningContentRepository extends JpaRepository<LearningContent, Long> {
    
    List<LearningContent> findByTutor(TutorProfile tutor);
    
    List<LearningContent> findByTutorAndStatus(TutorProfile tutor, String status);
    
    List<LearningContent> findByStatusAndIsPublicTrue(String status);
    
    Page<LearningContent> findByStatusAndIsPublicTrue(String status, Pageable pageable);
    
    List<LearningContent> findBySubjectAreaAndStatusAndIsPublicTrue(String subjectArea, String status);
    
    Page<LearningContent> findBySubjectAreaAndStatusAndIsPublicTrue(String subjectArea, String status, Pageable pageable);
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true AND " +
           "c.dyslexiaFriendly = :dyslexiaFriendly")
    List<LearningContent> findDyslexiaFriendlyContent(@Param("dyslexiaFriendly") Boolean dyslexiaFriendly);
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true AND " +
           "c.adhdFriendly = :adhdFriendly")
    List<LearningContent> findADHDFriendlyContent(@Param("adhdFriendly") Boolean adhdFriendly);
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true AND " +
           "c.autismFriendly = :autismFriendly")
    List<LearningContent> findAutismFriendlyContent(@Param("autismFriendly") Boolean autismFriendly);
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true AND " +
           "(c.dyslexiaFriendly = true OR c.adhdFriendly = true OR c.autismFriendly = true OR " +
           "c.visualImpairmentFriendly = true OR c.hearingImpairmentFriendly = true OR c.motorImpairmentFriendly = true)")
    List<LearningContent> findAccessibleContent();
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true AND " +
           "c.subjectArea = :subject AND " +
           "(:dyslexia = false OR c.dyslexiaFriendly = true) AND " +
           "(:adhd = false OR c.adhdFriendly = true) AND " +
           "(:autism = false OR c.autismFriendly = true)")
    Page<LearningContent> findContentByAccessibilityFilters(
            @Param("subject") String subject,
            @Param("dyslexia") Boolean dyslexia,
            @Param("adhd") Boolean adhd,
            @Param("autism") Boolean autism,
            Pageable pageable);
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true AND " +
           "c.ratingAverage >= :minRating ORDER BY c.ratingAverage DESC")
    List<LearningContent> findHighRatedContent(@Param("minRating") BigDecimal minRating);
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true " +
           "ORDER BY c.viewCount DESC")
    List<LearningContent> findPopularContent(Pageable pageable);
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true " +
           "ORDER BY c.createdAt DESC")
    List<LearningContent> findLatestContent(Pageable pageable);
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true AND " +
           "c.difficultyLevel = :level")
    List<LearningContent> findContentByDifficultyLevel(@Param("level") String level);
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true AND " +
           "c.targetAgeMin <= :age AND c.targetAgeMax >= :age")
    List<LearningContent> findContentForAge(@Param("age") Integer age);
    
    @Query("SELECT c FROM LearningContent c WHERE c.status = 'published' AND c.isPublic = true AND " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.topicTags) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<LearningContent> searchContent(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT c FROM LearningContent c LEFT JOIN FETCH c.tutor WHERE c.status = 'published' AND c.isPublic = true AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.topicTags) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<LearningContent> searchContentWithTutor(@Param("query") String query);
    
    @Query("SELECT c FROM LearningContent c LEFT JOIN FETCH c.tutor WHERE c.status = 'published' AND c.isPublic = true")
    List<LearningContent> findAllPublishedWithTutor();
    
    @Query("SELECT COUNT(c) FROM LearningContent c WHERE c.tutor.id = :tutorId")
    Long countContentByTutor(@Param("tutorId") Long tutorId);
    
    @Query("SELECT COUNT(c) FROM LearningContent c WHERE c.status = 'published'")
    Long countPublishedContent();
    
    @Query("SELECT c.subjectArea, COUNT(c) FROM LearningContent c WHERE c.status = 'published' " +
           "GROUP BY c.subjectArea ORDER BY COUNT(c) DESC")
    List<Object[]> getSubjectAreaStats();
}
