package com.mcp.server.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for mapping SQL types to Java types
 * Handles PostgreSQL specific types and provides import requirements
 */
@Component
@Slf4j
public class TypeMapper {

    private static final Map<String, String> SQL_TO_JAVA_MAPPING;
    private static final Map<String, String> JAVA_TYPE_IMPORTS;
    private static final Map<String, String> JDBC_TYPE_MAPPING;
    private static final Map<String, String> HIBERNATE_TYPE_MAPPING;

    static {
        // Initialize SQL to Java type mappings
        SQL_TO_JAVA_MAPPING = new HashMap<>();

        // Numeric types
        SQL_TO_JAVA_MAPPING.put("SMALLINT", "Short");
        SQL_TO_JAVA_MAPPING.put("SMALLSERIAL", "Short");
        SQL_TO_JAVA_MAPPING.put("INTEGER", "Integer");
        SQL_TO_JAVA_MAPPING.put("INT", "Integer");
        SQL_TO_JAVA_MAPPING.put("INT2", "Short");
        SQL_TO_JAVA_MAPPING.put("INT4", "Integer");
        SQL_TO_JAVA_MAPPING.put("INT8", "Long");
        SQL_TO_JAVA_MAPPING.put("SERIAL", "Integer");
        SQL_TO_JAVA_MAPPING.put("BIGINT", "Long");
        SQL_TO_JAVA_MAPPING.put("BIGSERIAL", "Long");
        SQL_TO_JAVA_MAPPING.put("DECIMAL", "BigDecimal");
        SQL_TO_JAVA_MAPPING.put("NUMERIC", "BigDecimal");
        SQL_TO_JAVA_MAPPING.put("REAL", "Float");
        SQL_TO_JAVA_MAPPING.put("FLOAT", "Float");
        SQL_TO_JAVA_MAPPING.put("FLOAT4", "Float");
        SQL_TO_JAVA_MAPPING.put("FLOAT8", "Double");
        SQL_TO_JAVA_MAPPING.put("DOUBLE", "Double");
        SQL_TO_JAVA_MAPPING.put("DOUBLE PRECISION", "Double");
        SQL_TO_JAVA_MAPPING.put("MONEY", "BigDecimal");

        // Character types
        SQL_TO_JAVA_MAPPING.put("VARCHAR", "String");
        SQL_TO_JAVA_MAPPING.put("CHAR", "String");
        SQL_TO_JAVA_MAPPING.put("CHARACTER", "String");
        SQL_TO_JAVA_MAPPING.put("CHARACTER VARYING", "String");
        SQL_TO_JAVA_MAPPING.put("TEXT", "String");
        SQL_TO_JAVA_MAPPING.put("NAME", "String");
        SQL_TO_JAVA_MAPPING.put("BPCHAR", "String");

        // Binary types
        SQL_TO_JAVA_MAPPING.put("BYTEA", "byte[]");
        SQL_TO_JAVA_MAPPING.put("BLOB", "byte[]");
        SQL_TO_JAVA_MAPPING.put("BINARY", "byte[]");
        SQL_TO_JAVA_MAPPING.put("VARBINARY", "byte[]");

        // Date/Time types
        SQL_TO_JAVA_MAPPING.put("DATE", "LocalDate");
        SQL_TO_JAVA_MAPPING.put("TIME", "LocalTime");
        SQL_TO_JAVA_MAPPING.put("TIMETZ", "OffsetTime");
        SQL_TO_JAVA_MAPPING.put("TIME WITH TIME ZONE", "OffsetTime");
        SQL_TO_JAVA_MAPPING.put("TIME WITHOUT TIME ZONE", "LocalTime");
        SQL_TO_JAVA_MAPPING.put("TIMESTAMP", "LocalDateTime");
        SQL_TO_JAVA_MAPPING.put("TIMESTAMPTZ", "OffsetDateTime");
        SQL_TO_JAVA_MAPPING.put("TIMESTAMP WITH TIME ZONE", "OffsetDateTime");
        SQL_TO_JAVA_MAPPING.put("TIMESTAMP WITHOUT TIME ZONE", "LocalDateTime");
        SQL_TO_JAVA_MAPPING.put("INTERVAL", "Duration");

        // Boolean
        SQL_TO_JAVA_MAPPING.put("BOOLEAN", "Boolean");
        SQL_TO_JAVA_MAPPING.put("BOOL", "Boolean");
        SQL_TO_JAVA_MAPPING.put("BIT", "Boolean");

        // UUID
        SQL_TO_JAVA_MAPPING.put("UUID", "UUID");

        // JSON types
        SQL_TO_JAVA_MAPPING.put("JSON", "String");
        SQL_TO_JAVA_MAPPING.put("JSONB", "String");

        // Arrays
        SQL_TO_JAVA_MAPPING.put("INTEGER[]", "List<Integer>");
        SQL_TO_JAVA_MAPPING.put("BIGINT[]", "List<Long>");
        SQL_TO_JAVA_MAPPING.put("VARCHAR[]", "List<String>");
        SQL_TO_JAVA_MAPPING.put("TEXT[]", "List<String>");
        SQL_TO_JAVA_MAPPING.put("UUID[]", "List<UUID>");

        // Geometric types (PostgreSQL specific)
        SQL_TO_JAVA_MAPPING.put("POINT", "PGpoint");
        SQL_TO_JAVA_MAPPING.put("LINE", "PGline");
        SQL_TO_JAVA_MAPPING.put("LSEG", "PGlseg");
        SQL_TO_JAVA_MAPPING.put("BOX", "PGbox");
        SQL_TO_JAVA_MAPPING.put("PATH", "PGpath");
        SQL_TO_JAVA_MAPPING.put("POLYGON", "PGpolygon");
        SQL_TO_JAVA_MAPPING.put("CIRCLE", "PGcircle");

        // Network types
        SQL_TO_JAVA_MAPPING.put("INET", "InetAddress");
        SQL_TO_JAVA_MAPPING.put("CIDR", "String");
        SQL_TO_JAVA_MAPPING.put("MACADDR", "String");
        SQL_TO_JAVA_MAPPING.put("MACADDR8", "String");

        // Initialize Java type imports
        JAVA_TYPE_IMPORTS = new HashMap<>();
        JAVA_TYPE_IMPORTS.put("BigDecimal", "java.math.BigDecimal");
        JAVA_TYPE_IMPORTS.put("LocalDate", "java.time.LocalDate");
        JAVA_TYPE_IMPORTS.put("LocalTime", "java.time.LocalTime");
        JAVA_TYPE_IMPORTS.put("LocalDateTime", "java.time.LocalDateTime");
        JAVA_TYPE_IMPORTS.put("OffsetDateTime", "java.time.OffsetDateTime");
        JAVA_TYPE_IMPORTS.put("OffsetTime", "java.time.OffsetTime");
        JAVA_TYPE_IMPORTS.put("Duration", "java.time.Duration");
        JAVA_TYPE_IMPORTS.put("Instant", "java.time.Instant");
        JAVA_TYPE_IMPORTS.put("UUID", "java.util.UUID");
        JAVA_TYPE_IMPORTS.put("List", "java.util.List");
        JAVA_TYPE_IMPORTS.put("Set", "java.util.Set");
        JAVA_TYPE_IMPORTS.put("Map", "java.util.Map");
        JAVA_TYPE_IMPORTS.put("ArrayList", "java.util.ArrayList");
        JAVA_TYPE_IMPORTS.put("HashSet", "java.util.HashSet");
        JAVA_TYPE_IMPORTS.put("HashMap", "java.util.HashMap");
        JAVA_TYPE_IMPORTS.put("InetAddress", "java.net.InetAddress");
        JAVA_TYPE_IMPORTS.put("PGpoint", "org.postgresql.geometric.PGpoint");
        JAVA_TYPE_IMPORTS.put("PGline", "org.postgresql.geometric.PGline");
        JAVA_TYPE_IMPORTS.put("PGlseg", "org.postgresql.geometric.PGlseg");
        JAVA_TYPE_IMPORTS.put("PGbox", "org.postgresql.geometric.PGbox");
        JAVA_TYPE_IMPORTS.put("PGpath", "org.postgresql.geometric.PGpath");
        JAVA_TYPE_IMPORTS.put("PGpolygon", "org.postgresql.geometric.PGpolygon");
        JAVA_TYPE_IMPORTS.put("PGcircle", "org.postgresql.geometric.PGcircle");

        // JDBC type mappings
        JDBC_TYPE_MAPPING = new HashMap<>();
        JDBC_TYPE_MAPPING.put("VARCHAR", "VARCHAR");
        JDBC_TYPE_MAPPING.put("TEXT", "LONGVARCHAR");
        JDBC_TYPE_MAPPING.put("INTEGER", "INTEGER");
        JDBC_TYPE_MAPPING.put("BIGINT", "BIGINT");
        JDBC_TYPE_MAPPING.put("SMALLINT", "SMALLINT");
        JDBC_TYPE_MAPPING.put("DECIMAL", "DECIMAL");
        JDBC_TYPE_MAPPING.put("NUMERIC", "NUMERIC");
        JDBC_TYPE_MAPPING.put("REAL", "REAL");
        JDBC_TYPE_MAPPING.put("DOUBLE", "DOUBLE");
        JDBC_TYPE_MAPPING.put("BOOLEAN", "BOOLEAN");
        JDBC_TYPE_MAPPING.put("DATE", "DATE");
        JDBC_TYPE_MAPPING.put("TIME", "TIME");
        JDBC_TYPE_MAPPING.put("TIMESTAMP", "TIMESTAMP");
        JDBC_TYPE_MAPPING.put("BINARY", "BINARY");
        JDBC_TYPE_MAPPING.put("VARBINARY", "VARBINARY");
        JDBC_TYPE_MAPPING.put("BLOB", "BLOB");
        JDBC_TYPE_MAPPING.put("CLOB", "CLOB");

        // Hibernate type mappings
        HIBERNATE_TYPE_MAPPING = new HashMap<>();
        HIBERNATE_TYPE_MAPPING.put("JSON", "json");
        HIBERNATE_TYPE_MAPPING.put("JSONB", "jsonb");
        HIBERNATE_TYPE_MAPPING.put("UUID", "uuid-char");
        HIBERNATE_TYPE_MAPPING.put("INET", "inet");
        HIBERNATE_TYPE_MAPPING.put("INTEGER[]", "int-array");
        HIBERNATE_TYPE_MAPPING.put("VARCHAR[]", "string-array");
    }

    /**
     * Map SQL type to Java type
     * @param sqlType SQL type name
     * @return Java type name
     */
    public String toJavaType(String sqlType) {
        if (sqlType == null) {
            return "String";
        }

        String upperType = sqlType.toUpperCase().trim();

        // Handle parameterized types (e.g., VARCHAR(255))
        if (upperType.contains("(")) {
            upperType = upperType.substring(0, upperType.indexOf("(")).trim();
        }

        String javaType = SQL_TO_JAVA_MAPPING.get(upperType);

        if (javaType == null) {
            log.warn("Unknown SQL type: {}, defaulting to String", sqlType);
            return "String";
        }

        return javaType;
    }

    /**
     * Map SQL type to Java type with custom field configuration
     * @param sqlType SQL type
     * @param fieldConfig field configuration
     * @return Java type
     */
    public String toJavaType(String sqlType, Map<String, Object> fieldConfig) {
        // Check for custom type override
        if (fieldConfig != null && fieldConfig.containsKey("javaType")) {
            return (String) fieldConfig.get("javaType");
        }

        // Check for enum type
        if (fieldConfig != null && fieldConfig.containsKey("enum")) {
            String fieldName = (String) fieldConfig.get("name");
            return NamingUtils.toPascalCase(fieldName) + "Type";
        }

        // Check for JSON object mapping
        if ("JSONB".equalsIgnoreCase(sqlType) || "JSON".equalsIgnoreCase(sqlType)) {
            if (fieldConfig != null && fieldConfig.containsKey("jsonClass")) {
                return (String) fieldConfig.get("jsonClass");
            }
            // Default to Map for JSON objects
            if (fieldConfig != null && Boolean.TRUE.equals(fieldConfig.get("jsonObject"))) {
                return "Map<String, Object>";
            }
        }

        return toJavaType(sqlType);
    }

    /**
     * Get required imports for Java types
     * @param javaTypes collection of Java types
     * @return set of import statements
     */
    public Set<String> getRequiredImports(Collection<String> javaTypes) {
        Set<String> imports = new TreeSet<>(); // TreeSet for sorted imports

        for (String javaType : javaTypes) {
            // Handle generic types
            String baseType = javaType;
            if (javaType.contains("<")) {
                baseType = javaType.substring(0, javaType.indexOf("<"));
                // Also add imports for generic type parameters
                String genericType = javaType.substring(javaType.indexOf("<") + 1, javaType.lastIndexOf(">"));
                if (JAVA_TYPE_IMPORTS.containsKey(genericType)) {
                    imports.add(JAVA_TYPE_IMPORTS.get(genericType));
                }
            }

            if (JAVA_TYPE_IMPORTS.containsKey(baseType)) {
                imports.add(JAVA_TYPE_IMPORTS.get(baseType));
            }
        }

        return imports;
    }

    /**
     * Get JDBC type for SQL type
     * @param sqlType SQL type
     * @return JDBC type constant name
     */
    public String toJdbcType(String sqlType) {
        if (sqlType == null) {
            return "VARCHAR";
        }

        String upperType = sqlType.toUpperCase().trim();
        if (upperType.contains("(")) {
            upperType = upperType.substring(0, upperType.indexOf("(")).trim();
        }

        return JDBC_TYPE_MAPPING.getOrDefault(upperType, "OTHER");
    }

    /**
     * Get Hibernate type for special PostgreSQL types
     * @param sqlType SQL type
     * @return Hibernate type annotation value or null
     */
    public String toHibernateType(String sqlType) {
        if (sqlType == null) {
            return null;
        }

        String upperType = sqlType.toUpperCase().trim();
        return HIBERNATE_TYPE_MAPPING.get(upperType);
    }

    /**
     * Check if type requires special Hibernate handling
     * @param sqlType SQL type
     * @return true if type needs @Type annotation
     */
    public boolean requiresHibernateType(String sqlType) {
        return toHibernateType(sqlType) != null;
    }

    /**
     * Check if Java type is a collection type
     * @param javaType Java type
     * @return true if collection type
     */
    public boolean isCollectionType(String javaType) {
        return javaType != null && (
                javaType.startsWith("List<") ||
                        javaType.startsWith("Set<") ||
                        javaType.startsWith("Collection<") ||
                        javaType.endsWith("[]")
        );
    }

    /**
     * Check if Java type is a primitive wrapper
     * @param javaType Java type
     * @return true if primitive wrapper
     */
    public boolean isPrimitiveWrapper(String javaType) {
        return Arrays.asList(
                "Boolean", "Byte", "Character", "Short",
                "Integer", "Long", "Float", "Double"
        ).contains(javaType);
    }

    /**
     * Get validation annotation for SQL type
     * @param sqlType SQL type
     * @param nullable whether field is nullable
     * @return appropriate validation annotation or null
     */
    public String getValidationAnnotation(String sqlType, boolean nullable) {
        if (nullable) {
            return null;
        }

        String javaType = toJavaType(sqlType);

        if ("String".equals(javaType)) {
            return "@NotBlank";
        } else {
            return "@NotNull";
        }
    }

    /**
     * Map collection of SQL types to Java types
     * @param sqlTypes SQL types
     * @return map of SQL type to Java type
     */
    public Map<String, String> mapTypes(Collection<String> sqlTypes) {
        return sqlTypes.stream()
                .collect(Collectors.toMap(
                        type -> type,
                        this::toJavaType,
                        (existing, replacement) -> existing
                ));
    }
}