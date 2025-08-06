package com.mcp.server.model;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Field definition
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Field {
    private String name;
    private String type;
    private Integer length;
    private Integer precision;
    private Integer scale;
    private boolean primaryKey;
    private boolean autoIncrement;
    private boolean nullable = true;
    private boolean unique;
    private Object defaultValue;
    private List<String> enumValues;
    private ForeignKey foreignKey;
}