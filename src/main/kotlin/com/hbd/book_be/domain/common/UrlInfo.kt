package com.hbd.book_be.domain.common

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter as _Converter

data class UrlInfo(
    val url: String,
    val type: String
) {

    @_Converter
    class Converter : AttributeConverter<List<UrlInfo>, String> {
        private val objectMapper = jacksonObjectMapper()

        override fun convertToDatabaseColumn(attribute: List<UrlInfo>?): String? {
            return if (attribute.isNullOrEmpty()) {
                null
            } else {
                objectMapper.writeValueAsString(attribute)
            }
        }

        override fun convertToEntityAttribute(dbData: String?): List<UrlInfo> {
            return if (dbData.isNullOrBlank() || dbData == "[]") {
                emptyList()
            } else {
                objectMapper.readValue(dbData, object : TypeReference<List<UrlInfo>>() {})
            }
        }
    }
}

