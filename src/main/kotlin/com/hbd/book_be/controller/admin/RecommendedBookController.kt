package com.hbd.book_be.controller.admin

import com.hbd.book_be.dto.RecommendedBookDto
import com.hbd.book_be.dto.request.RecommendedBookCreateRequest
import com.hbd.book_be.service.RecommendedBookService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin - Recommended Book API", description = "관리자 - 추천 도서 관리 API")
@RestController
@RequestMapping("/admin/v1/recommended-books")
class RecommendedBookController(
    @Autowired
    private val recommendedBookService: RecommendedBookService
) {

    @Operation(
        summary = "단일 추천 도서 추가",
        description = "특정 ISBN의 도서를 추천 도서로 추가합니다."
    )
    @PostMapping
    fun createRecommendedBook(
        @RequestBody request: RecommendedBookCreateRequest
    ): ResponseEntity<RecommendedBookDto> {
        val recommendedBook = recommendedBookService.createRecommendedBook(request)
        return ResponseEntity.ok(recommendedBook)
    }

    @Operation(
        summary = "추천 도서 삭제",
        description = "특정 ISBN의 추천 도서를 삭제합니다."
    )
    @DeleteMapping("/{isbn}")
    fun deleteRecommendedBook(
        @Parameter(description = "삭제할 추천 도서의 ISBN", required = true) @PathVariable isbn: String
    ): ResponseEntity<Void> {
        recommendedBookService.deleteRecommendedBook(isbn)
        return ResponseEntity.noContent().build()
    }

}
