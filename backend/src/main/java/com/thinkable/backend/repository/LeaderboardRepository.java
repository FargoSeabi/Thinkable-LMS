package com.thinkable.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.thinkable.backend.model.Leaderboard;
import com.thinkable.backend.model.User;
import com.thinkable.backend.model.Quiz;

import java.util.List;

public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {
    long countByUserId(Long userId);
    
    List<Leaderboard> findByUserAndQuizOrderBySubmittedAtDesc(User user, Quiz quiz);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Leaderboard l WHERE l.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Leaderboard l WHERE l.quiz.id = :quizId")
    void deleteByQuizId(@Param("quizId") Long quizId);
}
