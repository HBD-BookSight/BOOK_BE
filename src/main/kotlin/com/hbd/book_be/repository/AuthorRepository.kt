package com.hbd.book_be.repository

import com.hbd.book_be.domain.Author
import org.springframework.data.jpa.repository.JpaRepository

interface AuthorRepository : JpaRepository<Author, Long>