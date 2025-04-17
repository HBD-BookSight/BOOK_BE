package com.hbd.book_be.repository

import com.hbd.book_be.domain.DiscoveryContents
import com.hbd.book_be.domain.QDiscoveryContents.discoveryContents

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
class DiscoveryContentsRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : DiscoveryContentsRepositoryCustom {
    override fun findContentsWithConditions(pageable: Pageable): Page<DiscoveryContents> {
        val totalCount = queryFactory.select(discoveryContents.count())
            .from(discoveryContents)
            .where()
            .fetchOne()

        var query = queryFactory.selectFrom(discoveryContents)
            .where()
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        for (order in pageable.sort) {
            val entityPath: PathBuilder<*> = PathBuilder(DiscoveryContents::class.java, "contents")

            val orderSpecifier = OrderSpecifier(
                if (order.isAscending) Order.ASC else Order.DESC,
                entityPath[order.property] as Expression<Comparable<*>>
            )
            query = query.orderBy(orderSpecifier)
        }

        val result = query.fetch()

        return PageImpl(result, pageable, totalCount ?: 0L)
    }
}