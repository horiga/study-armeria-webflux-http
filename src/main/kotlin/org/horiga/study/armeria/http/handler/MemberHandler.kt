package org.horiga.study.armeria.http.handler

import com.linecorp.armeria.server.annotation.*
import org.horiga.study.armeria.http.service.MemberService
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

data class AddMemberMessage(
    var name: String = "",
    var type: String = ""
)

@Component
class MemberHandler(
    val memberService: MemberService
) {
    @Throws(Exception::class)
    @Get("/member")
    @Produces("application/json; charset=utf-8")
    fun search(@Param("name") @Default("") name: String) = if (name.isNotBlank())
        memberService.search(name)
    else Flux.empty()

    @Post("/member")
    @Consumes("application/json")
    @Produces("application/json; charset=utf-8")
    fun add(
        @RequestObject message: AddMemberMessage,
        @Param("error") @Default("false") error: Boolean
    ) = memberService.add(message.name, message.type, error)
}
