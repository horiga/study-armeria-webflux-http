package org.horiga.study.armeria.http.service

import org.horiga.study.armeria.http.repository.MemberEntity
import org.horiga.study.armeria.http.repository.MemberRepository
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux

@Service
class MemberService(
    val memberRepository: MemberRepository
) {
    companion object {
        val log = LoggerFactory.getLogger(MemberService::class.java)!!
    }

    // test, what difference throw, Flux.error
    fun search(name: String): Flux<MemberEntity> = when (name) {
        "error" -> Flux.error(IllegalStateException("The name is error."))
        "throw" -> throw IllegalStateException("The name is error.")
        else -> search1(name).map { entity -> entity.copy(type = entity.type.toUpperCase()) }
    }

    fun search1(name: String): Flux<MemberEntity> = memberRepository.findByName("%$name%")

    @Transactional
    fun add(name: String, type: String?, error: Boolean = false) = memberRepository.save(
        MemberEntity(name, type ?: "general", DateTime.now())
    ).doOnNext {
        log.info("Finished, Member add {}", it)
        // for tx rollback testing
        if (error) {
            throw IllegalStateException("FAIL!! ROLLBACK!!")
        }
    }
}
