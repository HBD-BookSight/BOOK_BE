package com.hbd.book_be.repository

import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.QBook.book
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class BookRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : BookRepositoryCustom {
    override fun findAllActive(keyword: String?, pageable: Pageable): Page<Book> {
        val whereClause = BooleanBuilder()
        whereClause.and(book.deletedAt.isNull)

        if (!keyword.isNullOrBlank()) {
            whereClause.and(
                book.title.containsIgnoreCase(keyword)
                    .or(book.bookAuthorList.any().author().name.containsIgnoreCase(keyword))
                    .or(book.publisher().name.containsIgnoreCase(keyword))
            )
        }

        val totalCount = queryFactory
            .select(book.count())
            .from(book)
            .where(whereClause)
            .fetchOne() ?: 0L

        var query = queryFactory
            .selectFrom(book)
            .where(whereClause)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        for (order in pageable.sort) {
            val entityPath: PathBuilder<*> = PathBuilder(Book::class.java, "book")
            val orderSpecifier = OrderSpecifier(
                if (order.isAscending) Order.ASC else Order.DESC,
                entityPath[order.property] as Expression<Comparable<*>>
            )
            query = query.orderBy(orderSpecifier)
        }

        val result = query.fetch()

        return PageImpl(result, pageable, totalCount)
    }
}