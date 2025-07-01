package com.hbd.book_be.controller.api

import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.EventDto
import com.hbd.book_be.dto.request.EventCreateRequest
import com.hbd.book_be.dto.request.EventSearchRequest
import com.hbd.book_be.dto.request.EventUpdateRequest
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.enums.EventFlag
import com.hbd.book_be.enums.EventLocation
import com.hbd.book_be.exception.ErrorCodes
import com.hbd.book_be.exception.ValidationException
import com.hbd.book_be.service.EventService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Event API", description = "이벤트 관련 API")
@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val eventService: EventService
) {

    @Operation(
        summary = "이벤트 상세 정보 조회",
        description = "이벤트 ID를 사용하여 특정 이벤트의 상세 정보를 조회합니다."
    )
    @GetMapping("/{eventId}")
    fun getEvent(
        @Parameter(description = "조회할 이벤트의 ID", required = true)
        @PathVariable eventId: Long
    ): ResponseEntity<EventDto> {
        val event = eventService.getEvent(eventId)
        return ResponseEntity.ok(event)
    }

    @Operation(
        summary = "이벤트 목록 조회",
        description = "조건에 따라 페이징된 이벤트 목록을 조회합니다."
    )
    @GetMapping
    fun getEvents(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam("page", defaultValue = "0") page: Int,
        @Parameter(description = "페이지당 항목 수", example = "10")
        @RequestParam("limit", defaultValue = "10") limit: Int,
        @Parameter(description = "이벤트 플래그 필터")
        @RequestParam("eventFlag", required = false) eventFlag: EventFlag?,
        @Parameter(description = "이벤트 위치 필터")
        @RequestParam("location", required = false) location: EventLocation?,
        @Parameter(description = "이벤트 타입 필터")
        @RequestParam("eventType", required = false) eventType: String?,
        @Parameter(description = "시작 날짜 필터", example = "2024-01-01")
        @RequestParam("startDate", required = false) startDate: LocalDate?,
        @Parameter(description = "종료 날짜 필터", example = "2024-12-31")
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

    @Operation(
        summary = "새 이벤트 생성",
        description = "새로운 이벤트를 생성합니다. 이벤트 종료 날짜는 오늘 이후여야 합니다."
    )
    @PostMapping
    @RequireAdminRole
    fun createEvent(
        @RequestBody
        eventCreateRequest: EventCreateRequest
    ): ResponseEntity<EventDto> {
        if (eventCreateRequest.endDate < LocalDate.now().minusDays(1)) {
            throw ValidationException(
                message = "Event endDate must be later than today.",
                errorCode = ErrorCodes.INVALID_EVENT_DATE
            )
        }

        val eventDto = eventService.createEvent(eventCreateRequest)
        return ResponseEntity.ok(eventDto)
    }

    @Operation(
        summary = "이벤트 관련 도서 목록 조회",
        description = "특정 이벤트와 관련된 도서 목록을 조회합니다."
    )
    @GetMapping("/{eventId}/books")
    fun getEventBooks(
        @Parameter(description = "도서 목록을 조회할 이벤트의 ID", required = true)
        @PathVariable eventId: Long
    ): ResponseEntity<ListResponse<BookDto>> {
        val bookDtoList = eventService.getEventBooks(eventId)
        val listResponse = ListResponse(items = bookDtoList, length = bookDtoList.size)
        return ResponseEntity.ok(listResponse)
    }

    @Operation(
        summary = "이벤트 수정",
        description = "기존 이벤트의 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @PutMapping("/{eventId}")
    @RequireAdminRole
    fun updateEvent(
        @Parameter(description = "수정할 이벤트의 ID", required = true)
        @PathVariable eventId: Long,
        @RequestBody eventUpdateRequest: EventUpdateRequest
    ): ResponseEntity<EventDto> {
        val eventDto = eventService.updateEvent(eventId, eventUpdateRequest)
        return ResponseEntity.ok(eventDto)
    }

    @Operation(
        summary = "이벤트 삭제",
        description = "이벤트 정보를 삭제합니다. 관리자만 삭제할 수 있습니다. 관리자 권한이 필요합니다."
    )
    @DeleteMapping("/{eventId}")
    @RequireAdminRole
    fun deleteEvent(
        @Parameter(description = "삭제할 이벤트의 ID", required = true)
        @PathVariable eventId: Long
    ): ResponseEntity<Void> {
        eventService.deleteEvent(eventId)
        return ResponseEntity.noContent().build()
    }

}