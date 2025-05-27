package com.hbd.book_be.external.loader

import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.dto.request.BookCreateRequest
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.transaction.annotation.Transactional
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.time.LocalDateTime

open class BookJdbcRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    @Transactional
    open fun saveBooksWithJdbc(requests: List<BookCreateRequest>) {
        val books = mutableListOf<Book>()
        val bookAuthorPairs = mutableListOf<Pair<Int, Long>>()

        requests.forEachIndexed { index, req ->
            runCatching {
                val publisherId = findOrInsertPublisherId(req.publisherName)
                val book = createBookStub(req, publisherId)
                books.add(book)

                // null 체크 추가
                req.authorNameList?.forEach { name ->
                    val authorId = findOrInsertAuthorId(name)
                    bookAuthorPairs.add(index to authorId)
                }
            }.onFailure {
                println("❌ Book 변환 실패: ${req.title} (${it.message})")
            }
        }

        insertBooksBatch(books)
        insertBookAuthorsBatch(bookAuthorPairs, books)
        println("✅ JDBC 저장 완료 (${books.size}권)")
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
            translator = req.translator ?: emptyList(),
            status = req.status
        )
    }

    private fun insertBooksBatch(books: List<Book>) {
        val sql = """
            INSERT INTO book (
                isbn, title, summary, published_date, title_image, 
                price, publisher_id, detail_url, translator, status, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                title = VALUES(title),
                summary = VALUES(summary),
                published_date = VALUES(published_date),
                title_image = VALUES(title_image),
                price = VALUES(price),
                detail_url = VALUES(detail_url),
                translator = VALUES(translator),
                updated_at = NOW()
        """.trimIndent()

        jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, i: Int) {
                val book = books[i]
                ps.setString(1, book.isbn)
                ps.setString(2, book.title)
                ps.setString(3, book.summary)
                ps.setTimestamp(4, Timestamp.valueOf(book.publishedDate)) // LocalDateTime으로 저장
                ps.setObject(5, book.titleImage, Types.VARCHAR)
                ps.setObject(6, book.price, Types.INTEGER)
                ps.setLong(7, book.publisher.id!!)
                ps.setObject(8, book.detailUrl, Types.VARCHAR)
                // translator를 문자열로 변환 (중요: ArrayList → String 변환)
                val translatorString = convertListToString(book.translator)
                if (translatorString != null) {
                    ps.setString(9, translatorString)
                } else {
                    ps.setNull(9, Types.VARCHAR)
                }
                ps.setString(10, book.status) // status 필드 추가
                ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()))
            }

            override fun getBatchSize(): Int = books.size
        })
    }

    private fun insertBookAuthorsBatch(pairs: List<Pair<Int, Long>>, books: List<Book>) {
        val sql = "INSERT IGNORE INTO book_author (isbn, author_id) VALUES (?, ?)"

        jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, i: Int) {
                val (bookIdx, authorId) = pairs[i]
                ps.setString(1, books[bookIdx].isbn)
                ps.setLong(2, authorId)
            }

            override fun getBatchSize(): Int = pairs.size
        })
    }

    // List<String>을 문자열로 변환하는 메서드 추가
    private fun convertListToString(list: List<String>?): String? {
        return if (list.isNullOrEmpty()) {
            null
        } else {
            list.joinToString(",") // StringListConverter와 동일한 방식
        }
    }

    private fun findOrInsertPublisherId(name: String?): Long {
        val key = name?.takeIf { it.isNotBlank() } ?: "알 수 없음"
        return findOrInsertPublisher(key)
    }

    private fun findOrInsertAuthorId(name: String): Long {
        return findOrInsertAuthor(name)
    }

    private fun findOrInsertPublisher(name: String): Long {
        val existing = jdbcTemplate.queryForList(
            "SELECT id FROM publisher WHERE name = ? LIMIT 1",
            Long::class.java,
            name
        )
        if (existing.isNotEmpty()) return existing.first()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ con ->
            con.prepareStatement(
                """
                    INSERT INTO publisher (name, eng_name, logo, urls, description, is_official, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(), arrayOf("id")
            ).apply {
                setString(1, name)
                setNull(2, Types.VARCHAR) // eng_name
                setNull(3, Types.VARCHAR) // logo
                setString(4, "[]")         // urls
                setNull(5, Types.VARCHAR) // description
                setBoolean(6, false)      // is_official
                setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()))
                setTimestamp(8, Timestamp.valueOf(LocalDateTime.now())) // updated_at 추가
            }
        }, keyHolder)

        return keyHolder.key!!.toLong()
    }

    private fun findOrInsertAuthor(name: String): Long {
        val existing = jdbcTemplate.queryForList(
            "SELECT id FROM author WHERE name = ? AND deleted_at IS NULL LIMIT 1",
            Long::class.java,
            name
        )
        if (existing.isNotEmpty()) return existing.first()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ con ->
            con.prepareStatement(
                """
                    INSERT INTO author (name, description, profile, is_official, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(), arrayOf("id")
            ).apply {
                setString(1, name)
                setNull(2, Types.VARCHAR) // description
                setNull(3, Types.VARCHAR) // profile
                setBoolean(4, false)      // is_official
                setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()))
                setTimestamp(6, Timestamp.valueOf(LocalDateTime.now())) // updated_at 추가
            }
        }, keyHolder)

        return keyHolder.key!!.toLong()
    }
}
