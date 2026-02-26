package com.spoony.backend.infrastructure.exception;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Email ou mot de passe incorrect", 401);
    }
}
