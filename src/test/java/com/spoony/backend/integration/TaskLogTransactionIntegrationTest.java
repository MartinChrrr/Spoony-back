package com.spoony.backend.integration;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    void should_DecrementSnapshotCost_When_TaskCostChangedAfterCompletion() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task = createTask(user.getId(), "Original name", 3);
        DailyEnergyEntity energy = createEnergy(user.getId(), 10, 0);
        UserTaskLogEntity log = createLog(user.getId(), task.getId(), LocalDate.now(), TaskLogStatus.PLANNED);

        mockMvc.perform(patch("/api/task-logs/" + log.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk());

        task.setName("Renamed");
        task.setSpoonCost((short) 5);
        userTaskRepository.saveAndFlush(task);

        mockMvc.perform(patch("/api/task-logs/" + log.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PLANNED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskName").value("Original name"))
                .andExpect(jsonPath("$.data.spoonCost").value(3));

        DailyEnergyEntity updated = dailyEnergyRepository.findById(energy.getId()).orElseThrow();
        assertThat(updated.getSpoonsUsed()).isZero();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void should_RollBackStatus_When_DecrementWouldMakeBalanceNegative() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task = createTask(user.getId(), "T", 3);
        createEnergy(user.getId(), 10, 0);
        UserTaskLogEntity log = createLog(user.getId(), task.getId(), LocalDate.now(), TaskLogStatus.COMPLETED);

        mockMvc.perform(patch("/api/task-logs/" + log.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PLANNED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.code").value("SPOON_BALANCE_CONFLICT"));

        UserTaskLogEntity unchanged = userTaskLogRepository.findById(log.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(TaskLogStatus.COMPLETED);
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
