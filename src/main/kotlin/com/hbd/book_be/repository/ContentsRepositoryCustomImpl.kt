package com.hbd.book_be.repository

import com.hbd.book_be.domain.Contents
import com.hbd.book_be.domain.QContents.contents
import com.hbd.book_be.domain.enums.ContentType
import com.hbd.book_be.dto.request.ContentsSearchRequest
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ContentsRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : ContentsRepositoryCustom {
    override fun findContentsWithConditions(searchRequest: ContentsSearchRequest, pageable: Pageable): Page<Contents> {
        val totalCount = queryFactory.select(contents.count())
            .from(contents)
            .where(
                contents.deletedAt.isNull,
                typeEq(searchRequest.type)
            )
            .fetchOne()

        var query = queryFactory.selectFrom(contents)
            .where(
                contents.deletedAt.isNull,
                typeEq(searchRequest.type)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        for (order in pageable.sort) {
            val entityPath: PathBuilder<*> = PathBuilder(Contents::class.java, "contents")

            val orderSpecifier = OrderSpecifier(
                if (order.isAscending) Order.ASC else Order.DESC,
                entityPath[order.property] as Expression<Comparable<*>>
            )
            query = query.orderBy(orderSpecifier)
        }

        val result = query.fetch()

        return PageImpl(result, pageable, totalCount ?: 0L)
    }

    private fun typeEq(type: ContentType?): BooleanExpression? {
        return type?.let { contents.type.eq(it) }
    }

}