# PostgreSQL Java SDK Usage Guide

## Quick Start

1. Configure your database schema
2. Use MCP server to generate code
3. Customize templates as needed

## Template Variables

- `${className}` - Entity class name
- `${tableName}` - Database table name  
- `${fields}` - List of field definitions
- `${package.*}` - Package structure
- `${lombok}` - Lombok enabled flag
- `${validation}` - Validation enabled flag

## Customization

Edit templates in the `templates/` directory to customize generated code.