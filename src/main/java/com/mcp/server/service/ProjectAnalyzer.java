package com.mcp.server.service;

import com.mcp.server.model.ProjectContext;
import com.mcp.server.model.Dependency;
import com.mcp.server.model.PackageStructure;
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

        // Create project context builder
        ProjectContext context = new ProjectContext();
        context.setProjectPath(projectPath);
        context.setBuildTool(buildTool);

        if ("Maven".equals(buildTool)) {
            analyzeMavenProject(path, context);
        } else if ("Gradle".equals(buildTool)) {
            analyzeGradleProject(path, context);
        } else {
            // Default values for unknown project
            setDefaultValues(context);
        }

        // Detect package structure
        detectPackageStructure(path, context);

        // Detect features
        Map<String, Boolean> features = detectFeatures(path);
        context.setFeatures(features);

        return context;
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
    private void analyzeMavenProject(Path projectPath, ProjectContext context) {
        try {
            File pomFile = projectPath.resolve("pom.xml").toFile();
            if (!pomFile.exists()) {
                setDefaultValues(context);
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
            context.setLanguage("Java");
            context.setLanguageVersion(javaVersion);

            // Get group ID as base package
            String groupId = getXmlValue(doc, "groupId");
            String artifactId = getXmlValue(doc, "artifactId");
            if (groupId != null) {
                context.setBasePackage(groupId + (artifactId != null ? "." + artifactId.replace("-", "") : ""));
            } else {
                context.setBasePackage("com.example");
            }

            // Detect Spring Boot
            NodeList dependencies = doc.getElementsByTagName("dependency");
            List<Dependency> projectDeps = new ArrayList<>();
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

                Dependency dependency = new Dependency();
                dependency.setGroupId(depGroupId);
                dependency.setArtifactId(depArtifactId);
                dependency.setVersion(depVersion);
                projectDeps.add(dependency);
            }

            if (isSpringBoot) {
                context.setFramework("Spring Boot");
                context.setFrameworkVersion(springBootVersion);
            }

            context.setDependencies(projectDeps);

        } catch (Exception e) {
            log.error("Error analyzing Maven project: ", e);
            setDefaultValues(context);
        }
    }

    /**
     * Analyze Gradle project
     */
    private void analyzeGradleProject(Path projectPath, ProjectContext context) {
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
                    context.setFramework("Spring Boot");
                    context.setFrameworkVersion("3.2.0"); // Default version
                }

                // Detect Java version
                String javaVersion = lines.stream()
                        .filter(line -> line.contains("sourceCompatibility") || line.contains("targetCompatibility"))
                        .findFirst()
                        .map(this::extractVersion)
                        .orElse("17");

                context.setLanguage("Java");
                context.setLanguageVersion(javaVersion);
                context.setBasePackage("com.example");
            }
        } catch (Exception e) {
            log.error("Error analyzing Gradle project: ", e);
            setDefaultValues(context);
        }
    }

    /**
     * Set default values
     */
    private void setDefaultValues(ProjectContext context) {
        context.setLanguage("Java");
        context.setLanguageVersion("17");
        context.setFramework("Spring Boot");
        context.setFrameworkVersion("3.2.0");
        context.setBasePackage("com.example");
        context.setDependencies(new ArrayList<>());
    }

    /**
     * Detect package structure
     */
    private void detectPackageStructure(Path projectPath, ProjectContext context) {
        Path srcPath = projectPath.resolve("src/main/java");

        PackageStructure packageStructure = new PackageStructure();

        if (Files.exists(srcPath)) {
            try {
                // Find base package directory
                String basePackage = context.getBasePackage();
                if (basePackage != null) {
                    Path basePath = srcPath.resolve(basePackage.replace('.', '/'));

                    if (Files.exists(basePath)) {
                        // Check for common package names
                        packageStructure.setEntityPackage(
                                Files.exists(basePath.resolve("entity")) ? basePackage + ".entity" :
                                        Files.exists(basePath.resolve("model")) ? basePackage + ".model" :
                                                basePackage + ".entity"
                        );

                        packageStructure.setRepositoryPackage(
                                Files.exists(basePath.resolve("repository")) ? basePackage + ".repository" :
                                        Files.exists(basePath.resolve("repo")) ? basePackage + ".repo" :
                                                basePackage + ".repository"
                        );

                        packageStructure.setServicePackage(
                                Files.exists(basePath.resolve("service")) ? basePackage + ".service" :
                                        basePackage + ".service"
                        );

                        packageStructure.setControllerPackage(
                                Files.exists(basePath.resolve("controller")) ? basePackage + ".controller" :
                                        Files.exists(basePath.resolve("web")) ? basePackage + ".web" :
                                                Files.exists(basePath.resolve("rest")) ? basePackage + ".rest" :
                                                        basePackage + ".controller"
                        );

                        packageStructure.setDtoPackage(
                                Files.exists(basePath.resolve("dto")) ? basePackage + ".dto" :
                                        basePackage + ".dto"
                        );

                        packageStructure.setConfigPackage(
                                Files.exists(basePath.resolve("config")) ? basePackage + ".config" :
                                        Files.exists(basePath.resolve("configuration")) ? basePackage + ".configuration" :
                                                basePackage + ".config"
                        );
                    } else {
                        // Set default package structure
                        packageStructure.setEntityPackage(basePackage + ".entity");
                        packageStructure.setRepositoryPackage(basePackage + ".repository");
                        packageStructure.setServicePackage(basePackage + ".service");
                        packageStructure.setControllerPackage(basePackage + ".controller");
                        packageStructure.setDtoPackage(basePackage + ".dto");
                        packageStructure.setConfigPackage(basePackage + ".config");
                    }
                }
            } catch (Exception e) {
                log.error("Error detecting package structure: ", e);
                // Set defaults if error occurs
                String basePackage = context.getBasePackage();
                packageStructure.setEntityPackage(basePackage + ".entity");
                packageStructure.setRepositoryPackage(basePackage + ".repository");
                packageStructure.setServicePackage(basePackage + ".service");
                packageStructure.setControllerPackage(basePackage + ".controller");
                packageStructure.setDtoPackage(basePackage + ".dto");
                packageStructure.setConfigPackage(basePackage + ".config");
            }
        }

        context.setPackageStructure(packageStructure);
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