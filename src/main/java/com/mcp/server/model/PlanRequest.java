package com.mcp.server.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Request model for creating an integration plan
 */
@Data
public class PlanRequest {

    @NotBlank(message = "Action is required")
    private String action = "create_plan";

    @NotBlank(message = "Capability is required")
    private String capability; // e.g., "postgresql"

    private ProjectInfo projectInfo;

    private Map<String, Object> preferences;
}

/**
 * Project information
 */
@Data
class ProjectInfo {
    private String path;
    private String description;
}