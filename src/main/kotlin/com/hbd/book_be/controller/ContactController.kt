package com.hbd.book_be.controller

import com.hbd.book_be.dto.ContactDto
import com.hbd.book_be.dto.request.ContactCreateRequest
import com.hbd.book_be.service.ContactService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contact")
class ContactController(
    private val contactService: ContactService
) {

    @PostMapping
    fun createContact(@RequestBody contactCreateRequest: ContactCreateRequest): ResponseEntity<ContactDto> {
        val contactDto = contactService.createContact(contactCreateRequest)
        return ResponseEntity.ok(contactDto)
    }
}