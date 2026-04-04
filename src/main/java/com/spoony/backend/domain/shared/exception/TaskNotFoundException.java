package com.spoony.backend.domain.shared.exception;

public class TaskNotFoundException extends BusinessException {

    public TaskNotFoundException() {
        super("TASK_NOT_FOUND", "Tâche non trouvée", 404);
    }
}
