package com.mcp.server.model;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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