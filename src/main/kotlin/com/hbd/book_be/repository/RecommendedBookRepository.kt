package com.hbd.book_be.repository

import com.hbd.book_be.domain.RecommendedBook
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RecommendedBookRepository : JpaRepository<RecommendedBook, String> {

    @Query(
        "select rm from RecommendedBook rm " +
                "where rm.recommendedDate = (select max(sub.recommendedDate) from RecommendedBook sub)"
    )
    fun findRecentRecommendedBooks(): List<RecommendedBook>
}