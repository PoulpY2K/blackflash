# Blackflash — Discord Music Bot
FROM maven:3.9.14-eclipse-temurin-25 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY lombok.config .
COPY src ./src

RUN mvn clean package "-Dgit.tag=$TAG" -DskipTests -B

FROM eclipse-temurin:25-jre AS runtime

LABEL maintainer="Jérémy Laurent <contact@jeremy-laurent.fr>"
LABEL org.opencontainers.image.title="Blackflash"
LABEL org.opencontainers.image.description="Blackflash is a Discord music bot."
LABEL org.opencontainers.image.url="https://github.com/poulpy2k/blackflash"
LABEL org.opencontainers.image.source="https://github.com/poulpy2k/blackflash"
LABEL org.opencontainers.image.documentation="https://github.com/poulpy2k/blackflash#readme"
LABEL org.opencontainers.image.vendor="POULPY2K"
LABEL org.opencontainers.image.licenses="MIT"
LABEL org.opencontainers.image.authors="poulpyy"

ENV TZ="Europe/Paris"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl tzdata \
    && ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo "${TZ}" > /etc/timezone \
    && dpkg-reconfigure -f noninteractive tzdata \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 674 blackflash \
    && useradd --system --uid 674 --gid blackflash \
       --shell /usr/sbin/nologin --no-create-home blackflash

WORKDIR /app

COPY --from=build --chmod=555 /build/target/blackflash-*.jar blackflash.jar

USER blackflash:blackflash

EXPOSE 54001

ENV LAVALINK_NAME="blackflash" \
    LAVALINK_URI="lavalink:2333" \
    JAVA_OPTS="-Xmx2G -XX:+UseZGC"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl --fail --silent http://localhost:54001/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar blackflash.jar"]
