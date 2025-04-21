package com.hbd.book_be.dto.request

import com.hbd.book_be.enums.EventFlag
import com.hbd.book_be.enums.EventLocation
import java.time.LocalDate

data class EventSearchRequest(
    val eventFlag: EventFlag? = null,
    val location: EventLocation? = null,
    val eventType: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
) 