package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostExecutionStep {
    private int step;
    private String action;
    private String description;
    private boolean required;
    private String resource;

    @Override
    public String toString() {
        return "PostExecutionStep{" +
                "step=" + step +
                ", action='" + action + '\'' +
                ", description='" + description + '\'' +
                ", required=" + required +
                ", resource='" + resource + '\'' +
                '}';
    }
}