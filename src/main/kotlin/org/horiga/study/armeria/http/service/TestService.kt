package org.horiga.study.armeria.http.service

import org.horiga.study.armeria.http.repository.TestEntity
import org.horiga.study.armeria.http.repository.TestRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Duration

@Service
class TestService(
    val testRepository: TestRepository
) {
    fun search(name: String): Flux<TestEntity> =
        testRepository.findByName("%$name%")
            .timeout(Duration.ofMillis(3000))
}
