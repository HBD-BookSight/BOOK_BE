package com.hbd.book_be.batch.processer

import com.hbd.book_be.external.kakao.KakaoApiRequest
import com.hbd.book_be.external.kakao.KakaoApiResponse
import com.hbd.book_be.external.kakao.KakaoBookSearchClient
import com.hbd.book_be.external.national_library.dto.NationalLibraryBook
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.item.ItemProcessor

open class NationalLibraryToKakaoBookProcessor(
    private val kakaoClient: KakaoBookSearchClient
) : ItemProcessor<NationalLibraryBook, KakaoApiResponse.Document>, StepExecutionListener {
    private val log: Logger = LoggerFactory.getLogger(NationalLibraryToKakaoBookProcessor::class.java)
    private var processedBookCount: Long = 0L

    companion object {
        const val PROCESSED_BOOK_COUNT_KEY = "NationalLibraryToKakaoBookProcessor.processedBookCount"
    }

    override fun process(item: NationalLibraryBook): KakaoApiResponse.Document? {
        if (item.eaIsbn == null) {
            return null
        }

        val response = kakaoClient.searchBook(
            KakaoApiRequest(
                query = item.eaIsbn.toString(),
                target = "isbn"
            )
        ) ?: return null

        if (response.documents.isEmpty()) {
            log.debug("National Library Book(title=${item.title}, isbn=${item.eaIsbn}) has no kakao book")
            return null
        }

        if (response.documents.size > 1) {
            log.warn(
                "National Library Book(title=${item.title}, isbn=${item.eaIsbn}) has more than one kakao book(search result=${response.documents.size})\n " +
                        "Only return first matched one(title=${response.documents[0].title}, isbn=${response.documents[0].isbn})"
            )
        }

        processedBookCount++
        return response.documents.first()
    }

    override fun beforeStep(stepExecution: StepExecution) {
        log.info("Start NationalLibraryToKakaoBookProcessor")
        processedBookCount = 0
        super.beforeStep(stepExecution)
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        log.info("Finished NationalLibraryToKakaoBookProcessor. $PROCESSED_BOOK_COUNT_KEY=$processedBookCount")
        stepExecution.executionContext.putLong(PROCESSED_BOOK_COUNT_KEY, processedBookCount)
        return super.afterStep(stepExecution)
    }

}