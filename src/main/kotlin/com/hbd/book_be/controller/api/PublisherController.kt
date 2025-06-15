package com.hbd.book_be.controller.api

import com.hbd.book_be.dto.PublisherDto
import com.hbd.book_be.dto.request.PublisherCreateRequest
import com.hbd.book_be.dto.request.enums.PublisherSortBy
import com.hbd.book_be.dto.request.enums.SortDirection
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.PublisherService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Publisher API", description = "출판사 관련 API")
@RestController
@RequestMapping("/api/v1/publishers")
class PublisherController(
    @Autowired
    private val publisherService: PublisherService
) {

    @Operation(
        summary = "출판사 목록 조회",
        description = "검색 조건에 따라 페이징된 출판사 목록을 반환합니다. 정렬 기준과 방향을 설정할 수 있습니다."
    )
    @GetMapping
    fun getPublishers(
        @Parameter(description = "페이지 번호 (0부터 시작)")
        @RequestParam("page", defaultValue = "0")
        page: Int,

        @Parameter(description = "페이지 크기")
        @RequestParam("limit", defaultValue = "10")
        limit: Int,

        @Parameter(description = "정렬 기준 (Name: 이름순, CreatedAt: 생성일순)")
        @RequestParam("orderBy", defaultValue = "Name")
        orderBy: PublisherSortBy = PublisherSortBy.Name,

        @Parameter(description = "정렬 방향 (asc: 오름차순, desc: 내림차순)")
        @RequestParam("direction", defaultValue = "desc")
        direction: SortDirection = SortDirection.desc
    ): ResponseEntity<PageResponse<PublisherDto>> {
        val pagePublisherDto = publisherService.getPublishers(page, limit, orderBy, direction)
        val pageResponse = PageResponse(
            items = pagePublisherDto.content,
            totalCount = pagePublisherDto.totalElements,
            totalPages = pagePublisherDto.totalPages,
            hasNext = pagePublisherDto.hasNext(),
            hasPrevious = pagePublisherDto.hasPrevious(),
        )
        return ResponseEntity.ok(pageResponse)
    }

    @Operation(
        summary = "새 출판사 정보 생성",
        description = "새로운 출판사 정보를 추가합니다. 출판사의 기본 정보와 관련 도서, 태그를 포함할 수 있습니다."
    )
    @PostMapping
    fun createPublisher(
        @RequestBody
        publisherCreateRequest: PublisherCreateRequest
    ): ResponseEntity<PublisherDto.Detail> {
        val publisherDto = publisherService.createPublisher(publisherCreateRequest)
        return ResponseEntity.ok(publisherDto)
    }

    @Operation(
        summary = "출판사 ID로 특정 출판사의 상세 정보 조회",
        description = "출판사 ID를 사용하여 특정 출판사의 출간 도서 목록과 태그를 포함한 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    fun getDetailedPublisher(
        @Parameter(description = "조회할 출판사의 ID", required = true) @PathVariable id: Long
    ): ResponseEntity<PublisherDto.Detail> {
        val publisherDto = publisherService.getPublisherDetail(id)
        return ResponseEntity.ok(publisherDto)
    }

}