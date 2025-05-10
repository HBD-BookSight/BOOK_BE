package com.hbd.book_be.loader.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CulturalBookDto(
    @JsonProperty("SEQ_NO") val seqNo: Int = 0,
    @JsonProperty("ISBN_NO") val isbnNo: String? = null,
    @JsonProperty("ISBN_THIRTEEN_NO") val isbnThirteenNo: String? = null,
    @JsonProperty("VLM_NM") val vlmNm: String? = null,
    @JsonProperty("TITLE_NM") val titleNm: String? = null,
    @JsonProperty("AUTHR_NM") val authrNm: String? = null,
    @JsonProperty("PUBLISHER_NM") val publisherNm: String? = null,
    @JsonProperty("PBLICTE_DE") val pblicteDe: String? = null,
    @JsonProperty("ADTION_SMBL_NM") val adtionSmblNm: String? = null,
    @JsonProperty("PRC_VALUE") val prcValue: String? = null,
    @JsonProperty("IMAGE_URL") val imageUrl: String? = null,
    @JsonProperty("BOOK_INTRCN_CN") val bookIntrcnCn: String? = null,
    @JsonProperty("KDC_NM") val kdcNm: String? = null,
    @JsonProperty("TITLE_SBST_NM") val titleSbstNm: String? = null,
    @JsonProperty("AUTHR_SBST_NM") val authrSbstNm: String? = null,
    @JsonProperty("TWO_PBLICTE_DE") val twoPblicteDe: String? = null,
    @JsonProperty("INTNT_BOOKST_BOOK_EXST_AT") val intntBookstBookExstAt: String? = null,
    @JsonProperty("PORTAL_SITE_BOOK_EXST_AT") val portalSiteBookExstAt: String? = null
)
