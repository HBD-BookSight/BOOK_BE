package com.hbd.book_be.controller

import com.hbd.book_be.dto.ContentsDetailedDto
import com.hbd.book_be.dto.ContentsDto
import com.hbd.book_be.dto.request.ContentsCreateRequest
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.ContentsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/contents")
class ContentsController (
    @Autowired
    private val contentsService: ContentsService
){

    @GetMapping
    fun getContents(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("limit", defaultValue = "10") limit: Int,
        @RequestParam("orderBy", defaultValue = "publishedDate") orderBy: String,
        @RequestParam("direction", defaultValue = "desc") direction: String
    ): ResponseEntity<PageResponse<ContentsDto>> {
        val pageContentsDto = contentsService.getContents(page = page, limit = limit, orderBy = orderBy, direction = direction)
        val pageContentsResponse = PageResponse<ContentsDto>(
            items = pageContentsDto.content,
            totalCount = pageContentsDto.totalElements,
            totalPages = pageContentsDto.totalPages,
            hasNext = pageContentsDto.hasNext(),
            hasPrevious = pageContentsDto.hasPrevious(),
        )

        return ResponseEntity.ok(pageContentsResponse)
    }

    @GetMapping("/{id}")
    fun getDetailedContents(@PathVariable id: Long): ResponseEntity<ContentsDetailedDto> {
        val contentsDetailedDto = contentsService.getContentsDetail(id)
        return ResponseEntity.ok(contentsDetailedDto)
    }

    @GetMapping("/discovery")
    fun getDiscoveryContents(): ListResponse<ContentsDto> {
        val contentsList = contentsService.getDiscoveryContents()
        return ListResponse(items = contentsList, length = contentsList.size)
    }

    @PostMapping
    fun addContents(@RequestBody request: ContentsCreateRequest): ResponseEntity<ContentsDto> {
        val contents = contentsService.addContents(request)
        return ResponseEntity.ok(contents)
    }
}