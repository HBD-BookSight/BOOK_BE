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
        disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    private val outputPath = Paths.get(loaderProperties.outputPath)
    private val progressPath = Paths.get(loaderProperties.progressPath ?: "${loaderProperties.outputPath}.progress")
    private val fileSize = 5000 // 파일당 라인 수 (CSV 예상 + JSONL 저장 단위)
    private val totalFileCount = 32 // dataset-1.csv ~ dataset-32.csv

    // JSONL 청크 관리 변수
    private var currentJsonlChunkIndex = 0

    override fun run(vararg args: String?) {
        log.info("[🚀] CulturalDatasetLoader 시작됨 (external-loader.enabled=true)")

        initializeOutputFiles()
        checkExistingFiles()

        // 진행 상황에서 시작할 파일 인덱스 로드
        val (startFileIndex, startLineNumber) = loadOverallProgress()

        // JSONL 청크 상태 초기화 (기존 JSONL 파일 분석)
        initializeJsonlChunk()

        // 모든 파일이 이미 완료된 경우 체크
        if (startFileIndex > totalFileCount) {
            log.info("[🎉] 모든 CSV 파일이 이미 처리 완료되었습니다. DB 저장만 진행합니다.")
        } else {
            // CSV → JSONL 처리
            processAllCsvFiles(startFileIndex, startLineNumber)
        }

        // JSONL → DB 저장
        log.info("[💾] CSV 처리 완료. 이제 JSONL에서 DB 저장 시작...")
        if (Files.exists(outputPath)) {
            saveJsonlToDatabase()
        } else {
            log.warn("[⚠️] JSONL 파일을 찾을 수 없습니다.")
        }

        log.info("[🎉] 모든 처리 완료!")
    }

    private fun processAllCsvFiles(startFileIndex: Int, startLineNumber: Int) {
        // dataset-1.csv부터 dataset-32.csv까지 순서대로 처리
        for (fileIndex in startFileIndex..totalFileCount) {
            val csvFileName = "dataset-${fileIndex}.csv"
            log.info("[📂] 처리 중: $csvFileName (${fileIndex}/${totalFileCount})")

            try {
                // 첫 번째 재시작 파일인 경우 저장된 라인 번호부터 시작, 그 외에는 0부터 시작
                val startLineIndex = if (fileIndex == startFileIndex) startLineNumber else 0
                processCsvFileFromProgress(csvFileName, startLineIndex)

                log.info("[✅] $csvFileName 처리 완료")

                // 파일 처리 완료 시 다음 파일로 진행 상황 업데이트
                saveOverallProgress(fileIndex + 1, 0)

            } catch (e: Exception) {
                log.error("[❌] $csvFileName 처리 실패: ${e.message}", e)
                throw e
            }
        }
    }

    private fun checkExistingFiles() {
        // 기존 JSONL 파일들 확인
        if (Files.exists(outputPath)) {
            Files.list(outputPath).use { files ->
                val jsonlFiles = files.filter { it.toString().endsWith(".jsonl") }.count()
                log.info("[📋] 기존 JSONL 파일 ${jsonlFiles}개 발견")
            }
        } else {
            log.info("[📋] 새로운 JSONL 디렉토리 생성 예정")
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
        Files.createDirectories(outputPath)
        Files.createDirectories(progressPath.parent)
    }

    private fun initializeJsonlChunk() {
        // 기존 JSONL 파일들을 분석해서 현재 청크 인덱스 결정
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
            val lastChunkIndex = jsonlFiles.size - 1

            if (lineCount >= fileSize) {
                // 현재 청크가 가득 찼으면 다음 청크 시작
                currentJsonlChunkIndex = lastChunkIndex + 1
                log.info("[📖] 마지막 청크가 가득 함, 새 청크 #${currentJsonlChunkIndex} 시작")
            } else {
                // 현재 청크에서 이어쓰기
                currentJsonlChunkIndex = lastChunkIndex
                log.info("[📖] 청크 #${currentJsonlChunkIndex}에서 이어쓰기 (현재 ${lineCount}줄)")
            }
        }
    }

    private fun processCsvFileFromProgress(csvFileName: String, startLineIndex: Int = 0) {
        val totalRows = getTotalCsvRows(csvFileName)

        log.info("[📊] $csvFileName: 총 ${totalRows}행, ${startLineIndex}행부터 시작")
        if (startLineIndex > 0) {
            log.info("[⏭️] ${startLineIndex}행 스킵하고 ${startLineIndex + 1}행부터 처리 시작")
        } else {
            log.info("[🎯] 파일 시작부터 처리")
        }

        val csvMapper = createCsvMapper()
        val schema = CsvSchema.emptySchema().withHeader()

        javaClass.getResourceAsStream("/dataset/$csvFileName")?.use { inputStream ->
            val reader = csvMapper.readerFor(CulturalBookDto::class.java).with(schema)
            val iterator = reader.readValues<CulturalBookDto>(inputStream)

            // 이미 처리된 행들 스킵
            skipCsvRows(iterator, startLineIndex)

            var currentLineIndex = startLineIndex
            val currentFileIndex = csvFileName.removePrefix("dataset-").removeSuffix(".csv").toInt()

            while (iterator.hasNext()) {
                try {
                    val dto = iterator.next()
                    currentLineIndex++

                    // 한 줄씩 즉시 처리 → JSONL 저장
                    val request = parseToRequest(dto)
                    if (request != null) {
                        val enrichedRequest = enrichBookRequest(request)

                        // 즉시 JSONL에 저장 (메모리에 보관하지 않음)
                        appendSingleLineToJsonl(enrichedRequest)
                    }

                    // 100줄마다 Progress 업데이트 및 로그
                    if (currentLineIndex % 100 == 0) {
                        saveOverallProgress(currentFileIndex, currentLineIndex)
                        val progress = (currentLineIndex.toDouble() / totalRows * 100).toInt()
                        log.info("[📊] $csvFileName: ${currentLineIndex}/${totalRows}행 처리 완료 (${progress}%)")
                    }

                } catch (e: Exception) {
                    log.warn("[⚠️] CSV 행 ${currentLineIndex + 1} 파싱 오류 (스킵): ${e.message}")
                    currentLineIndex++
                    continue
                }
            }

            // 최종 Progress 업데이트
            saveOverallProgress(currentFileIndex, currentLineIndex)
            log.info("[✅] $csvFileName: 총 ${currentLineIndex}행 처리 완료")

        } ?: throw ValidationException(
            "CSV 파일을 찾을 수 없습니다: $csvFileName",
            ErrorCodes.CSV_FILE_NOT_FOUND
        )
    }

    private fun getTotalCsvRows(csvFileName: String): Int {
        val csvMapper = createCsvMapper()
        val schema = CsvSchema.emptySchema().withHeader()

        javaClass.getResourceAsStream("/dataset/$csvFileName")?.use { inputStream ->
            val reader = csvMapper.readerFor(CulturalBookDto::class.java).with(schema)
            val iterator = reader.readValues<CulturalBookDto>(inputStream)

            var count = 0
            while (iterator.hasNext()) {
                try {
                    iterator.next()
                    count++
                } catch (e: Exception) {
                    log.warn("[⚠️] $csvFileName 행 ${count + 1} 파싱 오류 (카운트만 증가): ${e.message}")
                    count++
                }
            }
            log.info("[📊] $csvFileName 총 행 수: $count")
            return count
        } ?: throw ValidationException(
            "CSV 파일을 찾을 수 없습니다: $csvFileName",
            ErrorCodes.CSV_FILE_NOT_FOUND
        )
    }

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

    private fun appendSingleLineToJsonl(request: BookCreateRequest) {
        val currentJsonlFile = outputPath.resolve("chunk-${String.format("%03d", currentJsonlChunkIndex)}.jsonl")

        // 현재 파일의 라인 수 확인
        val currentLines = if (Files.exists(currentJsonlFile)) {
            Files.lines(currentJsonlFile).use { it.count().toInt() }
        } else {
            0
        }

        // 5000줄이 되면 다음 파일로
        if (currentLines >= fileSize) {
            currentJsonlChunkIndex++
            log.info("[📄] 새로운 JSONL 청크 시작: chunk-${String.format("%03d", currentJsonlChunkIndex)}.jsonl")
        }

        // 한 줄 즉시 저장
        val targetJsonlFile = outputPath.resolve("chunk-${String.format("%03d", currentJsonlChunkIndex)}.jsonl")
        BufferedWriter(FileWriter(targetJsonlFile.toFile(), true)).use { writer ->
            writer.write(mapper.writeValueAsString(request))
            writer.write("\n")
        }
    }

    // JSONL 파일들을 스트리밍으로 읽어서 청크 단위로 DB 저장
    private fun saveJsonlToDatabase() {
        Files.list(outputPath).use { files ->
            val jsonlFiles = files.filter { it.toString().endsWith(".jsonl") }.sorted().toList()

            log.info("[📖] 총 ${jsonlFiles.size}개 JSONL 파일에서 스트리밍 DB 저장 시작")

            var totalSavedCount = 0
            var batch = mutableListOf<BookCreateRequest>()
            var batchIndex = 1

            jsonlFiles.forEach { jsonlFile ->
                log.info("[📄] ${jsonlFile.fileName} 처리 중...")

                Files.lines(jsonlFile).use { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            try {
                                val request = mapper.readValue(line, BookCreateRequest::class.java)
                                batch.add(request)

                                // 청크 크기만큼 모이면 DB 저장
                                if (batch.size >= loaderProperties.batchSize) {
                                    saveChunkToDatabase(batch, batchIndex)
                                    totalSavedCount += batch.size
                                    batch.clear()
                                    batchIndex++
                                }

                            } catch (e: Exception) {
                                log.warn("[⚠️] JSONL 라인 파싱 실패: $line")
                            }
                        }
                    }
                }

                log.info("[✅] ${jsonlFile.fileName} 처리 완료")
            }

            // 마지막 남은 배치 저장
            if (batch.isNotEmpty()) {
                saveChunkToDatabase(batch, batchIndex)
                totalSavedCount += batch.size
            }

            log.info("[🎉] 스트리밍 DB 저장 완료: 총 ${totalSavedCount}개 데이터")
        }
    }

    // 청크 단위로 DB 저장
    private fun saveChunkToDatabase(batch: List<BookCreateRequest>, batchIndex: Int) {
        try {
            jdbcRepository.saveBooksWithJdbc(batch)
            log.info("[✅] ${batchIndex}번째 DB 청크 저장 성공 (${batch.size}권)")
        } catch (e: Exception) {
            log.error("[❌] ${batchIndex}번째 DB 청크 저장 실패: ${e.message}", e)
            throw e // 실패 시 중단
        }
    }

    // 전체 진행 상황 저장: "파일인덱스:라인번호" 형태
    private fun saveOverallProgress(fileIndex: Int, lineNumber: Int) {
        val progressInfo = "$fileIndex:$lineNumber"
        Files.writeString(progressPath, progressInfo)
    }

    // 전체 진행 상황 로드: (파일인덱스, 라인번호) 반환
    private fun loadOverallProgress(): Pair<Int, Int> {
        return if (Files.exists(progressPath)) {
            val progressInfo = Files.readString(progressPath).trim()
            log.info("[📋] Progress 파일 발견: '$progressInfo'")

            val parts = progressInfo.split(":")
            when (parts.size) {
                2 -> {
                    // 기존 또는 새 형식: 파일인덱스:라인번호
                    val fileIndex = parts[0].toIntOrNull() ?: 1
                    val lineNumber = parts[1].toIntOrNull() ?: 0

                    log.info("[📋] Progress: dataset-${fileIndex}.csv의 ${lineNumber}행부터 시작")
                    fileIndex to lineNumber
                }

                3 -> {
                    // 이전 복잡한 형식을 단순하게 변환
                    val fileIndex = parts[0].toIntOrNull() ?: 1
                    val chunkIndex = parts[1].toIntOrNull() ?: 0
                    val lineNumber = chunkIndex * 1000  // 기존 청크 인덱스를 라인 번호로 변환 (1000개 단위)

                    log.info("[🔄] 기존 복잡한 형식에서 변환: $fileIndex:$lineNumber")
                    saveOverallProgress(fileIndex, lineNumber)

                    fileIndex to lineNumber
                }

                4 -> {
                    // 이전 4개 파라미터 형식을 단순하게 변환
                    val fileIndex = parts[0].toIntOrNull() ?: 1
                    val chunkIndex = parts[1].toIntOrNull() ?: 0
                    val lineNumber = chunkIndex * 1000  // 기존 청크 인덱스를 라인 번호로 변환

                    log.info("[🔄] 기존 4개 파라미터 형식에서 변환: $fileIndex:$lineNumber")
                    saveOverallProgress(fileIndex, lineNumber)

                    fileIndex to lineNumber
                }

                else -> {
                    log.warn("[⚠️] Progress 형식 오류 ('파일인덱스:라인번호' 형태여야 함), 처음부터 시작")
                    1 to 0
                }
            }
        } else {
            log.info("[📋] Progress 파일 없음, 처음부터 시작")
            1 to 0
        }
    }

    // 유틸리티 메서드들
    private fun createCsvMapper(): CsvMapper {
        return CsvMapper().apply {
            disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            enable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        }
    }

    private fun skipCsvRows(iterator: Iterator<CulturalBookDto>, skipLines: Int) {
        if (skipLines > 0) {
            log.info("[⏩] ${skipLines}행 스킵 중...")
            var skippedCount = 0
            repeat(skipLines) {
                if (iterator.hasNext()) {
                    try {
                        iterator.next()
                        skippedCount++
                        if (skippedCount % 10000 == 0) {
                            log.info("[⏩] ${skippedCount}/${skipLines}행 스킵 완료...")
                        }
                    } catch (e: Exception) {
                        log.warn("[⚠️] ${skippedCount + 1}행 스킵 중 오류 (무시하고 계속): ${e.message}")
                        skippedCount++
                    }
                }
            }
            log.info("[✅] 스킵 완료. 이제 ${skipLines + 1}행부터 처리 시작")
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
            log.debug("[⚠️] '${request.title} ${request.isbn}' enrich 실패 (검색 결과 없음)")
            request
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