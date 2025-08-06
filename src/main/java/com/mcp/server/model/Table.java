package com.mcp.server.model;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Table definition
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    private String name;
    private List<Field> fields;
}
