package com.hbd.book_be.batch

import java.time.LocalDateTime

data class JobResult(
    val jobId: Long?,
    val createTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val batchStatus: String,
    val parameters: Map<String, Any?>?,
    val executionContext: Map<String, Any?>?,
)