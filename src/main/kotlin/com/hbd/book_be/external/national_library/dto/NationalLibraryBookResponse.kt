package com.hbd.book_be.external.national_library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "국립중앙도서관 도서 정보 응답")
data class NationalLibraryBookResponse(
    @Schema(description = "전체 출력수")
    @JsonProperty("TOTAL_COUNT")
    val totalCount: Long? = null,

    @Schema(description = "현재 쪽번호")
    @JsonProperty("PAGE_NO")
    val pageNo: Long? = null,

    @Schema(description = "도서 목록")
    @JsonProperty("docs")
    val docs: List<NationalLibraryBook> = emptyList()
)

@Schema(description = "국립중앙도서관 도서 상세 정보")
data class NationalLibraryBook(
    @Schema(description = "표제")
    @JsonProperty("TITLE")
    val title: String? = null,

    @Schema(description = "권차")
    @JsonProperty("VOL")
    val vol: String? = null,

    @Schema(description = "총서명")
    @JsonProperty("SERIES_TITLE")
    val seriesTitle: String? = null,

    @Schema(description = "총서편차")
    @JsonProperty("SERIES_NO")
    val seriesNo: String? = null,

    @Schema(description = "저자")
    @JsonProperty("AUTHOR")
    val author: String? = null,

    @Schema(description = "ISBN")
    @JsonProperty("EA_ISBN")
    val eaIsbn: String? = null,

    @Schema(description = "ISBN 부가기호")
    @JsonProperty("EA_ADD_CODE")
    val eaAddCode: String? = null,

    @Schema(description = "세트 ISBN")
    @JsonProperty("SET_ISBN")
    val setIsbn: String? = null,

    @Schema(description = "세트 ISBN 부가기호")
    @JsonProperty("SET_ADD_CODE")
    val setAddCode: String? = null,

    @Schema(description = "세트표현 (세트, 전2권.)")
    @JsonProperty("SET_EXPRESSION")
    val setExpression: String? = null,

    @Schema(description = "발행처")
    @JsonProperty("PUBLISHER")
    val publisher: String? = null,

    @Schema(description = "판사항")
    @JsonProperty("EDITION_STMT")
    val editionStmt: String? = null,

    @Schema(description = "예정가격")
    @JsonProperty("PRE_PRICE")
    val prePrice: String? = null,
    
    @Schema(description = "실제가격")
    @JsonProperty("REAL_PRICE")
    val realPrice: String? = null,

    @Schema(description = "한국십진분류 (2020년 12월 31일 이후 데이터 제공불가)")
    @JsonProperty("KDC")
    val kdc: String? = null,

    @Schema(description = "듀이십진분류")
    @JsonProperty("DDC")
    val ddc: String? = null,

    @Schema(description = "페이지")
    @JsonProperty("PAGE")
    val page: String? = null,

    @Schema(description = "책크기")
    @JsonProperty("BOOK_SIZE")
    val bookSize: String? = null,

    @Schema(description = "발행제본형태")
    @JsonProperty("FORM")
    val form: String? = null,
    
    @Schema(description = "형태 상세")
    @JsonProperty("FORM_DETAIL")
    val formDetail: String? = null,

    @Schema(description = "출판예정일")
    @JsonProperty("PUBLISH_PREDATE")
    val publishPredate: String? = null,
    
    @Schema(description = "실제출판일")
    @JsonProperty("REAL_PUBLISH_DATE")
    val realPublishDate: String? = null,

    @Schema(description = "주제 (KDC 대분류)")
    @JsonProperty("SUBJECT")
    val subject: String? = null,

    @Schema(description = "전자책여부 (Y: 전자책, N : 인쇄책)")
    @JsonProperty("EBOOK_YN")
    val ebookYn: String? = null,

    @Schema(description = "CIP 신청여부 (Y: CIP신청, N: CIP신청안함)")
    @JsonProperty("CIP_YN")
    val cipYn: String? = null,
    
    @Schema(description = "납본 여부 (Y: 납본, N: 미납본)")
    @JsonProperty("DEPOSIT_YN")
    val depositYn: String? = null,

    @Schema(description = "CIP 제어번호")
    @JsonProperty("CONTROL_NO")
    val controlNo: String? = null,

    @Schema(description = "표지이미지 URL")
    @JsonProperty("TITLE_URL")
    val titleUrl: String? = null,

    @Schema(description = "목차 URL")
    @JsonProperty("BOOK_TB_CNT_URL")
    val bookTbCntUrl: String? = null,

    @Schema(description = "목차 내용")
    @JsonProperty("BOOK_TB_CNT")
    val bookTbCnt: String? = null,

    @Schema(description = "책소개 URL")
    @JsonProperty("BOOK_INTRODUCTION_URL")
    val bookIntroductionUrl: String? = null,

    @Schema(description = "책소개 내용")
    @JsonProperty("BOOK_INTRODUCTION")
    val bookIntroduction: String? = null,

    @Schema(description = "책요약 URL")
    @JsonProperty("BOOK_SUMMARY_URL")
    val bookSummaryUrl: String? = null,

    @Schema(description = "책요약 내용")
    @JsonProperty("BOOK_SUMMARY")
    val bookSummary: String? = null,

    @Schema(description = "출판사 홈페이지 URL")
    @JsonProperty("PUBLISHER_URL")
    val publisherUrl: String? = null,
    
    @Schema(description = "관련 ISBN")
    @JsonProperty("RELATED_ISBN")
    val relatedIsbn: String? = null,
    
    @Schema(description = "서지기술 생성용 기초 정보 여부")
    @JsonProperty("BIB_YN")
    val bibYn: String? = null,

    @Schema(description = "등록날짜")
    @JsonProperty("INPUT_DATE")
    val inputDate: String? = null,

    @Schema(description = "수정날짜")
    @JsonProperty("UPDATE_DATE")
    val updateDate: String? = null
) 