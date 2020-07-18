package org.horiga.study.armeria.http.controller

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.Instant

data class HelloReply(
    val message: String,
    @JsonProperty("received_at")
    val receivedAt: Instant
)

@RestController
class HelloController(val objectMapper: ObjectMapper) {
    @GetMapping("hello")
    fun hello(
        @RequestParam(value = "hello", required = false, defaultValue = "John") hello: String
    ): Mono<HelloReply> {
        return Mono.just(HelloReply(hello, Instant.now()))
    }
}