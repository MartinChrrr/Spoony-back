package com.spoony.backend.integration;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskLogArchiveIntegrationTest extends IntegrationTestSupport {

    @Test
    void should_ExcludeCompletedOver24h_When_GetWithoutIncludeArchived() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task1 = createTask(user.getId(), "T1", 2);
        UserTaskEntity task2 = createTask(user.getId(), "T2", 2);

        UserTaskLogEntity recent = createLog(user.getId(), task1.getId(),
                LocalDate.now(), TaskLogStatus.COMPLETED);
        recent.setCompletedAt(LocalDateTime.now().minusHours(1));
        userTaskLogRepository.save(recent);

        UserTaskLogEntity stale = createLog(user.getId(), task2.getId(),
                LocalDate.now(), TaskLogStatus.COMPLETED);
        stale.setCompletedAt(LocalDateTime.now().minusHours(25));
        userTaskLogRepository.save(stale);

        mockMvc.perform(get("/api/task-logs")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(recent.getId().toString()));
    }

    @Test
    void should_IncludeAll_When_IncludeArchivedTrue() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task1 = createTask(user.getId(), "T1", 2);
        UserTaskEntity task2 = createTask(user.getId(), "T2", 2);

        UserTaskLogEntity recent = createLog(user.getId(), task1.getId(),
                LocalDate.now(), TaskLogStatus.COMPLETED);
        recent.setCompletedAt(LocalDateTime.now().minusHours(1));
        userTaskLogRepository.save(recent);

        UserTaskLogEntity stale = createLog(user.getId(), task2.getId(),
                LocalDate.now(), TaskLogStatus.COMPLETED);
        stale.setCompletedAt(LocalDateTime.now().minusHours(25));
        userTaskLogRepository.save(stale);

        mockMvc.perform(get("/api/task-logs")
                        .param("include_archived", "true")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}
