package com.hbd.book_be.dto

import com.hbd.book_be.domain.Publisher

data class PublisherDto(
    val id: Long,
    val name: String,
    val logo: String?,
    val isOfficial: Boolean,
    val description: String?,
) {
    companion object {
        fun fromEntity(publisher: Publisher): PublisherDto {
            if (publisher.id == null) {
                throw IllegalArgumentException("Publisher id can not be null.")
            }

            return PublisherDto(
                id = publisher.id!!,
                name = publisher.name,
                logo = publisher.logo,
                isOfficial = publisher.isOfficial,
                description = publisher.description,
            )
        }
    }
}