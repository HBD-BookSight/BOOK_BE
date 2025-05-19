package com.hbd.book_be.external.national_library.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.bind.annotation.RequestParam

data class NationalLibrarySearchRequest(
    // if certKey doesn't be delivered, use server default certKey
    @Schema(description = "인증키", required = false)
    @RequestParam(required = false)
    val certKey: String? = null,

    @Schema(description = "결과 형식 (json,xml)", required = true, allowableValues = ["json", "xml"])
    @RequestParam(required = true, defaultValue = "json")
    val resultStyle: String = "json",

    @Schema(description = "현재 쪽번호(페이지 1부터 시작)", required = true, defaultValue = "1")
    @RequestParam(required = true, defaultValue = "1")
    val pageNo: Long = 1,

    @Schema(description = "쪽당 출력건수", required = true, defaultValue = "10")
    @RequestParam(required = true, defaultValue = "10")
    val pageSize: Long = 10,

    @Schema(description = "ISBN", required = false)
    @RequestParam(required = false)
    val isbn: String? = null,

    @Schema(description = "SET ISBN", required = false)
    @RequestParam(required = false)
    val setIsbn: String? = null,

    @Schema(description = "전자책여부", required = false, allowableValues = ["Y", "N"])
    @RequestParam(required = false)
    val ebookYn: String? = null,

    @Schema(description = "본표제", required = false)
    @RequestParam(required = false)
    val title: String? = null,

    @Schema(description = "발행예정일 시작(8자리, yyyymmdd)", required = false)
    @RequestParam(required = false)
    val startPublishDate: String? = null,

    @Schema(description = "발행예정일 끝(8자리, yyyymmdd)", required = false)
    @RequestParam(required = false)
    val endPublishDate: String? = null,

    @Schema(description = "CIP 신청여부", required = false, allowableValues = ["Y", "N"])
    @RequestParam(required = false)
    val cipYn: String? = null,

    @Schema(description = "납본유무", required = false, allowableValues = ["Y", "N"])
    @RequestParam(required = false)
    val depositYn: String? = null,

    @Schema(description = "총서명", required = false)
    @RequestParam(required = false)
    val seriesTitle: String? = null,

    @Schema(description = "발행처명", required = false)
    @RequestParam(required = false)
    val publisher: String? = null,

    @Schema(description = "저자", required = false)
    @RequestParam(required = false)
    val author: String? = null,

    @Schema(
        description = "형태사항",
        required = false,
        allowableValues = [
            "종이책", "혼합자료", "전자책", "오디오북", 
            "기타 전자출판물", "다양한 제본형태", "다양한 형식혼합 세트"
        ]
    )
    @RequestParam(required = false)
    val form: String? = null,

    @Schema(
        description = "정렬",
        required = false,
        allowableValues = ["PUBLISH_PREDATE", "INPUT_DATE", "INDEX_TITLE", "INDEX_PUBLISHER"],
        defaultValue = "PUBLISH_PREDATE"
    )
    @RequestParam(required = false, defaultValue = "PUBLISH_PREDATE")
    val sort: String = "PUBLISH_PREDATE",

    @Schema(
        description = "정렬방식",
        required = false,
        allowableValues = ["ASC", "DESC"],
        defaultValue = "DESC"
    )
    @RequestParam(required = false, defaultValue = "DESC")
    val orderBy: String = "DESC"
) 