package io.github.lmqvq.lldcard.backend.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeploymentConfigurationTest {

    private static final Path PROJECT_ROOT = Path.of("..").toAbsolutePath().normalize();

    @Test
    @SuppressWarnings("unchecked")
    void composeDefinesHealthyServiceChain() throws IOException {
        Map<String, Object> services = loadComposeServices();
        assertEquals(Set.of("mysql", "redis", "backend", "frontend"), services.keySet());

        for (String serviceName : services.keySet()) {
            Map<String, Object> service = (Map<String, Object>) services.get(serviceName);
            assertTrue(service.containsKey("healthcheck"), serviceName + " must define a healthcheck");
        }

        Map<String, Object> backend = (Map<String, Object>) services.get("backend");
        Map<String, Object> backendDependencies = (Map<String, Object>) backend.get("depends_on");
        assertHealthyDependency(backendDependencies, "mysql");
        assertHealthyDependency(backendDependencies, "redis");

        Map<String, Object> frontend = (Map<String, Object>) services.get("frontend");
        Map<String, Object> frontendDependencies = (Map<String, Object>) frontend.get("depends_on");
        assertHealthyDependency(frontendDependencies, "backend");
    }

    @Test
    @SuppressWarnings("unchecked")
    void composeRequiresRedisPasswordForRedisAndBackend() throws IOException {
        Map<String, Object> services = loadComposeServices();
        Map<String, Object> redis = (Map<String, Object>) services.get("redis");
        Map<String, Object> redisEnvironment = (Map<String, Object>) redis.get("environment");
        List<String> redisCommand = (List<String>) redis.get("command");
        Map<String, Object> redisHealthcheck = (Map<String, Object>) redis.get("healthcheck");
        List<String> healthcheckCommand = (List<String>) redisHealthcheck.get("test");

        String requiredPassword = "${REDIS_PASSWORD:?Set REDIS_PASSWORD in .env}";
        assertEquals(requiredPassword, redisEnvironment.get("REDIS_PASSWORD"));
        assertTrue(String.join(" ", redisCommand).contains("--requirepass \"$${REDIS_PASSWORD}\""));
        assertTrue(String.join(" ", healthcheckCommand).contains("REDISCLI_AUTH=\"$${REDIS_PASSWORD}\""));

        Map<String, Object> backend = (Map<String, Object>) services.get("backend");
        Map<String, Object> backendEnvironment = (Map<String, Object>) backend.get("environment");
        assertEquals(requiredPassword, backendEnvironment.get("SPRING_DATA_REDIS_PASSWORD"));
    }

    @Test
    void publicSchemaContainsNoExportedRows() throws IOException {
        Path schemaPath = PROJECT_ROOT.resolve("docker/mysql/init/01-schema.sql");
        String schema = Files.readString(schemaPath);

        assertTrue(schema.contains("CREATE TABLE"));
        assertFalse(schema.contains("INSERT INTO"));
        assertFalse(schema.toLowerCase().contains("小小怪"));
        assertTrue(schema.contains("CREATE TABLE `SPRING_SESSION`"));
        assertTrue(schema.contains("CREATE TABLE `SPRING_SESSION_ATTRIBUTES`"));
        assertTrue(schema.contains("REFERENCES `SPRING_SESSION` (`PRIMARY_ID`)"));
        assertFalse(schema.contains("CREATE TABLE `spring_session`"));
        assertFalse(schema.contains("CREATE TABLE `spring_session_attributes`"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadComposeServices() throws IOException {
        Path composePath = PROJECT_ROOT.resolve("docker-compose.yml");

        try (InputStream input = Files.newInputStream(composePath)) {
            Map<String, Object> root = new Yaml().load(input);
            return (Map<String, Object>) root.get("services");
        }
    }

    @SuppressWarnings("unchecked")
    private void assertHealthyDependency(Map<String, Object> dependencies, String serviceName) {
        Map<String, Object> dependency = (Map<String, Object>) dependencies.get(serviceName);
        assertEquals("service_healthy", dependency.get("condition"));
    }
}