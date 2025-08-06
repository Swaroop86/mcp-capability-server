package com.mcp.server.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for handling various naming conventions
 * Converts between snake_case, camelCase, PascalCase, kebab-case, etc.
 */
@Component
public class NamingUtils {

    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("([a-z])([A-Z])");
    private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_([a-z])");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9]+");

    // Reserved keywords that need escaping
    private static final List<String> JAVA_KEYWORDS = Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "true", "false", "null"
    );

    private static final List<String> SQL_KEYWORDS = Arrays.asList(
            "user", "order", "group", "table", "column", "index", "key", "primary",
            "foreign", "references", "check", "default", "unique", "not", "null"
    );

    /**
     * Convert to camelCase
     * Examples: user_name -> userName, first-name -> firstName
     */
    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Handle already camelCase
        if (!input.contains("_") && !input.contains("-") && !input.contains(" ")) {
            return Character.toLowerCase(input.charAt(0)) + input.substring(1);
        }

        // Split by delimiters
        String[] parts = input.toLowerCase().split("[_\\-\\s]+");
        if (parts.length == 0) {
            return input;
        }

        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    result.append(parts[i].substring(1));
                }
            }
        }

        return result.toString();
    }

    /**
     * Convert to PascalCase (UpperCamelCase)
     * Examples: user_name -> UserName, first-name -> FirstName
     */
    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String camelCase = toCamelCase(input);
        return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
    }

    /**
     * Convert to snake_case
     * Examples: userName -> user_name, FirstName -> first_name
     */
    public static String toSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Handle already snake_case
        if (input.contains("_") && !input.contains("-") && input.equals(input.toLowerCase())) {
            return input;
        }

        // Replace spaces and hyphens with underscores
        String result = input.replaceAll("[\\s-]+", "_");

        // Insert underscore before uppercase letters
        result = SNAKE_CASE_PATTERN.matcher(result).replaceAll("$1_$2");

        return result.toLowerCase();
    }

    /**
     * Convert to UPPER_SNAKE_CASE (CONSTANT_CASE)
     * Examples: userName -> USER_NAME, FirstName -> FIRST_NAME
     */
    public static String toUpperSnakeCase(String input) {
        return toSnakeCase(input).toUpperCase();
    }

    /**
     * Convert to kebab-case
     * Examples: userName -> user-name, FirstName -> first-name
     */
    public static String toKebabCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return toSnakeCase(input).replace('_', '-');
    }

    /**
     * Convert to dot.case
     * Examples: userName -> user.name, FirstName -> first.name
     */
    public static String toDotCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return toSnakeCase(input).replace('_', '.');
    }

    /**
     * Pluralize a word (simple English pluralization)
     * Examples: user -> users, category -> categories, person -> people
     */
    public static String pluralize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        String lower = word.toLowerCase();

        // Irregular plurals
        switch (lower) {
            case "person": return word.substring(0, word.length() - 6) + "people";
            case "child": return word.substring(0, word.length() - 5) + "children";
            case "man": return word.substring(0, word.length() - 3) + "men";
            case "woman": return word.substring(0, word.length() - 5) + "women";
            case "tooth": return word.substring(0, word.length() - 5) + "teeth";
            case "foot": return word.substring(0, word.length() - 4) + "feet";
            case "mouse": return word.substring(0, word.length() - 5) + "mice";
            case "goose": return word.substring(0, word.length() - 5) + "geese";
        }

        // Regular pluralization rules
        if (lower.endsWith("s") || lower.endsWith("ss") || lower.endsWith("sh") ||
                lower.endsWith("ch") || lower.endsWith("x") || lower.endsWith("z")) {
            return word + "es";
        } else if (lower.endsWith("y") && !isVowel(lower.charAt(lower.length() - 2))) {
            return word.substring(0, word.length() - 1) + "ies";
        } else if (lower.endsWith("f")) {
            return word.substring(0, word.length() - 1) + "ves";
        } else if (lower.endsWith("fe")) {
            return word.substring(0, word.length() - 2) + "ves";
        } else if (lower.endsWith("o") && !isVowel(lower.charAt(lower.length() - 2))) {
            return word + "es";
        } else {
            return word + "s";
        }
    }

    /**
     * Singularize a word (simple English singularization)
     * Examples: users -> user, categories -> category, people -> person
     */
    public static String singularize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        String lower = word.toLowerCase();

        // Irregular singulars
        switch (lower) {
            case "people": return word.substring(0, word.length() - 6) + "person";
            case "children": return word.substring(0, word.length() - 8) + "child";
            case "men": return word.substring(0, word.length() - 3) + "man";
            case "women": return word.substring(0, word.length() - 5) + "woman";
            case "teeth": return word.substring(0, word.length() - 5) + "tooth";
            case "feet": return word.substring(0, word.length() - 4) + "foot";
            case "mice": return word.substring(0, word.length() - 4) + "mouse";
            case "geese": return word.substring(0, word.length() - 5) + "goose";
        }

        // Regular singularization rules
        if (lower.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
        } else if (lower.endsWith("ves")) {
            return word.substring(0, word.length() - 3) + "f";
        } else if (lower.endsWith("oes") || lower.endsWith("xes") || lower.endsWith("ches") ||
                lower.endsWith("shes") || lower.endsWith("sses")) {
            return word.substring(0, word.length() - 2);
        } else if (lower.endsWith("s") && !lower.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        } else {
            return word;
        }
    }

    /**
     * Convert table name to entity class name
     * Examples: user_profiles -> UserProfile, order_items -> OrderItem
     */
    public static String tableNameToClassName(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return tableName;
        }

        // Remove common table prefixes
        String cleaned = tableName;
        if (cleaned.startsWith("tbl_") || cleaned.startsWith("tab_")) {
            cleaned = cleaned.substring(4);
        }

        // Singularize and convert to PascalCase
        String singular = singularize(cleaned);
        return toPascalCase(singular);
    }

    /**
     * Convert class name to table name
     * Examples: UserProfile -> user_profiles, OrderItem -> order_items
     */
    public static String classNameToTableName(String className) {
        if (className == null || className.isEmpty()) {
            return className;
        }

        String snakeCase = toSnakeCase(className);
        return pluralize(snakeCase);
    }

    /**
     * Escape Java keywords
     * Examples: class -> clazz, import -> import_
     */
    public static String escapeJavaKeyword(String name) {
        if (JAVA_KEYWORDS.contains(name.toLowerCase())) {
            return name + "_";
        }
        return name;
    }

    /**
     * Escape SQL keywords
     * Examples: user -> "user", order -> "order"
     */
    public static String escapeSqlKeyword(String name) {
        if (SQL_KEYWORDS.contains(name.toLowerCase())) {
            return "\"" + name + "\"";
        }
        return name;
    }

    /**
     * Generate getter method name
     * Examples: name -> getName, isActive -> isActive, hasChildren -> hasChildren
     */
    public static String toGetterName(String fieldName, String fieldType) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }

        String capitalizedName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        // Boolean fields use 'is' or keep 'has'/'can'
        if ("boolean".equalsIgnoreCase(fieldType) || "Boolean".equals(fieldType)) {
            if (fieldName.startsWith("is") || fieldName.startsWith("has") || fieldName.startsWith("can")) {
                return fieldName;
            }
            return "is" + capitalizedName;
        }

        return "get" + capitalizedName;
    }

    /**
     * Generate setter method name
     * Examples: name -> setName, isActive -> setActive
     */
    public static String toSetterName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }

        // Remove boolean prefixes
        String cleanName = fieldName;
        if (fieldName.startsWith("is") && fieldName.length() > 2 && Character.isUpperCase(fieldName.charAt(2))) {
            cleanName = Character.toLowerCase(fieldName.charAt(2)) + fieldName.substring(3);
        }

        return "set" + Character.toUpperCase(cleanName.charAt(0)) + cleanName.substring(1);
    }

    /**
     * Generate package name from base package and module
     * Examples: (com.example, user) -> com.example.user
     */
    public static String toPackageName(String basePackage, String module) {
        if (basePackage == null || basePackage.isEmpty()) {
            return module.toLowerCase();
        }

        if (module == null || module.isEmpty()) {
            return basePackage;
        }

        return basePackage + "." + module.toLowerCase();
    }

    /**
     * Check if character is a vowel
     */
    private static boolean isVowel(char c) {
        return "aeiouAEIOU".indexOf(c) >= 0;
    }

    /**
     * Clean string for use as identifier
     * Removes non-alphanumeric characters and formats appropriately
     */
    public static String cleanIdentifier(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Remove non-alphanumeric characters
        String cleaned = NON_ALPHANUMERIC.matcher(input).replaceAll("_");

        // Remove leading/trailing underscores
        cleaned = cleaned.replaceAll("^_+|_+$", "");

        // Ensure it starts with a letter
        if (!cleaned.isEmpty() && Character.isDigit(cleaned.charAt(0))) {
            cleaned = "n" + cleaned;
        }

        return cleaned;
    }

    /**
     * Generate API path from entity name
     * Examples: UserProfile -> /user-profiles, OrderItem -> /order-items
     */
    public static String toApiPath(String entityName) {
        if (entityName == null || entityName.isEmpty()) {
            return "/";
        }

        String kebab = toKebabCase(entityName);
        String plural = pluralize(kebab);
        return "/" + plural;
    }

    /**
     * Generate variable name from class name
     * Examples: UserService -> userService, OrderController -> orderController
     */
    public static String toVariableName(String className) {
        if (className == null || className.isEmpty()) {
            return className;
        }

        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
}