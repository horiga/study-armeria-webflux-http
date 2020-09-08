package org.horiga.study.armeria.http

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.security.Security

@SpringBootApplication
class Main {
    companion object {
        init {
            Security.setProperty("networkaddress.cache.ttl", "10")
            Security.setProperty("networkaddress.cache.negative.ttl", "0")
            System.setProperty("io.netty.tryReflectionSetAccessible", "true")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<Main>(*args)
}
