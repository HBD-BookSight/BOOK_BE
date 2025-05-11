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

        val contact = Contact(
            name = contactCreateRequest.name,
            message = contactCreateRequest.message,
            email = contactCreateRequest.email,
        )

        val saved = contactRepository.save(contact)
        return ContactDto.fromEntity(saved)
    }

    @Transactional(readOnly = true)
    fun getContacts(): List<ContactDto> {
        return contactRepository.findAll().map { ContactDto.fromEntity(it) }
    }
}