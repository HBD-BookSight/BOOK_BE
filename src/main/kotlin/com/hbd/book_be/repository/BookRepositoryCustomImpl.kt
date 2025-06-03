package com.hbd.book_be.repository

import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.QBook.book
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Repository
class BookRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : BookRepositoryCustom {
    override fun findAllActive(publishedDate: LocalDate?, pageable: Pageable): Page<Book> {
        val whereClause = BooleanBuilder()
        whereClause.and(book.deletedAt.isNull)
        if (publishedDate != null) {
            whereClause.and(
                book.publishedDate.between(
                    publishedDate.atStartOfDay(),
                    publishedDate.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.MILLIS)
                )
            )
        }

        val totalCount = queryFactory
            .select(book.count())
            .from(book)
            .where(whereClause)
            .fetchOne() ?: 0L

        val query = queryFactory
            .selectFrom(book)
            .where(whereClause)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val orderedQuery = applyOrderBy(query, pageable)

        val result = orderedQuery.fetch()
        return PageImpl(result, pageable, totalCount)
    }


    override fun findActiveByAuthorName(authorName: String, publishedDate: LocalDate?, pageable: Pageable): Page<Book> {
        val whereClause = BooleanBuilder()
        whereClause.and(
            book.deletedAt.isNull
        )
        whereClause.and(
            book.bookAuthorList.any().author().name.containsIgnoreCase(authorName)
        )
        if (publishedDate != null) {
            whereClause.and(
                book.publishedDate.between(
                    publishedDate.atStartOfDay(),
                    publishedDate.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.MILLIS)
                )
            )
        }

        val totalCount = queryFactory.select(book.count()).from(book).where(whereClause).fetchOne() ?: 0L
        val query = queryFactory
            .selectFrom(book)
            .where(whereClause).offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
        val orderedQuery = applyOrderBy(query, pageable)
        val result = orderedQuery.fetch()

        return PageImpl(result, pageable, totalCount)
    }

    override fun findActiveByPublisherName(
        publisherName: String, publishedDate: LocalDate?, pageable: Pageable
    ): Page<Book> {
        val whereClause = BooleanBuilder()
        whereClause.and(
            book.deletedAt.isNull
        )
        whereClause.and(
            book.publisher().name.containsIgnoreCase(publisherName)
        )
        if (publishedDate != null) {
            whereClause.and(
                book.publishedDate.between(
                    publishedDate.atStartOfDay(),
                    publishedDate.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.MILLIS)
                )
            )
        }

        val totalCount = queryFactory
            .select(book.count())
            .from(book)
            .where(whereClause)
            .fetchOne() ?: 0L

        val query = queryFactory
            .selectFrom(book)
            .where(whereClause)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
        val orderedQuery = applyOrderBy(query, pageable)
        val result = orderedQuery.fetch()

        return PageImpl(result, pageable, totalCount)
    }

    override fun findActiveByTitle(title: String, publishedDate: LocalDate?, pageable: Pageable): Page<Book> {
        val whereClause = BooleanBuilder()
        whereClause.and(
            book.deletedAt.isNull
        )
        whereClause.and(
            book.title.containsIgnoreCase(title)
        )

        if (publishedDate != null) {
            whereClause.and(
                book.publishedDate.between(
                    publishedDate.atStartOfDay(),
                    publishedDate.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.MILLIS)
                )
            )
        }

        val totalCount = queryFactory
            .select(book.count())
            .from(book)
            .where(whereClause)
            .fetchOne() ?: 0L

        val query = queryFactory
            .selectFrom(book)
            .where(whereClause)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
        val orderedQuery = applyOrderBy(query, pageable)
        val result = orderedQuery.fetch()

        return PageImpl(result, pageable, totalCount)
    }

    private fun applyOrderBy(
        query: JPAQuery<Book>,
        pageable: Pageable
    ): JPAQuery<Book> {
        var orderedQuery = query
        for (order in pageable.sort) {
            val entityPath: PathBuilder<*> = PathBuilder(Book::class.java, "book")
            val orderSpecifier = OrderSpecifier(
                if (order.isAscending) Order.ASC else Order.DESC,
                entityPath[order.property] as Expression<Comparable<*>>
            )
            orderedQuery = orderedQuery.orderBy(orderSpecifier)
        }
        return orderedQuery
    }
}