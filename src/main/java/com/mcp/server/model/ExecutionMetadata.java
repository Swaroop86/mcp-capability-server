package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionMetadata {
    private String executionTime;
    private String sdkVersion;
    private String standardsVersion;
    private String timestamp;
}