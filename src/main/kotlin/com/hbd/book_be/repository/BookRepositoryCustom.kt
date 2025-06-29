package com.hbd.book_be.repository

import com.hbd.book_be.domain.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface BookRepositoryCustom {
    fun findAllActive(keyword: String?, pageable: Pageable, publishedDate: LocalDate? = null): Page<Book>
    fun findByPublishedMonthAndDay(publishedMonth: Int, publishedDay: Int, pageable: Pageable): Page<Book>
}