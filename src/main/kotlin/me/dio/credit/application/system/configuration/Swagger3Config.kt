package me.dio.credit.application.system.configuration

import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class Swagger3Config {
    @Bean
    fun publicApi(): GroupedOpenApi?{
        return GroupedOpenApi.builder()
            .group("springcreditappsystem-public")
            .pathsToMatch("/api/customers/**", "/api/credits/**")
            .build()
    }

}