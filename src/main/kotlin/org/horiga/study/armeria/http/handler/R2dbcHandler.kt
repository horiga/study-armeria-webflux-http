package org.horiga.study.armeria.http.handler

import com.linecorp.armeria.server.annotation.Default
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Produces
import org.horiga.study.armeria.http.service.R2dbcService
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class R2dbcHandler(
    val r2dbcService: R2dbcService
) {
    @Throws(Exception::class)
    @Get("/annotated/r2dbc/search")
    @Produces("application/json; charset=utf-8")
    fun search(@Param("name") @Default("") name: String) = if (name.isNotBlank())
        r2dbcService.search(name)
    else Flux.empty()
}
