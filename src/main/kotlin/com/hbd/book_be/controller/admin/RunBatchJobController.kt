package com.hbd.book_be.controller.admin

import com.hbd.book_be.batch.BatchJobService
import com.hbd.book_be.batch.JobResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(name = "Admin - Batch Job API", description = "관리자 - 배치 작업 실행 API")
@RestController
@RequestMapping("/admin/v1/run-batch-job")
class RunBatchJobController(
    private val batchJobService: BatchJobService
) {

    @Operation(
        summary = "새로 출간된 책 추가 배치 작업 실행",
        description = "지정된 날짜의 새로 출간된 책을 추가하는 배치 작업을 실행합니다."
    )
    @PostMapping("/add-new-published-book")
    @RequireAdminRole
    fun runAddNewPublishedBook(
        @Parameter(description = "배치 작업 대상 날짜", required = true, example = "2024-01-01")
        @RequestParam targetDate: LocalDate,
        @Parameter(description = "강제 실행 여부", required = true, example = "false")
        @RequestParam forceExecution: Boolean
    ): ResponseEntity<JobResult> {
        val result = batchJobService.runDailyAddNewPublishedBookJob(
            targetDate = targetDate,
            forceExecution = forceExecution
        )

        return ResponseEntity.ok(result)
    }

    @Operation(
        summary = "새로 검색된 책 추가 배치 작업 실행",
        description = "지정된 날짜의 새로 검색된 책을 추가하는 배치 작업을 실행합니다."
    )
    @PostMapping("/add-new-searched-book")
    @RequireAdminRole
    fun runAddNewSearchedBook(
        @Parameter(description = "배치 작업 대상 날짜", required = true, example = "2024-01-01")
        @RequestParam targetDate: LocalDate,
        @Parameter(description = "강제 실행 여부", required = true, example = "false")
        @RequestParam forceExecution: Boolean
    ): ResponseEntity<JobResult> {
        val result = batchJobService.runDailyAddNewSearchedBookJob(
            targetDate = targetDate,
            forceExecution = forceExecution
        )

        return ResponseEntity.ok(result)
    }
}