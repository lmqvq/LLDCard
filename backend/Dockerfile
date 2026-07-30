FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache mariadb-client \
    && addgroup -S lldcard \
    && adduser -S -G lldcard lldcard \
    && mkdir -p /app/keys /app/backups /app/logs \
    && chown -R lldcard:lldcard /app

WORKDIR /app

COPY --from=build --chown=lldcard:lldcard /workspace/target/lldcard-backend-*.jar /app/app.jar

USER lldcard

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=10 \
  CMD wget -q -O - http://127.0.0.1:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]