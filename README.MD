# CodeForge - AI-Powered Capability Integration Platform

<div align="center">

[![MCP Protocol](https://img.shields.io/badge/MCP-Protocol-blue)](https://modelcontextprotocol.org)
[![Status](https://img.shields.io/badge/Status-Prototype-orange)](https://github.com/codeforge)
[![Discord](https://img.shields.io/badge/Discord-Community-7289da)](https://discord.gg/codeforge)

**Generate production-ready integration code for databases, authentication, messaging, and more**

[Quick Start](#-quick-start) • [Architecture](#-architecture) • [Supported Capabilities](#-supported-capabilities) • [Installation Scripts](#-installation)

</div>

---

## 🎯 What is CodeForge?

CodeForge is a code generation platform that helps developers quickly integrate various capabilities (databases, authentication, messaging, caching, etc.) into their applications. Instead of writing boilerplate integration code, you describe what you need in natural language, and CodeForge generates production-ready code following best practices.

### What CodeForge Does

- **Generates Integration Code**: Complete code for connecting your application to databases, auth systems, message queues, etc.
- **Follows Best Practices**: Generated code includes proper error handling, connection pooling, retry logic, and security patterns
- **Multi-Stack Support**: Works with Java/Spring Boot, Python/FastAPI, Node.js/Express, Go, and more
- **IDE Integration**: Works seamlessly within your existing development environment

### What CodeForge Does NOT Do

- Does not generate business logic or application features
- Does not modify your core application architecture
- Does not require cloud dependencies (can run entirely local)

## 🏗️ Architecture

### How CodeForge Works

```
┌──────────────────────────────────────────────────────────────────┐
│                        Your Development Machine                    │
│                                                                    │
│  ┌─────────────────┐        ┌─────────────────────────────────┐  │
│  │   Your IDE      │        │   CodeForge MCP Adapter         │  │
│  │  (Cursor/VS    │◄──────►│  • Installed locally            │  │
│  │   Code/etc)    │ stdio  │  • Translates IDE requests      │  │
│  │                │        │  • Manages file operations      │  │
│  └─────────────────┘        └──────────────┬──────────────────┘  │
│                                            │                      │
└────────────────────────────────────────────┼──────────────────────┘
                                             │ HTTPS
                                             ▼
┌──────────────────────────────────────────────────────────────────┐
│                    CodeForge MCP Server                           │
│                  (Cloud or Self-Hosted)                           │
│                                                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  • Processes capability requests                             │ │
│  │  • Loads appropriate SDK templates                           │ │
│  │  • Generates integration code                                │ │
│  │  • Returns structured file content                           │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                              │                                     │
│                              ▼                                     │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │           SDK Repositories (GitHub)                          │ │
│  │  • PostgreSQL + Spring Boot                                  │ │
│  │  • MongoDB + FastAPI                                         │ │
│  │  • Redis + Express.js                                        │ │
│  │  • Auth0 + Next.js                                           │ │
│  │  • Kafka + Go                                                │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

1. **MCP Adapter** (Runs on your machine)
   - Connects your IDE to CodeForge server
   - Analyzes your project structure
   - Applies generated code to correct locations
   - Creates backups before modifying files

2. **MCP Server** (Cloud/Self-hosted)
   - Processes generation requests
   - Manages SDK templates
   - Handles code generation logic
   - Caches templates for performance

3. **SDK Repositories** (GitHub)
   - Contain templates for specific integrations
   - Define type mappings and conventions
   - Include best practices and patterns

## 🚀 Quick Start

### Step 1: Install CodeForge MCP Adapter

Run the appropriate installation script for your operating system:

#### macOS / Linux

```bash
# Download and run the installation script
curl -sSL https://codeforge.dev/install.sh | bash

# Or manually install via npm
npm install -g @codeforge/mcp-adapter

# Verify installation
codeforge-mcp --version
```

#### Windows

```powershell
# Download and run the installation script
iwr -useb https://codeforge.dev/install.ps1 | iex

# Or manually install via npm
npm install -g @codeforge/mcp-adapter

# Verify installation
codeforge-mcp --version
```

### Step 2: Configure Your IDE

#### Cursor IDE Setup

```bash
# Run the automatic setup script
codeforge-mcp setup cursor

# This will:
# 1. Detect Cursor installation
# 2. Add CodeForge to MCP servers
# 3. Configure connection settings
# 4. Test the connection
```

Manual configuration for Cursor:
1. Open Cursor Settings (`Cmd/Ctrl + ,`)
2. Search for "Model Context Protocol" or "MCP"
3. Add to MCP Servers configuration:

```json
{
  "mcpServers": {
    "codeforge": {
      "command": "codeforge-mcp",
      "args": ["--server", "https://api.codeforge.dev/mcp"],
      "env": {
        "CODEFORGE_API_KEY": "optional-api-key"
      }
    }
  }
}
```

#### VS Code Setup

```bash
# Run the automatic setup script
codeforge-mcp setup vscode

# For VS Code with Continue extension
codeforge-mcp setup vscode-continue

# For VS Code with GitHub Copilot
codeforge-mcp setup vscode-copilot
```

Manual configuration for VS Code:
1. Install the Continue extension or GitHub Copilot
2. Run: `codeforge-mcp config vscode --print`
3. Copy the output to your VS Code settings

#### IntelliJ IDEA / WebStorm Setup

```bash
# Run the automatic setup script
codeforge-mcp setup intellij
```

### Step 3: Test the Installation

```bash
# Test connection to CodeForge server
codeforge-mcp test

# This will verify:
# ✓ Adapter is installed correctly
# ✓ Can connect to CodeForge server
# ✓ IDE configuration is valid
# ✓ Project detection is working
```

### Step 4: Start Using CodeForge

In your IDE, use natural language to request capability integrations:

```typescript
// @codeforge integrate PostgreSQL with users and posts tables

// @codeforge add Redis caching for user sessions

// @codeforge integrate JWT authentication with refresh tokens

// @codeforge add Kafka messaging for order events
```

## 📦 Installation Scripts

### Universal Installation Script

Save this as `install-codeforge.sh` (macOS/Linux) or `install-codeforge.ps1` (Windows):

#### macOS/Linux (`install-codeforge.sh`)

```bash
#!/bin/bash

set -e

echo "🚀 Installing CodeForge MCP Adapter..."

# Detect OS
OS="$(uname -s)"
ARCH="$(uname -m)"

# Check Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is required but not installed."
    echo "Please install Node.js 18+ from https://nodejs.org"
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Node.js 18+ is required. Current version: $(node -v)"
    exit 1
fi

# Install adapter
echo "📦 Installing CodeForge MCP Adapter..."
npm install -g @codeforge/mcp-adapter

# Detect IDEs
echo "🔍 Detecting installed IDEs..."

IDES_FOUND=""

# Check for Cursor
if [ -d "$HOME/.cursor" ] || [ -d "/Applications/Cursor.app" ]; then
    IDES_FOUND="$IDES_FOUND cursor"
    echo "✓ Cursor detected"
fi

# Check for VS Code
if command -v code &> /dev/null || [ -d "/Applications/Visual Studio Code.app" ]; then
    IDES_FOUND="$IDES_FOUND vscode"
    echo "✓ VS Code detected"
fi

# Check for IntelliJ
if [ -d "$HOME/.IntelliJIdea"* ] || [ -d "/Applications/IntelliJ IDEA.app" ]; then
    IDES_FOUND="$IDES_FOUND intellij"
    echo "✓ IntelliJ IDEA detected"
fi

# Configure detected IDEs
for IDE in $IDES_FOUND; do
    echo "⚙️  Configuring $IDE..."
    codeforge-mcp setup $IDE --auto
done

# Test installation
echo "🧪 Testing CodeForge installation..."
if codeforge-mcp test; then
    echo "✅ CodeForge successfully installed!"
    echo ""
    echo "📝 Next steps:"
    echo "1. Open your IDE"
    echo "2. Create or open a project"
    echo "3. Type: @codeforge <your integration request>"
    echo ""
    echo "📚 Examples:"
    echo "  @codeforge integrate PostgreSQL with user management"
    echo "  @codeforge add Redis caching"
    echo "  @codeforge integrate JWT authentication"
else
    echo "⚠️  Installation completed but test failed."
    echo "Run 'codeforge-mcp test --debug' for more information."
fi
```

#### Windows (`install-codeforge.ps1`)

```powershell
# PowerShell installation script for Windows

Write-Host "🚀 Installing CodeForge MCP Adapter..." -ForegroundColor Cyan

# Check Node.js
try {
    $nodeVersion = node -v
    $majorVersion = [int]($nodeVersion -replace 'v(\d+)\..*', '$1')
    if ($majorVersion -lt 18) {
        Write-Host "❌ Node.js 18+ is required. Current version: $nodeVersion" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Node.js is required but not installed." -ForegroundColor Red
    Write-Host "Please install Node.js 18+ from https://nodejs.org" -ForegroundColor Yellow
    exit 1
}

# Install adapter
Write-Host "📦 Installing CodeForge MCP Adapter..." -ForegroundColor Green
npm install -g @codeforge/mcp-adapter

# Detect IDEs
Write-Host "🔍 Detecting installed IDEs..." -ForegroundColor Cyan

$idesFound = @()

# Check for Cursor
if (Test-Path "$env:LOCALAPPDATA\Programs\cursor" -or Test-Path "$env:USERPROFILE\.cursor") {
    $idesFound += "cursor"
    Write-Host "✓ Cursor detected" -ForegroundColor Green
}

# Check for VS Code
if (Get-Command code -ErrorAction SilentlyContinue) {
    $idesFound += "vscode"
    Write-Host "✓ VS Code detected" -ForegroundColor Green
}

# Configure detected IDEs
foreach ($ide in $idesFound) {
    Write-Host "⚙️  Configuring $ide..." -ForegroundColor Yellow
    codeforge-mcp setup $ide --auto
}

# Test installation
Write-Host "🧪 Testing CodeForge installation..." -ForegroundColor Cyan
$testResult = codeforge-mcp test

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ CodeForge successfully installed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📝 Next steps:" -ForegroundColor Cyan
    Write-Host "1. Open your IDE"
    Write-Host "2. Create or open a project"
    Write-Host "3. Type: @codeforge <your integration request>"
} else {
    Write-Host "⚠️  Installation completed but test failed." -ForegroundColor Yellow
    Write-Host "Run 'codeforge-mcp test --debug' for more information."
}
```

### One-Line Installation

For quick installation, users can run:

```bash
# macOS/Linux
curl -sSL https://codeforge.dev/install.sh | bash

# Windows (PowerShell)
iwr -useb https://codeforge.dev/install.ps1 | iex

# Or use npm directly
npm install -g @codeforge/mcp-adapter && codeforge-mcp setup --auto
```

## 🔧 Supported Capabilities

### Database Integrations

| Database | Supported Operations | Frameworks |
|----------|---------------------|------------|
| **PostgreSQL** | CRUD, Relationships, Migrations, Indexes | Spring Boot, Django, Express, FastAPI |
| **MySQL** | CRUD, Stored Procedures, Views | Spring Boot, Laravel, Express |
| **MongoDB** | Documents, Aggregations, Indexes | Spring Boot, FastAPI, Express |
| **Redis** | Caching, Pub/Sub, Sessions | All frameworks |
| **DynamoDB** | Tables, GSI, Streams | Serverless, Spring Boot |
| **Elasticsearch** | Indexing, Search, Aggregations | Spring Boot, FastAPI |

### Authentication & Authorization

| Provider | Features | Frameworks |
|----------|----------|------------|
| **JWT** | Tokens, Refresh, Claims | All frameworks |
| **OAuth2** | Social login, PKCE | Spring Boot, Express |
| **Auth0** | Complete integration | React, Next.js, Angular |
| **Firebase Auth** | Users, Roles, Rules | React, Flutter |
| **AWS Cognito** | User pools, Federation | Serverless, Spring Boot |

### Messaging & Events

| System | Patterns | Frameworks |
|--------|----------|------------|
| **Kafka** | Producer, Consumer, Streams | Spring Boot, Go |
| **RabbitMQ** | Publish, Subscribe, RPC | All frameworks |
| **AWS SQS** | Queues, DLQ, FIFO | Serverless, Spring Boot |
| **Redis Pub/Sub** | Real-time messaging | Node.js, Python |

### Cloud Services

| Service | Integrations | Frameworks |
|---------|-------------|------------|
| **AWS S3** | Upload, Download, Presigned URLs | All frameworks |
| **SendGrid** | Email templates, Webhooks | All frameworks |
| **Stripe** | Payments, Subscriptions | Node.js, Python |
| **Twilio** | SMS, Voice, Video | All frameworks |

## 🎯 Usage Examples

### Database Integration

```typescript
// In your IDE, type:
@codeforge integrate PostgreSQL with these tables:
- users: id, email, username, password_hash, created_at
- posts: id, user_id, title, content, published_at
- comments: id, post_id, user_id, content, created_at

// CodeForge will generate:
// ✓ Entity classes with relationships
// ✓ Repository interfaces
// ✓ Service layer with business logic
// ✓ REST controllers
// ✓ Database migrations
// ✓ Connection configuration
```

### Authentication Setup

```typescript
@codeforge add JWT authentication with:
- Access tokens (15 min expiry)
- Refresh tokens (7 days)
- Role-based access (admin, user, guest)
- Password reset flow
- Email verification

// Generates complete auth system
```

### Message Queue Integration

```typescript
@codeforge integrate Kafka for:
- Order events (created, processed, shipped)
- Inventory updates
- Email notifications

// Generates producers, consumers, and event schemas
```

### Caching Layer

```typescript
@codeforge add Redis caching for:
- User sessions (30 min TTL)
- API responses (5 min TTL)
- Rate limiting (100 req/min)

// Generates caching service and decorators
```

## 🔍 How It Works - Step by Step

1. **You Request a Capability**
   - Type your request in natural language in your IDE
   - Example: "integrate PostgreSQL for user management"

2. **Adapter Analyzes Your Project**
   - Detects language (Java, Python, TypeScript, etc.)
   - Identifies framework (Spring Boot, FastAPI, Express, etc.)
   - Finds project structure and package organization

3. **Server Creates Integration Plan**
   - Selects appropriate SDK templates
   - Plans what files to generate/modify
   - Calculates dependencies needed

4. **You Review the Plan**
   - See exactly what will be generated
   - Customize options (Lombok, validation, etc.)
   - Approve or modify the plan

5. **Code Generation**
   - Server generates all integration code
   - Adapter applies files to your project
   - Creates backups of modified files

6. **Ready to Use**
   - Complete integration code in place
   - Dependencies added to build file
   - Configuration files updated
   - Run your application

## 🛠️ Configuration

### Server Configuration

By default, CodeForge connects to the cloud server. For self-hosted or development:

```bash
# Use a different server
export CODEFORGE_SERVER=https://your-server.com/mcp

# Or configure in .env file
CODEFORGE_SERVER=http://localhost:8080/mcp
CODEFORGE_API_KEY=your-api-key
```

### Project Configuration

Create `.codeforgerc.json` in your project root:

```json
{
  "preferences": {
    "language": "java",
    "framework": "spring-boot",
    "useLombok": true,
    "generateTests": true,
    "includeDocumentation": true
  },
  "exclude": [
    "node_modules",
    "target",
    "build"
  ]
}
```

## 🧪 Testing Your Setup

After installation, verify everything works:

```bash
# Full system test
codeforge-mcp test --full

# This checks:
# ✓ Adapter installation
# ✓ Node.js version
# ✓ IDE configuration
# ✓ Server connectivity
# ✓ Project detection
# ✓ File permissions
# ✓ Backup directory

# Quick connectivity test
codeforge-mcp ping

# Check project detection
codeforge-mcp analyze
```

## 🔧 Troubleshooting

### Common Issues and Solutions

#### "Command not found: codeforge-mcp"
```bash
# Ensure npm global bin is in PATH
export PATH="$PATH:$(npm config get prefix)/bin"

# Or reinstall
npm install -g @codeforge/mcp-adapter
```

#### "Cannot connect to server"
```bash
# Check server status
curl https://api.codeforge.dev/mcp/health

# Try with explicit server
codeforge-mcp test --server https://api.codeforge.dev/mcp
```

#### "IDE doesn't recognize CodeForge"
```bash
# Reinstall IDE configuration
codeforge-mcp setup [ide-name] --force

# Check IDE logs
codeforge-mcp diagnose [ide-name]
```

#### "Project type not detected"
```bash
# Manually specify project type
codeforge-mcp --project-type spring-boot

# Or add to project config
echo '{"project": {"type": "spring-boot"}}' > .codeforgerc.json
```

## 📊 Local Development

For testing with a local server:

```bash
# Clone and run the MCP server locally
git clone https://github.com/codeforge/mcp-server
cd mcp-server
mvn spring-boot:run

# Configure adapter to use local server
export CODEFORGE_SERVER=http://localhost:8080/mcp

# Test local setup
codeforge-mcp test
```

## 🤝 Getting Help

### Resources
- **Documentation**: [docs.codeforge.dev](https://docs.codeforge.dev)
- **Discord Community**: [discord.gg/codeforge](https://discord.gg/codeforge)
- **GitHub Issues**: [github.com/codeforge/mcp-adapter/issues](https://github.com/codeforge/mcp-adapter/issues)

### Support Commands
```bash
# Get system information for bug reports
codeforge-mcp info --system

# Generate diagnostic report
codeforge-mcp diagnose --output report.json

# Check for updates
codeforge-mcp update --check
```

## 📄 License

MIT License - Free for commercial and personal use.

---

<div align="center">

**CodeForge** - Generate capability integrations in seconds, not hours

[Documentation](https://docs.codeforge.dev) • [Discord](https://discord.gg/codeforge) • [GitHub](https://github.com/codeforge)

</div>