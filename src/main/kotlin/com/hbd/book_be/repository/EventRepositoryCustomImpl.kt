package com.hbd.book_be.repository

import com.hbd.book_be.domain.Event
import com.hbd.book_be.domain.QEvent.event
import com.hbd.book_be.dto.request.EventSearchRequest
import com.hbd.book_be.enums.EventFlag
import com.hbd.book_be.enums.EventLocation
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
import java.time.LocalDate

@Repository
class EventRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : EventRepositoryCustom {

    override fun findAllActiveWithConditions(searchRequest: EventSearchRequest, pageable: Pageable): Page<Event> {
        val totalCount = queryFactory
            .select(event.count())
            .from(event)
            .where(
                event.deletedAt.isNull,
                eventFlagEq(searchRequest.eventFlag),
                locationEq(searchRequest.location),
                eventTypeEq(searchRequest.eventType),
                dateCondition(searchRequest.startDate, searchRequest.endDate)
            )
            .fetchOne()

        var query = queryFactory
            .selectFrom(event)
            .where(
                event.isPosting.isTrue, // return only "isPosting" true
                eventFlagEq(searchRequest.eventFlag),
                locationEq(searchRequest.location),
                eventTypeEq(searchRequest.eventType),
                dateCondition(searchRequest.startDate, searchRequest.endDate)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        // Add sorting
        if (!pageable.sort.isSorted) {
            query = query.orderBy(event.startDate.asc())
        }

        for (order in pageable.sort) {
            val entityPath: PathBuilder<*> = PathBuilder(Event::class.java, "event")
            val orderSpecifier = OrderSpecifier(
                if (order.isAscending) Order.ASC else Order.DESC,
                entityPath[order.property] as Expression<Comparable<*>>
            )
            query = query.orderBy(orderSpecifier)
        }

        val result = query.fetch()

        return PageImpl(result, pageable, totalCount ?: 0L)
    }

    private fun eventFlagEq(eventFlag: EventFlag?): BooleanExpression? {
        return eventFlag?.let { event.eventFlag.eq(it) }
    }

    private fun locationEq(location: EventLocation?): BooleanExpression? {
        return location?.let { event.location.eq(it) }
    }

    private fun eventTypeEq(eventType: String?): BooleanExpression? {
        return eventType?.let { event.eventType.eq(it) }
    }

    private fun dateCondition(startDate: LocalDate?, endDate: LocalDate?): BooleanExpression? {
        return if (startDate != null && endDate != null) {
            // If date condition exists, return all events included in these period.
            event.startDate.loe(endDate).and(event.endDate.goe(startDate))
        } else {
            // If date condition doesn't exist, return all events from now
            event.endDate.goe(LocalDate.now())
        }
    }
}