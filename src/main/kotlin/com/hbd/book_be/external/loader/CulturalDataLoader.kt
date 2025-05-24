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
    private val csvChunkSize = 1000
    private val totalFileCount = 32 // dataset-1.csv ~ dataset-32.csv
    private val linesPerFile = 5000 // 각 dataset 파일의 예상 행 수

    override fun run(vararg args: String?) {
        log.info("[🚀] CulturalDatasetLoader 시작됨 (external-loader.enabled=true)")

        initializeOutputFiles()
        checkExistingFiles()

        // 진행 상황에서 시작할 파일 인덱스 로드
        val (startFileIndex, startChunkIndex) = loadOverallProgress()

        // 1단계: JSONL 생성
        val allEnrichedRequests = mutableListOf<BookCreateRequest>()

        // 모든 파일이 이미 완료된 경우 체크
        if (startFileIndex > totalFileCount) {
            log.info("[🎉] 모든 CSV 파일이 이미 처리 완료되었습니다. DB 저장만 진행합니다.")
            // JSONL에서 데이터 로드해서 DB 저장
            if (Files.exists(outputPath)) {
                val jsonlData = loadFromJsonl()
                saveToDatabase(jsonlData)
            } else {
                log.warn("[⚠️] JSONL 파일을 찾을 수 없습니다.")
            }
            return
        }

        // dataset-1.csv부터 dataset-32.csv까지 순서대로 처리
        for (fileIndex in startFileIndex..totalFileCount) {
            val csvFileName = "dataset-${fileIndex}.csv"
            log.info("[📂] 처리 중: $csvFileName (${fileIndex}/${totalFileCount})")

            try {
                // 첫 번째 재시작 파일인 경우 저장된 청크 인덱스부터 시작, 그 외에는 0부터 시작
                val chunkStartIndex = if (fileIndex == startFileIndex) startChunkIndex else 0
                val enrichedRequests = processCsvFileFromProgress(csvFileName, chunkStartIndex)
                allEnrichedRequests.addAll(enrichedRequests)

                log.info("[✅] $csvFileName 처리 완료 (${enrichedRequests.size}개 데이터)")

                // 파일 처리 완료 시 다음 파일로 진행 상황 업데이트
                saveOverallProgress(fileIndex + 1, 0)

            } catch (e: Exception) {
                log.error("[❌] $csvFileName 처리 실패: ${e.message}", e)
                throw e
            }
        }

        // 2단계: DB 저장
        log.info("[💾] JSONL 생성 완료. 이제 DB 저장 시작...")
        saveToDatabase(allEnrichedRequests)

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

    private fun processCsvFileFromProgress(csvFileName: String, startChunkIndex: Int = 0): List<BookCreateRequest> {
        val totalRows = getTotalCsvRows(csvFileName)
        val totalChunks = (totalRows + csvChunkSize - 1) / csvChunkSize
        val skipRows = startChunkIndex * csvChunkSize

        // 예상 행 수와 실제 행 수 비교
        if (totalRows != linesPerFile) {
            log.warn("[⚠️] $csvFileName: 예상 ${linesPerFile}행과 실제 ${totalRows}행이 다릅니다!")
        }

        log.info("[📊] $csvFileName: 총 ${totalRows}행, ${totalChunks}청크 중 ${startChunkIndex}번째부터 시작")
        if (skipRows > 0) {
            log.info("[⏭️] ${skipRows}행 스킵하고 ${skipRows + 1}행부터 처리 시작")
        } else {
            log.info("[🎯] 파일 시작부터 처리")
        }

        val csvMapper = CsvMapper().apply {
            disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            enable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        }
        val schema = CsvSchema.emptySchema().withHeader()
        val allEnrichedRequests = mutableListOf<BookCreateRequest>()

        javaClass.getResourceAsStream("/dataset/$csvFileName")?.use { inputStream ->
            val reader = csvMapper.readerFor(CulturalBookDto::class.java).with(schema)
            val iterator = reader.readValues<CulturalBookDto>(inputStream)

            // 이미 처리된 행들 스킵
            if (skipRows > 0) {
                log.info("[⏩] ${skipRows}행 스킵 중...")
                var skippedCount = 0
                repeat(skipRows) {
                    if (iterator.hasNext()) {
                        try {
                            iterator.next()
                            skippedCount++
                            if (skippedCount % 10000 == 0) {
                                log.info("[⏩] ${skippedCount}/${skipRows}행 스킵 완료...")
                            }
                        } catch (e: Exception) {
                            log.warn("[⚠️] ${skippedCount + 1}행 스킵 중 오류 (무시하고 계속): ${e.message}")
                            skippedCount++
                        }
                    }
                }
                log.info("[✅] 스킵 완료. 이제 ${skipRows + 1}행부터 처리 시작")
            }

            var currentChunkIndex = startChunkIndex
            var chunk = mutableListOf<CulturalBookDto>()

            while (iterator.hasNext()) {
                try {
                    val dto = iterator.next()
                    chunk.add(dto)
                } catch (e: Exception) {
                    log.warn("[⚠️] CSV 행 파싱 오류 (스킵): ${e.message}")
                    continue
                }

                if (chunk.size >= csvChunkSize) {
                    val enrichedRequests = processChunk(chunk, currentChunkIndex)
                    allEnrichedRequests.addAll(enrichedRequests)
                    chunk.clear()
                    currentChunkIndex++

                    // 현재 파일의 진행 상황 저장
                    val currentFileIndex = csvFileName.removePrefix("dataset-").removeSuffix(".csv").toInt()
                    saveOverallProgress(currentFileIndex, currentChunkIndex)

                    val processedRows = currentChunkIndex * csvChunkSize
                    val progress = (processedRows.toDouble() / totalRows * 100).toInt()
                    log.info("[📊] $csvFileName: ${processedRows}/${totalRows}행 처리 완료 (${progress}%) - 청크 #${currentChunkIndex}")
                }
            }

            // 마지막 청크 처리
            if (chunk.isNotEmpty()) {
                val enrichedRequests = processChunk(chunk, currentChunkIndex)
                allEnrichedRequests.addAll(enrichedRequests)
                log.info("[✅] $csvFileName: JSONL 생성 완료")
            }
        } ?: throw ValidationException(
            "CSV 파일을 찾을 수 없습니다: $csvFileName",
            ErrorCodes.CSV_FILE_NOT_FOUND
        )

        return allEnrichedRequests
    }

    private fun getTotalCsvRows(csvFileName: String): Int {
        val csvMapper = CsvMapper().apply {
            disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            enable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        }
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

    // 전체 진행 상황 저장: "파일인덱스:청크인덱스" 형태
    private fun saveOverallProgress(fileIndex: Int, chunkIndex: Int) {
        val progressInfo = "$fileIndex:$chunkIndex"
        Files.writeString(progressPath, progressInfo)
    }

    // 전체 진행 상황 로드: (파일인덱스, 청크인덱스) 반환
    private fun loadOverallProgress(): Pair<Int, Int> {
        return if (Files.exists(progressPath)) {
            val progressInfo = Files.readString(progressPath).trim()
            log.info("[📋] Progress 파일 발견: '$progressInfo'")

            val parts = progressInfo.split(":")
            if (parts.size == 2) {
                val firstPart = parts[0]
                val secondPart = parts[1].toIntOrNull() ?: 0

                // 기존 format: "dataset.csv:62" -> 새로운 format으로 변환
                if (firstPart == "dataset.csv") {
                    val totalProcessedChunks = secondPart
                    val totalProcessedRows = totalProcessedChunks * csvChunkSize

                    log.info("[🔄] 기존 형식 감지: dataset.csv 기준 ${totalProcessedChunks}청크 (${totalProcessedRows}행) 처리됨")

                    // 분할된 파일 기준으로 변환
                    val fileIndex = (totalProcessedRows / linesPerFile) + 1
                    val remainingRows = totalProcessedRows % linesPerFile
                    val chunkIndex = remainingRows / csvChunkSize

                    log.info("[🔀] 새로운 형식으로 변환: dataset-${fileIndex}.csv의 ${chunkIndex}번째 청크부터")

                    // 새로운 형식으로 progress 파일 업데이트
                    saveOverallProgress(fileIndex, chunkIndex)

                    return fileIndex to chunkIndex
                }
                // 새로운 format: "파일인덱스:청크인덱스"
                else {
                    val fileIndex = firstPart.toIntOrNull() ?: 1
                    val chunkIndex = secondPart

                    // 각 파일당 청크 수 계산 (5000줄 ÷ 1000 = 5청크: 0,1,2,3,4)
                    val chunksPerFile = (linesPerFile + csvChunkSize - 1) / csvChunkSize

                    // 파일이 완전히 처리되었는지 확인
                    if (chunkIndex >= chunksPerFile) {
                        // 현재 파일 완료, 다음 파일로
                        val nextFileIndex = fileIndex + 1
                        log.info("[✅] dataset-${fileIndex}.csv 완료됨. 다음 파일: dataset-${nextFileIndex}.csv로 이동")
                        return if (nextFileIndex <= totalFileCount) {
                            nextFileIndex to 0
                        } else {
                            log.info("[🎉] 모든 파일 처리 완료!")
                            totalFileCount + 1 to 0 // 모든 파일 완료 표시
                        }
                    }

                    // 파일 인덱스가 범위를 벗어나면 1부터 시작
                    if (fileIndex in 1..totalFileCount) {
                        fileIndex to chunkIndex
                    } else {
                        log.warn("[⚠️] 파일 인덱스 범위 초과 (${fileIndex}), 처음부터 시작")
                        1 to 0
                    }
                }
            } else {
                log.warn("[⚠️] Progress 형식 오류 ('파일인덱스:청크인덱스' 형태여야 함), 처음부터 시작")
                1 to 0
            }
        } else {
            log.info("[📋] Progress 파일 없음, 처음부터 시작")
            1 to 0
        }
    }

    // JSONL 파일에서 데이터 로드
    private fun loadFromJsonl(): List<BookCreateRequest> {
        val requests = mutableListOf<BookCreateRequest>()
        Files.lines(outputPath).use { lines ->
            lines.forEach { line ->
                if (line.isNotBlank()) {
                    try {
                        val request = mapper.readValue(line, BookCreateRequest::class.java)
                        requests.add(request)
                    } catch (e: Exception) {
                        log.warn("[⚠️] JSONL 라인 파싱 실패: $line")
                    }
                }
            }
        }
        log.info("[📖] JSONL에서 ${requests.size}개 데이터 로드 완료")
        return requests
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