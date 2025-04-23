package com.hbd.book_be.loader

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.hbd.book_be.domain.Author
import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.loader.dto.ContributorResult
import com.hbd.book_be.loader.dto.CulturalBookDto
import com.hbd.book_be.repository.AuthorRepository
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.PublisherRepository
import com.hbd.book_be.util.DateUtil
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Component
class CulturalDatasetLoader(
    private val bookRepository: BookRepository,
    private val publisherRepository: PublisherRepository,
    private val authorRepository: AuthorRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) = runBlocking {
        println("🚀 CulturalDatasetLoader 시작됨")

        val csvMapper = CsvMapper()
        val schema = CsvSchema.emptySchema().withHeader()
        val inputStream = javaClass.getResourceAsStream("/dataset/dataset.csv")
        val reader = csvMapper.readerFor(CulturalBookDto::class.java).with(schema)
        val dataList: List<CulturalBookDto> = reader.readValues<CulturalBookDto>(inputStream).readAll()

        val allBooks = mutableListOf<Book>()
        val publisherCache = mutableMapOf<String, Publisher>()
        val authorCache = mutableMapOf<String, Author>()

        for (dto in dataList) {
            try {
                val publisherName = dto.publisherNm?.takeIf { it.isNotBlank() } ?: "알 수 없음"
                val publisher = publisherCache.getOrPut(publisherName) {
                    publisherRepository.findByName(publisherName)
                        ?: publisherRepository.save(
                            Publisher(
                                name = publisherName,
                                engName = null,
                                logo = null,
                                description = null
                            )
                        )
                }

                val rawDate = (dto.pblicteDe ?: dto.twoPblicteDe).takeIf { !it.isNullOrBlank() } ?: "1001-01-01"
                val parsedDate = DateUtil.parseFlexibleDate(rawDate)

                val contributorResult = parseContributors(dto.authrNm, authorCache)

                val book = Book(
                    isbn = dto.isbnThirteenNo ?: dto.isbnNo ?: "UNKNOWN",
                    title = dto.titleNm ?: "제목 없음",
                    summary = dto.bookIntrcnCn ?: "",
                    publishedDate = parsedDate,
                    titleImage = dto.imageUrl,
                    price = dto.prcValue?.toIntOrNull(),
                    publisher = publisher,
                    detailUrl = null,
                    translator = contributorResult.translators.joinToString(", ")
                )

                contributorResult.authors.forEach { book.addAuthor(it) }
                allBooks += book
            } catch (e: Exception) {
                println("⚠️ Book 생성 실패: ${dto.titleNm} (${e.message})")
                e.printStackTrace()
            }
        }

        try {
            bookRepository.saveAll(allBooks)
            println("✅ 문화 데이터셋 로딩 완료: ${allBooks.size}건")
        } catch (e: Exception) {
            println("❌ 저장 실패: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun parseContributors(raw: String?, cache: MutableMap<String, Author>): ContributorResult {
        val authors = mutableListOf<Author>()
        val translators = mutableListOf<String>()

        if (raw.isNullOrBlank()) return ContributorResult(authors, translators)

        raw.split(",").map { it.trim() }.forEach { person ->
            when {
                person.contains("지은이") -> {
                    val name = person.replace("(지은이)", "").trim()
                    val author = cache.getOrPut(name) {
                        authorRepository.findFirstByName(name).getOrNull()
                            ?: authorRepository.save(Author(name = name, description = null, profile = null))
                    }
                    authors.add(author)
                }

                person.contains("옮긴이") -> {
                    val name = person.replace("(옮긴이)", "").trim()
                    translators.add(name)
                }
            }
        }

        return ContributorResult(authors, translators)
    }
}
