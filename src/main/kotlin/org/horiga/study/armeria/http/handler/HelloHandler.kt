package org.horiga.study.armeria.http.handler

import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Produces
import org.horiga.study.armeria.http.controller.HelloReply
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant

@Component
class HelloHandler() {

    companion object {
        val log = LoggerFactory.getLogger(HelloHandler::class.java)!!
    }

    @Throws(Exception::class)
    @Get("/annotated/hello")
    @Produces("application/json; charset=utf-8") // for JacksonResponseConverterFunction
    fun hello(@Param("hello") hello: String = "Sarah") = if (hello != "error")
        Mono.just(HelloReply("Hello, annotated, $hello", Instant.now()))
    else {
        throw IllegalStateException("Test failures")
    }
}