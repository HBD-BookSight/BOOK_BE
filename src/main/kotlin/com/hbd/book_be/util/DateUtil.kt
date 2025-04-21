package com.hbd.book_be.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtil {

    private val formatters = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyyMMdd"),
        DateTimeFormatter.ofPattern("yyyy.MM.dd")
    )

    /**
     * 문자열 날짜를 다양한 포맷으로 파싱하여 LocalDateTime으로 반환합니다.
     * 파싱에 실패하면 IllegalArgumentException을 던집니다.
     */
    
    fun parseFlexibleDate(dateStr: String?): LocalDateTime {
        if (dateStr.isNullOrBlank()) {
            throw IllegalArgumentException("날짜 문자열이 비어 있습니다.")
        }

        for (formatter in formatters) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter).atStartOfDay()
            } catch (_: DateTimeParseException) {
                // 계속 다음 포맷 시도
            }
        }

        throw IllegalArgumentException("날짜 파싱 실패: 지원되지 않는 형식 [$dateStr]")
    }
}
