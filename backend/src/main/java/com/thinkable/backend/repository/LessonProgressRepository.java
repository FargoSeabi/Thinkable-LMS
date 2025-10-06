package com.thinkable.backend.repository;

import com.thinkable.backend.model.LessonProgress;
import com.thinkable.backend.model.User;
import com.thinkable.backend.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    
    Optional<LessonProgress> findByUserAndLesson(User user, Lesson lesson);
    
    List<LessonProgress> findByUserOrderByLastAccessedDesc(User user);
    
    List<LessonProgress> findByUserAndCompletedTrueOrderByCompletedAtDesc(User user);
    
    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.user = :user AND lp.completed = true")
    Long countCompletedLessons(@Param("user") User user);
    
    @Query("SELECT AVG(lp.quizScore) FROM LessonProgress lp WHERE lp.user = :user AND lp.quizScore IS NOT NULL")
    Double getAverageQuizScore(@Param("user") User user);
    
    @Query("SELECT SUM(lp.timeSpentMinutes) FROM LessonProgress lp WHERE lp.user = :user")
    Long getTotalTimeSpent(@Param("user") User user);
    
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.user = :user AND lp.completed = false ORDER BY lp.lastAccessed DESC")
    List<LessonProgress> findInProgressLessons(@Param("user") User user);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM LessonProgress lp WHERE lp.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
