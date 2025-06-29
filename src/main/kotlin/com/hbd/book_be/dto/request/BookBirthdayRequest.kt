package com.hbd.book_be.dto.request

import com.hbd.book_be.dto.request.enums.BookSortBy
import com.hbd.book_be.dto.request.enums.SortDirection
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class BookBirthdayRequest(
    @field:Parameter(
        description = "검색할 책의 출간 월",
        required = true
    )
    @field:Max(value = 12, message = "월은 1부터 12까지 입력 가능합니다")
    @field:Min(value = 1, message = "월은 1부터 12까지 입력 가능합니다")
    val month: Int,

    @field:Parameter(
        description = "검색할 책의 출간 일",
        required = true
    )
    @field:Max(value = 31, message = "일은 1부터 31까지 입력 가능합니다")
    @field:Min(value = 1, message = "일은 1부터 31까지 입력 가능합니다")
    val day: Int,

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
        description = "결과 정렬 기준 필드.",
        example = "Title",
        required = false
    )
    val orderBy: BookSortBy = BookSortBy.Title,

    @field:Parameter(
        description = "정렬 방향.",
        example = "asc",
        required = false
    )
    val direction: SortDirection = SortDirection.asc
)
