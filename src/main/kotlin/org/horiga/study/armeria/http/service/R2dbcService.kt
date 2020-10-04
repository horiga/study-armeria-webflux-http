package org.horiga.study.armeria.http.service

import org.horiga.study.armeria.http.repository.R2dbcTestRepository
import org.horiga.study.armeria.http.repository.TestEntity
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class R2dbcService(
    val r2dbcTestRepository: R2dbcTestRepository
) {
    // test, what difference throw, Flux.error
    fun search(name: String): Flux<TestEntity> = when (name) {
        "error" -> Flux.error(IllegalStateException("The name is error."))
        "throw" -> throw IllegalStateException("The name is error.")
        else -> search1(name).map { entity -> entity.copy(type = entity.type.toUpperCase()) }
    }

    fun search1(name: String): Flux<TestEntity> = r2dbcTestRepository.findByName("%$name%")
}
