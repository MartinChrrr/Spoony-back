package com.spoony.backend.integration;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskLogTransactionIntegrationTest extends IntegrationTestSupport {

    @Test
    void should_IncrementSpoonsUsed_When_PatchToCompleted() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task = createTask(user.getId(), "T", 3);
        DailyEnergyEntity energy = createEnergy(user.getId(), 10, 0);
        UserTaskLogEntity log = createLog(user.getId(), task.getId(), LocalDate.now(), TaskLogStatus.PLANNED);

        mockMvc.perform(patch("/api/task-logs/" + log.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        DailyEnergyEntity updated = dailyEnergyRepository.findById(energy.getId()).orElseThrow();
        assertThat(updated.getSpoonsUsed()).isEqualTo((short) 3);
    }

    @Test
    void should_DecrementSpoonsUsed_When_PatchFromCompletedToPlanned() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task = createTask(user.getId(), "T", 3);
        DailyEnergyEntity energy = createEnergy(user.getId(), 10, 5);
        UserTaskLogEntity log = createLog(user.getId(), task.getId(), LocalDate.now(), TaskLogStatus.COMPLETED);

        mockMvc.perform(patch("/api/task-logs/" + log.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PLANNED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PLANNED"));

        DailyEnergyEntity updated = dailyEnergyRepository.findById(energy.getId()).orElseThrow();
        assertThat(updated.getSpoonsUsed()).isEqualTo((short) 2);
    }

    @Test
    void should_Return403_When_LogTooOld() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task = createTask(user.getId(), "T", 3);
        createEnergy(user.getId(), 10, 0);
        UserTaskLogEntity oldLog = createLog(user.getId(), task.getId(),
                LocalDate.now().minusDays(2), TaskLogStatus.PLANNED);

        mockMvc.perform(patch("/api/task-logs/" + oldLog.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.data.code").value("TASK_LOG_EXPIRED"));
    }
}
