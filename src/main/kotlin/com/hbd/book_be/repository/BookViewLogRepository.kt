package com.hbd.book_be.repository

import com.hbd.book_be.domain.BookViewLog
import org.springframework.data.jpa.repository.JpaRepository

interface BookViewLogRepository: JpaRepository<BookViewLog, Long>