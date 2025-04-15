package com.hbd.book_be.service

import com.hbd.book_be.dto.ContactDto
import com.hbd.book_be.repository.ContactRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ContactService (
    @Autowired
    private val contactRepository: ContactRepository
){
    fun addContact(contactDto: ContactDto): ContactDto {
        val entity = contactDto.toEntity()
        val saved = contactRepository.save(entity)
        return ContactDto.fromEntity(saved)
    }

    fun getAllContacts(): List<ContactDto> {
        return contactRepository.findAll().map { ContactDto.fromEntity(it) }
    }
}