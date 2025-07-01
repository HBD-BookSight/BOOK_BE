package com.hbd.book_be.dto.request

import com.hbd.book_be.domain.common.UrlInfo
import com.hbd.book_be.enums.EventFlag
import com.hbd.book_be.enums.EventLocation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class EventUpdateRequest(
    @field:Schema(
        description = "이벤트 제목",
        required = false,
        example = "2024 북 페어"
    )
    val title: String?,
    
    @field:ArraySchema(
        schema = Schema(
            description = "이벤트 관련 URL 목록",
            required = false,
            implementation = UrlInfo::class
        ),
        arraySchema = Schema(
            type = "array"
        )
    )
    val urls: List<UrlInfo>?,
    
    @field:Schema(
        description = "이벤트 주최자",
        required = false,
        example = "서울시립도서관"
    )
    val host: String?,
    
    @field:Schema(
        description = "이벤트 위치",
        required = false
    )
    val location: EventLocation?,
    
    @field:Schema(
        description = "이벤트 시작 날짜",
        required = false,
        format = "date",
        example = "2024-01-01"
    )
    val startDate: LocalDate?,
    
    @field:Schema(
        description = "이벤트 종료 날짜",
        required = false,
        format = "date",
        example = "2024-01-07"
    )
    val endDate: LocalDate?,
    
    @field:Schema(
        description = "이벤트 타입",
        required = false,
        example = "북 페어"
    )
    val eventType: String?,
    
    @field:Schema(
        description = "이벤트 플래그",
        required = false
    )
    val eventFlag: EventFlag?,
    
    @field:Schema(
        description = "게시 여부",
        required = false,
        example = "true"
    )
    val isPosting: Boolean?,
    
    @field:Schema(
        description = "관련 책 제목 (선택 사항)",
        required = false,
        example = "이방인"
    )
    val bookTitle: String?,
    
    @field:Schema(
        description = "발신자 이름 (선택 사항)",
        required = false,
        example = "홍길동"
    )
    val senderName: String?,
    
    @field:Schema(
        description = "발신자 이메일 (선택 사항)",
        required = false,
        example = "hong@example.com"
    )
    val senderEmail: String?,
    
    @field:Schema(
        description = "발신자 메시지 (선택 사항)",
        required = false,
        example = "이벤트에 대한 문의사항입니다."
    )
    val senderMessage: String?,
    
    @field:Schema(
        description = "이벤트 메모 (선택 사항)",
        required = false,
        example = "추가 정보나 메모"
    )
    val memo: String?,

    @field:ArraySchema(
        schema = Schema(
            description = "이벤트 태그 목록",
            required = false,
            type = "string"
        ),
        arraySchema = Schema(
            type = "array",
            example = "[\"북페어\", \"도서관\", \"문화행사\"]"
        )
    )
    val tagList: List<String>?,
    
    @field:ArraySchema(
        schema = Schema(
            description = "관련 책 ISBN 목록",
            required = false,
            type = "string"
        ),
        arraySchema = Schema(
            type = "array",
            example = "[\"9788954429559\", \"9788937460739\"]"
        )
    )
    val bookIsbnList: List<String>?
)
