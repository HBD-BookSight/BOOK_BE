package com.hbd.book_be.controller.admin

import com.hbd.book_be.dto.ContentsDto
import com.hbd.book_be.dto.request.ContentsCreateRequest
import com.hbd.book_be.dto.request.ContentsUpdateRequest
import com.hbd.book_be.service.ContentsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin - Contents API", description = "관리자 - 콘텐츠 관리 API")
@RestController
@RequestMapping("/admin/v1/contents")
class AdminContentsController(
    private val contentsService: ContentsService
) {

    @Operation(
        summary = "새 콘텐츠 생성",
        description = "새로운 콘텐츠를 생성합니다. 관리자 권한이 필요합니다."
    )
    @PostMapping
    fun createContents(
        @RequestBody request: ContentsCreateRequest
    ): ResponseEntity<ContentsDto.Detail> {
        val contentsDto = contentsService.createContents(request)
        return ResponseEntity.ok(contentsDto)
    }

    @Operation(
        summary = "콘텐츠 정보 수정",
        description = "기존 콘텐츠 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @PutMapping("/{id}")
    fun updateContents(
        @Parameter(description = "수정할 콘텐츠의 ID", required = true)
        @PathVariable id: Long,
        @RequestBody contentsUpdateRequest: ContentsUpdateRequest
    ): ResponseEntity<ContentsDto.Detail> {
        val updatedContents = contentsService.updateContents(id, contentsUpdateRequest)
        return ResponseEntity.ok(updatedContents)
    }

    @Operation(
        summary = "콘텐츠 정보 삭제",
        description = "콘텐츠 정보를 삭제합니다. 관리자 권한이 필요합니다."
    )
    @DeleteMapping("/{id}")
    fun deleteContents(
        @Parameter(description = "삭제할 콘텐츠의 ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        contentsService.deleteContents(id)
        return ResponseEntity.noContent().build()
    }
}