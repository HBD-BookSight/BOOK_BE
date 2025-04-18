package com.hbd.book_be.repository

import com.hbd.book_be.domain.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookRepositoryCustom {
    fun findAllActive(pageable: Pageable): Page<Book>
}