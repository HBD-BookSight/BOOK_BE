package com.hbd.book_be.loader

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.loader.dto.CulturalBookDto
import com.hbd.book_be.util.DateUtil
import kotlinx.coroutines.*
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class CulturalDatasetLoader(
    private val dataLoaderService: DataLoaderService
) : CommandLineRunner {

    override fun run(vararg args: String?) = runBlocking {
        println("🚀 CulturalDatasetLoader 시작됨")

        val dataList = loadCsvData()
        val requests = parseToRequests(dataList)

        println("📦 파싱 완료: ${requests.size}권")

        // 10000개씩 나눠서 저장 요청
        requests.chunked(10000).forEachIndexed { idx, chunk ->
            try {
                dataLoaderService.saveBooksWithJdbc(chunk)
                println("✅ ${idx + 1}번째 청크 저장 성공 (${chunk.size}권)")
            } catch (e: Exception) {
                println("❌ ${idx + 1}번째 청크 저장 실패: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun loadCsvData(): List<CulturalBookDto> {
        val csvMapper = CsvMapper()
        val schema = CsvSchema.emptySchema().withHeader()
        val inputStream = javaClass.getResourceAsStream("/dataset/dataset.csv")
        val reader = csvMapper.readerFor(CulturalBookDto::class.java).with(schema)
        return reader.readValues<CulturalBookDto>(inputStream).readAll()
    }

    private suspend fun parseToRequests(dataList: List<CulturalBookDto>): List<BookCreateRequest> =
        withContext(Dispatchers.Default) {
            dataList.map { dto ->
                async {
                    try {
                        val rawDate = (dto.pblicteDe ?: dto.twoPblicteDe)?.takeIf { it.isNotBlank() } ?: "1001-01-01"
                        val parsedDate = DateUtil.parseFlexibleDate(rawDate)
                        val (authors, translators) = parseContributors(dto.authrNm)

                        BookCreateRequest(
                            isbn = dto.isbnThirteenNo ?: dto.isbnNo ?: "UNKNOWN",
                            title = dto.titleNm ?: "제목 없음",
                            summary = dto.bookIntrcnCn ?: "",
                            publishedDate = parsedDate,
                            detailUrl = null,
                            translator = translators.joinToString(", "),
                            price = dto.prcValue?.toIntOrNull(),
                            titleImage = dto.imageUrl,
                            authorNameList = authors,
                            publisherName = dto.publisherNm ?: "알 수 없음"
                        )
                    } catch (e: Exception) {
                        println("⚠️ 파싱 실패: ${dto.titleNm} (${e.message})")
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

    private fun parseContributors(raw: String?): Pair<List<String>, List<String>> {
        val authors = mutableListOf<String>()
        val translators = mutableListOf<String>()

        raw?.split(",")?.map { it.trim() }?.forEach { person ->
            when {
                person.contains("지은이") -> authors.add(person.replace("(지은이)", "").trim())
                person.contains("옮긴이") -> translators.add(person.replace("(옮긴이)", "").trim())
            }
        }

        return authors to translators
    }
}