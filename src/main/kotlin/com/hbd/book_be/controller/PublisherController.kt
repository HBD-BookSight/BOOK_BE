package com.hbd.book_be.controller

import com.hbd.book_be.dto.PublisherDto
import com.hbd.book_be.dto.request.PublisherCreateRequest
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.service.PublisherService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/publishers")
class PublisherController(
    @Autowired
    private val publisherService: PublisherService
) {

    @GetMapping
    fun getPublishers(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("limit", defaultValue = "10") limit: Int,
        @RequestParam("orderBy", defaultValue = "name") orderBy: String,
        @RequestParam("direction", defaultValue = "asc") direction: String
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

    @PostMapping
    fun createPublisher(@RequestBody publisherCreateRequest: PublisherCreateRequest): ResponseEntity<PublisherDto.Detail> {
        val publisherDto = publisherService.createPublisher(publisherCreateRequest)
        return ResponseEntity.ok(publisherDto)
    }

    @GetMapping("/{id}")
    fun getDetailedPublisher(@PathVariable id: Long): ResponseEntity<PublisherDto.Detail> {
        val publisherDto = publisherService.getPublisherDetail(id)
        return ResponseEntity.ok(publisherDto)
    }

}