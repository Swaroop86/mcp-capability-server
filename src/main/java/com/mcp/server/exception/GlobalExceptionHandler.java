package com.mcp.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for the MCP server
 * Provides detailed error responses for debugging
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        log.error("Runtime exception occurred: ", ex);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("error", Map.of(
                "type", ex.getClass().getSimpleName(),
                "message", ex.getMessage(),
                "details", extractErrorDetails(ex)
        ));

        // Add helpful information for plan not found errors
        if (ex.getMessage() != null && ex.getMessage().contains("Plan not found")) {
            errorResponse.put("suggestion",
                    "The plan ID does not exist or has expired. " +
                            "Please create a new plan using /plan/create endpoint first.");
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        log.error("Resource not found: ", ex);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("error", Map.of(
                "type", "ResourceNotFound",
                "message", ex.getMessage()
        ));

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {

        log.error("Business exception: ", ex);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("error", Map.of(
                "type", "BusinessError",
                "message", ex.getMessage()
        ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Unexpected exception occurred: ", ex);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("error", Map.of(
                "type", "UnexpectedError",
                "message", "An unexpected error occurred: " + ex.getMessage()
        ));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    private String extractErrorDetails(Exception ex) {
        if (ex.getCause() != null) {
            return ex.getCause().getMessage();
        }

        // Extract first few stack trace elements for debugging
        StackTraceElement[] stackTrace = ex.getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement element = stackTrace[0];
            return String.format("at %s.%s(%s:%d)",
                    element.getClassName(),
                    element.getMethodName(),
                    element.getFileName(),
                    element.getLineNumber()
            );
        }

        return "No additional details available";
    }
}