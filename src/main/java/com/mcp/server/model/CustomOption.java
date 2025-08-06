package com.mcp.server.model;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomOption {
    private String id;
    private String label;
    private String type;
    private List<String> options;
    private Object defaultValue;
    private String description;
}
