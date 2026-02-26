package com.spoony.backend.infrastructure.exception;

public class TaskLogNotFoundException extends BusinessException {

    public TaskLogNotFoundException() {
        super("TASK_LOG_NOT_FOUND", "Log de tâche non trouvé", 404);
    }
}
