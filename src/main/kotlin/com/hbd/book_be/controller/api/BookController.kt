package com.hbd.book_be.controller.api

import com.hbd.book_be.aop.LogBookSearch
import com.hbd.book_be.aop.LogBookView
import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.ContentsDto
import com.hbd.book_be.dto.EventDto
import com.hbd.book_be.dto.RecommendedBookDto
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.dto.request.BookDetailRequest
import com.hbd.book_be.dto.request.BookSearchRequest
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.exception.ErrorCodes
import com.hbd.book_be.exception.ValidationException
import com.hbd.book_be.service.BookService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Book API", description = "책 관련 API")
@RestController
@RequestMapping("/api/v1/books")
class BookController(
    @Autowired private val bookService: BookService
) {

    @Operation(
        summary = "키워드로 책 검색 (책 제목, 저자, 출판사 통합 검색)",
        description = "검색 조건에 따라 페이징된 책 목록을 반환합니다. 키워드는 책 제목, 저자명, 출판사명에 대해 검색할 수 있습니다."
    )
    @GetMapping
    @LogBookSearch
    fun getBooks(
        @ParameterObject
        bookSearchRequest: BookSearchRequest
    ): ResponseEntity<PageResponse<BookDto>> {
        val pageBookDto = bookService.getBooks(
            bookSearchRequest
        )

        val pageBookResponse = PageResponse(
            items = pageBookDto.content,
            totalCount = pageBookDto.totalElements,
            totalPages = pageBookDto.totalPages,
            hasNext = pageBookDto.hasNext(),
            hasPrevious = pageBookDto.hasPrevious(),
        )

        return ResponseEntity.ok(pageBookResponse)
    }

    @Operation(
        summary = "ISBN으로 특정 책의 상세 정보 조회",
        description = "ISBN을 사용하여 특정 책의 저자 및 출판사 정보를 포함한 상세 정보를 조회합니다."
    )
    @GetMapping("/{isbn}")
    @LogBookView
    fun getDetailedBook(
        @ParameterObject
        @ModelAttribute request: BookDetailRequest
    ): ResponseEntity<BookDto.Detail> {
        val bookDetailDto = bookService.getBookDetail(request)
        return ResponseEntity.ok(bookDetailDto)
    }

    @Operation(
        summary = "새 책 정보 생성",
        description = "새로운 책을 추가합니다. 출판사 정보(publisherId 또는 publisherName)와 저자 정보(authorIdList 또는 authorNameList) 중 하나는 반드시 필요합니다."
    )
    @PostMapping
    fun createBook(
        @ParameterObject
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

    @Operation(
        summary = "추천 도서 목록 조회",
        description = "추천 도서 목록을 조회합니다."
    )
    @GetMapping("/recommended")
    fun getRecommendBooks(): ResponseEntity<ListResponse<RecommendedBookDto>> {
        val recommendedBookList = bookService.getRecommendedBooks()
        val listResponse = ListResponse(
            recommendedBookList,
            length = recommendedBookList.size,
        )
        return ResponseEntity.ok(listResponse)
    }

    @Operation(
        summary = "책 관련 이벤트 목록 조회",
        description = "특정 ISBN의 책과 관련된 이벤트 목록을 조회합니다."
    )
    @GetMapping("/{isbn}/events")
    fun getBookEvents(
        @Parameter(
            description = "이벤트를 조회할 책의 ISBN 번호입니다.",
            required = true
        ) @PathVariable isbn: String
    ): ResponseEntity<ListResponse<EventDto>> {
        val bookEventList: List<EventDto> = bookService.getBookEventList(isbn)
        val listResponse = ListResponse(
            items = bookEventList,
            length = bookEventList.size,
        )

        return ResponseEntity.ok(listResponse)
    }

    @Operation(
        summary = "책 목차 정보 조회",
        description = "특정 ISBN의 책에 대한 목차 정보를 조회합니다."
    )
    @GetMapping("/{isbn}/contents")
    fun getBookContents(
        @Parameter(
            description = "목차를 조회할 책의 ISBN 번호입니다.",
            required = true
        ) @PathVariable isbn: String
    ): ResponseEntity<ListResponse<ContentsDto>> {
        val bookContentsList: List<ContentsDto> = bookService.getBookContentsList(isbn)
        val listResponse = ListResponse(
            items = bookContentsList,
            length = bookContentsList.size,
        )

        return ResponseEntity.ok(listResponse)
    }
}