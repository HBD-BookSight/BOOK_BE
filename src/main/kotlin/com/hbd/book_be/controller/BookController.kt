package com.hbd.book_be.controller

import com.hbd.book_be.dto.BookDetailedDto
import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.BookService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
class BookController(
    @Autowired
    private val bookService: BookService
) {

    @GetMapping
    fun getBooks(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("limit", defaultValue = "10") limit: Int,
        @RequestParam("orderBy", defaultValue = "publishedDate") orderBy: String,
        @RequestParam("direction", defaultValue = "desc") direction: String
    ): ResponseEntity<PageResponse<BookDto>> {
        val pageBookDto = bookService.getBooks(page = page, limit = limit, orderBy = orderBy, direction = direction)
        val pageBookResponse = PageResponse<BookDto>(
            items = pageBookDto.content,
            totalCount = pageBookDto.totalElements,
            totalPages = pageBookDto.totalPages,
            hasNext = pageBookDto.hasNext(),
            hasPrevious = pageBookDto.hasPrevious(),
        )

        return ResponseEntity.ok(pageBookResponse)
    }

    @GetMapping("/{isbn}")
    fun getDetailedBook(@PathVariable isbn: String): ResponseEntity<BookDetailedDto> {
        val bookDetailDto = bookService.getBookDetail(isbn)
        return ResponseEntity.ok(bookDetailDto)
    }

    @PostMapping
    fun addBook(): ResponseEntity<BookDetailedDto> {
        TODO()
    }

    @GetMapping("/recommended")
    fun getRecommendBooks(): ListResponse<BookDto> {
        TODO()
    }


}