# MCP Server for PostgreSQL Integration

## Overview

This MCP (Model Context Protocol) server provides intelligent code generation for PostgreSQL database integration in Java Spring Boot applications. It works seamlessly with Cursor IDE to generate complete database layers including entities, repositories, services, and REST controllers.

## Features

- 🚀 **Two-Phase Generation**: Plan review before code generation
- 🏗️ **Complete Stack Generation**: Entity, Repository, Service, Controller, DTOs
- 📏 **Coding Standards**: Automatic application of Java/Spring Boot best practices
- 🔄 **Smart Type Mapping**: PostgreSQL to Java type conversion
- 💾 **Caching**: High-performance caching for plans and templates
- 🔧 **Customizable**: Configurable templates and generation rules
- 🎯 **Framework Detection**: Automatic project analysis
- ✅ **Validation**: Built-in schema and code validation

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker (optional)
- Cursor IDE

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/your-org/mcp-server-postgresql.git
cd mcp-server-postgresql
```

2. **Build the project**
```bash
mvn clean package
```

3. **Run the server**
```bash
mvn spring-boot:run
```

Or using Docker:
```bash
docker-compose up
```

4. **Verify installation**
```bash
curl http://localhost:8080/mcp/health
```

## Configuration

### Application Configuration

Edit `src/main/resources/application.yml`:

```yaml
mcp:
  sdk:
    base-path: /opt/mcp-resources/sdk-repositories
    default-sdk: postgresql-java-sdk
  standards:
    base-path: /opt/mcp-resources/coding-standards
    default-standard: java-spring-boot
  generation:
    default-package: com.example
    use-lombok: true
    include-validation: true
```

### Cursor IDE Integration

Add `.cursorrules` to your project:

```yaml
mcp:
  servers:
    - name: postgresql-mcp
      url: http://localhost:8080/mcp
      capabilities:
        - postgresql
        - code-generation
```

## Usage

### Step 1: Create Integration Plan

In Cursor IDE:
```
@mcp I need PostgreSQL integration for my Spring Boot project
```

### Step 2: Review Plan

The server returns a detailed plan showing:
- Files to be created/modified
- Dependencies to be added
- Configuration changes
- Impact assessment

### Step 3: Execute with Schema

```
@mcp execute the plan with:
- users table: id, username, email, password
- posts table: id, user_id, title, content
```

### Step 4: Apply Generated Code

Cursor applies the generated files to your project.

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/mcp/health` | GET | Health check |
| `/mcp/capabilities` | GET | List server capabilities |
| `/mcp/plan/create` | POST | Create integration plan |
| `/mcp/plan/{id}` | GET | Get existing plan |
| `/mcp/plan/execute` | POST | Execute plan with schema |

## Project Structure

```
mcp-server-postgresql/
├── src/main/java/com/mcp/server/
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   ├── model/           # Domain models
│   ├── service/         # Business logic
│   └── util/            # Utility classes
├── src/main/resources/
│   ├── application.yml   # Application config
│   └── templates/        # Freemarker templates
└── pom.xml              # Maven configuration
```

## SDK Repository Structure

The SDK repository (separate) contains:

```
postgresql-java-sdk/
├── sdk-config.yaml       # SDK configuration
├── templates/           # Code templates
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
└── docs/               # SDK documentation
```

## Type Mappings

| PostgreSQL | Java | Notes |
|------------|------|-------|
| BIGINT | Long | Primary keys |
| VARCHAR | String | Text fields |
| TIMESTAMP | LocalDateTime | Timestamps |
| BOOLEAN | Boolean | Flags |
| DECIMAL | BigDecimal | Money/precision |
| JSONB | String | JSON data |
| UUID | UUID | Unique IDs |

## Supported Features

- ✅ CRUD Operations
- ✅ Pagination & Sorting
- ✅ Validation
- ✅ Audit Fields
- ✅ Soft Delete
- ✅ Relationships (OneToMany, ManyToOne, etc.)
- ✅ Custom Queries
- ✅ DTOs & Mappers
- ✅ Exception Handling
- ✅ API Documentation

## Development

### Running Tests

```bash
mvn test
```

### Building Docker Image

```bash
docker build -t mcp-server-postgresql .
```

### Debugging

Enable debug logging:
```yaml
logging:
  level:
    com.mcp: DEBUG
```

## Architecture

```
┌─────────────┐     HTTP      ┌─────────────┐     Templates    ┌─────────────┐
│ Cursor IDE  │──────────────▶│ MCP Server  │◀────────────────│ SDK Repos   │
└─────────────┘               └─────────────┘                  └─────────────┘
                                     │
                                     ▼
                              ┌─────────────┐
                              │ Your Project│
                              └─────────────┘
```

## Troubleshooting

### Server won't start
- Check Java version: `java -version` (must be 17+)
- Verify port 8080 is available
- Check logs: `logs/mcp-server.log`

### Cursor can't connect
- Verify server is running
- Check `.cursorrules` configuration
- Test with curl: `curl http://localhost:8080/mcp/health`

### Generated code doesn't compile
- Ensure all dependencies are in `pom.xml`
- Check Java version compatibility
- Verify package structure

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

MIT License - see LICENSE file for details

## Support

- GitHub Issues: [Report bugs](https://github.com/your-org/mcp-server-postgresql/issues)
- Documentation: [Wiki](https://github.com/your-org/mcp-server-postgresql/wiki)
- Email: mcp-support@example.com

## Roadmap

- [ ] Support for more databases (MongoDB, MySQL)
- [ ] Additional frameworks (Quarkus, Micronaut)
- [ ] GraphQL support
- [ ] Reactive programming support
- [ ] Kubernetes deployment templates
- [ ] CI/CD integration

## Acknowledgments

- Spring Boot team for the excellent framework
- Freemarker for template engine
- Caffeine for high-performance caching
- PostgreSQL community

---

Built with ❤️ for developers who value clean, maintainable code.