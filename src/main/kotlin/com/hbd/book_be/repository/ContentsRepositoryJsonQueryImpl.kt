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

    @Value("\${database.platform:h2}")
    private lateinit var databasePlatform: String

    override fun findOnePerUrlType(): List<Contents> {
        return when (databasePlatform.lowercase()) {
            "mysql", "h2" -> executeQuery(getMySQLQuery())
            "oracle" -> findOnePerUrlTypeForOracle() // Oracle은 애플리케이션 레벨에서 처리
            else -> throw UnsupportedOperationException("Unsupported database platform: $databasePlatform")
        }
    }

    private fun executeQuery(query: String): List<Contents> {
        return entityManager.createNativeQuery(query, Contents::class.java)
            .resultList
            .filterIsInstance<Contents>()
    }

    // Oracle에서는 애플리케이션 레벨에서 처리 (JSON 함수 문제 회피)
    private fun findOnePerUrlTypeForOracle(): List<Contents> {
        val allContents = entityManager
            .createQuery("SELECT c FROM Contents c WHERE c.deletedAt IS NULL ORDER BY c.id", Contents::class.java)
            .resultList

        val urlTypeMap = mutableMapOf<String, Contents>()
        
        for (content in allContents) {
            for (urlInfo in content.urls) {
                val type = urlInfo.type
                if (!urlTypeMap.containsKey(type)) {
                    urlTypeMap[type] = content
                }
            }
        }
        
        return urlTypeMap.values.sortedBy { it.id }.toList()
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
}
