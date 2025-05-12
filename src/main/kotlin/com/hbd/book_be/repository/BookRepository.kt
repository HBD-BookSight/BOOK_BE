package com.hbd.book_be.repository

import com.hbd.book_be.domain.Book
import org.springframework.data.jpa.repository.JpaRepository

interface BookRepository : JpaRepository<Book, String>, BookRepositoryCustom {
    fun findByIsbnIn(isbnList: List<String>): List<Book>
}