package com.spoony.backend.domain.task.model;

import java.util.UUID;

public class TaskFromCatalogCommand {

    private UUID baseTaskId;
    private String name;

    public TaskFromCatalogCommand() {
    }

    public TaskFromCatalogCommand(UUID baseTaskId, String name) {
        this.baseTaskId = baseTaskId;
        this.name = name;
    }

    public UUID getBaseTaskId() {
        return baseTaskId;
    }

    public void setBaseTaskId(UUID baseTaskId) {
        this.baseTaskId = baseTaskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
