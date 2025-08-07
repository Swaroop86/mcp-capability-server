package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Response model for plan execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {
    private String executionId;
    private String planId;
    private String status;
    private ExecutionSummary summary;
    private List<FileCategory> generatedFiles;
    private List<PostExecutionStep> postExecutionSteps;
    private ValidationResult validation;
    private ExecutionMetadata metadata;
    private Map<String, Object> error;

    @Override
    public String toString() {
        return "ExecutionResponse{" +
                "executionId='" + executionId + '\'' +
                ", planId='" + planId + '\'' +
                ", status='" + status + '\'' +
                ", summary=" + summary +
                ", generatedFiles=" + generatedFiles +
                ", postExecutionSteps=" + postExecutionSteps +
                ", validation=" + validation +
                ", metadata=" + metadata +
                ", error=" + error +
                '}';
    }
}
