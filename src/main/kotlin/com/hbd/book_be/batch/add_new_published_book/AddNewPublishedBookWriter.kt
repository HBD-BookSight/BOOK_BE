package com.hbd.book_be.batch.add_new_published_book

import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.external.kakao.KakaoApiResponse
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.service.BookService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.OffsetDateTime

open class AddNewPublishedBookWriter(
    private val bookRepository: BookRepository,
    private val bookService: BookService
) : ItemWriter<KakaoApiResponse.Document>, StepExecutionListener {
    private val log: Logger = LoggerFactory.getLogger(AddNewPublishedBookWriter::class.java)
    private var writtenBookCount = 0L

    companion object {
        const val WRITTEN_BOOK_COUNT_KEY = "writtenBookCount"
    }

    @Transactional
    override fun write(chunk: Chunk<out KakaoApiResponse.Document>) {
        val existingIsbnList = bookRepository.findByIsbnIn(chunk.items.mapNotNull { it.isbn }).map { it.isbn }

        for (document in chunk.items) {
            if (document.isbn.isBlank()) {
                log.warn("Skipping book with blank ISBN: ${document.title}")
                continue
            }
            if (existingIsbnList.contains(document.isbn)) {
                log.info("Book with ISBN ${document.isbn} already exists. Skipping.")
                continue
            }

            val primaryIsbn = document.isbn.split(" ").first()

            val offsetDateTime = OffsetDateTime.parse(document.datetime)
            val localDateTime: LocalDateTime = offsetDateTime.toLocalDateTime()

            bookService.createBook(
                BookCreateRequest(
                    isbn = primaryIsbn,
                    title = document.title,
                    summary = document.contents,
                    publishedDate = localDateTime,
                    detailUrl = document.url,
                    translator = document.translators,
                    price = document.price,
                    titleImage = document.thumbnail,
                    status = document.status,
                    authorNameList = document.authors,
                    publisherName = document.publisher,
                )
            )
            writtenBookCount++
        }
    }

    override fun beforeStep(stepExecution: StepExecution) {
        log.info("Start AddNewPublishedBookWriter")
        writtenBookCount = 0
        super.beforeStep(stepExecution)
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        log.info("Finished AddNewPublishedBookWriter. $WRITTEN_BOOK_COUNT_KEY=$writtenBookCount")
        stepExecution.executionContext.putLong(WRITTEN_BOOK_COUNT_KEY, writtenBookCount)
        return super.afterStep(stepExecution)
    }
}