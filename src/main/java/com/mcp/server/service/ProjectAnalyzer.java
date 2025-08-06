package com.mcp.server.service;

import com.mcp.server.model.ProjectContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analyzing project structure and configuration
 */
@Service
@Slf4j
public class ProjectAnalyzer {

    /**
     * Analyze project at given path
     */
    public ProjectContext analyzeProject(String projectPath) {
        log.info("Analyzing project at: {}", projectPath);

        if (projectPath == null || projectPath.isEmpty()) {
            projectPath = ".";
        }

        Path path = Paths.get(projectPath);

        // Detect build tool
        String buildTool = detectBuildTool(path);

        // Parse project configuration
        ProjectContext.ProjectContextBuilder contextBuilder = ProjectContext.builder()
                .projectPath(projectPath)
                .buildTool(buildTool);

        if ("Maven".equals(buildTool)) {
            analyzeMavenProject(path, contextBuilder);
        } else if ("Gradle".equals(buildTool)) {
            analyzeGradleProject(path, contextBuilder);
        } else {
            // Default values for unknown project
            setDefaultValues(contextBuilder);
        }

        // Detect package structure
        detectPackageStructure(path, contextBuilder);

        // Detect features
        Map<String, Boolean> features = detectFeatures(path);
        contextBuilder.features(features);

        return contextBuilder.build();
    }

    /**
     * Detect build tool
     */
    private String detectBuildTool(Path projectPath) {
        if (Files.exists(projectPath.resolve("pom.xml"))) {
            return "Maven";
        } else if (Files.exists(projectPath.resolve("build.gradle")) ||
                Files.exists(projectPath.resolve("build.gradle.kts"))) {
            return "Gradle";
        }
        return "Unknown";
    }

    /**
     * Analyze Maven project
     */
    private void analyzeMavenProject(Path projectPath, ProjectContext.ProjectContextBuilder builder) {
        try {
            File pomFile = projectPath.resolve("pom.xml").toFile();
            if (!pomFile.exists()) {
                setDefaultValues(builder);
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile);
            doc.getDocumentElement().normalize();

            // Get Java version
            String javaVersion = getXmlValue(doc, "maven.compiler.source");
            if (javaVersion == null) {
                javaVersion = getXmlValue(doc, "java.version");
            }
            if (javaVersion == null) {
                javaVersion = "17"; // Default
            }
            builder.language("Java");
            builder.languageVersion(javaVersion);

            // Get group ID as base package
            String groupId = getXmlValue(doc, "groupId");
            String artifactId = getXmlValue(doc, "artifactId");
            if (groupId != null) {
                builder.basePackage(groupId + (artifactId != null ? "." + artifactId.replace("-", "") : ""));
            } else {
                builder.basePackage("com.example");
            }

            // Detect Spring Boot
            NodeList dependencies = doc.getElementsByTagName("dependency");
            List<ProjectContext.Dependency> projectDeps = new ArrayList<>();
            boolean isSpringBoot = false;
            String springBootVersion = "3.2.0"; // Default

            for (int i = 0; i < dependencies.getLength(); i++) {
                org.w3c.dom.Element dep = (org.w3c.dom.Element) dependencies.item(i);
                String depGroupId = getElementValue(dep, "groupId");
                String depArtifactId = getElementValue(dep, "artifactId");
                String depVersion = getElementValue(dep, "version");

                if ("org.springframework.boot".equals(depGroupId)) {
                    isSpringBoot = true;
                    if (depArtifactId.equals("spring-boot-starter-parent") && depVersion != null) {
                        springBootVersion = depVersion;
                    }
                }

                projectDeps.add(ProjectContext.Dependency.builder()
                        .groupId(depGroupId)
                        .artifactId(depArtifactId)
                        .version(depVersion)
                        .build());
            }

            if (isSpringBoot) {
                builder.framework("Spring Boot");
                builder.frameworkVersion(springBootVersion);
            }

            builder.dependencies(projectDeps);

        } catch (Exception e) {
            log.error("Error analyzing Maven project: ", e);
            setDefaultValues(builder);
        }
    }

    /**
     * Analyze Gradle project
     */
    private void analyzeGradleProject(Path projectPath, ProjectContext.ProjectContextBuilder builder) {
        // Simplified Gradle analysis
        try {
            Path buildFile = projectPath.resolve("build.gradle");
            if (!Files.exists(buildFile)) {
                buildFile = projectPath.resolve("build.gradle.kts");
            }

            if (Files.exists(buildFile)) {
                List<String> lines = Files.readAllLines(buildFile);

                // Detect Spring Boot
                boolean isSpringBoot = lines.stream()
                        .anyMatch(line -> line.contains("org.springframework.boot"));

                if (isSpringBoot) {
                    builder.framework("Spring Boot");
                    builder.frameworkVersion("3.2.0"); // Default version
                }

                // Detect Java version
                String javaVersion = lines.stream()
                        .filter(line -> line.contains("sourceCompatibility") || line.contains("targetCompatibility"))
                        .findFirst()
                        .map(line -> extractVersion(line))
                        .orElse("17");

                builder.language("Java");
                builder.languageVersion(javaVersion);
                builder.basePackage("com.example");
            }
        } catch (Exception e) {
            log.error("Error analyzing Gradle project: ", e);
            setDefaultValues(builder);
        }
    }

    /**
     * Set default values
     */
    private void setDefaultValues(ProjectContext.ProjectContextBuilder builder) {
        builder.language("Java");
        builder.languageVersion("17");
        builder.framework("Spring Boot");
        builder.frameworkVersion("3.2.0");
        builder.basePackage("com.example");
        builder.dependencies(new ArrayList<>());
    }

    /**
     * Detect package structure
     */
    private void detectPackageStructure(Path projectPath, ProjectContext.ProjectContextBuilder builder) {
        Path srcPath = projectPath.resolve("src/main/java");

        ProjectContext.PackageStructure.PackageStructureBuilder structureBuilder =
                ProjectContext.PackageStructure.builder();

        if (Files.exists(srcPath)) {
            try {
                // Find base package directory
                String basePackage = builder.build().getBasePackage();
                if (basePackage != null) {
                    Path basePath = srcPath.resolve(basePackage.replace('.', '/'));

                    if (Files.exists(basePath)) {
                        // Check for common package names
                        structureBuilder.entityPackage(
                                Files.exists(basePath.resolve("entity")) ? basePackage + ".entity" :
                                        Files.exists(basePath.resolve("model")) ? basePackage + ".model" :
                                                basePackage + ".entity"
                        );

                        structureBuilder.repositoryPackage(
                                Files.exists(basePath.resolve("repository")) ? basePackage + ".repository" :
                                        Files.exists(basePath.resolve("repo")) ? basePackage + ".repo" :
                                                basePackage + ".repository"
                        );

                        structureBuilder.servicePackage(
                                Files.exists(basePath.resolve("service")) ? basePackage + ".service" :
                                        basePackage + ".service"
                        );

                        structureBuilder.controllerPackage(
                                Files.exists(basePath.resolve("controller")) ? basePackage + ".controller" :
                                        Files.exists(basePath.resolve("web")) ? basePackage + ".web" :
                                                Files.exists(basePath.resolve("rest")) ? basePackage + ".rest" :
                                                        basePackage + ".controller"
                        );

                        structureBuilder.dtoPackage(
                                Files.exists(basePath.resolve("dto")) ? basePackage + ".dto" :
                                        basePackage + ".dto"
                        );

                        structureBuilder.configPackage(
                                Files.exists(basePath.resolve("config")) ? basePackage + ".config" :
                                        Files.exists(basePath.resolve("configuration")) ? basePackage + ".configuration" :
                                                basePackage + ".config"
                        );
                    }
                }
            } catch (Exception e) {
                log.error("Error detecting package structure: ", e);
            }
        }

        builder.packageStructure(structureBuilder.build());
    }

    /**
     * Detect project features
     */
    private Map<String, Boolean> detectFeatures(Path projectPath) {
        Map<String, Boolean> features = new HashMap<>();

        try {
            // Check for common dependencies in pom.xml
            Path pomPath = projectPath.resolve("pom.xml");
            if (Files.exists(pomPath)) {
                String pomContent = Files.readString(pomPath);

                features.put("lombok", pomContent.contains("lombok"));
                features.put("jpa", pomContent.contains("spring-boot-starter-data-jpa"));
                features.put("validation", pomContent.contains("spring-boot-starter-validation"));
                features.put("database", pomContent.contains("postgresql") ||
                        pomContent.contains("mysql") ||
                        pomContent.contains("h2database"));
                features.put("web", pomContent.contains("spring-boot-starter-web"));
                features.put("security", pomContent.contains("spring-boot-starter-security"));
            }
        } catch (Exception e) {
            log.error("Error detecting features: ", e);
        }

        return features;
    }

    // Helper methods
    private String getXmlValue(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private String getElementValue(org.w3c.dom.Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private String extractVersion(String line) {
        // Extract version number from line
        String[] parts = line.split("=");
        if (parts.length > 1) {
            return parts[1].trim().replaceAll("['\"]", "").replaceAll("\\D", "");
        }
        return "17";
    }
}