package com.hbd.book_be.controller.admin

import com.hbd.book_be.dto.EventDto
import com.hbd.book_be.dto.request.EventCreateRequest
import com.hbd.book_be.dto.request.EventUpdateRequest
import com.hbd.book_be.exception.ErrorCodes
import com.hbd.book_be.exception.ValidationException
import com.hbd.book_be.service.EventService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Admin - Event API", description = "관리자용 - 이벤트 관리 API")
@RestController
@RequestMapping("/admin/v1/events")
class AdminEventController(
    private val eventService: EventService
) {

    @Operation(
        summary = "새 이벤트 생성",
        description = "새로운 이벤트를 생성합니다. 관리자 권한이 필요합니다."
    )
    @PostMapping
    fun createEvent(
        @RequestBody eventCreateRequest: EventCreateRequest
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
        summary = "이벤트 수정",
        description = "기존 이벤트의 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @PutMapping("/{eventId}")
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
        description = "이벤트 정보를 삭제합니다. 관리자 권한이 필요합니다."
    )
    @DeleteMapping("/{eventId}")
    fun deleteEvent(
        @Parameter(description = "삭제할 이벤트의 ID", required = true)
        @PathVariable eventId: Long
    ): ResponseEntity<Void> {
        eventService.deleteEvent(eventId)
        return ResponseEntity.noContent().build()
    }
}
