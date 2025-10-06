package com.thinkable.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thinkable.backend.model.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
}
