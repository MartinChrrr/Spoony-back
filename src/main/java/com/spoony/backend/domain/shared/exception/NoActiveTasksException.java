package com.spoony.backend.domain.shared.exception;

public class NoActiveTasksException extends BusinessException {

    public NoActiveTasksException() {
        super("NO_ACTIVE_TASKS", "Aucune tâche active à reporter", 404);
    }
}
