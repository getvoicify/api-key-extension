# Keycloak API Key Extension

[![CI/CD Pipeline](https://github.com/getvoicify/api-key-extension/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/getvoicify/api-key-extension/actions/workflows/ci-cd.yml)
[![codecov](https://codecov.io/gh/getvoicify/api-key-extension/branch/main/graph/badge.svg)](https://codecov.io/gh/getvoicify/api-key-extension)

A Keycloak extension that provides API key functionality for the Voicify platform. This extension adds REST endpoints to create and validate API keys stored as user attributes in Keycloak.

## Features

- **API Key Creation**: Create API keys for authenticated users with proper role validation
- **API Key Retrieval**: Retrieve existing API keys for authenticated users
- **API Key Validation**: Validate API keys via REST endpoint
- **API Key Rotation**: Rotate existing API keys for enhanced security
- **Role-Based Access**: Requires "create-pat" role in "iam" client for API key operations
- **Jakarta EE Compatible**: Built for Keycloak 26.x with full Jakarta EE support

## Build and Test

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker (for integration tests)

### Commands

```bash
# Compile the project
./mvnw clean compile

# Run tests
./mvnw test

# Package JAR
./mvnw clean package

# Run all quality checks
./mvnw spotless:check spotbugs:check pmd:check

# Run security scan
./mvnw org.owasp:dependency-check-maven:check
```

## API Endpoints

The extension provides the following REST endpoints under `/realms/{realm-name}/api-key/`:

- `GET /check?apiKey=...` - Validates an API key
- `GET /` - Retrieves the authenticated user's API key
- `POST /` - Creates a new API key for authenticated users
- `PUT /rotate` - Rotates an existing API key for authenticated users

## Authentication

### API Key Creation (`POST /`)
- Requires Bearer token authentication
- User must have "create-pat" role in "iam" client
- API keys are stored as "api-key" user attributes

### API Key Retrieval (`GET /`)
- Requires Bearer token authentication
- User must have "create-pat" role in "iam" client
- Returns API key in JSON format: `{"apiKey": "..."}`
- Returns 404 if user has no existing API key

### API Key Rotation (`PUT /rotate`)
- Requires Bearer token authentication
- User must have "create-pat" role in "iam" client
- Replaces existing API key with new randomly generated key
- Returns 404 if user has no existing API key

### API Key Validation (`GET /check`)
- No authentication required
- Returns 401 if API key is invalid or missing
- Returns 200 if API key is valid

## Deployment

1. Build the JAR: `./mvnw clean package`
2. Copy the JAR to Keycloak's providers directory
3. Restart Keycloak to load the extension
4. API will be available at `/realms/{realm-name}/api-key/`

## Automated Versioning

This project uses **automated semantic versioning** based on [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` → Minor version bump (1.0.0 → 1.1.0)
- `fix:` → Patch version bump (1.0.0 → 1.0.1)  
- `BREAKING CHANGE:` or `!` → Major version bump (1.0.0 → 2.0.0)

**Example commits:**
```bash
git commit -m "feat: add API key rotation endpoint"     # → 1.1.0
git commit -m "fix: resolve authentication timeout"    # → 1.0.1
git commit -m "feat!: change API response format"      # → 2.0.0
```

See [VERSIONING.md](VERSIONING.md) for detailed documentation.

## GitHub Packages

This project automatically publishes artifacts to GitHub Packages when:
- Commits with `feat:`, `fix:`, or breaking changes are pushed to main branch
- Semantic version is automatically calculated and applied
- GitHub releases are created automatically with changelog

### Using the Package

Add this repository to your Maven settings.xml:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/getvoicify/api-key-extension</url>
  </repository>
</repositories>
```

Then include the dependency (check [releases](https://github.com/getvoicify/api-key-extension/releases) for latest version):

```xml
<dependency>
  <groupId>com.getvoicify</groupId>
  <artifactId>api-key-extension</artifactId>
  <version>${latest.version}</version>
</dependency>
```

## Development

See [CLAUDE.md](CLAUDE.md) for detailed development guidance and architecture information.

## CI/CD Pipeline

The project uses two separate GitHub Actions workflows:

### **Pull Request Workflow** (`pr-check.yml`)
- ✅ **Real-time PR status updates** with progress comments
- ✅ **Code Quality & Linting**: Spotless formatting, SpotBugs, PMD analysis
- ✅ **Build & Test**: Compilation and test execution with Testcontainers  
- ✅ **Security Scan**: OWASP dependency vulnerability check
- ✅ **Test Results**: Uploaded as artifacts for review

### **Main Branch Workflow** (`ci-cd.yml`)
- ✅ **Semantic Versioning**: Automatic version calculation from commits
- ✅ **Build & Test**: Full compilation and testing
- ✅ **GitHub Packages**: Automatic publishing for releases
- ✅ **GitHub Releases**: Automatic release creation with changelog

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.