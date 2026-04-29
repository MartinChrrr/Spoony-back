package com.spoony.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaDailyEnergyRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskLogRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskRepository;
import com.spoony.backend.infrastructure.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JpaUserRepository userRepository;

    @Autowired
    protected JpaUserTaskRepository userTaskRepository;

    @Autowired
    protected JpaUserTaskLogRepository userTaskLogRepository;

    @Autowired
    protected JpaDailyEnergyRepository dailyEnergyRepository;

    protected UserEntity createUser() {
        UserEntity user = new UserEntity(
                "user-" + UUID.randomUUID() + "@test.local",
                passwordHashFor("password123"),
                "Test"
        );
        return userRepository.save(user);
    }

    protected String bearer(UUID userId) {
        return "Bearer " + jwtTokenProvider.generateAccessToken(userId);
    }

    protected UserTaskEntity createTask(UUID userId, String name, int spoonCost) {
        return createTask(userId, name, spoonCost, LocalDate.now(), TaskStatus.ACTIVE);
    }

    protected UserTaskEntity createTask(UUID userId, String name, int spoonCost,
                                        LocalDate dueDate, TaskStatus status) {
        UserTaskEntity task = new UserTaskEntity(userId, name);
        task.setSpoonCost((short) spoonCost);
        task.setDueDate(dueDate);
        task.setStatus(status);
        return userTaskRepository.save(task);
    }

    protected DailyEnergyEntity createEnergy(UUID userId, int spoons, int spoonsUsed) {
        DailyEnergyEntity energy = new DailyEnergyEntity(userId, LocalDate.now(), (short) spoons);
        energy.setSpoonsUsed((short) spoonsUsed);
        return dailyEnergyRepository.save(energy);
    }

    protected UserTaskLogEntity createLog(UUID userId, UUID taskId, LocalDate date, TaskLogStatus status) {
        UserTaskLogEntity log = new UserTaskLogEntity(userId, taskId, date);
        log.setStatus(status);
        log.setSuggested(true);
        if (status == TaskLogStatus.COMPLETED) {
            log.setCompletedAt(LocalDateTime.now());
        }
        return userTaskLogRepository.save(log);
    }

    private String passwordHashFor(String raw) {
        return passwordEncoder.encode(raw);
    }
}
