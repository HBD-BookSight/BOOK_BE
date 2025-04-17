package com.hbd.book_be.controller

import com.hbd.book_be.domain.enums.EventFlag
import com.hbd.book_be.domain.enums.EventLocation
import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.EventDto
import com.hbd.book_be.dto.request.EventCreateRequest
import com.hbd.book_be.dto.request.EventSearchRequest
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.exception.ErrorCodes
import com.hbd.book_be.exception.ValidationException
import com.hbd.book_be.service.EventService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val eventService: EventService
) {

    @GetMapping("/{eventId}")
    fun getEvent(@PathVariable eventId: Long): ResponseEntity<EventDto> {
        val event = eventService.getEvent(eventId)
        return ResponseEntity.ok(event)
    }

    @GetMapping
    fun getEvents(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("limit", defaultValue = "10") limit: Int,
        @RequestParam("eventFlag", required = false) eventFlag: EventFlag?,
        @RequestParam("location", required = false) location: EventLocation?,
        @RequestParam("eventType", required = false) eventType: String?,
        @RequestParam("startDate", required = false) startDate: LocalDate?,
        @RequestParam("endDate", required = false) endDate: LocalDate?
    ): ResponseEntity<PageResponse<EventDto>> {
        val searchRequest = EventSearchRequest(
            eventFlag = eventFlag,
            location = location,
            eventType = eventType,
            startDate = startDate,
            endDate = endDate
        )
        val eventDtoPage = eventService.getEvents(page, limit, searchRequest)
        val pageResponse = PageResponse(
            items = eventDtoPage.content,
            totalPages = eventDtoPage.totalPages,
            totalCount = eventDtoPage.totalElements,
            hasNext = eventDtoPage.hasNext(),
            hasPrevious = eventDtoPage.hasPrevious()
        )

        return ResponseEntity.ok(pageResponse)
    }

    @PostMapping
    fun createEvent(@RequestBody eventCreateRequest: EventCreateRequest): ResponseEntity<EventDto> {
        if (eventCreateRequest.endDate < LocalDate.now().minusDays(1)) {
            throw ValidationException(
                message = "Event endDate must be later than today.",
                errorCode = ErrorCodes.INVALID_EVENT_DATE
            )
        }

        val eventDto = eventService.createEvent(eventCreateRequest)
        return ResponseEntity.ok(eventDto)
    }

    @GetMapping("/{eventId}/books")
    fun getEventBooks(@PathVariable eventId: Long): ResponseEntity<ListResponse<BookDto>> {
        val bookDtoList = eventService.getEventBooks(eventId)
        val listResponse = ListResponse(items=bookDtoList, length=bookDtoList.size)
        return ResponseEntity.ok(listResponse)
    }

}