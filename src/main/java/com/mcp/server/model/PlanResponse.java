package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Response model for integration plan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {

    private String planId;
    private String capability;
    private String status;
    private ProjectAnalysis projectAnalysis;
    private ProposedChanges proposedChanges;
    private PlanOptions options;
    private Impact impact;
    private Compatibility compatibility;
    private NextSteps nextSteps;
    private String expiresIn;
    private String created;
    private Map<String, Object> error;
}