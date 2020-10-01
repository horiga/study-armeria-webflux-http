package org.horiga.study.armeria.http.service

import org.horiga.study.armeria.http.repository.TestEntity
import org.horiga.study.armeria.http.repository.TestRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class TestService(
    val testRepository: TestRepository
) {
    // test, what difference throw, Flux.error
    fun search(name: String): Flux<TestEntity> = when (name) {
        "error" -> Flux.error(IllegalStateException("The name is error."))
        "throw" -> throw IllegalStateException("The name is error.")
        else -> search1(name).map { entity -> entity.copy(type = entity.type.toUpperCase()) }
    }

    fun search1(name: String): Flux<TestEntity> = testRepository.findByName("%$name%")
}
