package com.thinkable.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.thinkable.backend.model.Book;
import com.thinkable.backend.model.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Quiz findByLessonId(Long lessonId);
    List<Quiz> findByBook(Book book);
    List<Quiz> findByLearningContentId(Long learningContentId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Quiz q WHERE q.book.id = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);
}
