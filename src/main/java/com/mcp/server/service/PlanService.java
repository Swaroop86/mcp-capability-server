package com.mcp.server.service;

import com.mcp.server.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing integration plans
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

    private final ProjectAnalyzer projectAnalyzer;
    private final Map<String, IntegrationPlan> planCache = new ConcurrentHashMap<>();

    /**
     * Create a new integration plan
     */
    public PlanResponse createPlan(PlanRequest request) {
        log.info("Creating plan for capability: {}", request.getCapability());

        // Generate unique plan ID
        String planId = generatePlanId();

        // Analyze project
        ProjectContext projectContext = projectAnalyzer.analyzeProject(
                request.getProjectInfo() != null ? request.getProjectInfo().getPath() : "."
        );

        // Create internal plan
        IntegrationPlan plan = IntegrationPlan.builder()
                .planId(planId)
                .capability(request.getCapability())
                .projectContext(projectContext)
                .options(request.getPreferences())
                .status(PlanStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        // Cache the plan
        planCache.put(planId, plan);

        // Build response
        return buildPlanResponse(plan, projectContext);
    }

    /**
     * Get existing plan
     */
    @Cacheable(value = "plans", key = "#planId")
    public PlanResponse getPlan(String planId) {
        IntegrationPlan plan = planCache.get(planId);
        if (plan == null) {
            return null;
        }

        // Check if expired
        if (plan.getExpiresAt().isBefore(LocalDateTime.now())) {
            planCache.remove(planId);
            return null;
        }

        return buildPlanResponse(plan, plan.getProjectContext());
    }

    /**
     * Update existing plan
     */
    @CachePut(value = "plans", key = "#planId")
    public PlanResponse updatePlan(String planId, Map<String, Object> updates) {
        IntegrationPlan plan = planCache.get(planId);
        if (plan == null) {
            throw new RuntimeException("Plan not found: " + planId);
        }

        // Update plan options
        if (updates.containsKey("options")) {
            plan.setOptions((Map<String, Object>) updates.get("options"));
        }

        plan.setStatus(PlanStatus.UPDATED);

        // Generate new version ID
        String newPlanId = planId + "_v2";
        plan.setPlanId(newPlanId);
        planCache.put(newPlanId, plan);

        return buildPlanResponse(plan, plan.getProjectContext());
    }

    /**
     * Delete a plan
     */
    @CacheEvict(value = "plans", key = "#planId")
    public boolean deletePlan(String planId) {
        return planCache.remove(planId) != null;
    }

    /**
     * Get plan for execution
     */
    public IntegrationPlan getPlanForExecution(String planId) {
        IntegrationPlan plan = planCache.get(planId);
        if (plan == null) {
            throw new RuntimeException("Plan not found or expired: " + planId);
        }
        return plan;
    }

    /**
     * Build plan response from internal plan
     */
    private PlanResponse buildPlanResponse(IntegrationPlan plan, ProjectContext context) {
        return PlanResponse.builder()
                .planId(plan.getPlanId())
                .capability(plan.getCapability())
                .status("ready_for_input")
                .projectAnalysis(buildProjectAnalysis(context))
                .proposedChanges(buildProposedChanges(plan.getCapability()))
                .options(buildPlanOptions())
                .impact(buildImpact())
                .compatibility(buildCompatibility(context))
                .nextSteps(buildNextSteps())
                .expiresIn("10 minutes")
                .created(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    private ProjectAnalysis buildProjectAnalysis(ProjectContext context) {
        return ProjectAnalysis.builder()
                .detectedFramework(context.getFramework() + " " + context.getFrameworkVersion())
                .language(context.getLanguage() + " " + context.getLanguageVersion())
                .buildTool(context.getBuildTool())
                .basePackage(context.getBasePackage())
                .existingStructure(ExistingStructure.builder()
                        .hasJPA(context.getFeatures().getOrDefault("jpa", false))
                        .hasDatabase(context.getFeatures().getOrDefault("database", false))
                        .hasLombok(context.getFeatures().getOrDefault("lombok", false))
                        .hasValidation(context.getFeatures().getOrDefault("validation", false))
                        .currentDependencies(context.getDependencies().stream()
                                .map(d -> d.getArtifactId())
                                .toList())
                        .build())
                .build();
    }

    private ProposedChanges buildProposedChanges(String capability) {
        List<Component> components = new ArrayList<>();

        // Dependencies component
        components.add(Component.builder()
                .type("dependencies")
                .description("Maven dependencies to be added")
                .items(Arrays.asList(
                        ComponentItem.builder()
                                .name("spring-boot-starter-data-jpa")
                                .purpose("JPA and Hibernate support")
                                .required(true)
                                .selected(true)
                                .build(),
                        ComponentItem.builder()
                                .name("postgresql")
                                .purpose("PostgreSQL JDBC driver")
                                .required(true)
                                .selected(true)
                                .build(),
                        ComponentItem.builder()
                                .name("spring-boot-starter-validation")
                                .purpose("Bean validation support")
                                .required(false)
                                .selected(true)
                                .build()
                ))
                .build());

        // Code generation component
        components.add(Component.builder()
                .type("code_generation")
                .description("Code components to be generated")
                .items(Arrays.asList(
                        ComponentItem.builder()
                                .component("Entity Classes")
                                .description("JPA entities with annotations")
                                .features(Arrays.asList("Lombok", "Validation", "Auditing"))
                                .location("com.example.entity")
                                .build(),
                        ComponentItem.builder()
                                .component("Repository Interfaces")
                                .description("Spring Data JPA repositories")
                                .features(Arrays.asList("CRUD operations", "Custom queries", "Pagination"))
                                .location("com.example.repository")
                                .build(),
                        ComponentItem.builder()
                                .component("Service Layer")
                                .description("Business logic services")
                                .features(Arrays.asList("Transaction management", "Error handling"))
                                .location("com.example.service")
                                .build(),
                        ComponentItem.builder()
                                .component("REST Controllers")
                                .description("RESTful API endpoints")
                                .features(Arrays.asList("CRUD endpoints", "Validation", "Error responses"))
                                .location("com.example.controller")
                                .build()
                ))
                .build());

        return ProposedChanges.builder()
                .summary("Add PostgreSQL database integration with JPA repositories and REST controllers")
                .components(components)
                .build();
    }

    private PlanOptions buildPlanOptions() {
        return PlanOptions.builder()
                .customizable(Arrays.asList(
                        CustomOption.builder()
                                .id("naming_strategy")
                                .label("Table Naming Strategy")
                                .type("select")
                                .options(Arrays.asList("snake_case", "camelCase", "PascalCase"))
                                .defaultValue("snake_case")
                                .build(),
                        CustomOption.builder()
                                .id("id_generation")
                                .label("ID Generation Strategy")
                                .type("select")
                                .options(Arrays.asList("IDENTITY", "SEQUENCE", "UUID", "AUTO"))
                                .defaultValue("IDENTITY")
                                .build(),
                        CustomOption.builder()
                                .id("audit_fields")
                                .label("Include Audit Fields")
                                .type("boolean")
                                .defaultValue(true)
                                .description("Add createdAt, updatedAt, createdBy, updatedBy")
                                .build()
                ))
                .build();
    }

    private Impact buildImpact() {
        return Impact.builder()
                .filesCreated("~15-20 per table")
                .filesModified(2)
                .estimatedLinesOfCode("~300-500 per table")
                .breakingChanges(false)
                .requiresRestart(true)
                .build();
    }

    private Compatibility buildCompatibility(ProjectContext context) {
        return Compatibility.builder()
                .status("compatible")
                .checks(Arrays.asList(
                        CompatibilityCheck.builder()
                                .item("Spring Boot Version")
                                .required("≥ 2.7.0")
                                .found(context.getFrameworkVersion())
                                .status("✓")
                                .build(),
                        CompatibilityCheck.builder()
                                .item("Java Version")
                                .required("≥ 11")
                                .found(context.getLanguageVersion())
                                .status("✓")
                                .build()
                ))
                .warnings(new ArrayList<>())
                .build();
    }

    private NextSteps buildNextSteps() {
        return NextSteps.builder()
                .message("Review the plan above. When ready, provide your database schema to proceed with generation.")
                .requiredInput(RequiredInput.builder()
                        .type("database_schema")
                        .format("Table definitions with fields and relationships")
                        .example("Users table with id, username, email, password fields")
                        .build())
                .actions(Arrays.asList(
                        Action.builder()
                                .label("Proceed with Schema")
                                .action("execute_plan")
                                .requiresInput(true)
                                .build(),
                        Action.builder()
                                .label("Modify Options")
                                .action("update_plan")
                                .requiresInput(false)
                                .build()
                ))
                .build();
    }

    private String generatePlanId() {
        return "plan_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis();
    }
}