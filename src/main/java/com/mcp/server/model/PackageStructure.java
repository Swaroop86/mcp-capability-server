package com.mcp.server.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageStructure {
    private String entityPackage;
    private String repositoryPackage;
    private String servicePackage;
    private String controllerPackage;
    private String dtoPackage;
    private String configPackage;
}