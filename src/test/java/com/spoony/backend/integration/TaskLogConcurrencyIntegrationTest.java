package com.spoony.backend.integration;

import com.spoony.backend.application.tasklog.TaskLogApplicationService;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaDailyEnergyRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskLogRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskLogConcurrencyIntegrationTest {

    @Autowired
    private TaskLogApplicationService taskLogApplicationService;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private JpaUserTaskRepository taskRepository;

    @Autowired
    private JpaUserTaskLogRepository taskLogRepository;

    @Autowired
    private JpaDailyEnergyRepository energyRepository;

    @Test
    void should_CountSnapshotOnlyOnce_When_SameLogCompletedConcurrently() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity task = createTask(user, "Concurrent", 3);
        UserTaskLogEntity log = createLog(user, task, TaskLogStatus.PLANNED);
        DailyEnergyEntity energy = createEnergy(user, 0);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> complete(log.getId(), user.getId(), ready, start));
            Future<Boolean> second = executor.submit(() -> complete(log.getId(), user.getId(), ready, start));
            ready.await();
            start.countDown();
            first.get();
            second.get();
        } finally {
            executor.shutdownNow();
        }

        assertThat(taskLogRepository.findById(log.getId()).orElseThrow().getStatus())
                .isEqualTo(TaskLogStatus.COMPLETED);
        assertThat(energyRepository.findById(energy.getId()).orElseThrow().getSpoonsUsed())
                .isEqualTo((short) 3);
    }

    @Test
    void should_AddBothCosts_When_DifferentLogsCompletedConcurrently() throws Exception {
        UserEntity user = createUser();
        UserTaskEntity firstTask = createTask(user, "First", 2);
        UserTaskEntity secondTask = createTask(user, "Second", 4);
        UserTaskLogEntity firstLog = createLog(user, firstTask, TaskLogStatus.PLANNED);
        UserTaskLogEntity secondLog = createLog(user, secondTask, TaskLogStatus.PLANNED);
        DailyEnergyEntity energy = createEnergy(user, 0);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> complete(firstLog.getId(), user.getId(), ready, start));
            Future<Boolean> second = executor.submit(() -> complete(secondLog.getId(), user.getId(), ready, start));
            ready.await();
            start.countDown();
            assertThat(first.get()).isTrue();
            assertThat(second.get()).isTrue();
        } finally {
            executor.shutdownNow();
        }

        assertThat(energyRepository.findById(energy.getId()).orElseThrow().getSpoonsUsed())
                .isEqualTo((short) 6);
    }

    private boolean complete(UUID logId, UUID userId, CountDownLatch ready, CountDownLatch start)
            throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            taskLogApplicationService.updateStatus(logId, TaskLogStatus.COMPLETED, userId);
            return true;
        } catch (OptimisticLockingFailureException expectedRace) {
            return false;
        }
    }

    private UserEntity createUser() {
        return userRepository.save(new UserEntity(
                "concurrent-" + UUID.randomUUID() + "@test.local", "hash", "Test"));
    }

    private UserTaskEntity createTask(UserEntity user, String name, int cost) {
        UserTaskEntity task = new UserTaskEntity(user.getId(), name);
        task.setSpoonCost((short) cost);
        task.setDueDate(LocalDate.now());
        return taskRepository.save(task);
    }

    private UserTaskLogEntity createLog(UserEntity user, UserTaskEntity task, TaskLogStatus status) {
        UserTaskLogEntity log = new UserTaskLogEntity(user.getId(), task.getId(), LocalDate.now());
        log.setTaskNameSnapshot(task.getName());
        log.setSpoonCostSnapshot(task.getSpoonCost());
        log.setStatus(status);
        return taskLogRepository.save(log);
    }

    private DailyEnergyEntity createEnergy(UserEntity user, int used) {
        DailyEnergyEntity energy = new DailyEnergyEntity(user.getId(), LocalDate.now(), (short) 10);
        energy.setSpoonsUsed((short) used);
        return energyRepository.save(energy);
    }
}
