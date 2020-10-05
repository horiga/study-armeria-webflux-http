package org.horiga.study.armeria.http.handler

import com.linecorp.armeria.server.annotation.Default
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Produces
import org.horiga.study.armeria.http.service.Book
import org.horiga.study.armeria.http.service.IsbnService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class IsbnHandler(val isbnService: IsbnService) {

    companion object {
        val log = LoggerFactory.getLogger(IsbnHandler::class.java)!!
    }

    @Throws(Exception::class)
    @Get("/book")
    @Produces("application/json; charset=utf-8") // for JacksonResponseConverterFunction
    fun findByIsbn(
        @Param("isbn") isbn: String = "978-4-7808-0204-7",
        @Param("cache") @Default("true") cache: Boolean
    ): Mono<Book> = isbnService.findByIsbn(isbn, cache)
}
