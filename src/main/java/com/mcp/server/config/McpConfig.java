package com.mcp.server.config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import jakarta.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Main MCP Server Configuration
 * Handles all MCP-specific settings and beans
 */
@org.springframework.context.annotation.Configuration
@EnableAsync
@Slf4j
public class McpConfig {

    @Bean
    public McpProperties mcpProperties() {
        return new McpProperties();
    }

    /**
     * Configure Freemarker template engine
     */
    @Bean
    @Primary
    public Configuration freemarkerConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);

        // Load templates from classpath
        cfg.setClassForTemplateLoading(getClass(), "/templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);

        // Number and date formatting
        cfg.setNumberFormat("0.######");
        cfg.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        cfg.setDateFormat("yyyy-MM-dd");
        cfg.setTimeFormat("HH:mm:ss");
        cfg.setBooleanFormat("true,false");

        // SQL keywords
        cfg.setSQLDateAndTimeTimeZone(java.util.TimeZone.getDefault());

        log.info("Freemarker template engine configured");
        return cfg;
    }

    /**
     * Configure CORS for Cursor IDE access
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList("*")); // Allow all origins for development
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        log.info("CORS configuration enabled for MCP endpoints");
        return new CorsFilter(source);
    }

    /**
     * Async executor for parallel processing
     */
    @Bean(name = "mcpAsyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("MCP-Async-");
        executor.initialize();
        log.info("Async executor configured with max {} threads", executor.getMaxPoolSize());
        return executor;
    }

    /**
     * SDK Repository configuration
     */
    @Bean
    public SdkRepositoryConfig sdkRepositoryConfig(McpProperties properties) {
        return SdkRepositoryConfig.builder()
                .basePath(properties.getSdk().getBasePath())
                .defaultSdk(properties.getSdk().getDefaultSdk())
                .cacheEnabled(properties.getSdk().isCacheEnabled())
                .build();
    }

    /**
     * Standards Repository configuration
     */
    @Bean
    public StandardsRepositoryConfig standardsRepositoryConfig(McpProperties properties) {
        return StandardsRepositoryConfig.builder()
                .basePath(properties.getStandards().getBasePath())
                .defaultStandard(properties.getStandards().getDefaultStandard())
                .build();
    }
}

/**
 * MCP Properties loaded from application.yml
 */
@Component
@ConfigurationProperties(prefix = "mcp")
@Data
@Slf4j
class McpProperties {

    private Server server = new Server();
    private Sdk sdk = new Sdk();
    private Standards standards = new Standards();
    private Generation generation = new Generation();
    private Plan plan = new Plan();

    @PostConstruct
    public void init() {
        log.info("MCP Properties loaded:");
        log.info("  Server: {} v{}", server.getName(), server.getVersion());
        log.info("  SDK Base Path: {}", sdk.getBasePath());
        log.info("  Standards Base Path: {}", standards.getBasePath());
        log.info("  Plan Expiration: {} minutes", plan.getExpirationMinutes());
    }

    @Data
    public static class Server {
        private String name = "PostgreSQL Integration MCP Server";
        private String version = "1.0.0";
        private List<String> capabilities = Arrays.asList("postgresql", "database-integration", "code-generation");
    }

    @Data
    public static class Sdk {
        private String basePath = "/opt/mcp-resources/sdk-repositories";
        private boolean cacheEnabled = true;
        private String defaultSdk = "postgresql-java-sdk";
    }

    @Data
    public static class Standards {
        private String basePath = "/opt/mcp-resources/coding-standards";
        private String defaultStandard = "java-spring-boot";
    }

    @Data
    public static class Generation {
        private String defaultPackage = "com.example";
        private boolean useLombok = true;
        private boolean includeValidation = true;
        private boolean includeTests = false;
        private String namingStrategy = "snake_case";
    }

    @Data
    public static class Plan {
        private int expirationMinutes = 10;
        private int maxCachedPlans = 100;
    }
}

/**
 * SDK Repository Configuration
 */
@Data
@lombok.Builder
class SdkRepositoryConfig {
    private String basePath;
    private String defaultSdk;
    private boolean cacheEnabled;

    public Path getSdkPath(String sdkName) {
        return Paths.get(basePath, sdkName);
    }

    public Path getDefaultSdkPath() {
        return getSdkPath(defaultSdk);
    }
}

/**
 * Standards Repository Configuration
 */
@Data
@lombok.Builder
class StandardsRepositoryConfig {
    private String basePath;
    private String defaultStandard;

    public Path getStandardPath(String standardName) {
        return Paths.get(basePath, standardName);
    }

    public Path getDefaultStandardPath() {
        return getStandardPath(defaultStandard);
    }
}