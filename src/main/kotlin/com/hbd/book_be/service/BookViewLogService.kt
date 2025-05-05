package com.hbd.book_be.service

import com.hbd.book_be.dto.BookViewLogDto
import com.hbd.book_be.repository.BookViewLogRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookViewLogService(
    private val bookViewLogRepository: BookViewLogRepository
) {

    fun getBookViewLogs(page: Int, limit: Int): Page<BookViewLogDto> {
        val pageable = PageRequest.of(page, limit, Sort.Direction.DESC, "viewDateTime")
        val bookViewLogPage = bookViewLogRepository.findAll(pageable)
        return bookViewLogPage.map{
            BookViewLogDto.fromEntity(it)
        }
    }

}