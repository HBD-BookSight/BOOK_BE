package com.hbd.book_be.controller.admin

import com.hbd.book_be.dto.BookSearchLogDto
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.BookSearchLogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin - Book Search Log API", description = "관리자 - 책 검색 로그 관련 API")
@RestController
@RequestMapping("/admin/v1/book-search-log")
class BookSearchLogController(
    private val bookSearchLogService: BookSearchLogService
) {

    @Operation(
        summary = "책 검색 로그 목록 조회",
        description = "페이징된 책 검색 로그 목록을 조회합니다."
    )
    @GetMapping
    fun getBookSearchLogs(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @Parameter(description = "페이지당 항목 수", example = "10")
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