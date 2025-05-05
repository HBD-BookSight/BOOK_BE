package com.hbd.book_be.controller.admin

import com.hbd.book_be.dto.BookSearchLogDto
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.BookSearchLogService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/v1/book-search-log")
class BookSearchLogController(
    private val bookSearchLogService: BookSearchLogService
) {

    @GetMapping
    fun getBookSearchLogs(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int
    ): ResponseEntity<PageResponse<BookSearchLogDto>> {
        val response = bookSearchLogService.getBookSearchLogs(page, size)
        val pageResponse = PageResponse(
            items = response.content,
            totalCount = response.totalElements,
            totalPages = response.totalPages,
            hasNext = response.hasNext(),
            hasPrevious = response.hasPrevious(),
        )
        return ResponseEntity.ok(pageResponse)
    }
}