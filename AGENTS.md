# AGENTS.md - AI Agent Guide for Blackflash

## Project Overview

**Blackflash** is a Discord music bot built with Spring Boot 4.0.5 and Java 25. It uses **JDA v6.4.1** for Discord API
integration and **Lavalink Client v3.4.0** (by Arbjerg) for audio streaming and playback via an external Lavalink v4
node. It's configured for API-first development using OpenAPI specifications with code generation. The project
emphasizes:

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
java -jar target/blackflash-MANAGE_BY_EXTENSION.jar

# SonarQube analysis
mvn clean verify sonar:sonar
```

### Development Setup

- **Spring DevTools** enabled: auto-restart on classpath changes
- **Virtual threads**: Enabled via `spring.threads.virtual.enabled: true` (covers Spring MVC / `@Async`); JDA events
  also run on virtual threads via `.setEventPool(Executors.newVirtualThreadPerTaskExecutor(), true)` in
  `DiscordConfiguration` — every slash command interaction and voice event gets its own virtual thread
- **Docker Compose**: `docker-compose-dev.yml` is present — runs a Lavalink v4 (alpine) node on `127.0.0.1:2333`; config
  is
  mounted from `lavalink/application.yml`; start with `docker compose -f docker-compose-dev.yml up -d` before running
  the bot locally; also accepts `SPOTIFY_ID`, `SPOTIFY_SECRET`, `SPOTIFY_DC` env vars for Spotify source support
- **Banner**: Displays from `banner.txt` on startup (console mode)

## Architecture Patterns

### Object Serialization / Deserialization

The project uses a **custom `JsonMapper`** (`ObjectMapperConfiguration`) that:

- Converts all incoming string fields to **trimmed strings** via `StringTrimmingDeserializer`
- Uses **snake_case** for JSON properties (via `PropertyNamingStrategies.SNAKE_CASE`)
- Excludes null values from serialized JSON
- Ignores unknown JSON properties (does NOT fail on extra fields)
- Supports `java.time.*` classes via `DateTimeFeature.WRITE_DATES_WITH_ZONE_ID` (built into Jackson 3.x)

**Jackson 3.x (Spring Boot 4)**: Core API imports use `tools.jackson.*` (not `com.fasterxml.jackson.*`); Jackson
annotation imports (e.g., `@JsonInclude`) remain in `com.fasterxml.jackson.annotation.*` — that package did not move.
`StringTrimmingDeserializer`
extends `ValueDeserializer<String>`, and the bean type is `JsonMapper` (not `ObjectMapper`). There is no separate
`jackson-datatype-jsr310` dependency — JSR-310 support is built in.

**Impact**: When adding new DTOs or API endpoints, ensure string properties are trimmed automatically and snake_case
JSON names map correctly.

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
- **Lombok annotations**: Must use `@lombok.Builder(toBuilder=true)` and `@lombok.extern.jackson.Jacksonized` on
  generated DTOs
- **Character encoding**: UTF-8 throughout (resources, compilation)

### Configuration

- **application.yaml** drives all configuration - no .properties files
- **Actuator endpoints** exposed on port **54001** (separate management port via `management.server.port: 54001`):
  `/actuator/{health,info,metrics,caches}`
- **Health checks**: Liveness & readiness probes enabled (Kubernetes-compatible)
- **Caching**: Using Caffeine for in-memory caches
- **Security**: Spring Security enabled but custom `UserDetailsServiceAutoConfiguration` excluded

### Testing

- **Framework**: JUnit 5 (Spring Boot Test Suite)
- **Mocking**: Mockito 5 with byte-buddy inline mock maker (no javaagent required); `MockedStatic` is used for static
  method stubbing (e.g., `JDABuilder.createDefault`, `Helpers.getUserIdFromToken`); `MockedConstruction` is used for
  constructor stubbing (e.g., `LavalinkClient`, `TrackScheduler`)
- **Mock HTTP**: OkHttp3 with MockWebServer is available as test dependencies; no MockWebServer-based tests are
  currently implemented
- **Reactor**: `io.projectreactor:reactor-test` is available as a test dependency
- **Code coverage**: JaCoCo (reports to `target/site/jacoco/` and SonarQube integration)
- **Current tests**:
    - `BlackflashApplicationTests.java`: `contextLoads()` + `main_setsDefaultTimezoneToEuropeParis()` (uses
      `MockedStatic<SpringApplication>`)
    - `DiscordConfigurationTests.java`: invalid token is caught, unexpected exceptions are rethrown, success path builds
      JDA and registers commands
    - `LavalinkConfigurationTests.java`: verifies token extraction, node config, load-balancer penalty provider,
      event subscriptions (`ReadyEvent`, `StatsEvent`, `EmittedEvent`, node-level `TrackStartEvent`), and handler logic
    - `ObjectMapperConfigurationTests.java`: full coverage of trimming, snake_case, null exclusion, JSR-310, and
      `StringTrimmingDeserializer` directly
    - `SlashCommandListenerTests.java`: listener extends `ListenerAdapter`; `init()` does not throw;
      `TrackStartEvent`/`TrackEndEvent` subscriptions forward to the correct guild's `TrackScheduler` and do nothing
      when no manager is registered; `onReady` registers `CommandData` from all handlers; dispatch guards reject
      interactions when the member is not in a voice channel, when the bot is not in a voice channel (and the handler
      requires it), and when the command is unknown; dispatching calls `handler.handle()` when all guards pass
    - `SlashJoinCommandHandlerTests.java`: command data is guild-scoped with no options; `requiresBotInVoiceChannel()`
      returns `false`; handle replies ephemeral when bot is already in a channel; handle connects to the member's
      channel and replies when bot is absent; `joinChannel()` calls `registry.getOrCreate()`
    - `SlashLeaveCommandHandlerTests.java`: command data is guild-scoped with no options; handle disconnects,
      removes the manager via the registry, and replies
    - `SlashLoopCommandHandlerTests.java`: command data is guild-scoped with no options; all three cycle transitions
      (`DISABLED→TRACK` replies "🔂 Loop track enabled!", `TRACK→QUEUE` replies "🔁 Loop queue enabled!",
      `QUEUE→DISABLED` replies "Loop disabled!")
    - `SlashPlayCommandHandlerTests.java`: command data has exactly one required `STRING` option named `query`;
      `requiresBotInVoiceChannel()` returns `false`; handle defers reply and skips `joinChannel` when bot is already
      in a channel; handle calls `joinChannel` and loads the track when bot is absent
    - `SlashSkipCommandHandlerTests.java`: command data is guild-scoped with no options; handle calls `skip()` on the
      scheduler and replies "Skipped the current track!"
    - `SlashStopCommandHandlerTests.java`: command data is guild-scoped with no options; handle calls `stop()` on
      the manager and replies "Stopped the current track!"
    - `GuildMusicManagerRegistryTests.java`: `getOrCreate()` creates a new manager for an unknown guild and returns
      the same instance on subsequent calls; `remove()` removes the manager from the registry
    - `GuildMusicManagerTests.java`: constructor creates `TrackScheduler` via `MockedConstruction`; `getLink()`/
      `getPlayer()` return empty when no link is cached; return present values when link is cached; `stop()` clears
      queue and calls `setPaused(false)` (no throw when player is absent)
    - `AudioLoaderTests.java`: `ontrackLoaded` attaches `UserData` and enqueues; `onPlaylistLoaded` enqueues all tracks;
      `onSearchResultLoaded` picks first result (or sends "No tracks found!" if empty); `noMatches` and `loadFailed`
      send appropriate hook messages
    - `TrackSchedulerTests.java`: `enqueue()` starts track immediately when player absent or no current track, queues
      when playing; `enqueuePlaylist()` queues all when playing, starts first and queues rest otherwise; `onTrackStart`/
      `onTrackEnd` do not throw; `onTrackEnd` with `STOPPED` reason does not start next track; `DISABLED` mode starts
      next queue track; `TRACK` mode restarts ended track; `QUEUE` mode re-queues ended track and starts next (loops
      current track when queue is empty)
    - `UserDataTests.java`: stores requester ID; equal/hashCode for same ID; not equal for different IDs
- **Test reports**: Maven Surefire generates reports in `target/surefire-reports/`

### Resilience & Observability

- **Circuit Breaker**: Resilience4j via Spring Cloud (imports from spring-cloud-circuitbreaker-resilience4j)
- **Metrics**: Exported to Elasticsearch (enabled by `management.elastic.metrics.export`)
- **Observability**: OpenTelemetry for distributed tracing
- **Logs**: Log4j2 (not SLF4J+Logback) - configured via `spring-boot-starter-log4j2`

## Key Files & Their Roles

| File                                                               | Purpose                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `pom.xml`                                                          | Maven config: dependencies, plugins, compiler settings (MapStruct, Lombok), code generation                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `src/main/java/.../BlackflashApplication.java`                     | Entry point: sets Paris timezone, excludes DB & user-details auto-config                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `src/main/java/.../configurations/DiscordConfiguration.java`       | Creates and initializes JDA using `discord.token`/`discord.activity` properties; sets `.setEventPool(Executors.newVirtualThreadPerTaskExecutor(), true)` so every JDA event runs on its own virtual thread; injects `SlashCommandListener` and `LavalinkClient`; sets `JDAVoiceUpdateListener(lavalink)` as voice dispatch interceptor; enables DAVE E2E audio encryption via `AudioModuleConfig` + `JDaveSessionFactory`                                                                                                                            |
| `src/main/java/.../configurations/LavalinkConfiguration.java`      | Creates the `LavalinkClient` Spring bean; registers Lavalink nodes and infrastructure event listeners                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `src/main/java/.../configurations/ObjectMapperConfiguration.java`  | Custom Jackson `JsonMapper` bean with string trimming & snake_case deserialization (Jackson 3.x / `tools.jackson.*`)                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `src/main/java/.../discord/slash/SlashCommandHandler.java`         | Interface contract for a single slash-command handler: `commandData()`, `requiresBotInVoiceChannel()` (default `true`), `handle()`; discovered and dispatched by `SlashCommandListener` via Spring `List<SlashCommandHandler>` injection                                                                                                                                                                                                                                                                                                             |
| `src/main/java/.../discord/slash/SlashCommandListener.java`        | Spring `@Component`; extends `ListenerAdapter`; registers commands from injected `List<SlashCommandHandler>` in `onReady()`; dispatches interactions by `commandData().getName()`; guards member/bot voice-channel presence; subscribes to Lavalink track events via `@PostConstruct`                                                                                                                                                                                                                                                                |
| `src/main/java/.../discord/slash/handlers/`                        | One `@Component` per command: `SlashJoinCommandHandler`, `SlashLeaveCommandHandler`, `SlashLoopCommandHandler`, `SlashPlayCommandHandler`, `SlashSkipCommandHandler`, `SlashStopCommandHandler`; each implements `SlashCommandHandler` and owns its own `CommandData` definition                                                                                                                                                                                                                                                                     |
| `src/main/java/.../discord/slash/utils/SlashCommandConstants.java` | `@UtilityClass` holding string constants for all slash command names (`COMMAND_*`) and descriptions (`DESCRIPTION_*`), plus shared reply messages (`MESSAGE_*`)                                                                                                                                                                                                                                                                                                                                                                                      |
| `src/main/java/.../discord/slash/utils/SlashCommandUtils.java`     | `@UtilityClass` with shared voice-state helpers used by both the dispatcher and handlers; currently exposes `isBotInVoiceChannel(Guild)`                                                                                                                                                                                                                                                                                                                                                                                                             |
| `src/main/java/.../music/player/AudioLoader.java`                  | Per-query `AbstractAudioLoadResultHandler`; handles `ontrackLoaded`, `onPlaylistLoaded`, `onSearchResultLoaded`, `noMatches`, `loadFailed`; delegates to `TrackScheduler.enqueue()`                                                                                                                                                                                                                                                                                                                                                                  |
| `src/main/java/.../music/player/UserData.java`                     | Record `UserData(long requester)` — attached to each `Track` to store the requesting user's ID                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `src/main/java/.../music/manager/GuildMusicManagerRegistry.java`   | Spring `@Component`; thread-safe registry of per-guild `GuildMusicManager` instances; exposes `getOrCreate(long guildId)` (lock-free — delegates to `ConcurrentHashMap.computeIfAbsent` which is already atomic) and `remove(long guildId)`; backed by a `ConcurrentHashMap`                                                                                                                                                                                                                                                                         |
| `src/main/java/.../music/manager/GuildMusicManager.java`           | Per-guild manager (not a Spring bean); two constructors: `(long, LavalinkClient)` for normal use and `(TrackScheduler, long, LavalinkClient)` for test injection; exposes `stop()` (clears queue, calls `setPaused(false).setTrack(null).subscribe()`), `getLink()`, `getPlayer()`, `getTrackScheduler()`, `getGuildId()`                                                                                                                                                                                                                            |
| `src/main/java/.../music/player/LoopMode.java`                     | Enum with three values: `DISABLED`, `TRACK`, `QUEUE`; `next()` cycles `DISABLED → TRACK → QUEUE → DISABLED`; used by `TrackScheduler` and `SlashLoopCommandHandler`                                                                                                                                                                                                                                                                                                                                                                                  |
| `src/main/java/.../music/player/TrackScheduler.java`               | Per-guild Lavalink event handler; queue management implemented: `enqueue()`, `enqueuePlaylist()`, `startTrack()`, `skip()`; all public methods use `@Synchronized` (Lombok) — safe with virtual threads because Java 25 ships JEP 491 (no carrier-thread pinning); `loopMode` field (`LoopMode.DISABLED` by default, `@Getter @Setter`); `onTrackEnd` handles loop modes — `TRACK` replays ended track, `QUEUE` re-queues it and starts next, `DISABLED` starts next queue track; guarded by `endReason.getMayStartNext()`; `onTrackStart` logs only |
| `src/main/resources/application.yaml`                              | Runtime config: actuator endpoints, caching, metrics export, health probes                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `src/main/resources/api-spec/blackflash-api.yaml`                  | OpenAPI 3.1.1 specification (currently minimal)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `docker-compose-dev.yml`                                           | Starts a Lavalink v4 (alpine) container on `127.0.0.1:2333` for local development                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `lavalink/application.yml`                                         | Lavalink server config: password, enabled sources, filters, buffer settings                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `lavalink/plugins/`                                                | Pre-downloaded Lavalink plugin JARs: `youtube-plugin-1.18.0.jar`, `lavasrc-plugin-4.8.1.jar`, `lavasearch-plugin-1.0.0.jar`                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `lombok.config`                                                    | Lombok global settings: generated annotations, custom Builder class naming                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `docs/blackflash-sequence.puml`                                    | Sequence diagrams for Blackflash interactions                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |

## Integration Points

### External Communication

- **REST Client**: `spring-boot-starter-restclient` is included, but there are currently no `RestClient` usages in
  `src/main/java`

### Discord API — JDA v6.4.1

- **Library**: `net.dv8tion:JDA:6.4.1`
- Integrated through JDA in `DiscordConfiguration`; token comes from `DISCORD_TOKEN` via `application.yaml`
- `SlashCommandListener` is a Spring `@Component` injected into `DiscordConfiguration` via `@RequiredArgsConstructor` —
  **not** instantiated with `new`
- All `SlashCommandHandler` implementations are Spring `@Component`s collected by Spring into a
  `List<SlashCommandHandler>`
  and injected into `SlashCommandListener`; each handler defines its own `CommandData` — there is no
  `SlashCommandRegistry`
- Commands are registered globally via `onReady()` by iterating `slashCommandHandlers` and calling `commandData()` on
  each
- Bot uses slash-command interactions; avoid legacy prefix-based message commands
- **DAVE E2E audio encryption**: `AudioModuleConfig.withDaveSessionFactory(new JDaveSessionFactory())` is passed to
  `JDABuilder.setAudioModuleConfig()`; requires `club.minnced:jdave-api:0.1.8` and the matching native library
  (`jdave-native-win-x86-64` or `jdave-native-linux-x86-64`)
- **Voice dispatch interceptor**: `new JDAVoiceUpdateListener(lavalink)` is set via
  `JDABuilder.setVoiceDispatchInterceptor()`; this bridges JDA voice state update events to the Lavalink client
- **Enabled GatewayIntents**: `GUILD_MESSAGE_REACTIONS`, `GUILD_MEMBERS`, `GUILD_PRESENCES`, `GUILD_MESSAGES`,
  `GUILD_VOICE_STATES` (set in `initializeJDA()`)
- **Enabled CacheFlags**: `VOICE_STATE`, `ONLINE_STATUS`, `ACTIVITY`
- **Member caching**: `MemberCachePolicy.ALL`; `ChunkingFilter.include(100)`
- **Auto-reconnect** is enabled
- **Registered commands** (each defined in its own `SlashCommandHandler` implementation): `/help`, `/join`,
  `/play` (required `query` option), `/skip`, `/loop`, `/shuffle`, `/leave`, `/stop` — all scoped to
  `InteractionContextType.GUILD`
- **Implemented handlers**: `/join`, `/play`, `/stop`, `/leave`, `/loop`, `/skip`; `/shuffle` and `/help` log a warning
  and reply "Unknown command!" until implemented

### Audio Playback — Lavalink Client v3.4.0

- **Library**: `dev.arbjerg:lavalink-client:3.4.0` (from `https://maven.lavalink.dev/releases`, repo id `ll-releases`)
- **Architecture**: external Lavalink v4 node (separate process/container) — the bot communicates over WebSocket; no
  embedded audio processing
- **`LavalinkClient`** is the central Spring singleton bean (created in `LavalinkConfiguration`); it replaces the old
  `AudioPlayerManager`
- **`LavalinkConfiguration`** registers nodes from `lavalink.name` / `lavalink.uri` / `lavalink.password` properties;
  uses `VoiceRegionPenaltyProvider` with `RegionGroup.EUROPE` for load balancing; subscribes to `ReadyEvent`,
  `StatsEvent`, `EmittedEvent`, and `TrackStartEvent` for infrastructure logging
- **`GuildMusicManager`** (`music/manager/` package): per-guild manager, **not** a Spring bean — created and stored
  by `GuildMusicManagerRegistry`; two constructors: `(long, LavalinkClient)` for normal use and
  `(TrackScheduler, long, LavalinkClient)` for test injection; exposes `stop()` (clears `trackScheduler.queue`, then
  calls `player.setPaused(false).setTrack(null).subscribe()` if a player is cached), `getLink()`, `getPlayer()`,
  `getTrackScheduler()`, `getGuildId()`
- **`GuildMusicManagerRegistry`** (`music/manager/` package): Spring `@Component`; thread-safe registry of per-guild
  `GuildMusicManager` instances; exposes `getOrCreate(long guildId)` (lock-free — delegates to
  `ConcurrentHashMap.computeIfAbsent` which is already atomic) and `remove(long guildId)`; backed by a
  `ConcurrentHashMap`
- **`TrackScheduler`** (`music/player/` package): per-guild lifecycle handler; `queue` field is `public
  Queue<Track>` (directly accessed in tests); queue management **implemented**: `enqueue()` starts immediately if no
  track is playing, otherwise queues; `enqueuePlaylist()` adds all tracks then starts the first; `startTrack()` calls
  `link.createOrUpdatePlayer()` with volume 50 and `setEndTime(track.getInfo().getLength())`; `loopMode` field
  (`LoopMode.DISABLED` by default, `@Getter @Setter`); `onTrackEnd` handles loop modes — `TRACK` replays ended track,
  `QUEUE` re-queues it and starts next, `DISABLED` starts next queue track; guarded by `endReason.getMayStartNext()`;
  `onTrackStart` logs only (TODO: send Discord messages); `skip()` stops the current track and starts the next queued
  one
- **`AudioLoader`** (`music/player/` package): per-query `AbstractAudioLoadResultHandler`; handles
  `ontrackLoaded` (attaches `UserData`, calls `enqueue()`, sends `"Added to queue: {title}\nRequested by: <@{id}>"` via
  hook), `onPlaylistLoaded` (calls `enqueuePlaylist()`),
  `onSearchResultLoaded` (picks first result), `noMatches`, `loadFailed`; replies via `event.getHook()`
- **`UserData`** record (`music/player/` package): `record UserData(long requester)` — attached to each `Track`
  via `track.setUserData(new UserData(event.getUser().getIdLong()))`
- **`SlashCommandListener`**: subscribes to `TrackStartEvent` and `TrackEndEvent` via `@PostConstruct init()` and
  delegates to the relevant guild's `TrackScheduler` via `GuildMusicManagerRegistry`
- **Lavalink node config**: `lavalink/application.yml` (mounted into the Docker container); default password is
  `youshallnotpass`, port `2333`
- **Enabled sources** (in `lavalink/application.yml`): YouTube (via plugin), Spotify (via LavaSrc plugin),
  SoundCloud, Bandcamp, Twitch, Vimeo, Nico, HTTP — native YouTube source is disabled; youtube-source plugin is
  used instead (`lavalink/plugins/youtube-plugin-1.18.0.jar`)
- **LavaSrc plugin** (`lavalink/plugins/lavasrc-plugin-4.8.1.jar`): adds Spotify source (enabled), Apple Music,
  Deezer etc. (disabled); providers are configured with `spsearch:` and `ytsearch:` fallbacks
- **LavalinkConfiguration**: also subscribes to `TrackStartEvent` on the added **node** (via
  `client.addNode(...).on(TrackStartEvent.class)`) for per-node track-start logging, in addition to the three
  global client listeners (`ReadyEvent`, `StatsEvent`, `EmittedEvent`)

### Data Persistence

- **Database**: Spring Data JPA included but intentionally not wired (no DataSourceAutoConfiguration)
- If you need to add persistence: provide a DataSource bean and remove exclusion from `@SpringBootApplication`

### Dependency Management

- **Spring Cloud BOM**: v2025.1.1 (managed in `<dependencyManagement>`)
- **Resilience4J**: For circuit breaking external API calls
- **Jackson 3.x**: JSR-310 support built in (`DateTimeFeature.WRITE_DATES_WITH_ZONE_ID`); no separate
  `jackson-datatype-jsr310` dependency; use `tools.jackson.*` imports throughout
- **jdave-api** v0.1.8: DAVE protocol (Discord E2E audio encryption); `club.minnced:jdave-api` + platform-specific
  natives `jdave-native-win-x86-64` / `jdave-native-linux-x86-64`
- **commons-lang3** v3.20.0: General-purpose Apache utilities (`org.apache.commons:commons-lang3`)
- **spring-boot-starter-webmvc**: Spring MVC web layer (serves the REST API surface)
- **spring-boot-starter-validation**: Bean Validation (Jakarta) for request/DTO validation

## Code Quality & Analysis

- **Code Coverage**: JaCoCo enforced, reports published to SonarQube
- **SonarQube Integration**: Uses organization `poulp2k`, project `poulpy2k_blackflash`
- **Compiler Args**: MapStruct uses Spring's component model (`-Amapstruct.defaultComponentModel=spring`)
- **Encoding**: All sources explicitly UTF-8 at compile time

## Important Notes for AI Agents

1. **No active database** - if data persistence is needed, it requires setup (DataSource bean, liquibase/flyway)
2. **API generation is currently skipped** - set `<skip>false</skip>` in `openapi-generator-maven-plugin` execution
   `generate-rest-api` to enable OpenAPI→Java code generation
3. **Elasticsearch metrics** require credentials (`${ELASTIC_PASSWORD}` environment variable)
4. **Virtual threads** enabled - `spring.threads.virtual.enabled: true` covers Spring MVC / `@Async`; JDA events run on
   virtual threads via `setEventPool(Executors.newVirtualThreadPerTaskExecutor(), true)` — every slash command and voice
   event gets its own virtual thread; `@Synchronized` on `TrackScheduler` methods is safe because Java 25 (JEP 491)
   eliminates carrier-thread pinning
5. **Snake_case JSON** - all JSON properties are snake_case; use standard field names in Java classes
6. **String trimming is automatic** - all incoming string values are trimmed by ObjectMapperConfiguration
7. **Discord integration uses bot token auth** - `discord.token` defaults to `none` if `DISCORD_TOKEN` is not set, and
   JDA init errors are logged in `DiscordConfiguration`
8. **Lavalink node credentials** - `LavalinkConfiguration` reads `lavalink.name`, `lavalink.uri`, `lavalink.password`
   from application properties; supply these via environment variables (`LAVALINK_NAME`, `LAVALINK_URI`,
   `LAVALINK_PASSWORD`) or add them to `application.yaml`; for local dev the defaults match `docker-compose-dev.yml` (
   password `youshallnotpass`, uri `127.0.0.1:2333`)

