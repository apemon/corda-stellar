package com.apemon.webserver

import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType.SERVLET
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
@EnableSwagger2
private open class Starter

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    val app = SpringApplication(Starter::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.webApplicationType = SERVLET
    app.run(*args)
}

@Bean
fun standardApi(): Docket {
    return Docket(DocumentationType.SWAGGER_2)
            .groupName("standard-api")
            .apiInfo(apiInfo())
            .select()
            .paths(PathSelectors.regex("/api.*"))
            .build()
}

fun apiInfo(): ApiInfo {
    return ApiInfoBuilder()
            .title("hello world")
            .description("")
            .version("1.0")
            .build()
}
