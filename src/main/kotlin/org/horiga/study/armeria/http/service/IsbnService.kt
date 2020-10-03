package org.horiga.study.armeria.http.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.linecorp.armeria.client.WebClient
import com.linecorp.armeria.common.HttpHeaderNames
import com.linecorp.armeria.common.HttpMethod
import com.linecorp.armeria.common.RequestHeaders
import com.linecorp.armeria.internal.shaded.caffeine.cache.Cache
import com.linecorp.armeria.internal.shaded.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.cache.CacheMono
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import reactor.kotlin.core.publisher.toMono
import java.time.Duration

data class Book(
    val summary: Summary
) {
    data class Summary(
        val isbn: String,
        val title: String,
        val publisher: String,
        val pubdate: String,
        val cover: String,
        val author: String
    )
}

@Service
class IsbnService(val objectMapper: ObjectMapper) {

    companion object {
        val log = LoggerFactory.getLogger(IsbnService::class.java)!!
    }

    val client = WebClient.builder("https://api.openbd.jp")
        .responseTimeout(Duration.ofMillis(3000)).build()

    val cache: Cache<String, Book> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(20))
        .recordStats()
        .build()

    fun findByIsbn(isbn: String, useCache: Boolean = true): Mono<Book> =
        if (useCache) CacheMono.lookup<String, Book>(
            { key -> Mono.justOrEmpty(cache.getIfPresent(key)).map { Signal.next(it) } },
            isbn
        )
            .onCacheMissResume {
                log.info(">> onCacheMissResume")
                fromNetwork(isbn)
            }
            .andWriteWith { key, signal ->
                log.info(">> andWriteWith")
                Mono.fromRunnable() {
                    signal.get()?.let {
                        log.info("Prepare write to cache, key=$key, value=$it")
                        cache.put(key, it)
                    }
                }
            }
        else fromNetwork(isbn)

    // https://api.openbd.jp/v1/get?isbn=$isbn
    private fun fromNetwork(isbn: String): Mono<Book> = client.execute(
        RequestHeaders.of(
            HttpMethod.GET, "/v1/get?isbn=$isbn",
            HttpHeaderNames.ACCEPT, "application/json"
        )
    ).aggregate().handleAsync { res, err ->
        when {
            err != null -> throw IllegalStateException(err)
            res.status().isSuccess -> {
                if (res.contentUtf8().isEmpty()) return@handleAsync null
                log.info("fetch from network!! ISBN=$isbn")
                objectMapper.readValue(res.contentUtf8(), Book::class.java)
            }
            else -> {
                log.info("Received HTTP ${res.status().code()} from /v1/get?isbn=$isbn, ${res.contentUtf8()}")
                throw IllegalStateException("Received HTTP ${res.status().code()} from /v1/get?isbn=$isbn")
            }
        }
    }.toMono()
}
