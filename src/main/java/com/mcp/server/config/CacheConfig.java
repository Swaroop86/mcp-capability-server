package com.mcp.server.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration using Caffeine
 * Provides high-performance caching for plans, templates, and SDK configurations
 */
@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class CacheConfig implements CachingConfigurer {

    @Value("${mcp.plan.expiration-minutes:10}")
    private int planExpirationMinutes;

    @Value("${mcp.plan.max-cached-plans:100}")
    private int maxCachedPlans;

    /**
     * Primary cache manager with multiple caches
     */
    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Configure different caches with specific settings
        cacheManager.setCacheNames(Arrays.asList(
                "plans",           // Integration plans
                "templates",       // Compiled templates
                "sdk-configs",     // SDK configurations
                "project-context", // Analyzed project contexts
                "standards"        // Coding standards
        ));

        cacheManager.setCaffeine(caffeineBuilder());

        log.info("Cache manager configured with caches: {}", cacheManager.getCacheNames());
        return cacheManager;
    }

    /**
     * Plan-specific cache manager with shorter TTL
     */
    @Bean(name = "planCacheManager")
    public CacheManager planCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("plans");

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maxCachedPlans)
                .expireAfterWrite(planExpirationMinutes, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) ->
                        log.debug("Plan {} removed from cache: {}", key, cause))
        );

        return cacheManager;
    }

    /**
     * Template cache manager - longer TTL since templates don't change often
     */
    @Bean(name = "templateCacheManager")
    public CacheManager templateCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("templates");

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .recordStats()
        );

        return cacheManager;
    }

    /**
     * SDK configuration cache - refresh daily
     */
    @Bean(name = "sdkCacheManager")
    public CacheManager sdkCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("sdk-configs");

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats()
        );

        return cacheManager;
    }

    /**
     * Default Caffeine builder configuration
     */
    private Caffeine<Object, Object> caffeineBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .initialCapacity(50)
                .recordStats();
    }

    /**
     * Custom key generator for complex objects
     */
    @Override
    public KeyGenerator keyGenerator() {
        return new McpKeyGenerator();
    }

    /**
     * Cache resolver
     */
    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(cacheManager());
    }

    /**
     * Error handler for cache operations
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new McpCacheErrorHandler();
    }

    /**
     * Cache statistics logger - runs every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void logCacheStatistics() {
        CaffeineCacheManager cacheManager = (CaffeineCacheManager) cacheManager();
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                var caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache();
                var stats = caffeineCache.stats();
                if (stats.requestCount() > 0) {
                    log.debug("Cache '{}' stats - Hits: {}, Misses: {}, Hit Rate: {:.2f}%, Size: {}",
                            cacheName,
                            stats.hitCount(),
                            stats.missCount(),
                            stats.hitRate() * 100,
                            caffeineCache.estimatedSize()
                    );
                }
            }
        });
    }

    /**
     * Clear expired plans - runs every hour
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    public void cleanupExpiredPlans() {
        log.debug("Running cache cleanup for expired plans");
        var planCache = cacheManager().getCache("plans");
        if (planCache != null) {
            planCache.clear();
            log.info("Plan cache cleared");
        }
    }

    @PostConstruct
    public void init() {
        log.info("Cache configuration initialized:");
        log.info("  Plan expiration: {} minutes", planExpirationMinutes);
        log.info("  Max cached plans: {}", maxCachedPlans);
        log.info("  Cache names: {}", ((CaffeineCacheManager) cacheManager()).getCacheNames());
    }
}

/**
 * Custom key generator for MCP objects
 */
class McpKeyGenerator extends SimpleKeyGenerator {

    @Override
    public Object generate(Object target, java.lang.reflect.Method method, Object... params) {
        StringBuilder key = new StringBuilder();
        key.append(target.getClass().getSimpleName()).append(":");
        key.append(method.getName()).append(":");

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
    }
}

/**
 * Custom error handler for cache operations
 */
@Slf4j
class McpCacheErrorHandler extends SimpleCacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception,
                                    org.springframework.cache.Cache cache,
                                    Object key) {
        log.error("Cache get error for key '{}' in cache '{}': {}",
                key, cache.getName(), exception.getMessage());
        super.handleCacheGetError(exception, cache, key);
    }

    @Override
    public void handleCachePutError(RuntimeException exception,
                                    org.springframework.cache.Cache cache,
                                    Object key,
                                    Object value) {
        log.error("Cache put error for key '{}' in cache '{}': {}",
                key, cache.getName(), exception.getMessage());
        super.handleCachePutError(exception, cache, key, value);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception,
                                      org.springframework.cache.Cache cache,
                                      Object key) {
        log.error("Cache evict error for key '{}' in cache '{}': {}",
                key, cache.getName(), exception.getMessage());
        super.handleCacheEvictError(exception, cache, key);
    }
}