package com.hbd.book_be.controller

import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.request.KakaoBookRequest
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.service.KakaoBookService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/kakao/books")
class KakaoBookController(
    @Autowired
    private val kakaoBookService: KakaoBookService
) {

    @GetMapping
    fun getBooks(@ModelAttribute request: KakaoBookRequest): ResponseEntity<ListResponse<BookDto>> {
        val bookList = kakaoBookService.searchBook(request)

        val listResponse = ListResponse(
            bookList,
            length = bookList.size,
        )
        return ResponseEntity.ok(listResponse)
    }

    @PostMapping
    fun createBook(@RequestParam("isbn") isbn: String): ResponseEntity<BookDto.Detail> {
        val bookDetailedDto = kakaoBookService.createBook(isbn)
        return ResponseEntity.ok(bookDetailedDto)
    }
}