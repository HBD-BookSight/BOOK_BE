package com.hbd.book_be.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hbd.book_be.domain.DiscoveryContents
import com.hbd.book_be.domain.common.UrlInfo
import java.time.LocalDateTime

data class DiscoveryContentsDto(
    val id: Long,
    val urls: List<UrlInfo>,
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
                urls = contents.urls,
                image = contents.image,
                creatorDto = UserDto.fromEntity(contents.creator),
                createdAt = discoveryContents.createdAt
            )
        }
    }
}