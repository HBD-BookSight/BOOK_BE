package com.hbd.book_be.dto

import com.hbd.book_be.domain.Contact

data class ContactDto(
    val id: Long? = null,
    val email: String,
    val message: String
) {
    companion object {
        fun fromEntity(contact: Contact): ContactDto {
            requireNotNull(contact.id) { "Contact id can't be null" }

            return ContactDto(
                id = contact.id,
                email = contact.email,
                message = contact.message
            )
        }
    }
}
