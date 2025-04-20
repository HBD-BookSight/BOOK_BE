package com.hbd.book_be.controller

import com.hbd.book_be.dto.ContactDto
import com.hbd.book_be.dto.request.ContactCreateRequest
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.service.ContactService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/contacts")
class ContactController(
    @Autowired
    private val contactService: ContactService
) {
    @GetMapping
    fun getContacts(): ResponseEntity<ListResponse<ContactDto>> {
        val contactList = contactService.getContacts()
        val listResponse = ListResponse(items = contactList, length = contactList.size)
        return ResponseEntity.ok(listResponse)
    }

    @PostMapping
    fun createContact(@RequestBody contactCreateRequest: ContactCreateRequest): ResponseEntity<ContactDto> {
        val contactDto = contactService.createContact(contactCreateRequest)
        return ResponseEntity.ok(contactDto)
    }
}