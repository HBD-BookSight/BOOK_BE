package com.hbd.book_be.repository

import com.hbd.book_be.domain.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface BookRepositoryCustom {
    fun findAllActive(publishedDate: LocalDate?, pageable: Pageable): Page<Book>

    fun findActiveByAuthorName(authorName: String, publishedDate: LocalDate?, pageable: Pageable): Page<Book>

    fun findActiveByPublisherName(publisherName: String, publishedDate: LocalDate?, pageable: Pageable): Page<Book>

    fun findActiveByTitle(title: String, publishedDate: LocalDate?, pageable: Pageable): Page<Book>
}