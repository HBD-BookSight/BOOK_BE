package com.hbd.book_be.service

import com.hbd.book_be.domain.Contact
import com.hbd.book_be.dto.ContactDto
import com.hbd.book_be.dto.request.ContactCreateRequest
import com.hbd.book_be.repository.ContactRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ContactService(
    @Autowired
    private val contactRepository: ContactRepository
) {

    @Transactional
    fun createContact(contactCreateRequest: ContactCreateRequest): ContactDto {
        var contact = Contact(email = contactCreateRequest.email, message = contactCreateRequest.message)
        contact = contactRepository.save(contact)
        return ContactDto.fromEntity(contact)
    }
}