package com.hbd.book_be.dto.request

import com.hbd.book_be.domain.common.UrlInfo
import com.hbd.book_be.enums.EventFlag
import com.hbd.book_be.enums.EventLocation
import java.time.LocalDate

data class EventCreateRequest(
    val title: String,
    val urls: List<UrlInfo>,
    val host: String,
    val userId: Long,
    val location: EventLocation,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val eventType: String,
    val eventFlag: EventFlag,
    val isPosting: Boolean,
    val senderEmail: String?,
    val senderMessage: String?,
    val memo: String?,

    val tagList: List<String> = emptyList(),
    val bookIsbnList: List<String> = emptyList(),
)