package com.mcp.server.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request model for executing a plan with schema
 */
@Data
public class ExecutionRequest {

    private String action = "execute_plan";

    @NotBlank(message = "Plan ID is required")
    private String planId;

    @NotNull(message = "Database schema is required")
    private DatabaseSchema schema;

    private Map<String, Object> options;

    private boolean generateTests = false;
}