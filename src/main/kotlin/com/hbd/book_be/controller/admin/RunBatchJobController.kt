package com.hbd.book_be.controller.admin

import com.hbd.book_be.batch.BatchJobService
import com.hbd.book_be.batch.JobResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/admin/v1/run-batch-job")
class RunBatchJobController(
    private val batchJobService: BatchJobService
) {

    @PostMapping("/add-new-published-book")
    fun runAddNewPublishedBook(
        targetDate: LocalDate,
        forceExecution: Boolean
    ): ResponseEntity<JobResult> {
        val result = batchJobService.runDailyAddNewPublishedBookJob(
            targetDate = targetDate,
            forceExecution = forceExecution
        )

        return ResponseEntity.ok(result)
    }

    @PostMapping("/add-new-searched-book")
    fun runAddNewSearchedBook(
        targetDate: LocalDate,
        forceExecution: Boolean
    ): ResponseEntity<JobResult> {
        val result = batchJobService.runDailyAddNewSearchedBookJob(
            targetDate = targetDate,
            forceExecution = forceExecution
        )

        return ResponseEntity.ok(result)
    }
}