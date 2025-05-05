package com.hbd.book_be.repository

import com.hbd.book_be.domain.BookSearchLog
import org.springframework.data.jpa.repository.JpaRepository

interface BookSearchLogRepository : JpaRepository<BookSearchLog, Long>