package com.spoony.backend.integration;

import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiV1ContractIntegrationTest extends IntegrationTestSupport {

    @Test
    void should_ExposeVersionedRoutesAndKeepLegacyAlias_When_ClientCallsTasks() throws Exception {
        UserEntity user = createUser();
        String authorization = bearer(user.getId());

        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        mockMvc.perform(get("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void should_AllowUnauthenticatedLoginOnV1_When_CredentialsAreValid() throws Exception {
        UserEntity user = createUser();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(
                                user.getEmail(),
                                "password123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString());
    }

    @Test
    void should_PublishV1Paths_When_OpenApiContractIsGenerated() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.paths['/api/v1/tasks']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/users/me/export']").exists());
    }

    private record LoginBody(String email, String password) {
    }
}
