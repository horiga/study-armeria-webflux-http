package org.horiga.study.armeria.http.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.logging.LogLevel
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction
import com.linecorp.armeria.server.HttpService
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.SimpleDecoratingHttpService
import com.linecorp.armeria.server.annotation.JacksonResponseConverterFunction
import com.linecorp.armeria.server.logging.AccessLogWriter
import com.linecorp.armeria.server.logging.LoggingService
import com.linecorp.armeria.server.metric.MetricCollectingService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import io.netty.util.AttributeKey
import org.horiga.study.armeria.http.handler.HelloHandler
import org.horiga.study.armeria.http.handler.BookHandler
import org.horiga.study.armeria.http.handler.MyExceptionHandler
import org.horiga.study.armeria.http.handler.R2dbcHandler
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

class MyTestDecorator(delegate: HttpService): SimpleDecoratingHttpService(delegate) {

    companion object {
        val log = LoggerFactory.getLogger(MyTestDecorator::class.java)!!
        val REQUEST_ID = AttributeKey.valueOf<String>(String::class.java, "request_id")!!
    }

    override fun serve(ctx: ServiceRequestContext, req: HttpRequest): HttpResponse {
        ctx.setAttr(REQUEST_ID, req.headers()["x-request-id"] ?: UUID.randomUUID().toString())
        return unwrap().serve(ctx, req)
    }
}

@ConfigurationProperties(prefix = "myapp")
@ConstructorBinding
data class MyApplicationProperties(
    @NestedConfigurationProperty
    val book: BookProperties
) {
    data class BookProperties(
        val endpoint: String
    )
}

@Configuration
@EnableConfigurationProperties(MyApplicationProperties::class)
class ArmeriaServerConfiguration {

    // Refs: https://github.com/line/armeria/blob/master/examples/spring-boot-webflux/src/main/java/example/springframework/boot/webflux/HelloConfiguration.java
    @Bean
    fun armeriaServerConfigurator(
        helloHandler: HelloHandler,
        r2dbcHandler: R2dbcHandler,
        bookHandler: BookHandler,
        objectMapper: ObjectMapper,
        exceptionHandler: MyExceptionHandler
    ) = ArmeriaServerConfigurator { sb ->
        // Enable if support gRPC or Thrift RPC protocols.
        // sb.serviceUnder("/docs", DocService())
        sb.decorator(
            LoggingService.builder()
                .requestLogLevel(LogLevel.DEBUG)
                .successfulResponseLogLevel(LogLevel.DEBUG)
                .failureResponseLogLevel(LogLevel.WARN)
                .newDecorator()
        )
        sb.accessLogWriter(AccessLogWriter.combined(), false)

        sb.annotatedService()
            .decorator(
                MetricCollectingService.newDecorator(
                    MeterIdPrefixFunction
                        .ofDefault("armeria.server.http")
                        .withTags("service", "book")
                )
            )
            .responseConverters(JacksonResponseConverterFunction(objectMapper))
            .exceptionHandlers(exceptionHandler)
            .build(bookHandler)

        sb.annotatedService()
            .decorator(
                MetricCollectingService.newDecorator(
                    MeterIdPrefixFunction
                        .ofDefault("armeria.server.http")
                        .withTags("service", "hello")
                )
            )
            .responseConverters(JacksonResponseConverterFunction(objectMapper))
            .exceptionHandlers(exceptionHandler)
            .build(helloHandler)

        sb.annotatedService()
            .decorator(
                MetricCollectingService.newDecorator(
                    MeterIdPrefixFunction
                        .ofDefault("armeria.server.http")
                        .withTags("service", "test")
                )
            )
            .decorator { delegate -> MyTestDecorator(delegate) }
            .responseConverters(JacksonResponseConverterFunction(objectMapper))
            .exceptionHandlers(exceptionHandler)
            .build(r2dbcHandler)
    }

    @Bean
    fun exceptionHandler(objectMapper: ObjectMapper) = MyExceptionHandler(objectMapper)

    @Bean
    fun objectMapper() = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(SerializationFeature.INDENT_OUTPUT, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}
