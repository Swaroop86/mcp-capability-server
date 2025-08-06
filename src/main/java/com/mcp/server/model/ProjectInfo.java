package com.mcp.server.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Project information
 */
@Data
public class ProjectInfo {
    private String path;
    private String description;
}