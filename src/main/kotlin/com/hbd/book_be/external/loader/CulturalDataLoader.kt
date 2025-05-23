package com.hbd.book_be.external.loader

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hbd.book_be.config.properties.ExternalLoaderProperties
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.exception.ErrorCodes
import com.hbd.book_be.exception.ValidationException
import com.hbd.book_be.external.kakao.KakaoApiRequest
import com.hbd.book_be.external.kakao.KakaoBookSearchClient
import com.hbd.book_be.external.loader.dto.CulturalBookDto
import com.hbd.book_be.util.DateUtil
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths

@Component
@ConditionalOnProperty(
    prefix = "external.cultural-data-loader",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
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
    private val progressPath = Paths.get(loaderProperties.progressPath ?: "${loaderProperties.outputPath}.progress")
    private val csvChunkSize = 1000

    override fun run(vararg args: String?) {
        log.info("[🚀] CulturalDatasetLoader 시작됨 (external-loader.enabled=true)")

        initializeOutputFiles()
        checkExistingFiles()

        val csvFile = "dataset.csv"
        log.info("[📂] 처리 중: $csvFile")

        // 1단계: JSONL 생성
        val allEnrichedRequests = processCsvFileFromProgress(csvFile)

        // 2단계: DB 저장
        log.info("[💾] JSONL 생성 완료. 이제 DB 저장 시작...")
        saveToDatabase(allEnrichedRequests)

        // 완료 후 진행 상황 초기화
        clearProgress()
        log.info("[🎉] 모든 처리 완료!")
    }

    private fun checkExistingFiles() {
        // 기존 JSONL 파일 확인
        if (Files.exists(outputPath)) {
            val existingLines = Files.lines(outputPath).use { it.count() }
            log.info("[📋] 기존 JSONL 파일 발견: ${existingLines}줄")
        } else {
            log.info("[📋] 새로운 JSONL 파일 생성 예정")
        }

        // 기존 progress.txt 확인
        if (Files.exists(progressPath)) {
            val progressInfo = Files.readString(progressPath).trim()
            log.info("[📋] 기존 진행 상황 발견: $progressInfo")
        } else {
            log.info("[📋] 처음부터 시작")
        }
    }

    private fun initializeOutputFiles() {
        Files.createDirectories(outputPath.parent)
        Files.createDirectories(progressPath.parent)
    }

    private fun processCsvFileFromProgress(csvFileName: String): List<BookCreateRequest> {
        val startChunkIndex = loadProgress(csvFileName)
        val totalRows = getTotalCsvRows(csvFileName)
        val totalChunks = (totalRows + csvChunkSize - 1) / csvChunkSize
        val skipRows = startChunkIndex * csvChunkSize

        log.info("[📊] $csvFileName: 총 ${totalRows}행, ${totalChunks}청크 중 ${startChunkIndex}번째부터 시작")
        log.info("[⏭️] ${skipRows}행 스킵하고 ${skipRows + 1}행부터 처리 시작")

        val csvMapper = CsvMapper()
        val schema = CsvSchema.emptySchema().withHeader()
        val allEnrichedRequests = mutableListOf<BookCreateRequest>()

        javaClass.getResourceAsStream("/dataset/$csvFileName")?.use { inputStream ->
            val reader = csvMapper.readerFor(CulturalBookDto::class.java).with(schema)
            val iterator = reader.readValues<CulturalBookDto>(inputStream)

            // 이미 처리된 행들 스킵
            log.info("[⏩] ${skipRows}행 스킵 중...")
            var skippedCount = 0
            repeat(skipRows) {
                if (iterator.hasNext()) {
                    iterator.next()
                    skippedCount++
                    if (skippedCount % 10000 == 0) {
                        log.info("[⏩] ${skippedCount}/${skipRows}행 스킵 완료...")
                    }
                }
            }
            log.info("[✅] 스킵 완료. 이제 ${skipRows + 1}행부터 처리 시작")

            var currentChunkIndex = startChunkIndex
            var chunk = mutableListOf<CulturalBookDto>()

            while (iterator.hasNext()) {
                val dto = iterator.next()
                chunk.add(dto)

                if (chunk.size >= csvChunkSize) {
                    val enrichedRequests = processChunk(chunk, currentChunkIndex)
                    allEnrichedRequests.addAll(enrichedRequests)
                    chunk.clear()
                    currentChunkIndex++

                    // 진행 상황 저장
                    saveProgress(csvFileName, currentChunkIndex)

                    val processedRows = currentChunkIndex * csvChunkSize
                    val progress = (processedRows.toDouble() / totalRows * 100).toInt()
                    log.info("[📊] $csvFileName: ${processedRows}/${totalRows}행 처리 완료 (${progress}%) - 청크 #${currentChunkIndex}")
                }
            }

            // 마지막 청크 처리
            if (chunk.isNotEmpty()) {
                val enrichedRequests = processChunk(chunk, currentChunkIndex)
                allEnrichedRequests.addAll(enrichedRequests)
                currentChunkIndex++
                saveProgress(csvFileName, currentChunkIndex)
                log.info("[✅] $csvFileName: JSONL 생성 완료")
            }
        } ?: throw ValidationException(
            "CSV 파일을 찾을 수 없습니다: $csvFileName",
            ErrorCodes.CSV_FILE_NOT_FOUND
        )

        return allEnrichedRequests
    }

    private fun getTotalCsvRows(csvFileName: String): Int {
        val csvMapper = CsvMapper()
        val schema = CsvSchema.emptySchema().withHeader()

        javaClass.getResourceAsStream("/dataset/$csvFileName")?.use { inputStream ->
            val reader = csvMapper.readerFor(CulturalBookDto::class.java).with(schema)
            val iterator = reader.readValues<CulturalBookDto>(inputStream)

            var count = 0
            while (iterator.hasNext()) {
                iterator.next()
                count++
            }
            return count
        } ?: throw ValidationException(
            "CSV 파일을 찾을 수 없습니다: $csvFileName",
            ErrorCodes.CSV_FILE_NOT_FOUND
        )
    }

    private fun processChunk(chunk: List<CulturalBookDto>, chunkIndex: Int): List<BookCreateRequest> {
        val requests = parseToRequests(chunk)
        val enrichedRequests = mutableListOf<BookCreateRequest>()

        requests.forEach { request ->
            val enrichedRequest = enrichBookRequest(request)
            enrichedRequests.add(enrichedRequest)
        }

        // JSONL 형태로 저장 (DB 저장은 나중에)
        appendRequestsToJsonl(enrichedRequests)

        return enrichedRequests
    }

    private fun appendRequestsToJsonl(requests: List<BookCreateRequest>) {
        BufferedWriter(FileWriter(outputPath.toFile(), true)).use { writer ->
            requests.forEach { request ->
                val jsonString = mapper.writeValueAsString(request)
                writer.write(jsonString)
                writer.write("\n")
            }
        }
    }

    // JSONL 생성 완료 후 DB에 저장
    private fun saveToDatabase(allEnrichedRequests: List<BookCreateRequest>) {
        log.info("[💾] 총 ${allEnrichedRequests.size}개 데이터를 DB에 저장 시작...")

        allEnrichedRequests.chunked(loaderProperties.batchSize).forEachIndexed { idx, chunk ->
            try {
                jdbcRepository.saveBooksWithJdbc(chunk)
                log.info("[✅] ${idx + 1}번째 DB 청크 저장 성공 (${chunk.size}권)")
            } catch (e: Exception) {
                log.error("[❌] ${idx + 1}번째 DB 청크 저장 실패: ${e.message}", e)
                throw e // 실패 시 중단
            }
        }

        log.info("[🎉] DB 저장 완료: ${allEnrichedRequests.size}개 데이터")
    }

    // 진행 상황 저장: "파일명:청크인덱스" 형태
    private fun saveProgress(csvFileName: String, chunkIndex: Int) {
        val progressInfo = "$csvFileName:$chunkIndex"
        Files.writeString(progressPath, progressInfo)
    }

    // 진행 상황 로드
    private fun loadProgress(csvFileName: String): Int {
        return if (Files.exists(progressPath)) {
            val progressInfo = Files.readString(progressPath).trim()
            val parts = progressInfo.split(":")
            if (parts.size == 2 && parts[0] == csvFileName) {
                parts[1].toIntOrNull() ?: 0
            } else {
                0 // 다른 파일이거나 형식이 맞지 않으면 처음부터
            }
        } else {
            0
        }
    }

    // 처리 완료 후 진행 상황 파일 삭제
    private fun clearProgress() {
        if (Files.exists(progressPath)) {
            Files.delete(progressPath)
        }
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
            ?: DateUtil.parseFlexibleDate("0001-01-01")

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
}