package com.hbd.book_be.batch

import org.slf4j.LoggerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class BatchJobService(
    private val jobLauncher: JobLauncher,
    private val jobExplorer: JobExplorer,
    private val addNewPublishedBookJob: Job,
) {
    private val log = LoggerFactory.getLogger(BatchJobService::class.java)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd

    fun runDailyAddNewPublishedBookJob(
        targetDate: LocalDate,
        forceExecution: Boolean = false
    ): JobResult {
        log.info("Scheduled run: Checking for addNewPublishedBookJob...")
        val jobName = addNewPublishedBookJob.name

        if (!forceExecution && hasJobRunSuccessfullyForDate(jobName, targetDate)) {
            return JobResult(
                jobId = null,
                createTime = null,
                endTime = null,
                batchStatus = "skipped",
                parameters = null,
                executionContext = null
            )
        }

        log.info("Scheduled run: Launching $jobName for date: $targetDate")
        try {
            val jobParameters = JobParametersBuilder()
                .addString("targetDate", targetDate.format(dateFormatter))
                .addLong("scheduledRunTimestamp", System.currentTimeMillis())
                .toJobParameters()
            val jobExecution = jobLauncher.run(addNewPublishedBookJob, jobParameters)
            log.info("Scheduled $jobName launched successfully for date: $targetDate")
            val executionContext = mergeStepExecutionContext(jobExecution.stepExecutions)
            val parameters = mapOf(
                "targetDate" to jobExecution.jobParameters.getString("targetDate"),
            )

            return JobResult(
                jobId = jobExecution.jobId,
                createTime = jobExecution.createTime,
                endTime = jobExecution.endTime,
                batchStatus = jobExecution.status.name,
                parameters = parameters,
                executionContext = executionContext,
            )
        } catch (e: Exception) {
            log.error("Failed to launch scheduled $jobName for date: $targetDate", e)
            return JobResult(
                jobId = null,
                createTime = null,
                endTime = null,
                batchStatus = "failed",
                parameters = null,
                executionContext = null
            )
        }
    }


    private fun hasJobRunSuccessfullyForDate(jobName: String, targetDate: LocalDate): Boolean {
        val targetDateStr = targetDate.format(dateFormatter)
        val jobInstanceCount = jobExplorer.getJobInstanceCount(jobName)
        if (jobInstanceCount == 0L) return false

        val jobInstances = jobExplorer.findJobInstancesByJobName(jobName, 0, jobInstanceCount.toInt())

        for (jobInstance in jobInstances) {
            val jobExecutions = jobExplorer.getJobExecutions(jobInstance)
            for (jobExecution in jobExecutions) {
                val jobParameters = jobExecution.jobParameters
                val targetDateParam = jobParameters.getString("targetDate")
                if (jobExecution.status == BatchStatus.COMPLETED && targetDateParam == targetDateStr) {
                    log.info("Job '$jobName' has already run successfully for date $targetDateStr (JobExecutionId: ${jobExecution.id}). Skipping.")
                    return true
                }
            }
        }
        return false
    }

    private fun mergeStepExecutionContext(stepExecutionList: MutableCollection<StepExecution>): Map<String, Any?> {
        val merged = mutableMapOf<String, Any?>()

        stepExecutionList.forEach {
            merged.putAll(it.executionContext.toMap())
        }

        return merged
    }
}