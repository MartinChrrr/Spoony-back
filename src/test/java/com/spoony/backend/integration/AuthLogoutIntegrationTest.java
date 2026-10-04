package com.spoony.backend.integration;

import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthLogoutIntegrationTest extends IntegrationTestSupport {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void should_RevokeRefreshToken_When_AuthenticatedLogout() throws Exception {
        // Arrange
        UserEntity user = createUser();
        user.setRefreshTokenHash("some-stored-hash");
        userRepository.save(user);

        // Act
        mockMvc.perform(post("/api/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId())))
                .andExpect(status().isNoContent());

        // Assert
        entityManager.flush();
        entityManager.clear();
        UserEntity reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertThat(reloaded.getRefreshTokenHash()).isNull();
    }

    @Test
    void should_Return401_When_LogoutWithoutToken() throws Exception {
        // Act + Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }
}
