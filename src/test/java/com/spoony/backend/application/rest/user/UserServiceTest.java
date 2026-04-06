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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaUserTaskRepository userTaskRepository;

    @Mock
    private JpaUserTaskLogRepository userTaskLogRepository;

    @Mock
    private JpaDailyEnergyRepository dailyEnergyRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userTaskRepository, userTaskLogRepository, dailyEnergyRepository);
    }

    @Test
    void should_DeleteUser_When_UserExists() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void should_NotThrow_When_UserNotExists() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        userService.deleteUser(userId);

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void should_ReturnExportData_When_UserExists() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity("marie@example.com", "hash", "Marie");
        user.setId(userId);

        UserTaskEntity task = new UserTaskEntity();
        task.setId(UUID.randomUUID());
        task.setName("Courses");
        task.setSpoonCost((short) 3);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userTaskRepository.findByUserId(userId)).thenReturn(List.of(task));
        when(userTaskLogRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(dailyEnergyRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        UserExportResponse export = userService.exportUserData(userId);

        assertThat(export.getProfile().getEmail()).isEqualTo("marie@example.com");
        assertThat(export.getProfile().getFirstName()).isEqualTo("Marie");
        assertThat(export.getTasks()).hasSize(1);
        assertThat(export.getTasks().get(0).getName()).isEqualTo("Courses");
        assertThat(export.getTaskLogs()).isEmpty();
        assertThat(export.getEnergyDeclarations()).isEmpty();
    }

    @Test
    void should_ThrowUserNotFoundException_When_UserNotFoundForExport() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.exportUserData(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void should_ReturnEmptyLists_When_UserHasNoData() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity("test@example.com", "hash", "Test");
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userTaskRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userTaskLogRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(dailyEnergyRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        UserExportResponse export = userService.exportUserData(userId);

        assertThat(export.getTasks()).isEmpty();
        assertThat(export.getTaskLogs()).isEmpty();
        assertThat(export.getEnergyDeclarations()).isEmpty();
    }
}
