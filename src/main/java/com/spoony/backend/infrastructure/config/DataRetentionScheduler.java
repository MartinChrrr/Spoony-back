package com.spoony.backend.infrastructure.config;

import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataRetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataRetentionScheduler.class);
    private static final int RETENTION_MONTHS = 24;

    private final JpaUserRepository userRepository;

    public DataRetentionScheduler(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeInactiveAccounts() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(RETENTION_MONTHS);
        List<UserEntity> inactive = userRepository.findInactiveUsers(cutoff);

        if (!inactive.isEmpty()) {
            log.info("RGPD rétention : suppression de {} comptes inactifs (seuil={})", inactive.size(), cutoff);
            userRepository.deleteAll(inactive);
        }
    }
}
