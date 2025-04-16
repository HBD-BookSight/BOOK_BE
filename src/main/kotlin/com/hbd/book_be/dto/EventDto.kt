package com.hbd.book_be.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hbd.book_be.domain.Event
import com.hbd.book_be.domain.enums.EventFlag
import com.hbd.book_be.domain.enums.EventLocation
import java.time.LocalDate

data class EventDto(
    val id: Long,
    val title: String,
    val host: String,
    val creator: UserDto,
    val url: String,
    val senderEmail: String?,
    val senderMessage: String?,
    val location: EventLocation,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val eventType: String,
    val eventFlag: EventFlag,
    val memo: String?,

    @JsonProperty("books")
    val bookDtoList: List<BookDto>,
    @JsonProperty("tags")
    val tagDtoList: List<TagDto>
) {
    companion object {
        fun fromEntity(event: Event): EventDto {
            if (event.id == null) {
                throw IllegalArgumentException("Event ID cannot be null")
            }

            val tagDtoList = event.tagEventList.map { TagDto.fromEntity(it.tag) }
            val bookDtoList = event.bookEventList.map { BookDto.fromEntity(it.book) }

            return EventDto(
                id = event.id!!,
                title = event.title,
                host = event.host,
                creator = UserDto.fromEntity(event.creator),
                url = event.url,
                senderEmail = event.senderEmail,
                senderMessage = event.senderMessage,
                location = event.location,
                startDate = event.startDate,
                endDate = event.endDate,
                eventType = event.eventType,
                eventFlag = event.eventFlag,
                memo = event.memo,
                bookDtoList = bookDtoList,
                tagDtoList = tagDtoList
            )
        }
    }
}
