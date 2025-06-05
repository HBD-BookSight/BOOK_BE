package com.hbd.book_be.controller.api

import com.hbd.book_be.dto.AuthorDto
import com.hbd.book_be.dto.request.AuthorCreateRequest
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.AuthorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Author API", description = "작가 관련 API")
@RestController
@RequestMapping("/api/v1/authors")
class AuthorController(
    private val authorService: AuthorService
) {

    @Operation(
        summary = "작가 목록 조회",
        description = "페이징된 작가 목록을 반환합니다."
    )
    @GetMapping
    fun getAuthors(
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(value = "page", defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(value = "limit", defaultValue = "10") limit: Int,
    ): ResponseEntity<PageResponse<AuthorDto>> {
        val authorPage = authorService.getAuthors(
            page = page, limit = limit
        )

        val pageResponse = PageResponse<AuthorDto>(
            items = authorPage.content,
            totalCount = authorPage.totalElements,
            totalPages = authorPage.totalPages,
            hasNext = authorPage.hasNext(),
            hasPrevious = authorPage.hasPrevious(),
        )

        return ResponseEntity.ok(pageResponse)
    }

    @Operation(
        summary = "작가 ID로 특정 작가 상세 정보 조회",
        description = "작가 ID를 사용하여 특정 작가의 저서 목록을 포함한 상세 정보를 조회합니다."
    )
    @GetMapping("/{authorId}")
    fun getAuthor(
        @Parameter(description = "조회할 작가의 ID", required = true) @PathVariable authorId: Long
    ): ResponseEntity<AuthorDto> {
        val author = authorService.getAuthor(authorId)
        return ResponseEntity.ok(author)
    }

    @Operation(
        summary = "새 작가 정보 생성",
        description = "새로운 작가 정보를 추가합니다."
    )
    @PostMapping
    fun createAuthor(
        @RequestBody authorCreateRequest: AuthorCreateRequest
    ): ResponseEntity<AuthorDto> {
        val author = authorService.createAuthor(authorCreateRequest)
        return ResponseEntity.ok(author)
    }

}