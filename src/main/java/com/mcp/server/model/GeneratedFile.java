package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

/**
 * Represents a generated or modified file
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedFile {
    private String path;
    private String action; // create, modify, delete
    private String content;
    private int size; // lines of code
    private Map<String, Object> changes; // for modified files
}