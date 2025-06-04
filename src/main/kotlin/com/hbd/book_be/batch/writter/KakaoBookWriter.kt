package com.hbd.book_be.batch.writter

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

open class KakaoBookWriter(
    private val bookRepository: BookRepository,
    private val bookService: BookService
) : ItemWriter<KakaoApiResponse.Document>, StepExecutionListener {
    private val log: Logger = LoggerFactory.getLogger(KakaoBookWriter::class.java)
    private var writtenBookCount = 0L

    companion object {
        const val WRITTEN_BOOK_COUNT_KEY = "KakaoBookWriter.writtenBookCount"
    }

    @Transactional
    override fun write(chunk: Chunk<out KakaoApiResponse.Document>) {
        val existingIsbnList = bookRepository.findByIsbnIn(chunk.items.mapNotNull { it.isbn }).map { it.isbn }
        val processedIsbnList = mutableSetOf<String>()

        for (document in chunk.items) {
            val primaryIsbn = document.isbn.split(" ").first()

            if (primaryIsbn.isBlank()) {
                log.warn("Skipping book with blank ISBN $document")
                continue
            }

            if (existingIsbnList.contains(document.isbn) || processedIsbnList.contains(document.isbn)) {
                log.info("Book with $document already exists. Skipping.")
                continue
            }

            val offsetDateTime = OffsetDateTime.parse(document.datetime)
            val localDateTime: LocalDateTime = offsetDateTime.toLocalDateTime()

            val createdBook = bookService.createBook(
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
            processedIsbnList.add(createdBook.isbn)
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