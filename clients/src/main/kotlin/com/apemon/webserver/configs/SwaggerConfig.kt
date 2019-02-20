package com.apemon.webserver.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.UiConfiguration
import springfox.documentation.swagger.web.UiConfigurationBuilder
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
open class SwaggerConfig

@Bean
fun uiConfig(): UiConfiguration {
    return UiConfigurationBuilder.builder().build()
}

@Bean
fun api(): Docket {
    return Docket(DocumentationType.SWAGGER_2)
            .groupName("standard-api")
            .apiInfo(apiInfo())
            .select()
            .paths(PathSelectors.regex("/api/.*"))
            .build()
}

fun apiInfo(): ApiInfo {
    return ApiInfoBuilder()
            .title("hello world")
            .description("")
            .version("1.0")
            .build()
}