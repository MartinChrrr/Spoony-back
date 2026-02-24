package com.spoony.backend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "benevolent_messages")
public class BenevolentMessageEntity extends BaseEntity {

    @Column(name = "context", nullable = false, length = 50)
    private String context;

    @Column(name = "message_key", nullable = false, length = 100)
    private String messageKey;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public BenevolentMessageEntity() {
    }

    public BenevolentMessageEntity(String context, String messageKey) {
        this.context = context;
        this.messageKey = messageKey;
        this.active = true;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
