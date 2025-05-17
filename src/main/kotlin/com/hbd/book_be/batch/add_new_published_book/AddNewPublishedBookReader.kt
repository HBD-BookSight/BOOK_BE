package com.hbd.book_be.batch.add_new_published_book

import com.hbd.book_be.external.national_library.NationalLibraryClient
import com.hbd.book_be.external.national_library.dto.NationalLibraryBook
import com.hbd.book_be.external.national_library.dto.NationalLibraryBookResponse
import com.hbd.book_be.external.national_library.dto.NationalLibrarySearchRequest
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.item.ItemReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

open class AddNewPublishedBookReader(
    private val client: NationalLibraryClient,
    private val publishedDate: LocalDate
) : ItemReader<NationalLibraryBook>, StepExecutionListener {
    private val log = LoggerFactory.getLogger(AddNewPublishedBookReader::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val defaultPageSize = 100L
    private var currentPage = 1L
    private var isEnded = false
    private var currentNLBookList: MutableList<NationalLibraryBook>? = null
    private var readBookCount = 0L

    companion object {
        const val READ_BOOK_COUNT_KEY = "readBookCount"
    }


    override fun read(): NationalLibraryBook? {
        // 현재 bookList가 null인 경우 가장 처음이므로 currentDocs update
        if (currentNLBookList == null) {
            currentNLBookList = fetchNationalLibraryBookList()
        }

        // 현재 bookList가 비어있지 않다면 해당 book을 반환
        if (currentNLBookList!!.isNotEmpty()) {
            readBookCount++
            return currentNLBookList!!.removeFirst()
        }

        // 현재 bookList가 비어있으면서, 끝난 경우는 iteration 종료
        if (isEnded) {
            return null
        }

        // 현재 bookList가 비어있지만, 아직 끝나지 않은 경우 다시 fetch
        currentNLBookList = fetchNationalLibraryBookList()
        if (currentNLBookList!!.isEmpty()) {
            return null
        }

        readBookCount++
        return currentNLBookList!!.removeFirst()
    }

    private fun fetchNationalLibraryBookList(): MutableList<NationalLibraryBook> {
        val request = getRequest()
        val response = client.search(request) ?: throw RuntimeException("failed to request")
        updateIsEnded(response)
        updatePageNo(response)
        log.info(
            "Fetched National Library book list(totalCount=${response.totalCount}, totalPage=${
                ceil((response.totalCount?.toFloat() ?: 0.0F) / defaultPageSize).toLong()
            }, currentPage=${currentPage - 1})"
        )
        return response.docs.toMutableList()
    }

    private fun getRequest(): NationalLibrarySearchRequest {
        val publishDateString = publishedDate.format(dateFormatter)
        return NationalLibrarySearchRequest(
            pageNo = currentPage,
            pageSize = defaultPageSize,
            startPublishDate = publishDateString,
            endPublishDate = publishDateString,
        )
    }

    private fun updateIsEnded(response: NationalLibraryBookResponse) {
        if (response.totalCount == null || response.pageNo == null) {
            throw RuntimeException("failed to request")
        }
        val maxPageNum = ceil(response.totalCount.toFloat() / defaultPageSize).toInt()
        if (maxPageNum <= response.pageNo) {
            isEnded = true
        } else {
            isEnded = false
        }
    }

    private fun updatePageNo(response: NationalLibraryBookResponse) {
        if (response.pageNo == null) {
            currentPage += 1
        } else {
            currentPage = response.pageNo + 1
        }
    }

    override fun beforeStep(stepExecution: StepExecution) {
        log.info("Start AddNewPublishedBookReader(published_date=${publishedDate.format(dateFormatter)})")
        readBookCount = 0
        super.beforeStep(stepExecution)
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        log.info("Finished AddNewPublishedBookReader. $READ_BOOK_COUNT_KEY=$readBookCount")
        stepExecution.executionContext.putLong(READ_BOOK_COUNT_KEY, readBookCount)
        return super.afterStep(stepExecution)
    }
}