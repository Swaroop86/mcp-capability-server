package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Internal representation of an integration plan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationPlan {
    private String planId;
    private String capability;
    private ProjectContext projectContext;
    private Map<String, Object> options;
    private PlanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}

/**
 * Plan status enumeration
 */
public enum PlanStatus {
    CREATED,
    UPDATED,
    EXECUTING,
    COMPLETED,
    EXPIRED,
    ERROR
}