package com.hbd.book_be.loader

import com.hbd.book_be.domain.Author
import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.repository.AuthorRepository
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.PublisherRepository
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.time.LocalDateTime

@Service
class DataLoaderService(
    private val bookRepository: BookRepository,
    private val publisherRepository: PublisherRepository,
    private val authorRepository: AuthorRepository,
    private val jdbcTemplate: JdbcTemplate,
) {

    @Transactional
    fun saveBooksWithJpa(requests: List<BookCreateRequest>) {
        val publisherCache = mutableMapOf<String, Publisher>()
        val authorCache = mutableMapOf<String, Author>()

        val books = requests.mapNotNull { req ->
            runCatching {
                createBookEntity(req, publisherCache, authorCache)
            }.onFailure {
                println("❌ Book 생성 실패: ${req.title} (${it.message})")
            }.getOrNull()
        }

        runCatching {
            bookRepository.saveAll(books)
            println("✅ JPA 저장 완료 (${books.size}권)")
        }.onFailure {
            println("❌ JPA 저장 실패: ${it.message}")
            it.printStackTrace()
        }
    }

    @Transactional
    fun saveBooksWithJdbc(requests: List<BookCreateRequest>) {
        val publisherCache = mutableMapOf<String, Long>()
        val authorCache = mutableMapOf<String, Long>()
        val books = mutableListOf<Book>()
        val bookAuthorPairs = mutableListOf<Pair<Int, Long>>()

        requests.forEachIndexed { index, req ->
            runCatching {
                val publisherId = getOrInsertPublisherId(req.publisherName, publisherCache)
                val book = createBookStub(req, publisherId)
                books.add(book)

                req.authorNameList.forEach { name ->
                    val authorId = getOrInsertAuthorId(name, authorCache)
                    bookAuthorPairs.add(index to authorId)
                }
            }.onFailure {
                println("❌ Book 변환 실패: ${req.title} (${it.message})")
                it.printStackTrace()
            }
        }

        insertBooksBatch(books)
        insertBookAuthorsBatch(bookAuthorPairs, books)
        println("✅ JDBC 저장 완료 (${books.size}권)")
    }

    // --- Helper Methods ---

    private fun createBookEntity(
        req: BookCreateRequest,
        publisherCache: MutableMap<String, Publisher>,
        authorCache: MutableMap<String, Author>
    ): Book {
        val publisherName = req.publisherName?.takeIf { it.isNotBlank() } ?: "알 수 없음"
        val publisher = publisherCache.getOrPut(publisherName) {
            publisherRepository.findByName(publisherName)
                ?: publisherRepository.save(
                    Publisher(
                        name = publisherName,
                        engName = null,
                        logo = null,
                        description = null,
                        isOfficial = false
                    )
                )
        }

        val book = Book(
            isbn = req.isbn,
            title = req.title,
            summary = req.summary,
            publishedDate = req.publishedDate,
            titleImage = req.titleImage,
            price = req.price,
            publisher = publisher,
            detailUrl = req.detailUrl,
            translator = req.translator
        )

        req.authorNameList.forEach { name ->
            val author = authorCache.getOrPut(name) {
                authorRepository.findTopByNameAndDeletedAtIsNull(name)
                    ?: authorRepository.save(Author(name = name, description = null, profile = null))
            }
            book.addAuthor(author)
        }

        return book
    }

    private fun createBookStub(req: BookCreateRequest, publisherId: Long): Book {
        return Book(
            isbn = req.isbn,
            title = req.title,
            summary = req.summary,
            publishedDate = req.publishedDate,
            titleImage = req.titleImage,
            price = req.price,
            publisher = Publisher(
                id = publisherId,
                name = req.publisherName ?: "알 수 없음",
                engName = null,
                logo = null,
                description = null
            ),
            detailUrl = req.detailUrl,
            translator = req.translator
        )
    }

    private fun getOrInsertPublisherId(name: String?, cache: MutableMap<String, Long>): Long {
        val key = name?.takeIf { it.isNotBlank() } ?: "알 수 없음"
        return cache.getOrPut(key) { findOrInsertPublisher(key) }
    }

    private fun getOrInsertAuthorId(name: String, cache: MutableMap<String, Long>): Long {
        return cache.getOrPut(name) { findOrInsertAuthor(name) }
    }

    private fun insertBooksBatch(books: List<Book>) {
        val sql = """
            INSERT INTO book (
                isbn, title, summary, published_date,
                title_image, price, publisher_id, detail_url, translator,
                created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, i: Int) {
                val book = books[i]
                ps.setString(1, book.isbn)
                ps.setString(2, book.title)
                ps.setString(3, book.summary)
                ps.setDate(4, Date.valueOf(book.publishedDate.toLocalDate()))
                ps.setObject(5, book.titleImage, Types.VARCHAR)
                ps.setObject(6, book.price, Types.INTEGER)
                ps.setLong(7, book.publisher.id!!)
                ps.setObject(8, book.detailUrl, Types.VARCHAR)
                ps.setObject(9, book.translator, Types.VARCHAR)
                ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()))
            }

            override fun getBatchSize(): Int = books.size
        })
    }

    private fun insertBookAuthorsBatch(pairs: List<Pair<Int, Long>>, books: List<Book>) {
        val sql = "INSERT INTO book_author (isbn, author_id) VALUES (?, ?)"
        jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, i: Int) {
                val (bookIdx, authorId) = pairs[i]
                ps.setString(1, books[bookIdx].isbn)
                ps.setLong(2, authorId)
            }

            override fun getBatchSize(): Int = pairs.size
        })
    }

    fun findOrInsertPublisher(name: String): Long {
        val existing = jdbcTemplate.queryForList(
            "SELECT id FROM publisher WHERE name = ? LIMIT 1", Long::class.java, name
        )
        if (existing.isNotEmpty()) return existing.first()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ con ->
            con.prepareStatement(
                """
                    INSERT INTO publisher (name, eng_name, logo, urls, description, is_official, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(), arrayOf("id")
            ).apply {
                setString(1, name)
                setNull(2, Types.VARCHAR)
                setNull(3, Types.VARCHAR)
                setString(4, "[]")
                setNull(5, Types.VARCHAR)
                setBoolean(6, false)
                setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()))
            }
        }, keyHolder)

        return keyHolder.key!!.toLong()
    }

    fun findOrInsertAuthor(name: String): Long {
        val existing = jdbcTemplate.queryForList(
            "SELECT id FROM author WHERE name = ? AND deleted_at IS NULL LIMIT 1", Long::class.java, name
        )
        if (existing.isNotEmpty()) return existing.first()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ con ->
            con.prepareStatement(
                """
                    INSERT INTO author (name, description, profile, is_official, created_at)
                    VALUES (?, ?, ?, ?, ?)
                """.trimIndent(), arrayOf("id")
            ).apply {
                setString(1, name)
                setNull(2, Types.VARCHAR)
                setNull(3, Types.VARCHAR)
                setBoolean(4, false)
                setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()))
            }
        }, keyHolder)

        return keyHolder.key!!.toLong()
    }
}
