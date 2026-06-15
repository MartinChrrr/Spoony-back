package com.spoony.backend.application.auth;

import com.spoony.backend.domain.shared.exception.EmailAlreadyExistsException;
import com.spoony.backend.domain.shared.exception.InvalidCredentialsException;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserRepository;
import com.spoony.backend.infrastructure.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(JpaUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        UserEntity user = new UserEntity(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName()
        );
        // P0/RGPD Art. 9: explicit consent timestamp for health-data processing,
        // captured at account creation. @AssertTrue on the request already guarantees
        // consentGiven == true reached this point.
        user.setConsentGivenAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("User registered userId={}", user.getId());

        return generateTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in userId={}", user.getId());

        return generateTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String rawRefreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(rawRefreshToken)) {
            throw new InvalidCredentialsException();
        }

        String tokenType = jwtTokenProvider.getTokenType(rawRefreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new InvalidCredentialsException();
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(rawRefreshToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(InvalidCredentialsException::new);

        String storedHash = user.getRefreshTokenHash();
        String incomingHash = hashToken(rawRefreshToken);

        if (storedHash == null || !storedHash.equals(incomingHash)) {
            throw new InvalidCredentialsException();
        }

        // P0/ADR-015: a token refresh is real activity. Without this, a user who stays
        // logged in (only ever refreshing, never re-logging in) keeps a stale lastLoginAt
        // and gets purged as "inactive" by the retention scheduler despite daily use.
        user.setLastLoginAt(LocalDateTime.now());

        log.info("Token refreshed userId={}", userId);

        return generateTokens(user);
    }

    private AuthResponse generateTokens(UserEntity user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        user.setRefreshTokenHash(hashToken(refreshToken));
        userRepository.save(user);

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getFirstName());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
