package com.spoony.backend.infrastructure.exception;

public class NoActiveTasksException extends BusinessException {

    public NoActiveTasksException() {
        super("NO_ACTIVE_TASKS", "Aucune tâche active à reporter", 404);
    }
}
