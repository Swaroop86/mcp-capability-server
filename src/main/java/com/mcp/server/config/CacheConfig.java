package com.mcp.server.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration using Caffeine with reduced caching
 * FIXED: Removed template caching to avoid state persistence
 */
@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class CacheConfig {

    @Value("${mcp.plan.expiration-minutes:120}")
    private int planExpirationMinutes;

    @Value("${mcp.plan.max-cached-plans:100}")
    private int maxCachedPlans;

    /**
     * Primary cache manager with limited caches
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Configure different caches with specific settings
        // REMOVED "templates" from cache list
        cacheManager.setCacheNames(Arrays.asList(
                "plans",           // Integration plans
                "sdk-configs",     // SDK configurations
                "project-context", // Analyzed project contexts
                "standards"        // Coding standards
        ));

        // Set default cache configuration with shorter TTL
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)  // Reduced from 1000
                .expireAfterWrite(5, TimeUnit.MINUTES)  // Reduced from 2 hours
                .initialCapacity(10)
                .recordStats());

        log.info("Cache manager configured with caches: {}", cacheManager.getCacheNames());
        return cacheManager;
    }

    /**
     * Custom key generator for MCP objects
     */
    @Bean
    public KeyGenerator mcpKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder key = new StringBuilder();
            key.append(target.getClass().getSimpleName()).append(":");
            key.append(method.getName()).append(":");

            // Add timestamp to make keys more unique
            key.append(System.currentTimeMillis()).append(":");

            for (Object param : params) {
                if (param != null) {
                    if (param instanceof String || param instanceof Number) {
                        key.append(param).append(":");
                    } else {
                        key.append(param.hashCode()).append(":");
                    }
                }
            }

            return key.toString();
        };
    }

    /**
     * Cache statistics logger - runs every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void logCacheStatistics() {
        log.debug("Cache statistics logging triggered");
    }

    /**
     * Clear expired plans - runs every 30 minutes
     */
    @Scheduled(fixedDelay = 1800000) // 30 minutes
    public void cleanupExpiredPlans() {
        log.debug("Running cache cleanup for expired plans");
    }

    @PostConstruct
    public void init() {
        log.info("Cache configuration initialized:");
        log.info("  Plan expiration: {} minutes", planExpirationMinutes);
        log.info("  Max cached plans: {}", maxCachedPlans);
        log.info("  Template caching: DISABLED");
    }
}