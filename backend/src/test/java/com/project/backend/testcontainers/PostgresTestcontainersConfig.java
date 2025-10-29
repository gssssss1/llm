package com.project.backend.testcontainers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration for PostgreSQL integration tests.
 * <p>
 * This configuration provides a PostgreSQL container for integration testing.
 * Use it by adding the initializer to your test class:
 * <pre>
 * {@code
 * @SpringBootTest
 * @ContextConfiguration(initializers = PostgresTestcontainersConfig.Initializer.class)
 * class MyIntegrationTest {
 *     // test methods
 * }
 * }
 * </pre>
 */
@TestConfiguration
public class PostgresTestcontainersConfig {

    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        POSTGRES_CONTAINER.start();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + POSTGRES_CONTAINER.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRES_CONTAINER.getUsername(),
                    "spring.datasource.password=" + POSTGRES_CONTAINER.getPassword()
            ).applyTo(applicationContext.getEnvironment());
        }
    }

}
