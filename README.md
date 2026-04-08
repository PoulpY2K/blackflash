# ⚡ Blackflash

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 25](https://img.shields.io/badge/Java-25-blue.svg)](https://adoptium.net/)
[![Spring Boot 4.0.5](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Build](https://github.com/PoulpY2K/blackflash/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/PoulpY2K/blackflash/actions/workflows/maven.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=poulpy2k_blackflash&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=poulpy2k_blackflash)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=poulpy2k_blackflash&metric=coverage)](https://sonarcloud.io/summary/new_code?id=poulpy2k_blackflash)


**Blackflash** is a Discord music bot built with **Spring Boot 4.0.5** and **Java 25**. It
uses [JDA v6.4.1](https://github.com/discord-jda/JDA) for Discord API integration
and [Lavalink Client v3.4.0](https://github.com/lavalink-devs/lavalink-client) for high-quality audio streaming via an
external [Lavalink v4](https://github.com/lavalink-devs/Lavalink) node.

--

## ✨ Features

- 🎵 **Music playback** from YouTube, Spotify, SoundCloud, Bandcamp, Twitch, Vimeo, and more
- 🔗 **Lavalink v4** integration for performant, high-quality audio streaming
- 🔒 **DAVE E2E audio encryption** — Discord's end-to-end encrypted voice protocol
- ⚡ **Virtual threads** — leverages Java 25 virtual threads for efficient async I/O
- 📊 **Observability** — Actuator endpoints, Elasticsearch metrics export, and OpenTelemetry tracing
- 🛡️ **Resilience** — Circuit breaker support via Resilience4j
- 🧪 **Comprehensive test suite** with JaCoCo code coverage and SonarQube integration

## 🎮 Slash Commands

| Command    | Description                                        | Status        |
|------------|----------------------------------------------------|---------------|
| `/join`    | Join the voice channel                             | ✅ Implemented |
| `/play`    | Play a song or playlist from a URL or search query | ✅ Implemented |
| `/stop`    | Stop playback and clear the queue                  | ✅ Implemented |
| `/leave`   | Leave the voice channel                            | ✅ Implemented |
| `/loop`    | Cycle loop mode: track → queue → disabled          | ✅ Implemented |
| `/help`    | Display help information                           | 🚧 Planned    |
| `/skip`    | Skip the current track                             | 🚧 Planned    |
| `/shuffle` | Shuffle the playlist                               | 🚧 Planned    |

---

## 📋 Prerequisites

- **Java 25** — [Eclipse Adoptium JDK](https://adoptium.net/) or compatible
- **Maven 3.9.14+**
- **Docker & Docker Compose** — for running the Lavalink node locally
- A **Discord Bot Token** — from the [Discord Developer Portal](https://discord.com/developers/applications)
- *(Optional)* **Spotify credentials** — Client ID, Client Secret, and `sp_dc` cookie for Spotify source support

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/poulpy2k/blackflash.git
cd blackflash
```

### 2. Configure environment variables

Create a `.env` file or export the following variables:

```bash
# Required
DISCORD_TOKEN=your-discord-bot-token

# Lavalink (defaults match docker-compose-dev.yml)
LAVALINK_NAME=blackflash
LAVALINK_URI=127.0.0.1:2333
LAVALINK_PASSWORD=youshallnotpass

# Spotify (optional — for Spotify source support)
SPOTIFY_ID=your-spotify-client-id
SPOTIFY_SECRET=your-spotify-client-secret
SPOTIFY_DC=your-spotify-sp-dc-cookie

# Elasticsearch metrics (optional)
ELASTIC_PASSWORD=your-elastic-password
```

### 3. Start the Lavalink node

```bash
docker compose -f docker-compose-dev.yml up -d
```

This starts a Lavalink v4 (Alpine) container on `127.0.0.1:2333` with the configuration from `lavalink/application.yml`
and pre-downloaded plugins.

### 4. Build and run

```bash
# Build the project
mvn clean install

# Run the bot
java -jar target/blackflash-MANAGE_BY_EXTENSION.jar
```

---

## 🛠️ Development

### Build Commands

```bash
# Full build with tests and coverage
mvn clean install

# Compile only (skip tests — useful during development)
mvn clean compile

# Run tests with coverage report
mvn verify
# Coverage report → target/site/jacoco/index.html

# Package executable JAR
mvn clean package

# SonarQube analysis
mvn clean verify sonar:sonar
```

### Dev Tools

Spring Boot DevTools is included — the application auto-restarts on classpath changes during development.

---

### Key Design Decisions

- **Per-guild music managers** — each Discord guild gets its own `GuildMusicManager` with an independent
  `TrackScheduler` and queue
- **External Lavalink node** — audio processing is offloaded to a dedicated Lavalink v4 server communicating over
  WebSocket
- **Log4j2** — configured via `spring-boot-starter-log4j2` (not SLF4J + Logback)
- **Virtual threads** — enabled via `spring.threads.virtual.enabled: true` for efficient concurrency
- **Separate management port** — actuator endpoints are served on port `54001` (configured via `management.server.port`)
- **DAVE E2E audio encryption** — Discord's end-to-end encrypted voice protocol via `jdave-api`

---

## 🐳 Docker

A multi-stage `Dockerfile` is included for building and running the bot in a container.

### Build the image

```bash
docker build -t blackflash .
```

### Run the container

```bash
docker run -d \
  -e DISCORD_TOKEN=your-discord-bot-token \
  -e LAVALINK_NAME=blackflash \
  -e LAVALINK_URI=lavalink:2333 \
  -e LAVALINK_PASSWORD=youshallnotpass \
  --network lavalink \
  blackflash
```

The container exposes port `54001` for actuator health checks and uses ZGC with a 2 GB heap by default.

---

## 🧪 Testing

```bash
# Run tests
mvn test

# Run tests with coverage
mvn verify
```

- **JUnit 5** with Spring Boot Test
- **Mockito 5** — with `MockedStatic` and `MockedConstruction` for static/constructor stubbing
- **JaCoCo** — code coverage reports generated at `target/site/jacoco/index.html`
- **SonarQube** — integrated for continuous code quality analysis

### Test Coverage

Tests cover all major components:

- Application bootstrap and timezone configuration
- Discord configuration (JDA initialization, error handling)
- Lavalink configuration (node setup, event subscriptions)
- ObjectMapper configuration (trimming, snake_case, null handling, JSR-310)
- Slash command listener (join, play, stop, leave, loop — including all three loop-cycle transitions) and registry
- Audio loader, guild music manager, track scheduler (including all loop modes and end-reason handling)
- UserData record equality

---

## 📡 Observability

### Actuator Endpoints

Available at `http://localhost:54001/actuator/`:

| Endpoint            | Description                |
|---------------------|----------------------------|
| `/actuator/health`  | Health status with details |
| `/actuator/info`    | Application information    |
| `/actuator/metrics` | Application metrics        |
| `/actuator/caches`  | Cache statistics           |

### Health Probes

Kubernetes-compatible liveness and readiness probes are enabled:

- `http://localhost:54001/actuator/health/liveness`
- `http://localhost:54001/actuator/health/readiness`

### Metrics Export

Metrics are exported to **Elasticsearch** when `ELASTIC_PASSWORD` is configured.

---

## 📜 License

This project is licensed under the **MIT License** — see the [LICENSE.md](LICENSE.md) file for details.

## 👤 Author

**Jérémy Laurent** ([@poulpy2k](https://github.com/poulpy2k))

- Website: [jeremy-laurent.com](https://jeremy-laurent.com)
- Email: contact@jeremy-laurent.fr

