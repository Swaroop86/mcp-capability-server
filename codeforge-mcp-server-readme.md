# CodeForge MCP Server 🚀

<div align="center">

[![MCP Protocol](https://img.shields.io/badge/MCP-Protocol-blue)](https://modelcontextprotocol.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)

**The intelligent code generation engine that powers AI-assisted development**

[Documentation](https://docs.codeforge.dev) • [API Reference](https://api.codeforge.dev) • [SDK Registry](https://sdks.codeforge.dev) • [Status](https://status.codeforge.dev)

</div>

---

## 🎯 What is CodeForge MCP Server?

CodeForge MCP Server is the core intelligence engine of the CodeForge ecosystem - a high-performance, cloud-native service that orchestrates code generation across multiple languages, frameworks, and databases. It acts as the brain that processes natural language requests from AI assistants and transforms them into production-ready code using modular SDK repositories.

### 🌟 Key Features

- **🧠 Intelligent Plan Generation**: Two-phase approach with plan review before code generation
- **🔌 SDK Plugin Architecture**: Dynamically loads and caches SDK repositories from Git
- **⚡ High-Performance Caching**: Caffeine-powered caching for plans, templates, and SDKs
- **🔄 Hot-Reload Capabilities**: Automatically updates when SDKs are modified
- **📊 Multi-Database Support**: PostgreSQL, MySQL, MongoDB, DynamoDB, and more
- **🏗️ Framework Agnostic**: Supports Spring Boot, FastAPI, Express.js, and growing
- **🔐 Enterprise Ready**: Built-in security, rate limiting, and audit logging
- **☁️ Cloud Native**: Kubernetes-ready with health checks and metrics

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   MCP Adapters (Cursor, VSCode, etc.)       │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTPS/WebSocket
┌────────────────────────▼────────────────────────────────────┐
│                  CodeForge MCP Server                        │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                 Plan Generation Engine                  │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │                 Code Generation Engine                  │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │                   Template Processor                    │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │                    SDK Cache Manager                    │ │
│  └────────────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────────┘
                         │ Git Clone/Pull
┌────────────────────────▼────────────────────────────────────┐
│              SDK Repositories (GitHub/GitLab)                │
└──────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### Using Docker (Recommended for Production)

```bash
# Pull the official image
docker pull codeforge/mcp-server:latest

# Run with environment configuration
docker run -d \
  -p 8080:8080 \
  -e SDK_REGISTRY_URL=https://github.com/codeforge-sdks \
  -e CACHE_SIZE=2000 \
  -e PLAN_EXPIRATION_MINUTES=120 \
  --name codeforge-server \
  codeforge/mcp-server:latest

# Check health
curl http://localhost:8080/mcp/health
```

### Using Docker Compose

```yaml
# docker-compose.yml
version: '3.8'
services:
  codeforge:
    image: codeforge/mcp-server:latest
    ports:
      - "8080:8080"
    environment:
      - SDK_REGISTRY_URL=https://github.com/codeforge-sdks
      - SPRING_PROFILES_ACTIVE=production
      - CACHE_TYPE=redis
    volumes:
      - ./sdk-cache:/opt/codeforge/sdk-cache
      - ./logs:/opt/codeforge/logs
    restart: unless-stopped
```

### Local Development

```bash
# Clone the repository
git clone https://github.com/codeforge/mcp-server.git
cd mcp-server

# Build with Maven
mvn clean package

# Run with custom configuration
java -jar target/codeforge-mcp-server.jar \
  --spring.profiles.active=dev \
  --mcp.sdk.registry-url=https://github.com/codeforge-sdks

# Or use Spring Boot Maven plugin
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🔧 Configuration

### Core Configuration (`application.yml`)

```yaml
codeforge:
  server:
    mode: production
    
  sdk:
    registry-url: ${SDK_REGISTRY_URL:https://github.com/codeforge-sdks}
    cache-dir: ${SDK_CACHE_DIR:/opt/codeforge/sdk-cache}
    refresh-interval: ${SDK_REFRESH_INTERVAL:3600}
    allowed-organizations:
      - codeforge-sdks
      - your-org
    
  cache:
    type: ${CACHE_TYPE:caffeine}
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
    
  security:
    api-key-enabled: ${API_KEY_ENABLED:false}
    rate-limiting: ${RATE_LIMITING:true}
    max-requests-per-minute: ${MAX_REQUESTS:100}
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SDK_REGISTRY_URL` | Base URL for SDK repositories | `https://github.com/codeforge-sdks` |
| `SDK_CACHE_DIR` | Local cache directory for SDKs | `/opt/codeforge/sdk-cache` |
| `PLAN_EXPIRATION_MINUTES` | Plan cache expiration time | `120` |
| `CACHE_TYPE` | Cache implementation (caffeine/redis) | `caffeine` |
| `API_KEY_ENABLED` | Enable API key authentication | `false` |
| `LOG_LEVEL` | Logging level | `INFO` |

## 📡 API Endpoints

### Core Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/mcp/health` | GET | Health check and status |
| `/mcp/capabilities` | GET | List server capabilities and loaded SDKs |
| `/mcp/plan/create` | POST | Create integration plan |
| `/mcp/plan/{id}` | GET | Retrieve existing plan |
| `/mcp/plan/execute` | POST | Execute plan with schema |
| `/mcp/sdk/list` | GET | List available SDKs |
| `/mcp/sdk/refresh` | POST | Force refresh SDK cache |

### Admin Endpoints (Protected)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/admin/metrics` | GET | Server metrics and statistics |
| `/admin/cache/stats` | GET | Cache statistics |
| `/admin/sdk/reload` | POST | Reload specific SDK |
| `/admin/logs` | GET | Stream server logs |

## 🔌 SDK Integration

### Registering New SDKs

```bash
# Add SDK to registry
curl -X POST http://localhost:8080/mcp/sdk/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "postgresql-python-django",
    "repository": "https://github.com/codeforge-sdks/postgresql-python-django.git",
    "version": "1.0.0"
  }'
```

### SDK Discovery

The server automatically discovers SDKs from the configured registry:

1. Scans the registry organization for repositories
2. Validates SDK structure (`sdk-config.yaml` presence)
3. Clones/updates repositories to local cache
4. Loads templates and configurations into memory
5. Refreshes cache based on configured interval

## 🔒 Security

### API Key Authentication

```bash
# Generate API key
curl -X POST http://localhost:8080/admin/api-keys/generate \
  -H "Authorization: Bearer ADMIN_TOKEN"

# Use API key in requests
curl http://localhost:8080/mcp/plan/create \
  -H "X-API-Key: your-api-key" \
  -d '{"capability": "postgresql", ...}'
```

### Rate Limiting

- Default: 100 requests per minute per IP
- Configurable per API key
- Bypass available for trusted sources

## 📊 Monitoring & Observability

### Prometheus Metrics

```yaml
# Exposed at /actuator/prometheus
codeforge_plans_created_total
codeforge_code_generated_lines_total
codeforge_sdk_cache_hits_total
codeforge_request_duration_seconds
```

### Health Checks

```json
GET /mcp/health

{
  "status": "UP",
  "components": {
    "sdk-cache": "UP",
    "template-engine": "UP",
    "plan-store": "UP"
  },
  "sdks-loaded": 15,
  "uptime": "2d 14h 30m"
}
```

## 🚢 Deployment

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: codeforge-mcp-server
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: mcp-server
        image: codeforge/mcp-server:latest
        ports:
        - containerPort: 8080
        env:
        - name: SDK_REGISTRY_URL
          value: "https://github.com/codeforge-sdks"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
```

### AWS ECS

See [deployment/aws-ecs](deployment/aws-ecs) for CloudFormation templates

### Google Cloud Run

```bash
gcloud run deploy codeforge-mcp-server \
  --image codeforge/mcp-server:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars SDK_REGISTRY_URL=https://github.com/codeforge-sdks
```

## 🔄 High Availability

### Multi-Instance Setup

- Stateless design allows horizontal scaling
- Redis cache for shared state across instances
- Load balancer health checks via `/mcp/health`
- Graceful shutdown with connection draining

### Backup & Recovery

- Plans stored in Redis with configurable persistence
- SDK cache can be rebuilt from Git repositories
- Configuration managed via environment variables

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run with test profile
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Load testing
k6 run tests/load/performance.js
```

## 📈 Performance

- **Plan Creation**: < 100ms average
- **Code Generation**: < 500ms for typical schema
- **SDK Cache Hit Rate**: > 95%
- **Concurrent Requests**: 1000+ with proper resources
- **Memory Usage**: ~512MB baseline, scales with cache

## 🐛 Troubleshooting

### Common Issues

1. **SDK Loading Failures**
   ```bash
   # Check SDK cache directory
   ls -la /opt/codeforge/sdk-cache
   
   # Force refresh
   curl -X POST http://localhost:8080/mcp/sdk/refresh
   ```

2. **Plan Expiration**
   ```bash
   # Increase expiration time
   export PLAN_EXPIRATION_MINUTES=240
   ```

3. **Memory Issues**
   ```bash
   # Increase heap size
   java -Xmx2g -jar codeforge-mcp-server.jar
   ```

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

## 📄 License

MIT License - see [LICENSE](LICENSE) file.

## 🔗 Links

- **Documentation**: [docs.codeforge.dev](https://docs.codeforge.dev)
- **SDK Registry**: [github.com/codeforge-sdks](https://github.com/codeforge-sdks)
- **Discord Community**: [discord.gg/codeforge](https://discord.gg/codeforge)
- **Status Page**: [status.codeforge.dev](https://status.codeforge.dev)

---

<div align="center">

**Built with ❤️ by the CodeForge Team**

*Empowering developers to build faster, better, and smarter*

</div>