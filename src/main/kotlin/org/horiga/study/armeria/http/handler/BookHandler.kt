package org.horiga.study.armeria.http.handler

import com.linecorp.armeria.server.annotation.Default
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Produces
import org.horiga.study.armeria.http.service.Book
import org.horiga.study.armeria.http.service.BookService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class BookHandler(val bookService: BookService) {

    companion object {
        val log = LoggerFactory.getLogger(BookHandler::class.java)!!
    }

    @Throws(Exception::class)
    @Get("/book")
    @Produces("application/json; charset=utf-8") // for JacksonResponseConverterFunction
    fun findByIsbn(
        @Param("isbn") @Default("true") isbn: String = "978-4-7808-0204-7",
        @Param("cache") @Default("true") cache: Boolean
    ): Mono<Book> = bookService.findByIsbn(isbn, cache)
}
