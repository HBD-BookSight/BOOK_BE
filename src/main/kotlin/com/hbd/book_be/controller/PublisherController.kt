package com.hbd.book_be.controller

import com.hbd.book_be.dto.PublisherDto
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.service.PublisherService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/publisher")
class PublisherController(
    @Autowired
    private val publisherService: PublisherService
) {

    @GetMapping
    fun getPublisher(): ResponseEntity<ListResponse<PublisherDto>> {
        val publisherList = publisherService.getPublishers()
        val listResponse = ListResponse(items = publisherList, length = publisherList.size)
        return ResponseEntity.ok(listResponse)
    }

    @GetMapping("/{id}")
    fun getDetailedPublisher(@PathVariable id: Long): ResponseEntity<PublisherDto.Detail> {
        val publisherDto = publisherService.getPublisherDetail(id)
        return ResponseEntity.ok(publisherDto)
    }

}