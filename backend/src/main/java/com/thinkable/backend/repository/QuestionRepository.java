package com.thinkable.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.thinkable.backend.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Question q WHERE q.quiz.id IN (SELECT quiz.id FROM Quiz quiz WHERE quiz.book.id = :bookId)")
    void deleteByBookId(@Param("bookId") Long bookId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Question q WHERE q.quiz.id = :quizId")
    void deleteByQuizId(@Param("quizId") Long quizId);
}
