package com.spoony.backend.application.auth;

import com.spoony.backend.domain.shared.exception.EmailAlreadyExistsException;
import com.spoony.backend.domain.shared.exception.InvalidCredentialsException;
import com.spoony.backend.infrastructure.persistence.entity.UserEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserRepository;
import com.spoony.backend.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void should_RegisterUser_When_EmailIsNew() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Martin");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(UUID.class))).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class))).thenReturn("refresh-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(2)).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getAllValues().get(0);
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$10$hashedPassword");
        assertThat(savedUser.getFirstName()).isEqualTo("Martin");
    }

    @Test
    void should_PersistConsentGivenAt_When_Registering() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Martin", true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(UUID.class))).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class))).thenReturn("refresh-token");

        LocalDateTime before = LocalDateTime.now();

        // Act
        authService.register(request);

        // Assert
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(2)).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getAllValues().get(0);
        assertThat(savedUser.getConsentGivenAt())
                .isNotNull()
                .isAfterOrEqualTo(before);
    }

    @Test
    void should_StoreRefreshTokenHash_When_Registering() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Martin");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(UUID.class))).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class))).thenReturn("refresh-token");

        // Act
        authService.register(request);

        // Assert
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(2)).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getAllValues().get(1);
        assertThat(savedUser.getRefreshTokenHash()).isNotNull();
        assertThat(savedUser.getRefreshTokenHash()).isNotEmpty();
        assertThat(savedUser.getRefreshTokenHash()).isNotEqualTo("refresh-token");
    }

    @Test
    void should_ThrowEmailAlreadyExists_When_EmailIsTaken() {
        // Arrange
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", "Martin");
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new UserEntity()));

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void should_LoginUser_When_CredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        UserEntity user = new UserEntity("test@example.com", "$2a$10$hashedPassword", "Martin");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(UUID.class))).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class))).thenReturn("refresh-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void should_ThrowInvalidCredentials_When_EmailNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void should_ThrowInvalidCredentials_When_PasswordIsWrong() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        UserEntity user = new UserEntity("test@example.com", "$2a$10$hashedPassword", "Martin");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }

    @Test
    void should_RefreshTokens_When_RefreshTokenIsValid() {
        // Arrange
        String rawRefreshToken = "valid-refresh-token";
        RefreshRequest request = new RefreshRequest(rawRefreshToken);
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity("test@example.com", "$2a$10$hashedPassword", "Martin");

        when(jwtTokenProvider.validateToken(rawRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getTokenType(rawRefreshToken)).thenReturn("refresh");
        when(jwtTokenProvider.getUserIdFromToken(rawRefreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(UUID.class))).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class))).thenReturn("new-refresh-token");

        // Set the stored hash to match the incoming token
        user.setRefreshTokenHash(hashToken(rawRefreshToken));

        // Act
        AuthResponse response = authService.refresh(request);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void should_ThrowInvalidCredentials_When_RefreshTokenIsExpired() {
        // Arrange
        RefreshRequest request = new RefreshRequest("expired-token");
        when(jwtTokenProvider.validateToken("expired-token")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void should_ThrowInvalidCredentials_When_TokenIsNotRefreshType() {
        // Arrange
        RefreshRequest request = new RefreshRequest("access-token-used-as-refresh");
        when(jwtTokenProvider.validateToken("access-token-used-as-refresh")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("access-token-used-as-refresh")).thenReturn("access");

        // Act & Assert
        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void should_ThrowInvalidCredentials_When_RefreshTokenHashDoesNotMatch() {
        // Arrange
        String rawRefreshToken = "valid-but-revoked-token";
        RefreshRequest request = new RefreshRequest(rawRefreshToken);
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity("test@example.com", "$2a$10$hashedPassword", "Martin");
        user.setRefreshTokenHash("different-hash-from-another-token");

        when(jwtTokenProvider.validateToken(rawRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getTokenType(rawRefreshToken)).thenReturn("refresh");
        when(jwtTokenProvider.getUserIdFromToken(rawRefreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void should_ThrowInvalidCredentials_When_UserNotFoundOnRefresh() {
        // Arrange
        String rawRefreshToken = "valid-token-deleted-user";
        RefreshRequest request = new RefreshRequest(rawRefreshToken);
        UUID userId = UUID.randomUUID();

        when(jwtTokenProvider.validateToken(rawRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getTokenType(rawRefreshToken)).thenReturn("refresh");
        when(jwtTokenProvider.getUserIdFromToken(rawRefreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
