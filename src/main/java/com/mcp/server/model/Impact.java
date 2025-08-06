package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Impact {
    private String filesCreated;
    private int filesModified;
    private String estimatedLinesOfCode;
    private boolean breakingChanges;
    private boolean requiresRestart;
}