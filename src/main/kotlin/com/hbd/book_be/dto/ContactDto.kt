package com.hbd.book_be.dto

import com.hbd.book_be.domain.Contact

data class ContactDto(
    val name: String?,
    val email: String,
    val message: String
){

    companion object{
        fun fromEntity(contact: Contact): ContactDto {
            return ContactDto(
                name = contact.name,
                email = contact.email,
                message = contact.message
            )
        }
    }
}