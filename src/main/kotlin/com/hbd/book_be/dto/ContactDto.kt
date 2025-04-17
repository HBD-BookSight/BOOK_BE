package com.hbd.book_be.dto

import com.hbd.book_be.domain.Contact

data class ContactDto(
    val email: String,
    val message: String
){

    companion object{
        fun fromEntity(contact: Contact): ContactDto {
            return ContactDto(
                email = contact.email,
                message = contact.message
            )
        }
    }
}