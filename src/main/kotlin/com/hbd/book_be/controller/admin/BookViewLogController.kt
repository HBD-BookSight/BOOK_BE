package com.hbd.book_be.controller.admin

import com.hbd.book_be.dto.BookViewLogDto
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.BookViewLogService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/v1/book-view-logs")
class BookViewLogController(
    private val bookViewLogService: BookViewLogService
) {

    @GetMapping
    fun getBookViewLogs(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("limit", defaultValue = "10") limit: Int,
    ): ResponseEntity<PageResponse<BookViewLogDto>> {
        val bookViewLogPage = bookViewLogService.getBookViewLogs(page, limit)
        val pageResponse = PageResponse(
            items = bookViewLogPage.content,
            totalPages = bookViewLogPage.totalPages,
            totalCount = bookViewLogPage.totalElements,
            hasNext = bookViewLogPage.hasNext(),
            hasPrevious = bookViewLogPage.hasPrevious(),
        )

        return ResponseEntity.ok(pageResponse)
    }

}