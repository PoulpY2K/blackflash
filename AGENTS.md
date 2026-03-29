# AGENTS.md - AI Agent Guide for Blackflash

## Project Overview

**Blackflash** is a Discord music bot built with Spring Boot 4.0.4 and Java 25. It uses **JDA v6.4.0** for Discord API integration and **Lavaplayer v2.2.6** (by Arbjerg) for audio streaming and playback. It's configured for API-first development using OpenAPI specifications with code generation. The project emphasizes:
- **Minimal database usage** (DataSourceAutoConfiguration excluded) - this is primarily a service/integration bot
- **REST client communication** over database persistence
- **Observability** via actuator endpoints with ElasticSearch metrics export
- **Code generation** from OpenAPI specs using `openapi-generator-maven-plugin`

## Build & Development

### Prerequisites
- **Java 25** (Eclipse Adoptium JDK or compatible)
- **Maven 3.9.14+**
- **Timezone**: Paris (automatically set in `BlackflashApplication.main()`)

### Common Commands
```bash
# Clean build with tests and code coverage
mvn clean install

# Compile only (skips tests - useful during development)
mvn clean compile

# Run tests with coverage report
mvn verify
# Coverage output: `target/site/jacoco/index.html`

# Build executable JAR
mvn clean package

# Run the application
java -jar target/blackflash-main-SNAPSHOT.jar

# SonarQube analysis
mvn clean verify sonar:sonar
```

### Development Setup
- **Spring DevTools** enabled: auto-restart on classpath changes
- **Virtual threads**: Enabled via `spring.threads.virtual.enabled: true`
- **Docker Compose**: No `compose.yaml` is present in the repository currently
- **Banner**: Displays from `banner.txt` on startup (console mode)

## Architecture Patterns

### Object Serialization / Deserialization
The project uses a **custom ObjectMapper** (`ObjectMapperConfiguration`) that:
- Converts all incoming string fields to **trimmed strings** via `StringTrimmingDeserializer`
- Uses **snake_case** for JSON properties (via `PropertyNamingStrategies.SNAKE_CASE`)
- Excludes null values from serialized JSON
- Ignores unknown JSON properties (does NOT fail on extra fields)
- Automatically serializes `java.time.*` classes (JSR-310)

**Impact**: When adding new DTOs or API endpoints, ensure string properties are trimmed automatically and snake_case JSON names map correctly.

### Code Generation from OpenAPI Specs
- **Spec location**: `src/main/resources/api-spec/blackflash-api.yaml`
- **Generator**: `openapi-generator-maven-plugin` v7.21.0 configured in `pom.xml` with execution `generate-rest-api`
- **Current behavior**: generation is disabled via `<skip>true</skip>` in that execution
- **Generated packages**: 
  - Controllers: `fr.fumbus.blackflash.api.controllers`
  - DTOs: `fr.fumbus.blackflash.api.dtos`
- **DTO annotations**: Auto-applies `@Builder(toBuilder=true)` and `@Jacksonized` (Lombok)
- **Interface-only generation**: True - controllers are interfaces, you implement them

### Dependency Injection & Mapping
- **MapStruct** v1.6.3 is configured in the build, but no mapper interfaces are currently present in `src/main/java`
- **Spring component model**: MapStruct uses Spring's `@Component` by default
- **Lombok integration**: Configured with special binding (`lombok-mapstruct-binding`) to work with MapStruct processors

## Conventions & Project-Specific Patterns

### Naming & Code Style
- **Package structure**: `fr.fumbus.blackflash.*`
- **Base package constant**: `${java.code.base-package}` in pom.xml
- **Lombok annotations**: Must use `@lombok.Builder(toBuilder=true)` and `@lombok.extern.jackson.Jacksonized` on generated DTOs
- **Character encoding**: UTF-8 throughout (resources, compilation)

### Configuration
- **application.yaml** drives all configuration - no .properties files
- **Actuator endpoints** exposed on port 8080: `/actuator/{health,info,metrics,caches}`
- **Health checks**: Liveness & readiness probes enabled (Kubernetes-compatible)
- **Caching**: Using Caffeine for in-memory caches
- **Security**: Spring Security enabled but custom `UserDetailsServiceAutoConfiguration` excluded

### Testing
- **Framework**: JUnit 5 (Spring Boot Test Suite)
- **Mocking**: Mockito with javaagent (configured in surefire-plugin)
- **Mock HTTP**: OkHttp3 with MockWebServer is available as test dependencies; no MockWebServer-based tests are currently implemented
- **Code coverage**: JaCoCo (reports to `target/site/jacoco/` and SonarQube integration)
- **Current tests**: `src/test/java/fr/fumbus/blackflash/BlackflashApplicationTests.java` only contains a basic `contextLoads()` test
- **Test reports**: Maven Surefire generates reports in `target/surefire-reports/`

### Resilience & Observability
- **Circuit Breaker**: Resilience4j via Spring Cloud (imports from spring-cloud-circuitbreaker-resilience4j)
- **Metrics**: Exported to Elasticsearch (enabled by `management.elastic.metrics.export`)
- **Observability**: OpenTelemetry for distributed tracing
- **Logs**: Log4j2 (not SLF4J+Logback) - configured via `spring-boot-starter-log4j2`

## Key Files & Their Roles

| File | Purpose |
|------|---------|
| `pom.xml` | Maven config: dependencies, plugins, compiler settings (MapStruct, Lombok), code generation |
| `src/main/java/.../BlackflashApplication.java` | Entry point: sets Paris timezone, excludes DB & user-details auto-config |
| `src/main/java/.../configurations/DiscordConfiguration.java` | Creates and initializes JDA using `discord.token`/`discord.activity` properties |
| `src/main/java/.../configurations/ObjectMapperConfiguration.java` | Custom Jackson ObjectMapper bean with string trimming & snake_case deserialization |
| `src/main/java/.../listeners/SlashCommandListener.java` | JDA listener scaffold (extends `ListenerAdapter`, currently empty) |
| `src/main/resources/application.yaml` | Runtime config: actuator endpoints, caching, metrics export, health probes |
| `src/main/resources/api-spec/blackflash-api.yaml` | OpenAPI 3.1.1 specification (currently minimal) |
| `lombok.config` | Lombok global settings: generated annotations, custom Builder class naming |

## Integration Points

### External Communication
- **REST Client**: `spring-boot-starter-restclient` is included, but there are currently no `RestClient` usages in `src/main/java`

### Discord API — JDA v6.4.0
- **Library**: `net.dv8tion:JDA:6.4.0`
- Integrated through JDA in `DiscordConfiguration`; token comes from `DISCORD_TOKEN` via `application.yaml`
- `SlashCommandListener` extends JDA's `ListenerAdapter` — register it as an event listener on the `JDA` instance
- Bot uses slash-command interactions; avoid legacy prefix-based message commands

### Audio Playback — Lavaplayer v2.2.6
- **Library**: `dev.arbjerg:lavaplayer:2.2.6` (pulled from the JitPack repository)
- **`AudioPlayerManager`**: central Lavaplayer entry point; should be a Spring singleton bean
- **`AudioPlayer`**: one per guild (voice channel session); created via `AudioPlayerManager.createPlayer()`
- **`AudioTrack`** / **`AudioTrackEndReason`**: represent individual tracks and end-of-track events
- **`AudioLoadResultHandler`**: callback interface used when loading a track URL or search query
- **`AudioSendHandler`** bridge is required to connect a Lavaplayer `AudioPlayer` to a JDA `AudioManager`
- Supported sources out-of-the-box: YouTube, SoundCloud, Bandcamp, Twitch streams, Vimeo, HTTP streams, and local files

### Data Persistence
- **Database**: Spring Data JPA included but intentionally not wired (no DataSourceAutoConfiguration)
- If you need to add persistence: provide a DataSource bean and remove exclusion from `@SpringBootApplication`

### Dependency Management
- **Spring Cloud BOM**: v2025.1.1 (managed in `<dependencyManagement>`)
- **Resilience4J**: For circuit breaking external API calls
- **Jackson**: Extended with `jackson-datatype-jsr310` for Java time types

## Code Quality & Analysis

- **Code Coverage**: JaCoCo enforced, reports published to SonarQube
- **SonarQube Integration**: Uses organization `poulp2k`, project `poulpy2k_blackflash`
- **Compiler Args**: MapStruct uses Spring's component model (`-Amapstruct.defaultComponentModel=spring`)
- **Encoding**: All sources explicitly UTF-8 at compile time

## Important Notes for AI Agents

1. **No active database** - if data persistence is needed, it requires setup (DataSource bean, liquibase/flyway)
2. **API generation is currently skipped** - set `<skip>false</skip>` in `openapi-generator-maven-plugin` execution `generate-rest-api` to enable OpenAPI→Java code generation
3. **Elasticsearch metrics** require credentials (`${ELASTIC_PASSWORD}` environment variable)
4. **Virtual threads** enabled - async I/O patterns are preferred over traditional thread pools
5. **Snake_case JSON** - all JSON properties are snake_case; use standard field names in Java classes
6. **String trimming is automatic** - all incoming string values are trimmed by ObjectMapperConfiguration
7. **Discord integration uses bot token auth** - `discord.token` defaults to `none` if `DISCORD_TOKEN` is not set, and JDA init errors are logged in `DiscordConfiguration`

