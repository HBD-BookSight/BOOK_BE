package com.hbd.book_be.dto.request

import com.hbd.book_be.dto.request.enums.BookSortBy
import com.hbd.book_be.dto.request.enums.SortDirection
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class BookSearchRequest(
    @field:Parameter(
        description = "책 검색을 위한 키워드. 책 제목, 저자명, 출판사명 부분 일치 검색 가능. (선택 사항)",
        required = false,
        schema = Schema(
            type = "string",
            defaultValue = "",
            example = "이방인"
        )
    )
    val keyword: String? = null,

    @field:Parameter(
        description = "페이지 번호 (0부터 시작).",
        required = false,
        schema = Schema(
            type = "integer",
            defaultValue = "0",
            example = "0",
        )
    )
    val page: Int = 0,

    @field:Parameter(
        description = "페이지 당 항목 수.",
        required = false,
        schema = Schema(
            type = "integer",
            defaultValue = "10",
            example = "10",
        )
    )
    val limit: Int = 10,

    @field:Parameter(
        description = "검색할 도서의 출판일.(yyyy-mm-dd)",
        schema = Schema(
            type = "string",
            format = "date",
            required = false,
        )
    )
    val publishedDate: LocalDate? = null,

    @field:Parameter(
        description = "결과 정렬 기준 필드.",
        required = false
    )
    val orderBy: BookSortBy = BookSortBy.PublishedDate,

    @field:Parameter(
        description = "정렬 방향.",
        required = false
    )
    val direction: SortDirection = SortDirection.desc
)