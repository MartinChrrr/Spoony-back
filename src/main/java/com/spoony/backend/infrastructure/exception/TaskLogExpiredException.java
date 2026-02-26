package com.spoony.backend.infrastructure.exception;

public class TaskLogExpiredException extends BusinessException {

    public TaskLogExpiredException() {
        super("TASK_LOG_EXPIRED", "La modification de ce log a expiré (règle J+1)", 403);
    }
}
