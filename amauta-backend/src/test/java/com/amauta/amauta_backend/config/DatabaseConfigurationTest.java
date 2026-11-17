package com.amauta.amauta_backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigurationTest {
    private final MockEnvironment environment = new MockEnvironment()
            .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/hospedaje_db");

    @Test
    void missingPasswordExplainsHowToStart() {
        var error = assertThrows(IllegalStateException.class, () -> validate(environment));
        assertTrue(error.getMessage().contains("DB_PASSWORD"));
        assertTrue(error.getMessage().contains("scripts/start-backend.ps1"));
    }

    @Test
    void emptyPasswordIsRejected() {
        environment.withProperty("spring.datasource.password", "  ");
        assertThrows(IllegalStateException.class, () -> validate(environment));
    }

    @Test
    void configuredPasswordPassesValidation() {
        environment.withProperty("spring.datasource.password", "test-only-not-a-real-secret");
        assertDoesNotThrow(() -> validate(environment));
    }

    @Test
    void h2TestsDoNotRequirePostgresCredentials() {
        environment.withProperty("spring.datasource.url", "jdbc:h2:mem:test");
        assertDoesNotThrow(() -> validate(environment));
    }

    private void validate(MockEnvironment environment) {
        DatabaseConfiguration.validateDatabaseCredentials(environment)
                .postProcessBeanFactory(new DefaultListableBeanFactory());
    }
}
