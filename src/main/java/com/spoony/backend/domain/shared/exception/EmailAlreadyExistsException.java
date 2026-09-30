package com.spoony.backend.domain.shared.exception;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException() {
        super("EMAIL_ALREADY_EXISTS", "Cet email est déjà utilisé", 409);
    }
}
