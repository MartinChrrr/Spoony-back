package com.spoony.backend.application.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P0/RGPD Art. 9: validation du consentement explicite à l'inscription.
 * L'inscription doit être refusée si consentGiven n'est pas true (false ou null).
 */
class RegisterRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void should_BeValid_When_ConsentIsTrue() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Martin", true);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void should_RejectConsent_When_ConsentIsFalse() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Martin", false);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("consentGiven");
    }

    @Test
    void should_RejectConsent_When_ConsentIsNull() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Martin", null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("consentGiven");
    }
}
