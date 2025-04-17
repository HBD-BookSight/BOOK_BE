package com.hbd.book_be.dto.request

import com.hbd.book_be.domain.enums.EventFlag
import com.hbd.book_be.domain.enums.EventLocation
import java.time.LocalDate

data class EventSearchRequest(
    val eventFlag: EventFlag? = null,
    val location: EventLocation? = null,
    val eventType: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
) 