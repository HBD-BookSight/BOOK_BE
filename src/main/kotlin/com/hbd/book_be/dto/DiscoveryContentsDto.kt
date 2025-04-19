package com.hbd.book_be.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hbd.book_be.domain.DiscoveryContents
import com.hbd.book_be.domain.enums.ContentType
import java.time.LocalDateTime

data class DiscoveryContentsDto(
    val id: Long,
    val type: ContentType,
    val url: String,
    val image: String?,

    @JsonProperty("creator")
    val creatorDto: UserDto,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(discoveryContents: DiscoveryContents): DiscoveryContentsDto {
            val contents = discoveryContents.contents

            return DiscoveryContentsDto(
                id = contents.id!!,
                type = contents.type,
                url = contents.url,
                image = contents.image,
                creatorDto = UserDto.fromEntity(contents.creator),
                createdAt = discoveryContents.createdAt
            )
        }
    }
}