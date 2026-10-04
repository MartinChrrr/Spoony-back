package com.spoony.backend.integration;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserDeleteIntegrationTest extends IntegrationTestSupport {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void should_CascadeDeleteAllUserData_When_DeleteMyAccount() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task = createTask(user.getId(), "T", 2);
        createLog(user.getId(), task.getId(), LocalDate.now(), TaskLogStatus.PLANNED);
        createEnergy(user.getId(), 8, 0);

        mockMvc.perform(delete("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.getId())))
                .andExpect(status().isNoContent());

        // Force flush + clear so the cascade DELETEs hit the DB and the L1 cache
        // is reloaded from the (now cascaded) DB state.
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findById(user.getId())).isEmpty();
        assertThat(userTaskRepository.findByUserId(user.getId())).isEmpty();
        assertThat(userTaskLogRepository.findByUserId(user.getId())).isEmpty();
        assertThat(dailyEnergyRepository.findByUserId(user.getId())).isEmpty();
    }
}
