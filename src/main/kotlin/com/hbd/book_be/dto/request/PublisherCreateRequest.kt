package com.hbd.book_be.dto.request

import com.hbd.book_be.domain.common.UrlInfo
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class PublisherCreateRequest(
    @field:Schema(description = "출판사명", example = "민음사")
    val name: String,

    @field:Schema(description = "출판사 영어명", example = "Minumsa Publishing Group", nullable = true)
    val engName: String?,

    @field:Schema(
        description = "출판사 로고 이미지 URL",
        example = "https://image.aladin.co.kr/publisher/minumsa.jpg",
        nullable = true
    )
    val logo: String?,

    @field:Schema(
        description = "출판사 소개 및 설명",
        example = "1966년 창립된 한국의 대표 출판사 중 하나로, 문학과 인문학 도서를 주로 출간합니다.",
        nullable = true
    )
    val description: String?,

    @field:Schema(description = "출판사 메모 또는 추가 정보", example = "특별 이벤트 정보", nullable = true)
    val memo: String?,

    @field:ArraySchema(
        schema = Schema(implementation = UrlInfo::class),
        arraySchema = Schema(
            description = "출판사 관련 URL 목록 (홈페이지, 소셜미디어 등)",
            example = "[{\"url\": \"https://www.minumsa.com\", \"type\": \"youtube\"}, {\"url\": \"https://www.instagram.com/minumsa\", \"type\": \"instagram\"}]",
            defaultValue = "[]"
        )
    )
    val urls: List<UrlInfo> = emptyList(),

    @field:ArraySchema(
        schema = Schema(type = "string"),
        arraySchema = Schema(
            description = "출판사와 관련된 도서의 ISBN 목록",
            example = "[\"9791168473690\", \"9788937473845\"]",
            defaultValue = "[]"
        )
    )
    val bookIsbnList: List<String> = emptyList(),

    @field:ArraySchema(
        schema = Schema(type = "string"),
        arraySchema = Schema(
            description = "출판사와 관련된 태그 목록",
            example = "[\"문학\", \"인문학\", \"에세이\"]",
            defaultValue = "[]"
        )
    )
    val tagList: List<String> = emptyList(),
)
