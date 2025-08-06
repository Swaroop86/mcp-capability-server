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
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ExecutionSummary {
    private int tablesProcessed;
    private int filesGenerated;
    private int filesModified;
    private int dependenciesAdded;
    private int totalLinesOfCode;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class FileCategory {
    private String category;
    private List<GeneratedFile> files;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PostExecutionStep {
    private int step;
    private String action;
    private String description;
    private boolean required;
    private String resource;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ValidationResult {
    private String compilationCheck;
    private String dependencyCheck;
    private String namingConventions;
    private CodeQuality codeQuality;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CodeQuality {
    private int score;
    private List<String> issues;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ExecutionMetadata {
    private String executionTime;
    private String sdkVersion;
    private String standardsVersion;
    private String timestamp;
}