package com.spoony.backend.infrastructure.exception;

public class MessageNotFoundException extends BusinessException {

    public MessageNotFoundException() {
        super("MESSAGE_NOT_FOUND", "Aucun message trouvé pour ce contexte", 404);
    }
}
