package com.hbd.book_be.batch

import com.hbd.book_be.batch.reader.BookSearchLogKakaoBookPageStreamReader
import com.hbd.book_be.batch.writter.FlatteningItemWriter
import com.hbd.book_be.batch.writter.KakaoBookWriter
import com.hbd.book_be.domain.BookSearchLog
import com.hbd.book_be.external.kakao.KakaoApiResponse
import com.hbd.book_be.external.kakao.KakaoBookSearchClient
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.service.BookService
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate
import java.time.LocalTime

@Configuration
@EnableBatchProcessing
class AddNewSearchedBook(
    private val entityManagerFactory: EntityManagerFactory,
    private val transactionManager: PlatformTransactionManager,
    private val jobRepository: JobRepository,
) {

    companion object {
        private const val JOB_NAME = "addSearchedBookJob"
        private const val STEP_NAME = "addSearchedBookStep"
        private const val CHUNK_SIZE = 1 // real chunk size is CHUNK_SIZE * KAKAO_PAGE_SIZE

        private const val KAKAO_PAGE_SIZE = 50
        private const val READ_PAGE_SIZE = 10
    }

    @Bean
    fun addNewSearchedBookJob(
        addNewSearchedBookStep: Step
    ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(addNewSearchedBookStep)
            .build()
    }

    @Bean
    @JobScope
    fun addNewSearchedBookStep(
        reader: BookSearchLogKakaoBookPageStreamReader,
        writer: FlatteningItemWriter<KakaoApiResponse.Document>
    ): Step {
        return StepBuilder(STEP_NAME, jobRepository)
            .chunk<List<KakaoApiResponse.Document>, List<KakaoApiResponse.Document>>(CHUNK_SIZE, transactionManager)
            .reader(reader)
            .listener(reader)
            .writer(writer)
            .listener(writer)
            .build()
    }


    @Bean
    @JobScope
    fun bookSearchLogJpaReader(
        @Value("#{jobParameters['targetDate']}") targetDate: String?
    ): JpaPagingItemReader<BookSearchLog> {
        val searchDate = if (targetDate != null) {
            LocalDate.parse(targetDate)
        } else {
            LocalDate.now().minusDays(1) // default TargetDate
        }

        val startDateTime = searchDate.atStartOfDay()
        val endDateTime = searchDate.atTime(LocalTime.MAX)
        val parameterValues = mapOf<String, Any>(
            "startDateTime" to startDateTime,
            "endDateTime" to endDateTime
        )

        return JpaPagingItemReaderBuilder<BookSearchLog>()
            .name("bookSearchLogJpaReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(READ_PAGE_SIZE)
            .queryString(
                "SELECT log " +
                        "FROM BookSearchLog log " +
                        "WHERE log.searchDateTime between :startDateTime AND :endDateTime " +
                        "ORDER BY log.id ASC"
            )
            .parameterValues(parameterValues)
            .build()
    }

    @Bean
    @JobScope
    fun bookSearchLogKakaoBookPageStreamReader(
        bookSearchLogJpaReader: JpaPagingItemReader<BookSearchLog>,
        kakaoClient: KakaoBookSearchClient
    ): BookSearchLogKakaoBookPageStreamReader {
        return BookSearchLogKakaoBookPageStreamReader(
            delegateBookSearchLogReader = bookSearchLogJpaReader,
            kakaoClient = kakaoClient,
            kakaoPageSize = KAKAO_PAGE_SIZE
        )
    }

    @Bean
    @JobScope
    fun addNewSearchedBookWriter(
        bookRepository: BookRepository,
        bookService: BookService,
    ): FlatteningItemWriter<KakaoApiResponse.Document> {
        val kakaoBookWriter = KakaoBookWriter(
            bookRepository = bookRepository,
            bookService = bookService
        )

        return FlatteningItemWriter(
            itemWriter = kakaoBookWriter
        )
    }
}