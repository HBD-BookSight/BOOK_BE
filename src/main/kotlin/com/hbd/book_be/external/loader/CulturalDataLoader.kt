package com.hbd.book_be.external.loader

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hbd.book_be.config.properties.ExternalLoaderProperties
import com.hbd.book_be.external.kakao.KakaoBookSearchClient
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.exception.ErrorCodes
import com.hbd.book_be.exception.ValidationException
import com.hbd.book_be.external.kakao.KakaoApiRequest
import com.hbd.book_be.external.loader.dto.BookEnrichmentSnapshot
import com.hbd.book_be.external.loader.dto.CulturalBookDto
import com.hbd.book_be.util.DateUtil
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

@Component
class CulturalDatasetLoader(
    jdbcTemplate: JdbcTemplate,
    private val kakaoBookSearchClient: KakaoBookSearchClient,
    private val loaderProperties: ExternalLoaderProperties

) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(CulturalDatasetLoader::class.java)

    private val jdbcRepository = BookJdbcRepository(jdbcTemplate)
    private val mapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        enable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        enable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    }

    private val outputPath = Paths.get(loaderProperties.outputPath)
    private val snapshotPath = Paths.get(loaderProperties.snapshotPath)


    override fun run(vararg args: String?) {
        if (!loaderProperties.enabled) {
            return
        }

        val finalRequests = enrichAndSaveRequests()

        finalRequests.chunked(loaderProperties.batchSize).forEachIndexed { idx, chunk ->
            try {
                jdbcRepository.saveBooksWithJdbc(chunk)
                log.info("[✅] ${idx + 1}번째 청크 저장 성공 (${chunk.size}권)")
            } catch (e: Exception) {
                log.info("[❌] ${idx + 1}번째 청크 저장 실패: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun enrichAndSaveRequests(): List<BookCreateRequest> {
        val requests = parseToRequests(loadCsvData())
        val snapshots = loadSnapshot()
        val enrichedTitles = snapshots.filter { it.enriched }.map { it.title }.toSet()

        log.info("[ℹ️] ${enrichedTitles.size}권은 enrich 완료된 상태입니다. 이어서 enrich합니다.")
        val updatedSnapshots = snapshots.toMutableList()
        val enrichedRequests = mutableListOf<BookCreateRequest>()

        requests.forEach { request ->
            if (enrichedTitles.contains(request.title)) {
                enrichedRequests.add(request)
            } else {
                val enrichedRequest = enrichBookRequest(request)
                enrichedRequests.add(enrichedRequest)
                appendRequestToJson(enrichedRequest)
                updatedSnapshots.add(BookEnrichmentSnapshot(request.title, true))
                saveSnapshot(updatedSnapshots)
            }
        }

        return enrichedRequests
    }

    private fun enrichBookRequest(request: BookCreateRequest): BookCreateRequest {
        val isIsbnSearchable = request.isbn != "UNKNOWN" && request.isbn.isNotBlank()

        val kakaoRequest = KakaoApiRequest(
            query = if (isIsbnSearchable) request.isbn else request.title,
            target = if (isIsbnSearchable) "isbn" else "title",
            size = 1
        )

        val kakaoResponse = kakaoBookSearchClient.searchBook(kakaoRequest)
        val doc = kakaoResponse?.documents?.firstOrNull()
        val publishedDate = doc?.datetime
            ?.takeIf { it.length >= 10 }
            ?.substring(0, 10)
            ?.let { DateUtil.parseFlexibleDate(it) }
            ?: DateUtil.parseFlexibleDate("0001-01-01") // 안전 디폴트 값

        return if (doc != null) {
            request.copy(
                summary = doc.contents,
                publishedDate = publishedDate,
                detailUrl = doc.url,
                translator = doc.translators,
                price = doc.price,
                titleImage = doc.thumbnail,
                authorNameList = doc.authors,
                publisherName = doc.publisher
            )
        } else {
            log.info("[⚠️] '${request.title} ${request.isbn}' enrich 실패 (검색 결과 없음)")
            request
        }
    }

    private fun appendRequestToJson(newRequest: BookCreateRequest) {
        val existingRequests = if (Files.exists(outputPath)) {
            val json = Files.readString(outputPath)
            mapper.readValue(json, object : TypeReference<List<BookCreateRequest>>() {})
        } else {
            emptyList()
        }

        val updatedRequests = existingRequests + newRequest
        val jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(updatedRequests)
        Files.createDirectories(outputPath.parent)
        Files.writeString(outputPath, jsonString)
    }


    private fun loadCsvData(): List<CulturalBookDto> {
        val csvMapper = CsvMapper()
        val schema = CsvSchema.emptySchema().withHeader()
        javaClass.getResourceAsStream("/dataset/dataset.csv")?.use { inputStream ->
            val reader = csvMapper.readerFor(CulturalBookDto::class.java).with(schema)
            return reader.readValues<CulturalBookDto>(inputStream).readAll()
        } ?: throw ValidationException(
            message = "CSV 파일을 찾을 수 없습니다.",
            errorCode = ErrorCodes.CSV_FILE_NOT_FOUND
        )
    }

    private fun parseToRequests(dataList: List<CulturalBookDto>): List<BookCreateRequest> {
        return dataList.mapNotNull { dto ->
            try {
                val (authors, translators) = parseContributors(dto.authrNm)
                BookCreateRequest(
                    isbn = dto.isbnThirteenNo ?: dto.isbnNo ?: "UNKNOWN",
                    title = dto.titleNm ?: "제목 없음",
                    summary = dto.bookIntrcnCn.orEmpty(),
                    publishedDate = DateUtil.parseFlexibleDate(
                        (dto.pblicteDe ?: dto.twoPblicteDe).takeIf { !it.isNullOrBlank() } ?: "1001-01-01"
                    ),
                    detailUrl = null,
                    translator = translators,
                    price = dto.prcValue?.toIntOrNull(),
                    titleImage = dto.imageUrl,
                    authorNameList = authors,
                    publisherName = dto.publisherNm ?: "알 수 없음"
                )
            } catch (e: Exception) {
                log.info("[⚠️] 파싱 실패: ${dto.titleNm} (${e.message})")
                null
            }
        }
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

    private fun loadSnapshot(): List<BookEnrichmentSnapshot> {
        return if (Files.exists(snapshotPath)) {
            val json = Files.readString(snapshotPath)
            mapper.readValue(
                json,
                mapper.typeFactory.constructCollectionType(List::class.java, BookEnrichmentSnapshot::class.java)
            )
        } else {
            emptyList()
        }
    }

    private fun saveSnapshot(snapshots: List<BookEnrichmentSnapshot>) {
        val jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshots)
        Files.createDirectories(snapshotPath.parent)
        Files.writeString(snapshotPath, jsonString)
    }
}
