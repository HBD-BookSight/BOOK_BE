package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class BookCreateRequest(
    @field:Parameter(
        description = "책의 ISBN 번호",
        required = true,
        schema = Schema(
            type = "string",
            example = "9791168473690"
        )
    )
    val isbn: String,

    @field:Parameter(
        description = "책 제목",
        required = true,
        schema = Schema(
            type = "string",
            example = "모던 자바스크립트 Deep Dive"
        )
    )
    val title: String,

    @field:Parameter(
        description = "책 요약 정보",
        required = true,
        schema = Schema(
            type = "string",
            example = "자바스크립트를 깊이 있게 이해하고 싶은 개발자를 위한 안내서"
        )
    )
    val summary: String,

    @field:Parameter(
        description = "출판일",
        required = true,
        schema = Schema(
            type = "string",
            format = "date-time",
            example = "2020-09-24T00:00:00"
        )
    )
    val publishedDate: LocalDateTime,

    @field:Parameter(
        description = "책 상세 정보 URL",
        required = false,
        schema = Schema(
            type = "string",
            example = "https://www.yes24.com/Product/Goods/92742567"
        )
    )
    val detailUrl: String?,

    @field:Parameter(
        description = "번역가 목록",
        required = false
    )
    @field:ArraySchema(
        schema = Schema(type = "string"),
        arraySchema = Schema(
            type = "array",
            example = "[]",
            defaultValue = "[]"
        )
    )
    val translator: List<String>? = null,

    @field:Parameter(
        description = "책 가격 (원)",
        required = false,
        schema = Schema(
            type = "integer",
            example = "45000"
        )
    )
    val price: Int? = null,

    @field:Parameter(
        description = "책 표지 이미지 URL",
        required = false,
        schema = Schema(
            type = "string",
            example = "https://image.yes24.com/goods/92742567/XL"
        )
    )
    val titleImage: String? = null,

    @field:Parameter(
        description = "책의 현재 상태 (예: 판매중, 절판, 예약판매 등).",
        required = false,
        schema = Schema(
            type = "string",
            example = "판매중"
        )
    )
    val status: String? = null,

    @field:Parameter(
        description = "저자 ID 목록 (ID를 알고 있는 경우 사용)",
        required = false
    )
    @field:ArraySchema(
        schema = Schema(type = "integer", format = "int64"),
        arraySchema = Schema(
            type = "array",
            example = "[]",
            defaultValue = "[]"
        )
    )
    val authorIdList: List<Long> = listOf(),

    @field:Parameter(
        description = "저자 이름 목록 (ID를 모르는 경우 이름으로 검색/생성하기 위해 사용)",
        required = false
    )
    @field:ArraySchema(
        schema = Schema(type = "string"),
        arraySchema = Schema(
            type = "array",
            example = "[\"이용모\"]",
            defaultValue = "[]"
        )
    )
    val authorNameList: List<String> = listOf(),

    @field:Parameter(
        description = "출판사 ID (ID를 알고 있는 경우 사용)",
        required = false,
        schema = Schema(
            type = "integer",
            format = "int64"
        )
    )
    val publisherId: Long? = null,

    @field:Parameter(
        description = "출판사 이름 (ID를 모르는 경우 이름으로 검색/생성하기 위해 사용)",
        required = false,
        schema = Schema(
            type = "string",
            example = "위키북스"
        )
    )
    val publisherName: String? = null
)
