package com.spoony.backend.domain.shared.exception;

public class TaskLogNotFoundException extends BusinessException {

    public TaskLogNotFoundException() {
        super("TASK_LOG_NOT_FOUND", "Log de tâche non trouvé", 404);
    }
}
