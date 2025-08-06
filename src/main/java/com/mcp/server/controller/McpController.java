package com.mcp.server.controller;

import com.mcp.server.model.*;
import com.mcp.server.service.PlanService;
import com.mcp.server.service.ExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MCP REST Controller
 * Handles plan creation and execution requests from Cursor IDE
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow Cursor IDE to connect
public class McpController {

    private final PlanService planService;
    private final ExecutionService executionService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "MCP Server PostgreSQL",
                "version", "1.0.0",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Get server capabilities
     */
    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getCapabilities() {
        return ResponseEntity.ok(Map.of(
                "server", "MCP PostgreSQL Integration Server",
                "version", "1.0.0",
                "capabilities", new String[]{"postgresql", "database-integration", "code-generation"},
                "supportedFrameworks", new String[]{"spring-boot"},
                "supportedLanguages", new String[]{"java"}
        ));
    }

    /**
     * Phase 1: Create integration plan
     * Analyzes project and creates a plan for PostgreSQL integration
     */
    @PostMapping("/plan/create")
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody PlanRequest request) {
        log.info("Creating integration plan for capability: {}", request.getCapability());
        log.debug("Request details: {}", request);

        try {
            PlanResponse response = planService.createPlan(request);
            log.info("Successfully created plan with ID: {}", response.getPlanId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating plan: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorPlanResponse(e.getMessage()));
        }
    }

    /**
     * Get existing plan by ID
     */
    @GetMapping("/plan/{planId}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable String planId) {
        log.info("Retrieving plan: {}", planId);

        try {
            PlanResponse response = planService.getPlan(planId);
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving plan: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update existing plan
     */
    @PostMapping("/plan/update")
    public ResponseEntity<PlanResponse> updatePlan(@RequestBody Map<String, Object> updateRequest) {
        String planId = (String) updateRequest.get("planId");
        log.info("Updating plan: {}", planId);

        try {
            PlanResponse response = planService.updatePlan(planId, updateRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating plan: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorPlanResponse(e.getMessage()));
        }
    }

    /**
     * Phase 2: Execute plan with schema
     * Generates code based on the plan and provided schema
     */
    @PostMapping("/plan/execute")
    public ResponseEntity<ExecutionResponse> executePlan(@Valid @RequestBody ExecutionRequest request) {
        log.info("Executing plan: {} with {} tables",
                request.getPlanId(),
                request.getSchema() != null ? request.getSchema().getTables().size() : 0);
        log.debug("Execution request: {}", request);

        try {
            ExecutionResponse response = executionService.executePlan(request);
            log.info("Successfully executed plan: {}, generated {} files",
                    request.getPlanId(), response.getSummary().getFilesGenerated());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error executing plan: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorExecutionResponse(e.getMessage()));
        }
    }

    /**
     * Quick setup - combined plan and execute for simple cases
     */
    @PostMapping("/quick-setup")
    public ResponseEntity<ExecutionResponse> quickSetup(@RequestBody Map<String, Object> quickRequest) {
        log.info("Quick setup requested");

        try {
            // Create plan
            PlanRequest planRequest = new PlanRequest();
            planRequest.setCapability((String) quickRequest.get("capability"));
            planRequest.setProjectInfo((ProjectInfo) quickRequest.get("projectInfo"));

            PlanResponse plan = planService.createPlan(planRequest);

            // Execute immediately
            ExecutionRequest execRequest = new ExecutionRequest();
            execRequest.setPlanId(plan.getPlanId());
            execRequest.setSchema(convertToSchema(quickRequest.get("schema")));

            ExecutionResponse response = executionService.executePlan(execRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in quick setup: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorExecutionResponse(e.getMessage()));
        }
    }

    /**
     * Delete/cancel a plan
     */
    @DeleteMapping("/plan/{planId}")
    public ResponseEntity<Map<String, Object>> deletePlan(@PathVariable String planId) {
        log.info("Deleting plan: {}", planId);

        try {
            boolean deleted = planService.deletePlan(planId);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Plan deleted successfully",
                        "planId", planId
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting plan: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
        // Convert the schema object from the quick request
        // This would use Jackson ObjectMapper in a real implementation
        return new DatabaseSchema();
    }
}