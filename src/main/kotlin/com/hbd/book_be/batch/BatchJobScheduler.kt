package com.hbd.book_be.batch

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class BatchJobScheduler(
    private val batchJobService: BatchJobService
) {

    @Scheduled(cron = "0 0 0 * * ?")
    fun batchDailyAddNewPublishedBookJob() {
        batchJobService.runDailyAddNewPublishedBookJob(
            LocalDate.now().minusDays(1), // batch 기준 하루 전날
        )
    }

    @Scheduled(cron = "0 0 0 * * ?")
    fun batchDailyAddSearchedBookJob() {
        batchJobService.runDailyAddNewSearchedBookJob(
            LocalDate.now().minusDays(1), // batch 기준 하루 전날
        )
    }


} 