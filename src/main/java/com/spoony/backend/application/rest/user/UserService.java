package com.spoony.backend.application.rest.user;

import com.spoony.backend.domain.shared.exception.UserNotFoundException;
import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaDailyEnergyRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskLogRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final JpaUserRepository userRepository;
    private final JpaUserTaskRepository userTaskRepository;
    private final JpaUserTaskLogRepository userTaskLogRepository;
    private final JpaDailyEnergyRepository dailyEnergyRepository;

    public UserService(JpaUserRepository userRepository,
                       JpaUserTaskRepository userTaskRepository,
                       JpaUserTaskLogRepository userTaskLogRepository,
                       JpaDailyEnergyRepository dailyEnergyRepository) {
        this.userRepository = userRepository;
        this.userTaskRepository = userTaskRepository;
        this.userTaskLogRepository = userTaskLogRepository;
        this.dailyEnergyRepository = dailyEnergyRepository;
    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("RGPD delete requested for unknown userId={}", userId);
            return;
        }

        userRepository.deleteById(userId);
        log.info("RGPD: user deleted permanently userId={}", userId);
    }

    @Transactional(readOnly = true)
    public UserExportResponse exportUserData(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        List<UserTaskEntity> tasks = userTaskRepository.findByUserId(userId);
        List<UserTaskLogEntity> taskLogs = userTaskLogRepository.findByUserId(userId);
        List<DailyEnergyEntity> energyList = dailyEnergyRepository.findByUserId(userId);

        UserExportResponse.UserProfile profile = new UserExportResponse.UserProfile(
                user.getEmail(),
                user.getFirstName(),
                user.getCreatedAt()
        );

        List<UserExportResponse.ExportedTask> exportedTasks = tasks.stream()
                .map(t -> new UserExportResponse.ExportedTask(
                        t.getId(),
                        t.getName(),
                        t.getSpoonCost(),
                        t.getImportance(),
                        t.getCategory(),
                        t.getDueDate(),
                        t.getNotes(),
                        t.getStatus() != null ? t.getStatus().name() : null,
                        t.getCreatedAt()
                ))
                .toList();

        List<UserExportResponse.ExportedTaskLog> exportedLogs = taskLogs.stream()
                .map(l -> new UserExportResponse.ExportedTaskLog(
                        l.getId(),
                        l.getUserTaskId(),
                        l.getDate(),
                        l.getStatus() != null ? l.getStatus().name() : null,
                        l.isSuggested(),
                        l.getCompletedAt(),
                        l.getCreatedAt()
                ))
                .toList();

        List<UserExportResponse.ExportedEnergy> exportedEnergy = energyList.stream()
                .map(e -> new UserExportResponse.ExportedEnergy(
                        e.getId(),
                        e.getDate(),
                        e.getSpoons(),
                        e.getSpoonsUsed(),
                        e.getMoodStart(),
                        e.getEndOfDayMood(),
                        e.getCreatedAt()
                ))
                .toList();

        log.info("RGPD export: données exportées pour userId={}", userId);

        return new UserExportResponse(profile, exportedTasks, exportedLogs, exportedEnergy);
    }
}
