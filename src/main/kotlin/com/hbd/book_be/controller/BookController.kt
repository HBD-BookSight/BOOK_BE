package com.hbd.book_be.controller

import com.hbd.book_be.dto.*
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.exception.ErrorCodes
import com.hbd.book_be.exception.ValidationException
import com.hbd.book_be.service.BookService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/books")
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
    fun getDetailedBook(@PathVariable isbn: String): ResponseEntity<BookDto.Detail> {
        val bookDetailDto = bookService.getBookDetail(isbn)
        return ResponseEntity.ok(bookDetailDto)
    }

    @PostMapping
    fun createBook(
        @RequestBody bookCreateRequest: BookCreateRequest,
    ): ResponseEntity<BookDto.Detail> {
        if (bookCreateRequest.publisherId == null && bookCreateRequest.publisherName == null) {
            throw ValidationException(
                message = "Either publisherId or publisherName must not be null",
                errorCode = ErrorCodes.MISSING_PUBLISHER_INFO
            )
        }

        if (bookCreateRequest.authorIdList.isEmpty() && bookCreateRequest.authorNameList.isEmpty()) {
            throw ValidationException(
                message = "Either publisherId or publisherName must not be null",
                errorCode = ErrorCodes.MISSING_AUTHOR_INFO
            )
        }

        val bookDetailedDto = bookService.createBook(bookCreateRequest)
        return ResponseEntity.ok(bookDetailedDto)
    }

    @GetMapping("/recommended")
    fun getRecommendBooks(): ResponseEntity<ListResponse<RecommendedBookDto>> {
        val recommendedBookList = bookService.getRecommendedBooks()
        val listResponse = ListResponse(
            recommendedBookList,
            length = recommendedBookList.size,
        )
        return ResponseEntity.ok(listResponse)
    }

    @GetMapping("/{isbn}/events")
    fun getBookEvents(@PathVariable isbn: String): ResponseEntity<ListResponse<EventDto>> {
        val bookEventList: List<EventDto> = bookService.getBookEventList(isbn)
        val listResponse = ListResponse(
            items = bookEventList,
            length = bookEventList.size,
        )

        return ResponseEntity.ok(listResponse)
    }

    @GetMapping("/{isbn}/contents")
    fun getBookContents(@PathVariable isbn: String): ResponseEntity<ListResponse<ContentsDto>> {
        val bookContentsList: List<ContentsDto> = bookService.getBookContentsList(isbn)
        val listResponse = ListResponse(
            items = bookContentsList,
            length = bookContentsList.size,
        )

        return ResponseEntity.ok(listResponse)
    }
}