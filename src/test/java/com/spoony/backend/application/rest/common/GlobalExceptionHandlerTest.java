package com.spoony.backend.application.rest.common;

import com.spoony.backend.domain.shared.exception.EmailAlreadyExistsException;
import com.spoony.backend.domain.shared.exception.EnergyAlreadyDeclaredException;
import com.spoony.backend.domain.shared.exception.EnergyNotDeclaredException;
import com.spoony.backend.domain.shared.exception.InvalidCredentialsException;
import com.spoony.backend.domain.shared.exception.NoActiveTasksException;
import com.spoony.backend.domain.shared.exception.TaskLogExpiredException;
import com.spoony.backend.domain.shared.exception.TaskLogNotFoundException;
import com.spoony.backend.domain.shared.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void should_Return409_When_EnergyAlreadyDeclared() {
        ResponseEntity<JSendResponse<Map<String, String>>> response =
                handler.handleBusinessException(new EnergyAlreadyDeclaredException());

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().getStatus()).isEqualTo("fail");
        assertThat(response.getBody().getData().get("code")).isEqualTo("ENERGY_ALREADY_DECLARED");
    }

    @Test
    void should_Return404_When_EnergyNotDeclared() {
        ResponseEntity<JSendResponse<Map<String, String>>> response =
                handler.handleBusinessException(new EnergyNotDeclaredException());

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getData().get("code")).isEqualTo("ENERGY_NOT_DECLARED");
    }

    @Test
    void should_Return404_When_TaskNotFound() {
        ResponseEntity<JSendResponse<Map<String, String>>> response =
                handler.handleBusinessException(new TaskNotFoundException());

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getData().get("code")).isEqualTo("TASK_NOT_FOUND");
    }

    @Test
    void should_Return404_When_TaskLogNotFound() {
        ResponseEntity<JSendResponse<Map<String, String>>> response =
                handler.handleBusinessException(new TaskLogNotFoundException());

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getData().get("code")).isEqualTo("TASK_LOG_NOT_FOUND");
    }

    @Test
    void should_Return403_When_TaskLogExpired() {
        ResponseEntity<JSendResponse<Map<String, String>>> response =
                handler.handleBusinessException(new TaskLogExpiredException());

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody().getData().get("code")).isEqualTo("TASK_LOG_EXPIRED");
    }

    @Test
    void should_Return409_When_EmailAlreadyExists() {
        ResponseEntity<JSendResponse<Map<String, String>>> response =
                handler.handleBusinessException(new EmailAlreadyExistsException());

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().getData().get("code")).isEqualTo("EMAIL_ALREADY_EXISTS");
    }

    @Test
    void should_Return401_When_InvalidCredentials() {
        ResponseEntity<JSendResponse<Map<String, String>>> response =
                handler.handleBusinessException(new InvalidCredentialsException());

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody().getData().get("code")).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void should_Return404_When_NoActiveTasks() {
        ResponseEntity<JSendResponse<Map<String, String>>> response =
                handler.handleBusinessException(new NoActiveTasksException());

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getData().get("code")).isEqualTo("NO_ACTIVE_TASKS");
    }

    @Test
    void should_Return400_When_MalformedJson() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad json");
        ResponseEntity<JSendResponse<Map<String, String>>> response = handler.handleMalformedJson(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getStatus()).isEqualTo("fail");
        assertThat(response.getBody().getData().get("code")).isEqualTo("MALFORMED_JSON");
    }

    @Test
    void should_Return400_When_MissingParam() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("context", "String");
        ResponseEntity<JSendResponse<Map<String, String>>> response = handler.handleMissingParam(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getStatus()).isEqualTo("fail");
        assertThat(response.getBody().getData().get("code")).isEqualTo("MISSING_PARAMETER");
    }

    @Test
    void should_Return415_When_UnsupportedMediaType() {
        HttpMediaTypeNotSupportedException ex =
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<JSendResponse<Map<String, String>>> response = handler.handleUnsupportedMediaType(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(415);
        assertThat(response.getBody().getStatus()).isEqualTo("fail");
        assertThat(response.getBody().getData().get("code")).isEqualTo("UNSUPPORTED_MEDIA_TYPE");
    }

    @Test
    void should_Return500_When_UnexpectedException() {
        ResponseEntity<JSendResponse<Void>> response =
                handler.handleGenericException(new RuntimeException("unexpected"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getStatus()).isEqualTo("error");
        assertThat(response.getBody().getMessage()).isEqualTo("Une erreur inattendue est survenue");
    }
}
