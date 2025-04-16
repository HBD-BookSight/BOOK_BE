package com.hbd.book_be.controller

import com.hbd.book_be.dto.BookDetailedDto
import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.request.BookAddRequest
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.exception.ErrorCodes
import com.hbd.book_be.exception.ValidationException
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
    fun addBook(
        @RequestBody bookAddRequest: BookAddRequest,
    ): ResponseEntity<BookDetailedDto> {
        if (bookAddRequest.publisherId == null && bookAddRequest.publisherName == null) {
            throw ValidationException(
                message = "Either publisherId or publisherName must not be null",
                errorCode = ErrorCodes.MISSING_PUBLISHER_INFO
            )
        }

        if (bookAddRequest.authorIdList.isEmpty() && bookAddRequest.authorNameList.isEmpty()) {
            throw ValidationException(
                message = "Either publisherId or publisherName must not be null",
                errorCode = ErrorCodes.MISSING_AUTHOR_INFO
            )
        }

        val bookDetailedDto = bookService.addBook(bookAddRequest)
        return ResponseEntity.ok(bookDetailedDto)
    }

    @GetMapping("/recommended")
    fun getRecommendBooks(): ListResponse<BookDto> {
        TODO()
    }


}