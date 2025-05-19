package com.hbd.book_be.external.kakao

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.hbd.book_be.external.kakao.SortType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Kakao Book API 요청 객체")
data class KakaoApiRequest @JsonCreator constructor(
    @JsonProperty("query")
    @Schema(description = "검색할 키워드", example = "미움받을 용기")
    val query: String,

    @JsonProperty("sort")
    @Schema(
        description = "정렬 기준 (ACCURACY, LATEST)",
        allowableValues = ["ACCURACY", "LATEST"],
        example = "ACCURACY"
    )
    val sort: SortType = SortType.ACCURACY,

    @JsonProperty("page")
    @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
    val page: Int = 1,

    @JsonProperty("size")
    @Schema(description = "한 페이지당 결과 개수", example = "10")
    val size: Int = 10,

    @JsonProperty("target")
    @Schema(
        description = "검색 대상 필드 (title, isbn, publisher, person)",
        example = "title"
    )
    val target: String? = null
)