package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Relationship definition
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Relationship {
    private String type; // ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY
    private String from;
    private String to;
    private String mappedBy;
}