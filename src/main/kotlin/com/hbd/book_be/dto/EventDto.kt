package com.hbd.book_be.dto

import com.hbd.book_be.domain.Event
import com.hbd.book_be.domain.enums.EventFlag
import com.hbd.book_be.domain.enums.EventLocation
import java.time.LocalDateTime

data class EventDto(
    val id: Long,
    val title: String,
    val url: String,
    val location: EventLocation,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val eventType: String,
    val eventFlag: EventFlag,
) {
    companion object {
        fun fromEntity(event: Event): EventDto {
            if (event.id == null) {
                throw IllegalArgumentException("Event ID cannot be null")
            }

            return EventDto(
                id = event.id!!,
                title = event.title,
                url = event.url,
                location = event.location,
                startDate = event.startDate,
                endDate = event.endDate,
                eventType = event.eventType,
                eventFlag = event.eventFlag,
            )
        }
    }
}
