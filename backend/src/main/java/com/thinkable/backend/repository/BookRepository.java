package com.thinkable.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thinkable.backend.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
