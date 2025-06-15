package com.hbd.book_be.controller.api

import com.hbd.book_be.dto.ContactDto
import com.hbd.book_be.dto.request.ContactCreateRequest
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.service.ContactService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Contact API", description = "Contact 관련 API")
@RestController
@RequestMapping("/api/v1/contacts")
class ContactController(
    private val contactService: ContactService
) {
    @Operation(
        summary = "Contact 목록 조회",
        description = "등록된 모든 Contact 목록을 조회합니다."
    )
    @GetMapping
    fun getContacts(): ResponseEntity<ListResponse<ContactDto>> {
        val contactList = contactService.getContacts()
        val listResponse = ListResponse(items = contactList, length = contactList.size)
        return ResponseEntity.ok(listResponse)
    }

    @Operation(
        summary = "새 Contact 생성",
        description = "새로운 Contact를 생성합니다."
    )
    @PostMapping
    fun createContact(
        @RequestBody
        contactCreateRequest: ContactCreateRequest
    ): ResponseEntity<ContactDto> {
        val contactDto = contactService.createContact(contactCreateRequest)
        return ResponseEntity.ok(contactDto)
    }
}