package com.vietct.OrderFlow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class DatabaseMigrationIntegrationTest {

    // 1) Start a dev-like Postgres just for tests
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("orderflow_test")
                    .withUsername("test")
                    .withPassword("test");

    // 2) Point Spring Boot datasource to this container
    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // keep same behaviour as dev
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Spring context starts, Flyway runs, and product table is accessible")
    void contextLoadsAndFlywayApplied() {
        // If Flyway didn't create the table, this query will throw an exception.
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product",
                Integer.class
        );

        // If we get here, schema + connection are OK
        assertThat(count).isNotNull();
        assertThat(count).isGreaterThanOrEqualTo(0);
    }
}
