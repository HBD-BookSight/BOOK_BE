package com.hbd.book_be.external.kakao

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Kakao Book API 요청 객체")
data class KakaoApiRequest @JsonCreator constructor(
    @JsonProperty("query")
    @field:Parameter(
        description = "검색할 키워드",
        required = true,
        schema = Schema(
            type = "string",
            example = "미움받을 용기"
        )
    )
    val query: String,

    @JsonProperty("sort")
    @field:Parameter(
        description = "정렬 기준 (ACCURACY, LATEST)",
        schema = Schema(
            allowableValues = ["ACCURACY", "LATEST"],
            example = "ACCURACY",
        ),
        required = true,
    )
    val sort: SortType = SortType.ACCURACY,

    @JsonProperty("page")
    @field:Parameter(
        description = "페이지 번호 (1부터 시작)",
        required = false,
        schema = Schema(
            type = "integer",
            example = "1"
        )
    )
    val page: Int = 1,

    @JsonProperty("size")
    @field:Parameter(
        description = "한 페이지당 결과 개수",
        required = false,
        schema = Schema(
            type = "integer",
            example = "10"
        )
    )
    val size: Int = 10,

    @JsonProperty("target")
    @field:Parameter(
        description = "검색 대상 필드 (title, isbn, publisher, person)",
        required = false,
        schema = Schema(
            type = "string",
            allowableValues = ["title", "isbn", "publisher", "person"],
            example = "title"
        )
    )
    val target: String? = null
)