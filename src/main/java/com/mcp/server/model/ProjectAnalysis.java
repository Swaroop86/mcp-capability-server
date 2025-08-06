package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAnalysis {
    private String detectedFramework;
    private String language;
    private String buildTool;
    private String basePackage;
    private ExistingStructure existingStructure;
}