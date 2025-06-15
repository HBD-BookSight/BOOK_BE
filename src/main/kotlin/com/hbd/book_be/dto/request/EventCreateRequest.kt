package com.hbd.book_be.dto.request

import com.hbd.book_be.domain.common.UrlInfo
import com.hbd.book_be.enums.EventFlag
import com.hbd.book_be.enums.EventLocation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class EventCreateRequest(
    @field:Parameter(
        description = "이벤트 제목",
        required = true,
        schema = Schema(
            type = "string",
            example = "2024 북 페어"
        )
    )
    val title: String,
    
    @field:Parameter(
        description = "이벤트 관련 URL 목록",
        required = true
    )
    @field:ArraySchema(
        schema = Schema(implementation = UrlInfo::class),
        arraySchema = Schema(
            type = "array",
            defaultValue = "[]"
        )
    )
    val urls: List<UrlInfo>,
    
    @field:Parameter(
        description = "이벤트 주최자",
        required = true,
        schema = Schema(
            type = "string",
            example = "서울시립도서관"
        )
    )
    val host: String,
    
    @field:Parameter(
        description = "사용자 ID",
        required = true,
        schema = Schema(
            type = "integer",
            format = "int64",
            example = "1"
        )
    )
    val userId: Long,
    
    @field:Parameter(
        description = "이벤트 위치",
        required = true
    )
    val location: EventLocation,
    
    @field:Parameter(
        description = "이벤트 시작 날짜",
        required = true,
        schema = Schema(
            type = "string",
            format = "date",
            example = "2024-01-01"
        )
    )
    val startDate: LocalDate,
    
    @field:Parameter(
        description = "이벤트 종료 날짜",
        required = true,
        schema = Schema(
            type = "string",
            format = "date",
            example = "2024-01-07"
        )
    )
    val endDate: LocalDate,
    
    @field:Parameter(
        description = "이벤트 타입",
        required = true,
        schema = Schema(
            type = "string",
            example = "북 페어"
        )
    )
    val eventType: String,
    
    @field:Parameter(
        description = "이벤트 플래그",
        required = true
    )
    val eventFlag: EventFlag,
    
    @field:Parameter(
        description = "게시 여부",
        required = true,
        schema = Schema(
            type = "boolean",
            example = "true"
        )
    )
    val isPosting: Boolean,
    
    @field:Parameter(
        description = "관련 책 제목 (선택 사항)",
        required = false,
        schema = Schema(
            type = "string",
            example = "이방인"
        )
    )
    val bookTitle: String?,
    
    @field:Parameter(
        description = "발신자 이름 (선택 사항)",
        required = false,
        schema = Schema(
            type = "string",
            example = "홍길동"
        )
    )
    val senderName: String?,
    
    @field:Parameter(
        description = "발신자 이메일 (선택 사항)",
        required = false,
        schema = Schema(
            type = "string",
            example = "hong@example.com"
        )
    )
    val senderEmail: String?,
    
    @field:Parameter(
        description = "발신자 메시지 (선택 사항)",
        required = false,
        schema = Schema(
            type = "string",
            example = "이벤트에 대한 문의사항입니다."
        )
    )
    val senderMessage: String?,
    
    @field:Parameter(
        description = "이벤트 메모 (선택 사항)",
        required = false,
        schema = Schema(
            type = "string",
            example = "추가 정보나 메모"
        )
    )
    val memo: String?,

    @field:Parameter(
        description = "이벤트 태그 목록",
        required = false
    )
    @field:ArraySchema(
        schema = Schema(type = "string"),
        arraySchema = Schema(
            type = "array",
            defaultValue = "[]",
            example = "[\"북페어\", \"도서관\", \"문화행사\"]"
        )
    )
    val tagList: List<String> = emptyList(),
    
    @field:Parameter(
        description = "관련 책 ISBN 목록",
        required = false
    )
    @field:ArraySchema(
        schema = Schema(type = "string"),
        arraySchema = Schema(
            type = "array",
            defaultValue = "[]",
            example = "[\"9788954429559\", \"9788937460739\"]"
        )
    )
    val bookIsbnList: List<String> = emptyList(),
)