package com.spoony.backend.application.rest.user;

import com.spoony.backend.infrastructure.persistence.repository.JpaUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final JpaUserRepository userRepository;

    public UserService(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
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
}
