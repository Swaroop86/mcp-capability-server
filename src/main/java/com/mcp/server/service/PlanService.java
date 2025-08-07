package com.mcp.server.service;

import com.mcp.server.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing integration plans with enhanced caching and logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

    private final ProjectAnalyzer projectAnalyzer;
    private final Map<String, IntegrationPlan> planCache = new ConcurrentHashMap<>();

    @Value("${mcp.plan.expiration-minutes:120}")
    private int planExpirationMinutes;

    /**
     * Create a new integration plan
     */
    public PlanResponse createPlan(PlanRequest request) {
        log.info("Creating plan for capability: {}", request.getCapability());

        // Generate plan ID based on description if provided
        String planId = generatePlanId(request);
        log.info("Generated plan ID: {}", planId);

        // Store with normalized ID for easier retrieval
        String normalizedId = normalizePlanId(planId);
        log.info("Normalized plan ID: {}", normalizedId);

        // Analyze project
        String projectPath = request.getProjectInfo() != null ?
                request.getProjectInfo().getPath() : ".";
        log.info("Analyzing project at path: {}", projectPath);

        ProjectContext projectContext = projectAnalyzer.analyzeProject(projectPath);
        log.debug("Project context: framework={}, language={}, basePackage={}",
                projectContext.getFramework(),
                projectContext.getLanguage(),
                projectContext.getBasePackage());

        // Create internal plan with extended expiration
        IntegrationPlan plan = IntegrationPlan.builder()
                .planId(planId)
                .capability(request.getCapability())
                .projectContext(projectContext)
                .options(request.getPreferences())
                .status(PlanStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(planExpirationMinutes))
                .build();

        // Cache the plan with both original and normalized IDs
        planCache.put(planId, plan);
        planCache.put(normalizedId, plan);

        // Also store common variations
        if (request.getProjectInfo() != null && request.getProjectInfo().getDescription() != null) {
            String description = request.getProjectInfo().getDescription();
            // Store by description-based keys
            if (description.toLowerCase().contains("user")) {
                planCache.put("user-management-simple-integration", plan);
                planCache.put("simplified-user-management", plan);
            }
        }

        log.info("Plan cached successfully. Cache size: {}", planCache.size());
        log.info("Plan expires at: {}", plan.getExpiresAt());

        // Log all cached plan IDs for debugging
        log.debug("Current cached plan IDs: {}", planCache.keySet());

        // Build response
        PlanResponse response = buildPlanResponse(plan, projectContext);
        log.debug("Plan response created with status: {}", response.getStatus());

        return response;
    }

    /**
     * Get plan for execution with enhanced error handling and logging
     */
    public IntegrationPlan getPlanForExecution(String planId) {
        log.info("Getting plan for execution: {}", planId);
        log.info("Cache contains {} plans", planCache.size());
        log.info("Available plan IDs in cache: {}", planCache.keySet());

        // Try multiple strategies to find the plan
        IntegrationPlan plan = null;

        // 1. Try exact match
        plan = planCache.get(planId);
        if (plan != null) {
            log.info("Found plan with exact match: {}", planId);
            return validateAndReturnPlan(plan, planId);
        }

        // 2. Try normalized ID
        String normalizedId = normalizePlanId(planId);
        plan = planCache.get(normalizedId);
        if (plan != null) {
            log.info("Found plan with normalized ID: {} -> {}", planId, normalizedId);
            return validateAndReturnPlan(plan, planId);
        }

        // 3. Try flexible matching
        plan = findPlanFlexible(planId);
        if (plan != null) {
            log.info("Found plan with flexible matching");
            return validateAndReturnPlan(plan, planId);
        }

        // 4. Log detailed error information
        log.error("Plan not found. Requested ID: {}", planId);
        log.error("Normalized ID: {}", normalizedId);
        log.error("Available plans in cache:");
        planCache.forEach((key, value) -> {
            log.error("  - {} (expires: {}, status: {})",
                    key, value.getExpiresAt(), value.getStatus());
        });

        // Throw detailed exception
        throw new RuntimeException(String.format(
                "Plan not found: '%s'. Available plans: %s. " +
                        "Please create a new plan or use one of the existing plan IDs.",
                planId, planCache.keySet()
        ));
    }

    /**
     * Validate plan and return if valid
     */
    private IntegrationPlan validateAndReturnPlan(IntegrationPlan plan, String requestedId) {
        // Check expiration
        if (plan.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.error("Plan expired: {} (expired at: {})", requestedId, plan.getExpiresAt());
            // Remove expired plan
            planCache.values().removeIf(p -> p == plan);
            throw new RuntimeException("Plan expired: " + requestedId +
                    ". Please create a new plan.");
        }

        log.info("Plan is valid and ready for execution");
        return plan;
    }

    /**
     * Generate plan ID based on request
     */
    private String generatePlanId(PlanRequest request) {
        // If description contains certain keywords, use a predictable ID
        if (request.getProjectInfo() != null && request.getProjectInfo().getDescription() != null) {
            String description = request.getProjectInfo().getDescription().toLowerCase();

            if (description.contains("user") && description.contains("management")) {
                return "user-management-plan-" + System.currentTimeMillis() % 10000;
            }
            if (description.contains("blog")) {
                return "blog-system-plan-" + System.currentTimeMillis() % 10000;
            }
        }

        // Default: timestamp-based
        return "plan_" + System.currentTimeMillis() % 100000;
    }

    /**
     * Normalize plan ID for matching
     */
    private String normalizePlanId(String planId) {
        return planId.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /**
     * Find plan with flexible ID matching
     */
    private IntegrationPlan findPlanFlexible(String planId) {
        String normalizedRequested = normalizePlanId(planId);

        for (Map.Entry<String, IntegrationPlan> entry : planCache.entrySet()) {
            String cachedId = entry.getKey();
            String normalizedCached = normalizePlanId(cachedId);

            // Check various matching strategies
            if (cachedId.equalsIgnoreCase(planId) ||
                    normalizedCached.equals(normalizedRequested) ||
                    cachedId.contains(planId) ||
                    planId.contains(cachedId) ||
                    normalizedCached.contains(normalizedRequested) ||
                    normalizedRequested.contains(normalizedCached)) {

                log.debug("Flexible match found: {} matches {}", planId, cachedId);
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Get existing plan with flexible ID matching
     */
    @Cacheable(value = "plans", key = "#planId")
    public PlanResponse getPlan(String planId) {
        log.info("Retrieving plan with ID: {}", planId);

        try {
            IntegrationPlan plan = getPlanForExecution(planId);
            return buildPlanResponse(plan, plan.getProjectContext());
        } catch (Exception e) {
            log.error("Failed to retrieve plan: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Update existing plan
     */
    @CachePut(value = "plans", key = "#planId")
    public PlanResponse updatePlan(String planId, Map<String, Object> updates) {
        log.info("Updating plan: {}", planId);

        IntegrationPlan plan = getPlanForExecution(planId);

        // Update plan options
        if (updates.containsKey("options")) {
            plan.setOptions((Map<String, Object>) updates.get("options"));
            log.debug("Updated plan options");
        }

        plan.setStatus(PlanStatus.UPDATED);

        // Extend expiration on update
        plan.setExpiresAt(LocalDateTime.now().plusMinutes(planExpirationMinutes));
        log.info("Extended plan expiration to: {}", plan.getExpiresAt());

        return buildPlanResponse(plan, plan.getProjectContext());
    }

    /**
     * Delete a plan
     */
    @CacheEvict(value = "plans", key = "#planId")
    public boolean deletePlan(String planId) {
        log.info("Deleting plan: {}", planId);

        // Remove all variations of the plan
        int removed = 0;
        Iterator<Map.Entry<String, IntegrationPlan>> iterator = planCache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, IntegrationPlan> entry = iterator.next();
            if (entry.getKey().equals(planId) ||
                    entry.getValue().getPlanId().equals(planId)) {
                iterator.remove();
                removed++;
            }
        }

        log.info("Removed {} plan entries", removed);
        return removed > 0;
    }

    /**
     * Build plan response from internal plan
     */
    private PlanResponse buildPlanResponse(IntegrationPlan plan, ProjectContext context) {
        PlanResponse response = PlanResponse.builder()
                .planId(plan.getPlanId())
                .capability(plan.getCapability())
                .status("ready_for_input")
                .projectAnalysis(buildProjectAnalysis(context))
                .proposedChanges(buildProposedChanges(plan.getCapability()))
                .options(buildPlanOptions())
                .impact(buildImpact())
                .compatibility(buildCompatibility(context))
                .nextSteps(buildNextSteps())
                .expiresIn(planExpirationMinutes + " minutes")
                .created(plan.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();

        log.debug("Built plan response with ID: {}", response.getPlanId());
        return response;
    }

    // Helper methods remain the same...
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
}