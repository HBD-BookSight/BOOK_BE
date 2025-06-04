package com.hbd.book_be.batch.reader

import com.hbd.book_be.domain.BookSearchLog
import com.hbd.book_be.external.kakao.KakaoApiRequest
import com.hbd.book_be.external.kakao.KakaoApiResponse
import com.hbd.book_be.external.kakao.KakaoBookSearchClient
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStreamException
import org.springframework.batch.item.ItemStreamReader
import org.springframework.batch.item.database.JpaPagingItemReader

open class BookSearchLogKakaoBookPageStreamReader(
    private val delegateBookSearchLogReader: JpaPagingItemReader<BookSearchLog>,
    private val kakaoClient: KakaoBookSearchClient,
    private val kakaoPageSize: Int = 50,
) : ItemStreamReader<List<KakaoApiResponse.Document>>, StepExecutionListener {

    private val log = LoggerFactory.getLogger(this.javaClass)

    private var logCount = 0L
    private var kakaoBookCount = 0L
    private var currentKeyword: String? = null
    private var currentLogId: Long? = null
    private var currentPage: Int = 1
    private var isCurrentLogEnded: Boolean = true

    companion object {
        private const val LOG_READ_COUNT_KEY = "BookSearchLogKakaoBookPageStreamReader.LogCount"
        private const val KAKAO_BOOK_COUNT_KEY = "BookSearchLogKakaoBookPageStreamReader.KakaoBookCount"

        private const val CONTEXT_CURRENT_PAGE = "BookSearchLogKakaoBookPageStreamReader.currentPage"
        private const val CONTEXT_CURRENT_KEYWORD = "BookSearchLogKakaoBookPageStreamReader.currentKeyword"
        private const val CONTEXT_CURRENT_LOG_ID = "BookSearchLogKakaoBookPageStreamReader.currentLogId"
    }

    @Throws(Exception::class)
    override fun read(): List<KakaoApiResponse.Document>? {
        if (isCurrentLogEnded) {
            loadNextBookSearchLog()
            if (currentKeyword == null) {
                log.info("No more BookSearchLogs to process.")
                return null
            }
        }

        // Keyword should not be null.
        if (currentKeyword.isNullOrBlank()) {
            log.warn("BookSearchLog ID $currentLogId has null or blank keyword. Skipping.")
            isCurrentLogEnded = true // Mark as ended to try read next log
            return read() // Try to read again, will load next log or return null
        }

        log.info("Fetching page $currentPage for keyword: '$currentKeyword'(LogID=${currentLogId})")
        val response = kakaoClient.searchBook(
            KakaoApiRequest(
                query = currentKeyword!!,
                page = currentPage,
                size = kakaoPageSize
            )
        )

        if (response == null) {
            log.error("Kakao API returned null for keyword: '$currentKeyword', page: $currentPage. Marking log as ended.")
            isCurrentLogEnded = true
            return read() // Try to read again, will load next log or return null
        }

        log.info("Kakao API response for keyword '$currentKeyword', page $currentPage, Meta=${response.meta}, Documents count=${response.documents.size}")

        if (response.meta.isEnd || response.documents.isEmpty() || response.documents.size < kakaoPageSize) {
            log.info("Reached end for keyword: '$currentKeyword' (isEnd=${response.meta.isEnd}, docsEmpty=${response.documents.isEmpty()}).")
            isCurrentLogEnded = true
        } else {
            currentPage++
        }

        kakaoBookCount += response.documents.size
        return response.documents
    }

    private fun loadNextBookSearchLog() {
        val currentBookSearchLog = delegateBookSearchLogReader.read()
        if (currentBookSearchLog != null) {
            log.info("Loaded new BookSearchLog: ID=${currentBookSearchLog.id}, Keyword='${currentBookSearchLog.keyword}'")
            currentPage = 1
            currentKeyword = currentBookSearchLog.keyword
            currentLogId = currentBookSearchLog.id
            isCurrentLogEnded = false
        } else {
            isCurrentLogEnded = true
            currentKeyword = null
            currentLogId = null
        }
        logCount++
    }

    @Throws(ItemStreamException::class)
    override fun open(executionContext: ExecutionContext) {
        delegateBookSearchLogReader.open(executionContext)

        if (executionContext.containsKey(CONTEXT_CURRENT_PAGE)) {
            currentPage = executionContext.getInt(CONTEXT_CURRENT_PAGE)
            currentKeyword = executionContext.getString(CONTEXT_CURRENT_KEYWORD)
            currentLogId = executionContext.getLong(CONTEXT_CURRENT_LOG_ID)
            isCurrentLogEnded = false

            log.info("Restored state: currentLogId=${currentLogId}, currentPage=$currentPage, isLogEnded=$isCurrentLogEnded")
        } else {
            isCurrentLogEnded = true
            currentKeyword = null
            currentLogId = null
            currentPage = 1
            log.info("No previous state found. Initializing reader.")
        }
    }

    @Throws(ItemStreamException::class)
    override fun update(executionContext: ExecutionContext) {
        delegateBookSearchLogReader.update(executionContext)

        if (currentKeyword != null) {
            executionContext.putLong(CONTEXT_CURRENT_LOG_ID, currentLogId!!)
            executionContext.putString(CONTEXT_CURRENT_KEYWORD, currentKeyword)
            executionContext.putInt(CONTEXT_CURRENT_PAGE, currentPage)
        } else {
            // Remove previous status
            executionContext.remove(CONTEXT_CURRENT_LOG_ID)
            executionContext.remove(CONTEXT_CURRENT_KEYWORD)
            executionContext.remove(CONTEXT_CURRENT_PAGE)
        }
    }

    @Throws(ItemStreamException::class)
    override fun close() {
        delegateBookSearchLogReader.close()
        log.info("BookSearchLogKakaoBookPageStreamReader closed.")
    }

    override fun beforeStep(stepExecution: StepExecution) {
        log.info("Start BookSearchLogKakaoBookPageStreamReader")
        logCount = 0L
        kakaoBookCount = 0L
        super.beforeStep(stepExecution)
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        log.info("Finished BookSearchLogKakaoBookPageStreamReader")
        stepExecution.executionContext.putLong(LOG_READ_COUNT_KEY, logCount)
        stepExecution.executionContext.putLong(KAKAO_BOOK_COUNT_KEY, kakaoBookCount)
        return super.afterStep(stepExecution)
    }
}