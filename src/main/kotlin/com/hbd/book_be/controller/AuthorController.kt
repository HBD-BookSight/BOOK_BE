package com.hbd.book_be.controller

import com.hbd.book_be.dto.AuthorDto
import com.hbd.book_be.dto.request.AuthorCreateRequest
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.AuthorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/authors")
class AuthorController(
    private val authorService: AuthorService
) {

    @GetMapping
    fun getAuthors(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
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

    @GetMapping("/{authorId}")
    fun getAuthor(@PathVariable authorId: Long): ResponseEntity<AuthorDto> {
        val author = authorService.getAuthor(authorId)
        return ResponseEntity.ok(author)
    }

    @PostMapping
    fun createAuthor(@RequestBody authorCreateRequest: AuthorCreateRequest): ResponseEntity<AuthorDto> {
        val author = authorService.createAuthor(authorCreateRequest)
        return ResponseEntity.ok(author)
    }

}