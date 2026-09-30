package com.spoony.backend.infrastructure.config;

import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataRetentionSchedulerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-09-08T12:00:00Z"), ZoneId.of("Europe/Paris"));

    @Test
    void shouldNotQueryOrDeleteAccountsWhenDeletionIsDisabled() {
        JpaUserRepository repository = mock(JpaUserRepository.class);
        DataRetentionScheduler scheduler = new DataRetentionScheduler(repository, FIXED_CLOCK, false);

        scheduler.purgeInactiveAccounts();

        verify(repository, never()).findInactiveUsers(org.mockito.ArgumentMatchers.any());
        verify(repository, never()).deleteAll(org.mockito.ArgumentMatchers.<List<UserEntity>>any());
    }

    @Test
    void shouldDeleteInactiveAccountsWhenExplicitlyEnabled() {
        JpaUserRepository repository = mock(JpaUserRepository.class);
        DataRetentionScheduler scheduler = new DataRetentionScheduler(repository, FIXED_CLOCK, true);
        LocalDateTime expectedCutoff = LocalDateTime.of(2024, 9, 8, 14, 0);
        List<UserEntity> inactive = List.of(new UserEntity());
        when(repository.findInactiveUsers(expectedCutoff)).thenReturn(inactive);

        scheduler.purgeInactiveAccounts();

        verify(repository).deleteAll(inactive);
    }
}
