package com.hbd.book_be

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EntityScan("com.hbd.book_be.domain")
@SpringBootApplication
@EnableScheduling
class BookBeApplication

fun main(args: Array<String>) {
    runApplication<BookBeApplication>(*args)
}
