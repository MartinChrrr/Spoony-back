package com.spoony.backend.application.rest.message;

import com.spoony.backend.infrastructure.persistence.entity.BenevolentMessageEntity;

import java.util.UUID;

public class MessageResponse {

    private UUID id;
    private String key;
    private String context;

    public MessageResponse() {
    }

    public static MessageResponse fromEntity(BenevolentMessageEntity entity) {
        MessageResponse response = new MessageResponse();
        response.setId(entity.getId());
        response.setKey(entity.getMessageKey());
        response.setContext(entity.getContext());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
