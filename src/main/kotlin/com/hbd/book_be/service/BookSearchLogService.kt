package com.hbd.book_be.service

import com.hbd.book_be.dto.BookSearchLogDto
import com.hbd.book_be.repository.BookSearchLogRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookSearchLogService(
    private val bookSearchLogRepository: BookSearchLogRepository
) {

    fun getBookSearchLogs(page: Int, limit: Int): Page<BookSearchLogDto>{
        val pageRequest = PageRequest.of(page, limit, Sort.Direction.DESC, "searchDateTime")
        val pageResponse = bookSearchLogRepository.findAll(pageRequest)
        return pageResponse.map{BookSearchLogDto.fromEntity(it)}
    }
}