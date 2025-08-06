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
public class ComponentItem {
    private String name;
    private String component;
    private String purpose;
    private String description;
    private String location;
    private List<String> features;
    private List<String> changes;
    private boolean required;
    private boolean optional;
    private boolean selected;
}