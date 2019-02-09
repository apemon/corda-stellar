package com.apemon.webserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType.SERVLET
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
 * Our Spring Boot application.
 */
@SpringBootApplication(exclude = arrayOf(SecurityAutoConfiguration::class, ErrorMvcAutoConfiguration::class))
@ComponentScan(basePackages = arrayOf("com.github.manosbatsis.corbeans", "com.apemon.webserver"))
open class Starter

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    runApplication<Starter>(*args)
}
