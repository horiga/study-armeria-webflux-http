package org.horiga.study.armeria.http.configuration

import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.logging.AccessLogWriter
import com.linecorp.armeria.server.logging.LoggingService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ArmeriaServerConfiguration {

    // Refs: https://github.com/line/armeria/blob/master/examples/spring-boot-webflux/src/main/java/example/springframework/boot/webflux/HelloConfiguration.java
    @Bean
    fun armeriaServerConfigurator() = ArmeriaServerConfigurator { sb ->
        sb.serviceUnder("/docs", DocService())
        sb.decorator(LoggingService.newDecorator())
        sb.accessLogWriter(AccessLogWriter.combined(), false);
    }
}