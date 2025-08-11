package com.mcp.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.server.model.*;
import com.mcp.server.service.PlanService;
import com.mcp.server.service.ExecutionService;
import com.mcp.server.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * MCP REST Controller with enhanced logging for debugging
 * FIXED: Added cache clearing and request scope
 */
@RestController
@RequestMapping("/")
@RequestScope
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class McpController {

    private final PlanService planService;
    private final ExecutionService executionService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    private CacheManager cacheManager;

    private final AtomicLong requestCounter = new AtomicLong(0);
    private final Map<String, Long> requestTracker = new ConcurrentHashMap<>();

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "MCP Server PostgreSQL",
                "version", "1.0.0",
                "timestamp", System.currentTimeMillis(),
                "serverTime", LocalDateTime.now().toString()
        );
        log.debug("Health check response: {}", health);
        return ResponseEntity.ok(health);
    }

    /**
     * Get server capabilities
     */
    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getCapabilities() {
        Map<String, Object> capabilities = Map.of(
                "server", "MCP PostgreSQL Integration Server",
                "version", "1.0.0",
                "capabilities", new String[]{"postgresql", "database-integration", "code-generation"},
                "supportedFrameworks", new String[]{"spring-boot"},
                "supportedLanguages", new String[]{"java"},
                "features", Map.of(
                        "lombok", true,
                        "validation", true,
                        "auditing", true,
                        "softDelete", true
                )
        );
        log.debug("Capabilities response: {}", capabilities);
        return ResponseEntity.ok(capabilities);
    }

    /**
     * Phase 1: Create integration plan
     */
    @PostMapping(value = "/plan/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlanResponse> createPlan(
            @Valid @RequestBody PlanRequest request,
            HttpServletRequest httpRequest) {

        String adapterRequestId = httpRequest.getHeader("X-Adapter-Request-ID");
        Long serverRequestNum = requestTracker.get(adapterRequestId);

        try {
            log.info("╔═══════════════════════════════════════════════════════════════╗");
            log.info("║ PLAN CREATE - Server Request #{} / Adapter Request #{}        ║",
                    serverRequestNum, adapterRequestId);
            log.info("╠═══════════════════════════════════════════════════════════════╣");
            log.info("║ Request Body:                                                 ║");
            log.info("║ {}", objectMapper.writeValueAsString(request));
            log.info("╚═══════════════════════════════════════════════════════════════╝");

            PlanResponse response = planService.createPlan(request);

            log.info("╔═══════════════════════════════════════════════════════════════╗");
            log.info("║ PLAN CREATE RESPONSE - Request #{}                            ║", serverRequestNum);
            log.info("╠═══════════════════════════════════════════════════════════════╣");
            log.info("║ Plan ID: {}                                                   ║", response.getPlanId());
            log.info("║ Status: {}                                                    ║", response.getStatus());
            log.info("╚═══════════════════════════════════════════════════════════════╝");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error in request #{}: {}", serverRequestNum, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorPlanResponse(e.getMessage()));
        }
    }

    /**
     * Get existing plan by ID
     */
    @GetMapping("/plan/{planId}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable String planId) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        try {
            log.info("=== GET PLAN REQUEST [{}] ===", requestId);
            log.info("Plan ID [{}]: {}", requestId, planId);

            PlanResponse response = planService.getPlan(planId);

            if (response != null) {
                log.info("Plan found [{}]: {}", requestId, planId);
                log.debug("Plan details [{}]: {}", requestId,
                        objectMapper.writeValueAsString(response));
                return ResponseEntity.ok(response);
            } else {
                log.warn("Plan not found [{}]: {}", requestId, planId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving plan [{}]: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update existing plan
     */
    @PostMapping("/plan/update")
    public ResponseEntity<PlanResponse> updatePlan(@RequestBody Map<String, Object> updateRequest) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String planId = (String) updateRequest.get("planId");

        log.info("=== UPDATE PLAN REQUEST [{}] ===", requestId);
        log.info("Plan ID [{}]: {}", requestId, planId);

        try {
            PlanResponse response = planService.updatePlan(planId, updateRequest);
            log.info("Plan updated [{}]: {}", requestId, response.getPlanId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating plan [{}]: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorPlanResponse(e.getMessage()));
        }
    }

    /**
     * Phase 2: Execute plan with schema - FIXED with cache clearing
     */
    @PostMapping(value = "/plan/execute",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExecutionResponse> executePlan(
            @Valid @RequestBody ExecutionRequest request,
            HttpServletRequest httpRequest) {

        String adapterRequestId = httpRequest.getHeader("X-Adapter-Request-ID");
        Long serverRequestNum = requestTracker.get(adapterRequestId);

        // Clear template cache before execution
        clearTemplateCache();

        try {
            log.info("╔═══════════════════════════════════════════════════════════════╗");
            log.info("║ PLAN EXECUTE - Server Request #{} / Adapter Request #{}       ║",
                    serverRequestNum, adapterRequestId);
            log.info("╠═══════════════════════════════════════════════════════════════╣");
            log.info("║ Plan ID: {}                                                   ║", request.getPlanId());
            log.info("║ Tables: {}                                                    ║",
                    request.getSchema().getTables().stream()
                            .map(Table::getName)
                            .collect(Collectors.joining(", ")));
            log.info("╚═══════════════════════════════════════════════════════════════╝");

            ExecutionResponse response = executionService.executePlan(request);

            log.info("╔═══════════════════════════════════════════════════════════════╗");
            log.info("║ PLAN EXECUTE RESPONSE - Request #{}                           ║", serverRequestNum);
            log.info("╠═══════════════════════════════════════════════════════════════╣");
            log.info("║ Execution ID: {}                                              ║", response.getExecutionId());
            log.info("║ Files Generated: {}                                           ║",
                    response.getSummary().getFilesGenerated());
            log.info("╚═══════════════════════════════════════════════════════════════╝");
            log.info("║ File names Generated: {}                                           ║",
                    response.getGeneratedFiles());
            log.info("╚═══════════════════════════════════════════════════════════════╝");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error in request #{}: {}", serverRequestNum, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorExecutionResponse(e.getMessage()));
        }
    }

    /**
     * Clear all caches - NEW ENDPOINT
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        try {
            // Clear Spring caches
            if (cacheManager != null) {
                cacheManager.getCacheNames().forEach(cacheName -> {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                        log.info("Cleared cache: {}", cacheName);
                    }
                });
            }

            // Clear template cache
            templateService.clearCache();

            log.info("All caches cleared successfully");

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All caches cleared",
                    "timestamp", LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error clearing caches: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage(),
                            "timestamp", LocalDateTime.now().toString()
                    ));
        }
    }

    /**
     * Quick setup - combined plan and execute
     */
    @PostMapping("/quick-setup")
    public ResponseEntity<ExecutionResponse> quickSetup(@RequestBody Map<String, Object> quickRequest) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.info("=== QUICK SETUP REQUEST [{}] ===", requestId);

        // Clear caches before quick setup
        clearTemplateCache();

        try {
            // Create plan
            PlanRequest planRequest = new PlanRequest();
            planRequest.setCapability((String) quickRequest.getOrDefault("capability", "postgresql"));

            // Set project info
            ProjectInfo projectInfo = new ProjectInfo();
            projectInfo.setPath((String) quickRequest.getOrDefault("path", "."));
            projectInfo.setDescription((String) quickRequest.getOrDefault("description", "Quick setup"));
            planRequest.setProjectInfo(projectInfo);

            PlanResponse plan = planService.createPlan(planRequest);
            log.info("Quick setup plan created [{}]: {}", requestId, plan.getPlanId());

            // Execute immediately
            ExecutionRequest execRequest = new ExecutionRequest();
            execRequest.setPlanId(plan.getPlanId());
            execRequest.setSchema(convertToSchema(quickRequest.get("schema")));

            ExecutionResponse response = executionService.executePlan(execRequest);
            log.info("Quick setup completed [{}]", requestId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in quick setup [{}]: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorExecutionResponse(e.getMessage()));
        }
    }

    /**
     * Delete/cancel a plan
     */
    @DeleteMapping("/plan/{planId}")
    public ResponseEntity<Map<String, Object>> deletePlan(@PathVariable String planId) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.info("=== DELETE PLAN REQUEST [{}] ===", requestId);
        log.info("Plan ID [{}]: {}", requestId, planId);

        try {
            boolean deleted = planService.deletePlan(planId);
            if (deleted) {
                log.info("Plan deleted [{}]: {}", requestId, planId);
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Plan deleted successfully",
                        "planId", planId
                ));
            } else {
                log.warn("Plan not found for deletion [{}]: {}", requestId, planId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting plan [{}]: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clear template cache helper method
     */
    private void clearTemplateCache() {
        try {
            templateService.clearCache();
            log.debug("Template cache cleared before execution");
        } catch (Exception e) {
            log.warn("Could not clear template cache: {}", e.getMessage());
        }
    }

    // Helper methods
    private PlanResponse createErrorPlanResponse(String message) {
        PlanResponse response = new PlanResponse();
        response.setStatus("error");
        response.setError(Map.of(
                "message", message,
                "timestamp", System.currentTimeMillis()
        ));
        return response;
    }

    private ExecutionResponse createErrorExecutionResponse(String message) {
        ExecutionResponse response = new ExecutionResponse();
        response.setStatus("error");
        response.setError(Map.of(
                "message", message,
                "timestamp", System.currentTimeMillis()
        ));
        return response;
    }

    private DatabaseSchema convertToSchema(Object schemaObj) {
        try {
            return objectMapper.convertValue(schemaObj, DatabaseSchema.class);
        } catch (Exception e) {
            log.error("Error converting schema: ", e);
            return new DatabaseSchema();
        }
    }

    @ModelAttribute
    public void logRequestDetails(HttpServletRequest request) {
        long requestNumber = requestCounter.incrementAndGet();
        String requestId = request.getHeader("X-Adapter-Request-ID");

        if (requestId != null) {
            requestTracker.put(requestId, requestNumber);
        }

        log.info("════════════════════════════════════════════════════════════════");
        log.info("MCP SERVER REQUEST #{} [Adapter Request ID: {}]", requestNumber, requestId);
        log.info("────────────────────────────────────────────────────────────────");
        log.info("Time: {}", LocalDateTime.now());
        log.info("Method: {} {}", request.getMethod(), request.getRequestURI());
        log.info("Remote: {} ({})", request.getRemoteAddr(), request.getRemoteHost());
        log.info("Headers:");

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.info("  {}: {}", headerName, request.getHeader(headerName));
        }
        log.info("────────────────────────────────────────────────────────────────");
    }
}