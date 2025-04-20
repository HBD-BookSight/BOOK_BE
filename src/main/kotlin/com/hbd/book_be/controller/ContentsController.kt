package com.hbd.book_be.controller

import com.hbd.book_be.enums.ContentType
import com.hbd.book_be.dto.ContentsDto
import com.hbd.book_be.dto.DiscoveryContentsDto
import com.hbd.book_be.dto.request.ContentsCreateRequest
import com.hbd.book_be.dto.request.ContentsSearchRequest
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.ContentsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/contents")
class ContentsController(
    @Autowired
    private val contentsService: ContentsService
) {

    @GetMapping
    fun getContents(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("limit", defaultValue = "10") limit: Int,
        @RequestParam("orderBy", defaultValue = "createdAt") orderBy: String,
        @RequestParam("direction", defaultValue = "desc") direction: String,
        @RequestParam("type", defaultValue = "VIDEO") type: ContentType?
    ): ResponseEntity<PageResponse<ContentsDto>> {

        val searchRequest = ContentsSearchRequest(
            type = type,
        )
        val pageContentsDto = contentsService.getContents(page, limit, orderBy, direction, searchRequest)
        val pageResponse = PageResponse<ContentsDto>(
            items = pageContentsDto.content,
            totalCount = pageContentsDto.totalElements,
            totalPages = pageContentsDto.totalPages,
            hasNext = pageContentsDto.hasNext(),
            hasPrevious = pageContentsDto.hasPrevious(),
        )

        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/{id}")
    fun getDetailedContents(@PathVariable id: Long): ResponseEntity<ContentsDto.Detail> {
        val contentsDetailedDto = contentsService.getContentsDetail(id)
        return ResponseEntity.ok(contentsDetailedDto)
    }

    @GetMapping("/discovery")
    fun getDiscoveryContents(
        @RequestParam("limit", defaultValue = "10") limit: Int,
    ): ResponseEntity<ListResponse<DiscoveryContentsDto>> {
        val discoveryContentsList = contentsService.getDiscoveryContents(limit)

        val listResponse = ListResponse(items = discoveryContentsList, length = discoveryContentsList.size)
        return ResponseEntity.ok(listResponse)
    }

    @PostMapping
    fun createContents(@RequestBody request: ContentsCreateRequest): ResponseEntity<ContentsDto> {
        val contentsDto = contentsService.createContents(request)
        return ResponseEntity.ok(contentsDto)
    }
}