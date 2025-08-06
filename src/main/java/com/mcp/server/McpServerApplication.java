package com.mcp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MCP Server Application for PostgreSQL Integration
 * Provides code generation capabilities for database integration
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
        System.out.println("""
            
            ╔═══════════════════════════════════════════════════════╗
            ║     MCP Server for PostgreSQL Integration Started     ║
            ║                                                       ║
            ║     Server running at: http://localhost:8080/mcp     ║
            ║     Documentation: http://localhost:8080/mcp/docs    ║
            ╚═══════════════════════════════════════════════════════╝
            
            """);
    }
}