# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Keycloak extension that provides API key functionality for the Voicify platform. The extension adds REST endpoints to create, rotate, and validate API keys stored as user attributes in Keycloak.

## Build System

This project uses Maven with Java 17. Key commands:

- **Build the project**: `./mvnw clean compile`
- **Package as JAR**: `./mvnw clean package`
- **Run tests**: `./mvnw test`
- **Run single test**: `./mvnw test -Dtest=ApiKeyResourceProviderTest`

The final JAR is built as `com.getvoicify-api-key-extension.jar` in the `target/` directory.

## Architecture

### Core Components

1. **ApiKeyResourceProviderFactory** (`src/main/java/com/getvoicify/providers/ApiKeyResourceProviderFactory.java:10`) - Factory class that registers the "api-key" provider with Keycloak
2. **ApiKeyResourceProvider** (`src/main/java/com/getvoicify/providers/ApiKeyResourceProvider.java:14`) - Provider that creates the API key resource
3. **ApiKeyResource** (`src/main/java/com/getvoicify/resources/ApiKeyResource.java:21`) - Main REST resource with three endpoints:
   - `GET /check?apiKey=...` - Validates an API key
   - `POST /` - Creates a new API key for authenticated users
   - `PUT /rotate` - Rotates an existing API key for authenticated users

### Authentication Flow

The extension requires:
- Bearer token authentication for API key creation and rotation
- User must have the "create-pat" role in the "iam" client
- API keys are stored as "api-key" user attributes using JPA entities
- Rotation replaces existing API keys with newly generated ones

### Key Dependencies

- Keycloak 26.3.1 for core functionality
- JUnit Jupiter 5.8.1 for testing
- Testcontainers with Keycloak container for integration tests
- REST Assured for API testing

### Extension Registration

The extension is registered via the SPI file at `src/main/resources/META-INF/services/org.keycloak.services.resource.RealmResourceProviderFactory` which contains the factory class name.

## Testing

Tests use Testcontainers to spin up a Keycloak instance with the extension loaded. The test container automatically loads classes from `target/classes`.

Key test scenarios:
- API key validation without key (401 expected)
- API key creation without authentication (401 expected)
- Successful API key creation with proper authentication (200 expected)
- API key rotation without authentication (403 expected)
- Successful API key rotation with proper authentication (200 expected)
- Multiple rotations to ensure repeatability

## Deployment

To deploy this extension to Keycloak:
1. Build the JAR with `./mvnw clean package`
2. Copy the JAR to Keycloak's providers directory
3. Restart Keycloak to load the extension
4. The API will be available at `/realms/{realm-name}/api-key/`