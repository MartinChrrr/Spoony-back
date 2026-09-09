package com.spoony.backend.infrastructure.config;

import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataRetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataRetentionScheduler.class);
    private static final int RETENTION_MONTHS = 24;

    private final JpaUserRepository userRepository;
    private final Clock clock;
    private final boolean deletionEnabled;

    public DataRetentionScheduler(JpaUserRepository userRepository, Clock businessClock,
                                  @Value("${app.data-retention.deletion-enabled:false}") boolean deletionEnabled) {
        this.userRepository = userRepository;
        this.clock = businessClock;
        this.deletionEnabled = deletionEnabled;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeInactiveAccounts() {
        // Deletion remains fail-closed until the required advance notice workflow
        // exists. Enabling it is an explicit production decision, never a default.
        if (!deletionEnabled) {
            log.debug("RGPD rétention : purge automatique désactivée");
            return;
        }

        LocalDateTime cutoff = LocalDateTime.now(clock).minusMonths(RETENTION_MONTHS);
        List<UserEntity> inactive = userRepository.findInactiveUsers(cutoff);

        if (!inactive.isEmpty()) {
            log.info("RGPD rétention : suppression de {} comptes inactifs (seuil={})", inactive.size(), cutoff);
            userRepository.deleteAll(inactive);
        }
    }
}
