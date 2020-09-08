package org.horiga.study.armeria.http.handler

import com.linecorp.armeria.server.annotation.Default
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Produces
import org.horiga.study.armeria.http.service.TestService
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class TestHandler(
    val testService: TestService
) {
    @Throws(Exception::class)
    @Get("/annotated/test/search")
    @Produces("application/json; charset=utf-8")
    fun search(@Param("name") @Default("") name: String) = if (name.isNotBlank())
        testService.search(name)
    else Flux.empty()
}
