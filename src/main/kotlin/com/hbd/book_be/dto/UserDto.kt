package com.hbd.book_be.dto

import com.hbd.book_be.domain.User

data class UserDto(
    val id: Long,
    val name: String
) {
    companion object {
        fun fromEntity(user: User): UserDto {
            if(user.id == null){
                throw IllegalArgumentException("User id can't be null")
            }
            return UserDto(
                id = user.id!!,
                name = user.name
            )
        }
    }
}