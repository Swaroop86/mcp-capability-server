package com.mcp.server.model;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExistingStructure {
    private boolean hasJPA;
    private boolean hasDatabase;
    private boolean hasLombok;
    private boolean hasValidation;
    private List<String> currentDependencies;
}