package com.hbd.book_be.controller.api

import com.hbd.book_be.annotation.RequireAdminRole

import com.hbd.book_be.dto.ContentsDto
import com.hbd.book_be.dto.DiscoveryContentsDto
import com.hbd.book_be.dto.request.ContentsCreateRequest
import com.hbd.book_be.dto.request.ContentsUpdateRequest
import com.hbd.book_be.dto.request.enums.ContentsSortBy
import com.hbd.book_be.dto.request.enums.SortDirection
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.ContentsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Contents API", description = "콘텐츠 관련 API")
@RestController
@RequestMapping("/api/v1/contents")
class ContentsController(
    @Autowired
    private val contentsService: ContentsService
) {

    @Operation(
        summary = "콘텐츠 목록 조회",
        description = "페이징, 정렬 조건에 따라 콘텐츠 목록을 조회합니다."
    )
    @GetMapping
    fun getContents(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam("page", defaultValue = "0")
        page: Int,

        @Parameter(description = "페이지당 항목 수", example = "10")
        @RequestParam("limit", defaultValue = "10")
        limit: Int,

        @Parameter(description = "정렬 기준 필드", example = "CreatedAt")
        @RequestParam("orderBy", defaultValue = "CreatedAt")
        orderBy: ContentsSortBy = ContentsSortBy.CreatedAt,

        @Parameter(description = "정렬 방향 (asc, desc)", example = "desc")
        @RequestParam("direction", defaultValue = "desc")
        direction: SortDirection = SortDirection.desc,
    ): ResponseEntity<PageResponse<ContentsDto>> {
        val pageContentsDto = contentsService.getContents(page, limit, orderBy, direction)
        val pageResponse = PageResponse<ContentsDto>(
            items = pageContentsDto.content,
            totalCount = pageContentsDto.totalElements,
            totalPages = pageContentsDto.totalPages,
            hasNext = pageContentsDto.hasNext(),
            hasPrevious = pageContentsDto.hasPrevious(),
        )

        return ResponseEntity.ok(pageResponse)
    }

    @Operation(
        summary = "콘텐츠 상세 정보 조회",
        description = "콘텐츠 ID를 사용하여 특정 콘텐츠의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    fun getDetailedContents(
        @Parameter(description = "조회할 콘텐츠의 ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<ContentsDto.Detail> {
        val contentsDetailedDto = contentsService.getContentsDetail(id)
        return ResponseEntity.ok(contentsDetailedDto)
    }

    @Operation(
        summary = "발견 콘텐츠 목록 조회",
        description = "발견 페이지에 표시할 콘텐츠 목록을 조회합니다."
    )
    @GetMapping("/discovery")
    fun getDiscoveryContents(): ResponseEntity<ListResponse<ContentsDto>> {
        val discoveryContentsList = contentsService.getDiscoveryContents()

        val listResponse = ListResponse(items = discoveryContentsList, length = discoveryContentsList.size)
        return ResponseEntity.ok(listResponse)
    }

    @Operation(
        summary = "새 콘텐츠 생성",
        description = "새로운 콘텐츠를 생성합니다."
    )
    @PostMapping
    @RequireAdminRole
    fun createContents(
        @RequestBody
        request: ContentsCreateRequest
    ): ResponseEntity<ContentsDto.Detail> {
        val contentsDto = contentsService.createContents(request)
        return ResponseEntity.ok(contentsDto)
    }

    @Operation(
        summary = "콘텐츠 정보 수정",
        description = "기존 콘텐츠 정보를 수정합니다. 관리자만 수정할 수 있습니다."
    )
    @PutMapping("/{id}")
    @RequireAdminRole
    fun updateContents(
        @Parameter(description = "수정할 콘텐츠의 ID", required = true) @PathVariable id: Long,
        @RequestBody contentsUpdateRequest: ContentsUpdateRequest
    ): ResponseEntity<ContentsDto.Detail> {
        val updatedContents = contentsService.updateContents(id, contentsUpdateRequest)
        return ResponseEntity.ok(updatedContents)
    }

    @Operation(
        summary = "콘텐츠 정보 삭제",
        description = "콘텐츠 정보를 삭제합니다. 관리자만 삭제할 수 있습니다."
    )
    @DeleteMapping("/{id}")
    @RequireAdminRole
    fun deleteContents(
        @Parameter(description = "삭제할 콘텐츠의 ID", required = true) @PathVariable id: Long
    ): ResponseEntity<Void> {
        contentsService.deleteContents(id)
        return ResponseEntity.noContent().build()
    }
}