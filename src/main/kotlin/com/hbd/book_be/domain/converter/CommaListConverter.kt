package com.hbd.book_be.domain.converter

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.slf4j.LoggerFactory

@Converter
class CommaListConverter : AttributeConverter<List<String>, String> {
    private val objectMapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(CommaListConverter::class.java)

    override fun convertToDatabaseColumn(attribute: List<String>?): String? {
        return if (attribute.isNullOrEmpty()) {
            null
        } else {
            // 쉼표로 구분된 문자열로 저장
            attribute.joinToString(",")
        }
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        return when {
            dbData.isNullOrBlank() -> emptyList()
            dbData.startsWith("[") -> {
                // 기존 JSON 형태 데이터 처리 (하위 호환성)
                try {
                    objectMapper.readValue(dbData, object : TypeReference<List<String>>() {})
                } catch (e: JsonParseException) {
                    logger.warn("Failed to parse JSON array: $dbData", e)
                    emptyList()
                }
            }
            else -> {
                // 쉼표로 구분된 문자열 처리
                dbData.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
        }
    }
}
