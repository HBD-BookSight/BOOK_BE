package com.hbd.book_be.repository

import com.hbd.book_be.domain.Contact
import org.springframework.data.jpa.repository.JpaRepository

interface ContactRepository : JpaRepository<Contact, Long> {
}