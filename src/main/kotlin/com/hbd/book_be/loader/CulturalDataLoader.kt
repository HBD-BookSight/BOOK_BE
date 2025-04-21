package com.hbd.book_be.loader

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.hbd.book_be.domain.Author
import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher


import com.hbd.book_be.loader.dto.CulturalBookDto
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.loader.dto.ContributorResult
import com.hbd.book_be.repository.AuthorRepository
import com.hbd.book_be.repository.PublisherRepository
import com.hbd.book_be.util.DateUtil
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
class CulturalDatasetLoader(
    private val bookRepository: BookRepository,
    private val publisherRepository: PublisherRepository,
    private val authorRepository: AuthorRepository,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val csvMapper = CsvMapper()
        val schema = CsvSchema.emptySchema().withHeader()  // 첫 줄이 헤더인 경우

        val inputStream = javaClass.getResourceAsStream("dataset/dataset.csv")
        val reader = csvMapper.readerFor(CulturalBookDto::class.java).with(schema)

        val dataList: List<CulturalBookDto> = reader.readValues<CulturalBookDto>(inputStream).readAll()


        val entities = dataList.map { dto ->
            val contributorResult = parseContributors(dto.authrNm)
            val publisher = getOrCreatePublisher(dto.publisherNm)

            val book = Book(
                isbn = dto.isbnThirteenNo ?: dto.isbnNo ?: "UNKNOWN",
                title = dto.titleNm ?: "제목 없음",
                summary = dto.bookIntrcnCn ?: "",
                publishedDate = DateUtil.parseFlexibleDate(dto.pblicteDe ?: dto.twoPblicteDe),
                titleImage = dto.imageUrl,
                price = dto.prcValue?.toIntOrNull(),
                publisher = publisher,
                detailUrl = null,
                translator = null, // or contributorResult.translators
            )


            contributorResult.authors.forEach { author ->
                book.addAuthor(author)
            }

            book // ✅ 마지막 표현식으로 반환
        }


        bookRepository.saveAll(entities)
        println("✅ 문화 데이터셋 로딩 완료: ${entities.size}건")
    }

    fun parseContributors(raw: String?): ContributorResult {

        val authors = mutableListOf<Author>()
        val translators = mutableListOf<String>()

        if (raw.isNullOrBlank()) return ContributorResult(authors, translators)

        raw.split(",").map { it.trim() }.forEach { person ->
            when {
                person.contains("지은이") -> {
                    val name = person.replace("(지은이)", "").trim()
                    val author = authorRepository.findFirstByName(name).getOrNull()
                        ?: authorRepository.save(Author(name = name, description = null, profile = null))
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


    private fun getOrCreatePublisher(publisherNm: String?): Publisher {
        val name = publisherNm?.trim().takeIf { it?.isNotBlank() == true }
            ?: throw NotFoundException("Publisher name is required.")

        return publisherRepository.findByName(name)
            ?: publisherRepository.save(
                Publisher(
                    name = name,
                    engName = null,
                    logo = null,
                    description = null
                )
            )
    }

}
