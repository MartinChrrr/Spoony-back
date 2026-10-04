package com.spoony.backend.integration;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IDOR (P1.1) : un utilisateur ne doit jamais pouvoir créer un log, logguer
 * manuellement, ni faire varier les spoons d'une tâche appartenant à un autre
 * utilisateur. Toute tentative se solde par un 404 TASK_NOT_FOUND (on ne révèle
 * pas l'existence de la ressource d'autrui).
 */
class TaskLogIdorIntegrationTest extends IntegrationTestSupport {

    @Test
    void should_Return404_When_ManualLogOnOtherUsersTask() throws Exception {
        UserEntity attacker = createUser();
        UserEntity victim = createUser();
        UserTaskEntity victimTask = createTask(victim.getId(), "Victim task", 3);

        mockMvc.perform(post("/api/task-logs/manual")
                        .header(HttpHeaders.AUTHORIZATION, bearer(attacker.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userTaskId\":\"" + victimTask.getId() + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.data.code").value("TASK_NOT_FOUND"));

        // Aucun log ne doit avoir été créé pour l'attaquant.
        assertThat(userTaskLogRepository.findByUserIdAndDate(attacker.getId(), LocalDate.now())).isEmpty();
    }

    @Test
    void should_Return404_When_BulkLogContainsOtherUsersTask() throws Exception {
        UserEntity attacker = createUser();
        UserEntity victim = createUser();
        UserTaskEntity attackerTask = createTask(attacker.getId(), "Own task", 2);
        UserTaskEntity victimTask = createTask(victim.getId(), "Victim task", 3);

        mockMvc.perform(post("/api/task-logs")
                        .header(HttpHeaders.AUTHORIZATION, bearer(attacker.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userTaskIds\":[\"" + attackerTask.getId()
                                + "\",\"" + victimTask.getId() + "\"]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.code").value("TASK_NOT_FOUND"));

        // Transactionnel : le lot entier est rejeté, y compris la tâche légitime.
        assertThat(userTaskLogRepository.findByUserIdAndDate(attacker.getId(), LocalDate.now())).isEmpty();
    }

    @Test
    void should_Return404_When_PatchStatusOnOtherUsersLog() throws Exception {
        // Le log appartient à la victime ; l'attaquant tente de le compléter pour
        // (entre autres) faire varier les spoons. updateStatus filtre par userId.
        UserEntity attacker = createUser();
        UserEntity victim = createUser();
        UserTaskEntity victimTask = createTask(victim.getId(), "Victim task", 3);
        createEnergy(victim.getId(), 10, 0);
        UserTaskLogEntity victimLog = createLog(victim.getId(), victimTask.getId(),
                LocalDate.now(), TaskLogStatus.PLANNED);

        mockMvc.perform(patch("/api/task-logs/" + victimLog.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(attacker.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.code").value("TASK_LOG_NOT_FOUND"));

        // Le log de la victime n'a pas bougé.
        UserTaskLogEntity untouched = userTaskLogRepository.findById(victimLog.getId()).orElseThrow();
        assertThat(untouched.getStatus()).isEqualTo(TaskLogStatus.PLANNED);
    }
}
