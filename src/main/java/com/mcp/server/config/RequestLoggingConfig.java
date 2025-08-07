package com.mcp.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for request/response logging
 * Enables detailed logging of all HTTP requests and responses
 */
@Configuration
public class RequestLoggingConfig implements WebMvcConfigurer {

    /**
     * Configure request logging filter
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        filter.setIncludeClientInfo(true);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        filter.setBeforeMessagePrefix("INCOMING REQUEST: ");
        return filter;
    }

    /**
     * Add logging interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor());
    }
}