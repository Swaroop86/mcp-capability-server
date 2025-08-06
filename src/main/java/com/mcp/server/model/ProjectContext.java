package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Project context containing analyzed project information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectContext {
    private String projectPath;
    private String language;
    private String languageVersion;
    private String framework;
    private String frameworkVersion;
    private String buildTool;
    private String basePackage;
    private PackageStructure packageStructure;
    private List<Dependency> dependencies;
    private Map<String, Boolean> features;
}