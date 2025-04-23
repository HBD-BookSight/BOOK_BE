package com.hbd.book_be.domain.converter.base

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
open class ListConverter<T> : AttributeConverter<List<T>, String> {
    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<T>?): String? {
        return if (attribute.isNullOrEmpty()) {
            null
        } else {
            objectMapper.writeValueAsString(attribute)
        }
    }

    override fun convertToEntityAttribute(dbData: String?): List<T> {
        return if (dbData.isNullOrBlank() || dbData == "[]") {
            emptyList()
        } else {
            objectMapper.readValue(dbData, object : TypeReference<List<T>>() {})
        }
    }
}