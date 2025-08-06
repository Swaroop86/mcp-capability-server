package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Database schema definition
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseSchema {
    private List<Table> tables;
    private List<Relationship> relationships;
}

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

/**
 * Foreign key definition
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForeignKey {
    private String table;
    private String column;
    private String onDelete = "CASCADE";
    private String onUpdate = "CASCADE";
}

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