package com.mcp.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Interceptor for logging all HTTP requests and responses
 */
@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Generate request ID for tracking
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute("requestId", requestId);

        log.info("=== MCP Request Start [{}] ===", requestId);
        log.info("Method: {} URI: {} QueryString: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString());
        log.info("Remote Address: {} Remote Host: {}",
                request.getRemoteAddr(),
                request.getRemoteHost());

        // Log headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        log.info("Headers: {}", objectMapper.writeValueAsString(headers));

        // Log parameters
        Map<String, String[]> params = request.getParameterMap();
        if (!params.isEmpty()) {
            log.info("Parameters: {}", objectMapper.writeValueAsString(params));
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String requestId = (String) request.getAttribute("requestId");
        log.info("Response Status [{}]: {}", requestId, response.getStatus());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestId = (String) request.getAttribute("requestId");

        // Log request body if available
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                String requestBody = new String(content, StandardCharsets.UTF_8);
                log.info("Request Body [{}]: {}", requestId, requestBody);
            }
        }

        // Log exception if any
        if (ex != null) {
            log.error("Request failed [{}]: {}", requestId, ex.getMessage(), ex);
        }

        log.info("=== MCP Request End [{}] ===\n", requestId);
    }
}