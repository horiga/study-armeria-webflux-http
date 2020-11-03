package org.horiga.study.armeria.http.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.linecorp.armeria.client.WebClient
import com.linecorp.armeria.common.HttpHeaderNames
import com.linecorp.armeria.common.HttpMethod
import com.linecorp.armeria.common.RequestHeaders
import com.linecorp.armeria.internal.shaded.caffeine.cache.Cache
import com.linecorp.armeria.internal.shaded.caffeine.cache.Caffeine
import org.horiga.study.armeria.http.configuration.MyApplicationProperties
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
    companion object {
        fun empty() = Book(Summary("", "", "", "", "", ""))
    }

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
class BookService(
    val objectMapper: ObjectMapper,
    private val properties: MyApplicationProperties
) {

    companion object {
        val log = LoggerFactory.getLogger(BookService::class.java)!!
    }

    val client = WebClient.builder(properties.book.endpoint)
        .responseTimeout(Duration.ofMillis(3000)).build()

    val cacheRefs: Cache<String, Book> = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofSeconds(60))
        .recordStats()
        .build()

    fun findByIsbn(isbn: String, useCache: Boolean = true): Mono<Book> =
        if (useCache) {
            CacheMono.lookup<String, Book>(
                { key ->
                    Mono.justOrEmpty(cacheRefs.getIfPresent(key)).map { book ->
                        log.info(">> Hit from cached store({}): {}", key, book)
                        Signal.next(book)
                    }
                },
                isbn
            )
                .onCacheMissResume {
                    log.info("[[ onCacheMissResume ]]") // これはcacheにあっても毎回呼ばれる
                    // Mono.deferしとかないとfromNetwork呼ばれちゃうので注意が必要だった
                    Mono.defer {
                        fromNetwork(isbn).doOnSubscribe { _ ->
                            // subscribe は cache に無いとき、すなわちfromNetworkが呼び出されるときだけ呼ばれる
                            log.info("(onCacheMissResume/subscribe, Really fetch from network")
                        }
                    }
                }
                .andWriteWith { key, signal ->
                    Mono.fromRunnable<Void> {
                        log.info(">> andWriteWith: signal.get()={}", signal.get())
                        signal.get()?.let { book ->
                            log.info("[[ Put to cache store: key={} ]]", key)
                            cacheRefs.put(key, book)
                        }
                    }
                }
                .onErrorResume { err ->
                    log.error("Error, fetch from cache or network. isbn={}", isbn, err)
                    Mono.just(Book.empty())
                }
        } else fromNetwork(isbn)

    // https://api.openbd.jp/v1/get?isbn=978-4-7808-0204-7
    // - This API response '[null]', if not found that ISBN code.
    private fun fromNetwork(isbn: String): Mono<Book> =
        client.execute(
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
                        "fetch from network!! ISBN={}, res.contentUtf8().isEmpty()={} ",
                        isbn, res.contentUtf8().isEmpty()
                    )
                    if (res.contentUtf8().isEmpty()) return@handleAsync null
                    val result: List<Book> = objectMapper.readValue(content)
                    if (result.isNotEmpty()) result[0]
                    else throw NoSuchElementException("Not found!!")
                }
                else -> {
                    log.info(
                        "Received HTTP {} from /v1/get?isbn={}, {}",
                        res.status().code(), isbn, res.contentUtf8()
                    )
                    throw IllegalStateException("Received HTTP ${res.status().code()} from /v1/get?isbn=$isbn")
                }
            }
        }.toMono()

    // for test
    fun isCached(isbn: String) = cacheRefs.getIfPresent(isbn) != null
}
