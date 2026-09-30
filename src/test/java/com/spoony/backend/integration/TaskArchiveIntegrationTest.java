package com.spoony.backend.integration;

import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskArchiveIntegrationTest extends IntegrationTestSupport {

    @Test
    void should_ArchiveCompletedTaskWithoutDeletingHistoryOrChangingSpoons() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task = createTask(user.getId(), "Historical task", 3);
        DailyEnergyEntity energy = createEnergy(user.getId(), 10, 3);
        UserTaskLogEntity log = createLog(user.getId(), task.getId(), LocalDate.now(), TaskLogStatus.COMPLETED);

        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId())))
                .andExpect(status().isNoContent());

        assertThat(userTaskRepository.findById(task.getId()).orElseThrow().getStatus())
                .isEqualTo(TaskStatus.ARCHIVED);
        assertThat(userTaskLogRepository.findById(log.getId())).isPresent();
        assertThat(dailyEnergyRepository.findById(energy.getId()).orElseThrow().getSpoonsUsed())
                .isEqualTo((short) 3);

        mockMvc.perform(get("/api/task-logs")
                        .param("from", LocalDate.now().toString())
                        .param("to", LocalDate.now().toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].taskName").value("Historical task"))
                .andExpect(jsonPath("$.data[0].spoonCost").value(3));
    }

    @Test
    void should_RejectNewLogForArchivedTask() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task = createTask(user.getId(), "Archived", 2,
                LocalDate.now(), TaskStatus.ARCHIVED);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/task-logs/manual")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"userTaskId\":\"" + task.getId() + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.code").value("TASK_NOT_FOUND"));
    }
}
