package com.hbd.book_be.controller.admin

import com.hbd.book_be.dto.PublisherDto
import com.hbd.book_be.dto.request.PublisherCreateRequest
import com.hbd.book_be.dto.request.PublisherUpdateRequest
import com.hbd.book_be.service.PublisherService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin - Publisher API", description = "관리자 - 출판사 관리 API")
@RestController
@RequestMapping("/admin/v1/publishers")
class AdminPublisherController(
    private val publisherService: PublisherService
) {

    @Operation(
        summary = "새 출판사 정보 생성",
        description = "새로운 출판사 정보를 추가합니다. 관리자 권한이 필요합니다."
    )
    @PostMapping
    fun createPublisher(
        @RequestBody publisherCreateRequest: PublisherCreateRequest
    ): ResponseEntity<PublisherDto.Detail> {
        val publisherDto = publisherService.createPublisher(publisherCreateRequest)
        return ResponseEntity.ok(publisherDto)
    }

    @Operation(
        summary = "출판사 정보 수정",
        description = "기존 출판사 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @PutMapping("/{id}")
    fun updatePublisher(
        @Parameter(description = "수정할 출판사의 ID", required = true)
        @PathVariable id: Long,
        @RequestBody publisherUpdateRequest: PublisherUpdateRequest
    ): ResponseEntity<PublisherDto.Detail> {
        val updatedPublisher = publisherService.updatePublisher(id, publisherUpdateRequest)
        return ResponseEntity.ok(updatedPublisher)
    }

    @Operation(
        summary = "출판사 정보 삭제",
        description = "출판사 정보를 삭제합니다. 관리자 권한이 필요합니다."
    )
    @DeleteMapping("/{id}")
    fun deletePublisher(
        @Parameter(description = "삭제할 출판사의 ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        publisherService.deletePublisher(id)
        return ResponseEntity.noContent().build()
    }
}
