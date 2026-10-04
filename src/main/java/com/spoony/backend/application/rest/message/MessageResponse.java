package com.spoony.backend.application.rest.message;

import com.spoony.backend.infrastructure.persistence.entity.BenevolentMessageEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Message bienveillant contextuel")
public class MessageResponse {

    @Schema(description = "Identifiant unique du message", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Clé i18n du message", example = "messages.zero_energy.rest")
    private String key;

    @Schema(description = "Contexte du message", example = "ZERO_ENERGY", allowableValues = {"WELCOME", "REST", "COMPLETION", "LOW_ENERGY", "ZERO_ENERGY", "SKIP", "END_OF_DAY"})
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
