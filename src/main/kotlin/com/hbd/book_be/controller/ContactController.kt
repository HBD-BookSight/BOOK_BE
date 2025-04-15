package com.hbd.book_be.controller

import com.hbd.book_be.dto.ContactDto
import com.hbd.book_be.dto.response.ListResponse
import com.hbd.book_be.service.ContactService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/contact")
class ContactController (
    @Autowired
    private val contactService: ContactService
){
    @GetMapping
    fun getContacts(): ListResponse<ContactDto> {
        val contactList = contactService.getAllContacts()
        return ListResponse(items = contactList, length = contactList.size)
    }

    @PostMapping
    fun addContact(@RequestBody dto: ContactDto): ResponseEntity<ContactDto> {
        val contact = contactService.addContact(dto)
        return ResponseEntity.ok(contact)
    }
}