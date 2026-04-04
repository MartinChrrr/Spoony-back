package com.spoony.backend.application.rest.message;

import com.spoony.backend.application.rest.common.JSendResponse;
import com.spoony.backend.domain.shared.exception.MessageNotFoundException;
import com.spoony.backend.infrastructure.persistence.entity.BenevolentMessageEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaBenevolentMessageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "Messages bienveillants contextuels")
public class MessageController {

    private final JpaBenevolentMessageRepository messageRepository;

    public MessageController(JpaBenevolentMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/random")
    @Operation(
            summary = "Obtenir un message aléatoire",
            description = "Retourne un message bienveillant aléatoire parmi les messages actifs du contexte donné"
    )
    @ApiResponse(responseCode = "200", description = "Message retourné")
    @ApiResponse(responseCode = "404", description = "Aucun message pour ce contexte")
    public ResponseEntity<JSendResponse<MessageResponse>> getRandomMessage(
            @RequestParam String context,
            @RequestParam(required = false) String locale) {

        List<BenevolentMessageEntity> messages = messageRepository.findByContextAndActiveTrue(context);

        if (messages.isEmpty()) {
            throw new MessageNotFoundException();
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(messages.size());
        BenevolentMessageEntity chosen = messages.get(randomIndex);

        return ResponseEntity.ok(JSendResponse.success(MessageResponse.fromEntity(chosen)));
    }
}
