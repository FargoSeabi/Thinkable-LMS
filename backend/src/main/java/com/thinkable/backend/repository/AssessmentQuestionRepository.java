package com.thinkable.backend.repository;

import com.thinkable.backend.model.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {

    /**
     * Find questions by category
     */
    List<AssessmentQuestion> findByCategoryAndActiveTrue(String category);

    /**
     * Find questions by category and subcategory
     */
    List<AssessmentQuestion> findByCategoryAndSubcategoryAndActiveTrue(String category, String subcategory);

    /**
     * Find questions appropriate for a specific age
     */
    @Query("SELECT aq FROM AssessmentQuestion aq WHERE aq.active = true AND aq.ageMin <= :age AND aq.ageMax >= :age")
    List<AssessmentQuestion> findQuestionsForAge(@Param("age") Integer age);

    /**
     * Find questions by category for a specific age
     */
    @Query("SELECT aq FROM AssessmentQuestion aq WHERE aq.category = :category AND aq.active = true " +
           "AND aq.ageMin <= :age AND aq.ageMax >= :age ORDER BY aq.id")
    List<AssessmentQuestion> findQuestionsByCategoryForAge(@Param("category") String category, @Param("age") Integer age);

    /**
     * Find questions by type
     */
    List<AssessmentQuestion> findByQuestionTypeAndActiveTrue(String questionType);

    /**
     * Find all active questions ordered by category
     */
    @Query("SELECT aq FROM AssessmentQuestion aq WHERE aq.active = true ORDER BY aq.category, aq.subcategory, aq.id")
    List<AssessmentQuestion> findAllActiveQuestionsOrdered();

    /**
     * Get questions for full assessment (age-appropriate)
     */
    @Query("SELECT aq FROM AssessmentQuestion aq WHERE aq.active = true " +
           "AND aq.ageMin <= :age AND aq.ageMax >= :age " +
           "ORDER BY aq.category, aq.subcategory, aq.id")
    List<AssessmentQuestion> getFullAssessmentForAge(@Param("age") Integer age);

    /**
     * Get questions by categories for age
     */
    @Query("SELECT aq FROM AssessmentQuestion aq WHERE aq.category IN :categories " +
           "AND aq.active = true AND aq.ageMin <= :age AND aq.ageMax >= :age " +
           "ORDER BY aq.category, aq.id")
    List<AssessmentQuestion> getQuestionsByCategoriesForAge(@Param("categories") List<String> categories, @Param("age") Integer age);

    /**
     * Count questions by category
     */
    @Query("SELECT aq.category, COUNT(aq) FROM AssessmentQuestion aq WHERE aq.active = true GROUP BY aq.category")
    List<Object[]> countQuestionsByCategory();

    /**
     * Find font test questions
     */
    @Query("SELECT aq FROM AssessmentQuestion aq WHERE aq.questionType = 'font_test' AND aq.active = true")
    List<AssessmentQuestion> findFontTestQuestions();

    /**
     * Find likert scale questions for category
     */
    @Query("SELECT aq FROM AssessmentQuestion aq WHERE aq.category = :category " +
           "AND aq.questionType = 'likert' AND aq.active = true ORDER BY aq.id")
    List<AssessmentQuestion> findLikertQuestionsByCategory(@Param("category") String category);

    /**
     * Get questions with high scoring weights
     */
    @Query("SELECT aq FROM AssessmentQuestion aq WHERE aq.scoringWeight >= :minWeight AND aq.active = true")
    List<AssessmentQuestion> findHighWeightQuestions(@Param("minWeight") Double minWeight);

    /**
     * Find questions by age range
     */
    @Query("SELECT aq FROM AssessmentQuestion aq WHERE aq.ageMin >= :minAge AND aq.ageMax <= :maxAge AND aq.active = true")
    List<AssessmentQuestion> findQuestionsByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    /**
     * Get assessment structure (categories and subcategories)
     */
    @Query("SELECT DISTINCT aq.category, aq.subcategory FROM AssessmentQuestion aq WHERE aq.active = true ORDER BY aq.category, aq.subcategory")
    List<Object[]> getAssessmentStructure();

    /**
     * Count total active questions
     */
    @Query("SELECT COUNT(aq) FROM AssessmentQuestion aq WHERE aq.active = true")
    long countActiveQuestions();

    /**
     * Find questions needing review (very old or inactive)
     */
    @Query(value = "SELECT * FROM assessment_questions WHERE active = false OR created_at < CURRENT_TIMESTAMP - INTERVAL '365 days'", nativeQuery = true)
    List<AssessmentQuestion> findQuestionsNeedingReview();

    /**
     * Get random sample of questions by category
     */
    @Query(value = "SELECT * FROM assessment_questions WHERE category = :category AND active = true " +
                   "AND age_min <= :age AND age_max >= :age ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<AssessmentQuestion> getRandomQuestionsByCategoryForAge(@Param("category") String category, 
                                                                @Param("age") Integer age, 
                                                                @Param("limit") Integer limit);

    /**
     * Find essential questions (high weight, core categories)
     */
    @Query("SELECT aq FROM AssessmentQuestion aq WHERE aq.active = true " +
           "AND aq.scoringWeight >= 1.0 " +
           "AND aq.category IN ('attention', 'reading', 'social', 'sensory') " +
           "AND aq.ageMin <= :age AND aq.ageMax >= :age " +
           "ORDER BY aq.scoringWeight DESC, aq.category")
    List<AssessmentQuestion> findEssentialQuestionsForAge(@Param("age") Integer age);

    /**
     * Get question distribution by type
     */
    @Query("SELECT aq.questionType, COUNT(aq) FROM AssessmentQuestion aq WHERE aq.active = true GROUP BY aq.questionType")
    List<Object[]> getQuestionTypeDistribution();
}
