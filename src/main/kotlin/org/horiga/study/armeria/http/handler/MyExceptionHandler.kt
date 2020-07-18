package org.horiga.study.armeria.http.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.common.MediaType
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.ExceptionHandlerFunction
import org.slf4j.LoggerFactory

data class ErrorReply(val message: String)

class MyExceptionHandler(private val objectMapper: ObjectMapper) : ExceptionHandlerFunction {

    companion object {
        val log = LoggerFactory.getLogger(MyExceptionHandler::class.java)!!
    }

    override fun handleException(
        ctx: ServiceRequestContext,
        req: HttpRequest,
        cause: Throwable
    ): HttpResponse {
        log.warn("Handle HTTP exception: ${req.method()} ${req.path()}, cause: $cause", cause)
        val state = when(cause) {
            is IllegalStateException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        return HttpResponse.of(
            state,
            MediaType.JSON_UTF_8,
            objectMapper.writeValueAsString(ErrorReply(cause.message ?: ""))
        )
    }
}