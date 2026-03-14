package com.spoony.backend.application.rest.message;

import com.spoony.backend.infrastructure.persistence.entity.BenevolentMessageEntity;

public class MessageResponse {

    private String key;
    private String context;

    public MessageResponse() {
    }

    public static MessageResponse fromEntity(BenevolentMessageEntity entity) {
        MessageResponse response = new MessageResponse();
        response.setKey(entity.getMessageKey());
        response.setContext(entity.getContext());
        return response;
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
