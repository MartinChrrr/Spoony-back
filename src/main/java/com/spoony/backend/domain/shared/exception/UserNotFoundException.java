package com.spoony.backend.domain.shared.exception;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super("USER_NOT_FOUND", "Utilisateur non trouvé", 404);
    }
}
