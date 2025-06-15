package com.hbd.book_be.controller.admin

import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.external.kakao.KakaoApiRequest
import com.hbd.book_be.external.kakao.KakaoBookDto
import com.hbd.book_be.external.kakao.KakaoBookService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin - Kakao Book API", description = "관리자 - 카카오 책 API 관련")
@RestController
@RequestMapping("/admin/v1/kakao/books")
class KakaoBookController(
    @Autowired
    private val kakaoBookService: KakaoBookService
) {

    @Operation(
        summary = "카카오 API로 책 검색",
        description = "카카오 책 검색 API를 사용하여 책 목록을 조회합니다."
    )
    @GetMapping
    fun getBooks(
        @ParameterObject
        @ModelAttribute request: KakaoApiRequest
    ): ResponseEntity<ListResponse<KakaoBookDto>> {
        val bookList = kakaoBookService.searchBook(request)

        val listResponse = ListResponse(
            bookList,
            length = bookList.size,
        )
        return ResponseEntity.ok(listResponse)
    }

    @Operation(
        summary = "카카오 API로 책 생성",
        description = "카카오 API에서 ISBN으로 책 정보를 조회하여 새로운 책을 생성합니다."
    )
    @PostMapping
    fun createBook(
        @Parameter(description = "생성할 책의 ISBN 번호", required = true, example = "9791168473690")
        @RequestParam("isbn") isbn: String
    ): ResponseEntity<BookDto.Detail> {
        val bookDetailedDto = kakaoBookService.createBook(isbn)
        return ResponseEntity.ok(bookDetailedDto)
    }
}