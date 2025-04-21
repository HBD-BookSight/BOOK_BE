package com.hbd.book_be.loader.dto

data class CulturalBookDto(
    val isbnNo: String?,
    val seqNo: Int,
    val isbnThirteenNo: String? = null,
    val vlmNm: String? = null,
    val titleNm: String? = null,
    val authrNm: String? = null,
    val publisherNm: String? = null,
    val pblicteDe: String? = null,
    val adtionSmblNm: String? = null,
    val prcValue: String? = null,
    val imageUrl: String? = null,
    val bookIntrcnCn: String? = null,
    val kdcNm: String? = null,
    val titleSbstNm: String? = null,
    val authrSbstNm: String? = null,
    val twoPblicteDe: String? = null,
    val intntBookstBookExstAt: String? = null,
    val portalSiteBookExstAt: String? = null,
)
