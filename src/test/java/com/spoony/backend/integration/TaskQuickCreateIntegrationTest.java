package com.spoony.backend.integration;

import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskQuickCreateIntegrationTest extends IntegrationTestSupport {

    @Test
    void should_ApplyDefaults_When_NameOnly() throws Exception {
        UserEntity user = createUser();

        mockMvc.perform(post("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("Test"))
                .andExpect(jsonPath("$.data.spoonCost").value(2))
                .andExpect(jsonPath("$.data.importance").value("MEDIUM"))
                .andExpect(jsonPath("$.data.dueDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void should_Return400_When_NameMissing() throws Exception {
        UserEntity user = createUser();

        mockMvc.perform(post("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"));
    }
}
