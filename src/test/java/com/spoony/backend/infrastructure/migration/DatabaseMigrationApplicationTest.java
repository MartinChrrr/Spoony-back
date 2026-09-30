package com.spoony.backend.infrastructure.migration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseMigrationApplicationTest {

    @Test
    void acceptsAndQuotesSafePostgresIdentifiers() {
        assertThat(DatabaseMigrationApplication.identifier("spoony_app_1"))
                .isEqualTo("spoony_app_1");
        assertThat(DatabaseMigrationApplication.quoteIdentifier("spoony_app"))
                .isEqualTo("\"spoony_app\"");
    }

    @Test
    void rejectsInjectedOrInvalidPostgresIdentifiers() {
        assertThatThrownBy(() -> DatabaseMigrationApplication.identifier("app; DROP ROLE admin"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DatabaseMigrationApplication.identifier("1app"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void escapesPasswordLiteralsWithoutLoggingThem() {
        assertThat(DatabaseMigrationApplication.quoteLiteral("pa'ss"))
                .isEqualTo("'pa''ss'");
    }
}
