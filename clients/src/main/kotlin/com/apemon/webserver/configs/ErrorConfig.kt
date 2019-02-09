package com.apemon.webserver.configs

import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.HandlerExceptionResolver
import com.github.manosbatsis.scrudbeans.error.RestExceptionHandler

open class ErrorConfig

/**
 * Register our custom `HandlerExceptionResolver`
 */
@Bean
fun restExceptionHandler(): HandlerExceptionResolver {
    return RestExceptionHandler()
}