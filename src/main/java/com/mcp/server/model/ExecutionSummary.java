package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionSummary {
    private int tablesProcessed;
    private int filesGenerated;
    private int filesModified;
    private int dependenciesAdded;
    private int totalLinesOfCode;

    @Override
    public String toString() {
        return "ExecutionSummary{" +
                "tablesProcessed=" + tablesProcessed +
                ", filesGenerated=" + filesGenerated +
                ", filesModified=" + filesModified +
                ", dependenciesAdded=" + dependenciesAdded +
                ", totalLinesOfCode=" + totalLinesOfCode +
                '}';
    }
}