package com.hbd.book_be.controller.admin

import com.hbd.book_be.dto.BookViewLogDto
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.BookViewLogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin - Book View Log API", description = "관리자 - 책 조회 로그 관련 API")
@RestController
@RequestMapping("/admin/v1/book-view-logs")
class BookViewLogController(
    private val bookViewLogService: BookViewLogService
) {

    @Operation(
        summary = "책 조회 로그 목록 조회",
        description = "페이징된 책 조회 로그 목록을 조회합니다."
    )
    @GetMapping
    @RequireAdminRole
    fun getBookViewLogs(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam("page", defaultValue = "0") page: Int,
        @Parameter(description = "페이지당 항목 수", example = "10")
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