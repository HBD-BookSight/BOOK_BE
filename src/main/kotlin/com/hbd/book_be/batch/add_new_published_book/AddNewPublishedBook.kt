package com.hbd.book_be.batch.add_new_published_book

import com.hbd.book_be.external.kakao.KakaoApiResponse
import com.hbd.book_be.external.kakao.KakaoBookSearchClient
import com.hbd.book_be.external.national_library.NationalLibraryClient
import com.hbd.book_be.external.national_library.dto.NationalLibraryBook
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.service.BookService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate

@Configuration
@EnableBatchProcessing
class AddNewPublishedBook(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    companion object {
        private const val JOB_NAME = "addNewPublishedBookJob"
        private const val STEP_NAME = "addNewPublishedBookStep"
        private const val CHUNK_SIZE = 10
    }

    @Bean
    fun addNewPublishedBookJob(
        addNewPublishedBookStep: Step
    ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(addNewPublishedBookStep)
            .build()
    }


    @Bean
    @JobScope
    fun addNewPublishedBookStep(
        addNewPublishedBookProcessor: AddNewPublishedBookProcessor,
        addNewPublishedBookReader: AddNewPublishedBookReader,
        addNewPublishedBookWriter: AddNewPublishedBookWriter,
    ): Step {
        return StepBuilder(STEP_NAME, jobRepository)
            .chunk<NationalLibraryBook, KakaoApiResponse.Document>(CHUNK_SIZE, transactionManager)
            .reader(addNewPublishedBookReader)
            .listener(addNewPublishedBookReader)
            .processor(addNewPublishedBookProcessor)
            .listener(addNewPublishedBookProcessor)
            .writer(addNewPublishedBookWriter)
            .listener(addNewPublishedBookWriter)
            .build()
    }


    @Bean
    @JobScope
    fun addNewPublishedBookReader(
        nationalLibraryClient: NationalLibraryClient,
        @Value("#{jobParameters['targetDate']}") targetDate: String?
    ): AddNewPublishedBookReader {
        val publishedDate = if (targetDate != null) {
            LocalDate.parse(targetDate)
        } else {
            LocalDate.now().minusDays(1) // default TargetDate
        }

        return AddNewPublishedBookReader(
            client = nationalLibraryClient,
            publishedDate = publishedDate
        )
    }


    @Bean
    @JobScope
    fun addNewPublishedBookProcessor(
        kakaoClient: KakaoBookSearchClient
    ): AddNewPublishedBookProcessor {
        return AddNewPublishedBookProcessor(
            kakaoClient = kakaoClient
        )
    }


    @Bean
    @JobScope
    fun addNewPublishedBookWriter(
        bookRepository: BookRepository,
        bookService: BookService
    ): AddNewPublishedBookWriter {
        return AddNewPublishedBookWriter(
            bookRepository = bookRepository,
            bookService = bookService
        )
    }

}