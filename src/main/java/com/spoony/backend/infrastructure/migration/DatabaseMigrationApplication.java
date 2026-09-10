package com.spoony.backend.infrastructure.migration;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * One-shot database bootstrap and migration entry point for ECS.
 *
 * <p>The web task only receives the least-privilege runtime credentials. This
 * process is run as a separate, short-lived ECS task and is the only container
 * that receives the RDS administrator and Flyway migration secrets.</p>
 */
public final class DatabaseMigrationApplication {

    private static final Pattern POSTGRES_IDENTIFIER =
            Pattern.compile("[A-Za-z][A-Za-z0-9_]{0,62}");

    private DatabaseMigrationApplication() {
    }

    public static void main(String[] args) throws SQLException {
        Map<String, String> env = System.getenv();
        String url = required(env, "DATABASE_URL");
        String adminUser = identifier(required(env, "DATABASE_ADMIN_USER"));
        String adminPassword = required(env, "DATABASE_ADMIN_PASSWORD");
        String migrationUser = identifier(required(env, "DATABASE_MIGRATION_USER"));
        String migrationPassword = required(env, "DATABASE_MIGRATION_PASSWORD");
        String appUser = identifier(required(env, "DATABASE_APP_USER"));
        String appPassword = required(env, "DATABASE_APP_PASSWORD");

        if (adminUser.equals(migrationUser) || adminUser.equals(appUser)
                || migrationUser.equals(appUser)) {
            throw new IllegalArgumentException("Database administrator, migration and runtime users must be distinct");
        }

        prepareRoles(url, adminUser, adminPassword,
                migrationUser, migrationPassword, appUser, appPassword);

        Flyway.configure()
                .dataSource(url, migrationUser, migrationPassword)
                .locations("classpath:db/migration")
                .baselineOnMigrate(false)
                .validateMigrationNaming(true)
                .load()
                .migrate();

        grantRuntimePrivileges(url, migrationUser, migrationPassword, appUser);
        System.out.println("Database bootstrap and Flyway migrations completed successfully");
    }

    private static void prepareRoles(String url,
                                     String adminUser,
                                     String adminPassword,
                                     String migrationUser,
                                     String migrationPassword,
                                     String appUser,
                                     String appPassword) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, adminUser, adminPassword)) {
            connection.setAutoCommit(false);
            ensureLoginRole(connection, migrationUser, migrationPassword);
            ensureLoginRole(connection, appUser, appPassword);

            String database = currentDatabase(connection);
            try (Statement statement = connection.createStatement()) {
                statement.execute("REVOKE CREATE ON SCHEMA public FROM PUBLIC");
                statement.execute("GRANT CONNECT ON DATABASE " + quoteIdentifier(database)
                        + " TO " + quoteIdentifier(migrationUser));
                statement.execute("GRANT CONNECT ON DATABASE " + quoteIdentifier(database)
                        + " TO " + quoteIdentifier(appUser));
                statement.execute("GRANT USAGE, CREATE ON SCHEMA public TO "
                        + quoteIdentifier(migrationUser));
                statement.execute("GRANT USAGE ON SCHEMA public TO " + quoteIdentifier(appUser));
                statement.execute("REVOKE CREATE ON SCHEMA public FROM " + quoteIdentifier(appUser));
            }
            connection.commit();
        }
    }

    private static void ensureLoginRole(Connection connection, String role, String password)
            throws SQLException {
        boolean exists;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = ?)")) {
            statement.setString(1, role);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                exists = result.getBoolean(1);
            }
        }

        String roleSql = quoteIdentifier(role)
                + " WITH LOGIN PASSWORD " + quoteLiteral(password)
                + " NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOREPLICATION";
        try (Statement statement = connection.createStatement()) {
            statement.execute((exists ? "ALTER ROLE " : "CREATE ROLE ") + roleSql);
        }
    }

    private static void grantRuntimePrivileges(String url,
                                               String migrationUser,
                                               String migrationPassword,
                                               String appUser) throws SQLException {
        String app = quoteIdentifier(appUser);
        try (Connection connection = DriverManager.getConnection(url, migrationUser, migrationPassword);
             Statement statement = connection.createStatement()) {
            statement.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO " + app);
            statement.execute("GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO " + app);
            statement.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public "
                    + "GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO " + app);
            statement.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public "
                    + "GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO " + app);
            statement.execute("REVOKE ALL ON TABLE flyway_schema_history FROM " + app);
        }
    }

    private static String currentDatabase(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT current_database()")) {
            result.next();
            return identifier(result.getString(1));
        }
    }

    static String identifier(String value) {
        if (!POSTGRES_IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid PostgreSQL identifier");
        }
        return value;
    }

    static String quoteIdentifier(String value) {
        return "\"" + identifier(value).replace("\"", "\"\"") + "\"";
    }

    static String quoteLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    private static String required(Map<String, String> env, String key) {
        String value = env.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }
}
