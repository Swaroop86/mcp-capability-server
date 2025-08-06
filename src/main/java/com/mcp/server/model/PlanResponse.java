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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProjectAnalysis {
    private String detectedFramework;
    private String language;
    private String buildTool;
    private String basePackage;
    private ExistingStructure existingStructure;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ExistingStructure {
    private boolean hasJPA;
    private boolean hasDatabase;
    private boolean hasLombok;
    private boolean hasValidation;
    private List<String> currentDependencies;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProposedChanges {
    private String summary;
    private List<Component> components;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Component {
    private String type;
    private String description;
    private List<ComponentItem> items;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ComponentItem {
    private String name;
    private String component;
    private String purpose;
    private String description;
    private String location;
    private List<String> features;
    private List<String> changes;
    private boolean required;
    private boolean optional;
    private boolean selected;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PlanOptions {
    private List<CustomOption> customizable;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CustomOption {
    private String id;
    private String label;
    private String type;
    private List<String> options;
    private Object defaultValue;
    private String description;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Impact {
    private String filesCreated;
    private int filesModified;
    private String estimatedLinesOfCode;
    private boolean breakingChanges;
    private boolean requiresRestart;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Compatibility {
    private String status;
    private List<CompatibilityCheck> checks;
    private List<String> warnings;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CompatibilityCheck {
    private String item;
    private String required;
    private String found;
    private String status;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class NextSteps {
    private String message;
    private RequiredInput requiredInput;
    private List<Action> actions;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class RequiredInput {
    private String type;
    private String format;
    private String example;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Action {
    private String label;
    private String action;
    private boolean requiresInput;
}