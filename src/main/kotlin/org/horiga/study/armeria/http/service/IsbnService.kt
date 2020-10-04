package org.horiga.study.armeria.http.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
import java.util.Optional

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
        if (useCache) {
            // TODO: lookup がまだ正常に動いてない？
            CacheMono.lookup<String, Book>(
                { key ->
                    Mono.justOrEmpty(cache.getIfPresent(key)).map {
                        log.info(">> Cached: $it")
                        Signal.next(it)
                    }
                },
                isbn
            )
                .onCacheMissResume {
                    log.info(">> onCacheMissResume")
                    fromNetwork(isbn)
                }
                .andWriteWith { key, signal ->
                    log.info(">> andWriteWith")
                    Mono.fromRunnable() {
                        Optional.ofNullable(signal.get()).ifPresent { book ->
                            log.info("Put to cache store: key=$key")
                            cache.put(key, book)
                        }
                    }
                }
        } else fromNetwork(isbn)

    // https://api.openbd.jp/v1/get?isbn=978-4-7808-0204-7
    // - This API response '[null]', if not found that ISBN code.
    private fun fromNetwork(isbn: String): Mono<Book> = client.execute(
        RequestHeaders.of(
            HttpMethod.GET, "/v1/get?isbn=$isbn",
            HttpHeaderNames.ACCEPT, "application/json"
        )
    ).aggregate().handleAsync { res, err ->
        when {
            err != null -> throw IllegalStateException(err)
            res.status().isSuccess -> {
                val content = res.contentUtf8()
                log.info(
                    "fetch from network!! ISBN=$isbn, " +
                        "res.contentUtf8().isEmpty()=${res.contentUtf8().isEmpty()}, "
                    // "content=$content"
                )
                if (res.contentUtf8().isEmpty()) return@handleAsync null
                val result: List<Book> = objectMapper.readValue(content)
                if (result.isNotEmpty()) result[0]
                else throw NoSuchElementException("Not found!!")
            }
            else -> {
                log.info("Received HTTP ${res.status().code()} from /v1/get?isbn=$isbn, ${res.contentUtf8()}")
                throw IllegalStateException("Received HTTP ${res.status().code()} from /v1/get?isbn=$isbn")
            }
        }
    }.toMono()
}
