package com.spoony.backend.integration;

import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BulkPostponeIntegrationTest extends IntegrationTestSupport {

    @Test
    void should_PostponeAllActiveTasksToTomorrow_When_TasksExist() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task1 = createTask(user.getId(), "T1", 2);
        UserTaskEntity task2 = createTask(user.getId(), "T2", 3);

        mockMvc.perform(post("/api/task-logs/bulk-postpone")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.postponedCount").value(2))
                .andExpect(jsonPath("$.data.newDate").value(LocalDate.now().plusDays(1).toString()));

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        UserTaskEntity reloaded1 = userTaskRepository.findById(task1.getId()).orElseThrow();
        UserTaskEntity reloaded2 = userTaskRepository.findById(task2.getId()).orElseThrow();
        assertThat(reloaded1.getDueDate()).isEqualTo(tomorrow);
        assertThat(reloaded2.getDueDate()).isEqualTo(tomorrow);
    }

    @Test
    void should_PostponeOverdueAndTodayTasks_When_DueDateLessThanOrEqualToday() throws Exception {
        // ADR-008 : « Tout reporter » couvre les tâches en retard (due_date < today) ET du jour.
        UserEntity user = createUser();
        UserTaskEntity overdue = createTask(user.getId(), "Overdue", 2,
                LocalDate.now().minusDays(3), com.spoony.backend.domain.task.model.TaskStatus.ACTIVE);
        UserTaskEntity dueToday = createTask(user.getId(), "Today", 1);

        mockMvc.perform(post("/api/task-logs/bulk-postpone")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postponedCount").value(2));

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        assertThat(userTaskRepository.findById(overdue.getId()).orElseThrow().getDueDate()).isEqualTo(tomorrow);
        assertThat(userTaskRepository.findById(dueToday.getId()).orElseThrow().getDueDate()).isEqualTo(tomorrow);
    }

    @Test
    void should_Return404_When_NoActiveTasks() throws Exception {
        UserEntity user = createUser();

        mockMvc.perform(post("/api/task-logs/bulk-postpone")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.data.code").value("NO_ACTIVE_TASKS"));
    }
}
