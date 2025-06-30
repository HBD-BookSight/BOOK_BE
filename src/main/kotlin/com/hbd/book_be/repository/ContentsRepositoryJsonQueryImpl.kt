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

    override fun findOnePerUrlType(): List<Contents> {
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
}
