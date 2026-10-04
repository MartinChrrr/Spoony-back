package com.spoony.backend.infrastructure.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIfEnvironmentVariable(
        named = "SPRING_DATASOURCE_DRIVER_CLASS_NAME",
        matches = "org\\.postgresql\\.Driver"
)
class DatabaseRuntimePrivilegesIntegrationTest {

    private static final String INSUFFICIENT_PRIVILEGE = "42501";

    @Test
    void runtimeRoleCanUseApplicationTablesButCannotChangeSchemaOrReadFlywayHistory()
            throws SQLException {
        String url = requiredEnvironment("SPRING_DATASOURCE_URL");
        String user = requiredEnvironment("SPRING_DATASOURCE_USERNAME");
        String password = requiredEnvironment("SPRING_DATASOURCE_PASSWORD");

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("SELECT current_user")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString(1)).isEqualTo(user);
            }

            try (ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM users")) {
                assertThat(result.next()).isTrue();
            }

            SQLException createFailure = assertThrows(SQLException.class,
                    () -> statement.execute("CREATE TABLE runtime_role_must_not_create_tables (id bigint)"));
            assertThat(createFailure.getSQLState()).isEqualTo(INSUFFICIENT_PRIVILEGE);

            SQLException flywayFailure = assertThrows(SQLException.class,
                    () -> statement.executeQuery("SELECT COUNT(*) FROM flyway_schema_history"));
            assertThat(flywayFailure.getSQLState()).isEqualTo(INSUFFICIENT_PRIVILEGE);
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required for the PostgreSQL privilege test");
        }
        return value;
    }
}
