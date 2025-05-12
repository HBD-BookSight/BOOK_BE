package com.hbd.book_be.external.kakao

import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.response.ListResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/kakao/books")
class KakaoBookController(
    @Autowired
    private val kakaoBookService: KakaoBookService
) {

    @GetMapping
    fun getBooks(@ModelAttribute request: KakaoApiRequest): ResponseEntity<ListResponse<KakaoBookDto>> {
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