package com.hbd.book_be.repository

import com.hbd.book_be.domain.Contents
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

@Repository
class ContentsRepositoryJsonQueryImpl(
    @PersistenceContext private val entityManager: EntityManager
) : ContentsRepositoryJsonQuery {

    @Value("\${database.platform:oracle}")
    private lateinit var databasePlatform: String

    override fun findOnePerUrlType(): List<Contents> {
        val query = when (databasePlatform.lowercase()) {
            "mysql", "h2" -> getMySQLQuery()
            "oracle" -> getOracleQuery()
            else -> throw UnsupportedOperationException("Unsupported database platform: $databasePlatform")
        }

        return entityManager.createNativeQuery(query, Contents::class.java)
            .resultList
            .filterIsInstance<Contents>()
    }

    private fun getMySQLQuery() = """
        WITH ranked_contents AS (
            SELECT DISTINCT c.*, 
                   ROW_NUMBER() OVER (
                       PARTITION BY JSON_UNQUOTE(JSON_EXTRACT(jt.url_type, '$'))
                       ORDER BY c.id ASC
                   ) as rn
            FROM contents c
            CROSS JOIN JSON_TABLE(
                c.urls, 
                '$[*]' COLUMNS (
                    url_type JSON PATH '$.type'
                )
            ) AS jt
            WHERE c.deleted_at IS NULL
        )
        SELECT * FROM ranked_contents WHERE rn = 1
        ORDER BY id
    """.trimIndent()

    private fun getOracleQuery() = """
        WITH ranked_contents AS (
            SELECT c.*, 
                   ROW_NUMBER() OVER (
                       PARTITION BY json_value(url_item.COLUMN_VALUE, '$.type')
                       ORDER BY c.id ASC
                   ) as rn
            FROM contents c,
                 JSON_TABLE(c.urls, '$[*]' COLUMNS (COLUMN_VALUE CLOB FORMAT JSON PATH '$')) url_item
            WHERE c.deleted_at IS NULL
              AND json_value(url_item.COLUMN_VALUE, '$.type') IS NOT NULL
        )
        SELECT * FROM ranked_contents WHERE rn = 1
        ORDER BY id
    """.trimIndent()
}
