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
    }

    private val outputPath = Paths.get(loaderProperties.outputPath)
    private val progressPath = Paths.get(loaderProperties.progressPath)
    private val fileSize = 5000
    private val totalFileCount = 32
    private var currentJsonlChunkIndex = 0

    // ====================================
    // 1. 메인 진입점 (Main Entry Point)
    // ====================================

    override fun run(vararg args: String?) {
        log.info("[🚀] CulturalDatasetLoader 시작")

        initializeDirectories()
        val (startFileIndex, startLineNumber) = loadProgress()
        initializeJsonlChunk()

        if (startFileIndex <= totalFileCount) {
            processAllCsvFiles(startFileIndex, startLineNumber)
        }

        saveJsonlToDatabase()
        log.info("[🎉] 모든 처리 완료!")
    }

    // ====================================
    // 2. 초기화 관련 (Initialization)
    // ====================================

    private fun initializeDirectories() {
        Files.createDirectories(outputPath)
        Files.createDirectories(progressPath.parent)
    }

    private fun initializeJsonlChunk() {
        if (!Files.exists(outputPath)) {
            currentJsonlChunkIndex = 0
            return
        }

        Files.list(outputPath).use { files ->
            val jsonlFiles = files.filter { it.toString().endsWith(".jsonl") }.sorted().toList()
            if (jsonlFiles.isEmpty()) {
                currentJsonlChunkIndex = 0
                return
            }

            val lastFile = jsonlFiles.last()
            val lineCount = Files.lines(lastFile).use { it.count().toInt() }

            currentJsonlChunkIndex = if (lineCount >= fileSize) {
                jsonlFiles.size
            } else {
                jsonlFiles.size - 1
            }
        }
    }

    // ====================================
    // 3. 진행상태 관리 (Progress Management)
    // ====================================

    private fun loadProgress(): Pair<Int, Int> {
        if (!Files.exists(progressPath)) return 1 to 0

        val progressInfo = Files.readString(progressPath).trim()
        val parts = progressInfo.split(":")

        return when (parts.size) {
            2 -> {
                val fileIndex = parts[0].toIntOrNull() ?: 1
                val lineNumber = parts[1].toIntOrNull() ?: 0
                fileIndex to lineNumber
            }

            else -> {
                log.warn("[⚠️] Progress 형식 오류, 처음부터 시작")
                1 to 0
            }
        }
    }

    private fun saveProgress(fileIndex: Int, lineNumber: Int) {
        Files.writeString(progressPath, "$fileIndex:$lineNumber")
    }

    // ====================================
    // 4. CSV 처리 (CSV Processing)
    // ====================================

    private fun processAllCsvFiles(startFileIndex: Int, startLineNumber: Int) {
        for (fileIndex in startFileIndex..totalFileCount) {
            val csvFileName = "dataset-${fileIndex}.csv"
            log.info("[📂] 처리 중: $csvFileName (${fileIndex}/${totalFileCount})")

            val startLineIndex = if (fileIndex == startFileIndex) startLineNumber else 0
            processCsvFile(csvFileName, startLineIndex, fileIndex)

            saveProgress(fileIndex + 1, 0)
            log.info("[✅] $csvFileName 처리 완료")
        }
    }

    private fun processCsvFile(csvFileName: String, startLineIndex: Int, fileIndex: Int) {
        val csvMapper = CsvMapper().apply {
            disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        }
        val schema = CsvSchema.emptySchema().withHeader()

        javaClass.getResourceAsStream("/dataset/$csvFileName")?.use { inputStream ->
            val iterator = csvMapper.readerFor(CulturalBookDto::class.java)
                .with(schema)
                .readValues<CulturalBookDto>(inputStream)

            // 스킵할 라인이 있으면 스킵
            repeat(startLineIndex) {
                if (iterator.hasNext()) {
                    try {
                        iterator.next()
                    } catch (e: Exception) { /* 스킵 중 오류 무시 */
                    }
                }
            }

            var currentLineIndex = startLineIndex
            var successCount = 0
            var skipCount = 0

            while (iterator.hasNext()) {
                try {
                    val dto = iterator.next()
                    currentLineIndex++

                    parseToRequest(dto)?.let { request ->
                        enrichBookRequest(request)?.let { enrichedRequest ->
                            appendToJsonl(enrichedRequest)
                            successCount++
                        } ?: skipCount++
                    }

                    if (currentLineIndex % 100 == 0) {
                        saveProgress(fileIndex, currentLineIndex)
                        log.info("[📊] $csvFileName: ${currentLineIndex}행 처리 - 저장: ${successCount}, 스킵: ${skipCount}")
                    }

                } catch (e: Exception) {
                    log.warn("[⚠️] CSV 행 ${currentLineIndex + 1} 오류: ${e.message}")
                    currentLineIndex++
                }
            }

            log.info("[✅] $csvFileName: 총 저장: ${successCount}, 스킵: ${skipCount}")

        } ?: throw ValidationException("CSV 파일을 찾을 수 없습니다: $csvFileName", ErrorCodes.CSV_FILE_NOT_FOUND)
    }

    // ====================================
    // 5. 데이터 변환 (Data Transformation)
    // ====================================

    private fun parseToRequest(dto: CulturalBookDto): BookCreateRequest? {
        return try {
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
            log.debug("[⚠️] 파싱 실패: ${dto.titleNm} (${e.message})")
            null
        }
    }

    private fun parseContributors(raw: String?): Pair<List<String>, List<String>> {
        val authors = mutableListOf<String>()
        val translators = mutableListOf<String>()

        raw?.split(",")?.forEach { person ->
            val trimmed = person.trim()
            when {
                trimmed.contains("지은이") -> authors.add(trimmed.replace("(지은이)", "").trim())
                trimmed.contains("옮긴이") -> translators.add(trimmed.replace("(옮긴이)", "").trim())
            }
        }

        return authors to translators
    }

    // ====================================
    // 6. 외부 API 연동 (External API Integration)
    // ====================================

    private fun enrichBookRequest(request: BookCreateRequest): BookCreateRequest? {
        val isIsbnSearchable = request.isbn != "UNKNOWN" && request.isbn.isNotBlank()

        val kakaoRequest = KakaoApiRequest(
            query = if (isIsbnSearchable) request.isbn else request.title,
            target = if (isIsbnSearchable) "isbn" else "title",
            size = 1
        )

        val doc = kakaoBookSearchClient.searchBook(kakaoRequest)?.documents?.firstOrNull()
            ?: return null

        val publishedDate = doc.datetime
            ?.takeIf { it.length >= 10 }
            ?.substring(0, 10)
            ?.let { DateUtil.parseFlexibleDate(it) }
            ?: DateUtil.parseFlexibleDate("0001-01-01")

        return request.copy(
            summary = doc.contents,
            publishedDate = publishedDate,
            detailUrl = doc.url,
            translator = doc.translators,
            price = doc.price,
            titleImage = doc.thumbnail,
            authorNameList = doc.authors,
            publisherName = doc.publisher
        )
    }

    // ====================================
    // 7. JSONL 파일 관리 (JSONL File Management)
    // ====================================

    private fun appendToJsonl(request: BookCreateRequest) {
        val currentFile = outputPath.resolve("chunk-${String.format("%03d", currentJsonlChunkIndex)}.jsonl")

        // 현재 파일이 가득 찼는지 확인
        val currentLines = if (Files.exists(currentFile)) {
            Files.lines(currentFile).use { it.count().toInt() }
        } else 0

        if (currentLines >= fileSize) {
            currentJsonlChunkIndex++
        }

        val targetFile = outputPath.resolve("chunk-${String.format("%03d", currentJsonlChunkIndex)}.jsonl")
        BufferedWriter(FileWriter(targetFile.toFile(), true)).use { writer ->
            writer.write(mapper.writeValueAsString(request))
            writer.newLine()
        }
    }

    // ====================================
    // 8. 데이터베이스 저장 (Database Persistence)
    // ====================================

    private fun saveJsonlToDatabase() {
        if (!Files.exists(outputPath)) {
            log.warn("[⚠️] JSONL 디렉토리가 없습니다.")
            return
        }

        Files.list(outputPath).use { files ->
            val jsonlFiles = files.filter { it.toString().endsWith(".jsonl") }.sorted().toList()

            log.info("[📖] 총 ${jsonlFiles.size}개 JSONL 파일에서 스트리밍 DB 저장 시작")
            var totalSaved = 0
            var batch = mutableListOf<BookCreateRequest>()
            var batchIndex = 1

            jsonlFiles.forEach { jsonlFile ->
                Files.lines(jsonlFile).use { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            try {
                                val request = mapper.readValue(line, BookCreateRequest::class.java)
                                batch.add(request)

                                if (batch.size >= loaderProperties.batchSize) {
                                    jdbcRepository.saveBooksWithJdbc(batch)
                                    totalSaved += batch.size
                                    log.info("[✅] ${batchIndex}번째 배치 저장: ${batch.size}권")
                                    batch.clear()
                                    batchIndex++
                                }
                            } catch (e: Exception) {
                                log.warn("[⚠️] JSONL 라인 파싱 실패: $line")
                            }
                        }
                    }
                }
            }

            // 마지막 배치 저장
            if (batch.isNotEmpty()) {
                jdbcRepository.saveBooksWithJdbc(batch)
                totalSaved += batch.size
            }

            log.info("[🎉] DB 저장 완료: 총 ${totalSaved}개")
        }
    }
}