package com.hbd.book_be.dto.request

import com.hbd.book_be.domain.common.UrlInfo
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class PublisherUpdateRequest(
    @field:Schema(
        description = "출판사명",
        required = false,
        example = "민음사"
    )
    val name: String?,

    @field:Schema(
        description = "출판사 영어명",
        required = false,
        example = "Minumsa Publishing Group"
    )
    val engName: String?,

    @field:Schema(
        description = "출판사 로고 이미지 URL",
        required = false,
        example = "https://image.aladin.co.kr/publisher/minumsa.jpg"
    )
    val logo: String?,

    @field:Schema(
        description = "출판사 소개 및 설명",
        required = false,
        example = "1966년 창립된 한국의 대표 출판사 중 하나로, 문학과 인문학 도서를 주로 출간합니다."
    )
    val description: String?,

    @field:ArraySchema(
        schema = Schema(
            description = "출판사 관련 URL 목록 (홈페이지, 소셜미디어 등)",
            implementation = UrlInfo::class
        ),
        arraySchema = Schema(
            type = "array",
            required = false
        )
    )
    val urls: List<UrlInfo>?,

    @field:ArraySchema(
        schema = Schema(
            description = "출판사와 관련된 도서의 ISBN 목록",
            type = "string"
        ),
        arraySchema = Schema(
            type = "array",
            required = false,
            example = "[\"9791168473690\", \"9788937473845\"]"
        )
    )
    val bookIsbnList: List<String>?,

    @field:ArraySchema(
        schema = Schema(
            description = "출판사와 관련된 태그 목록",
            type = "string"
        ),
        arraySchema = Schema(
            type = "array",
            required = false,
            example = "[\"문학\", \"인문학\", \"에세이\"]"
        )
    )
    val tagList: List<String>?
)
