package com.spoony.backend.integration;

import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EnergyZeroSpoonsIntegrationTest extends IntegrationTestSupport {

    @Test
    void should_PostponeAllActiveTasks_When_DeclareZeroSpoons() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task1 = createTask(user.getId(), "T1", 2);
        UserTaskEntity task2 = createTask(user.getId(), "T2", 3);

        mockMvc.perform(post("/api/energy")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"spoons\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.spoons").value(0));

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        UserTaskEntity reloaded1 = userTaskRepository.findById(task1.getId()).orElseThrow();
        UserTaskEntity reloaded2 = userTaskRepository.findById(task2.getId()).orElseThrow();
        assertThat(reloaded1.getDueDate()).isEqualTo(tomorrow);
        assertThat(reloaded2.getDueDate()).isEqualTo(tomorrow);
    }

    @Test
    void should_ReturnEmptySuggestions_When_ZeroSpoons() throws Exception {
        UserEntity user = createUser();
        createTask(user.getId(), "Active task", 2);
        createEnergy(user.getId(), 0, 0);

        mockMvc.perform(get("/api/suggestions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
