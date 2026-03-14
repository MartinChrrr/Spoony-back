package com.spoony.backend.application.rest.message;

import com.spoony.backend.infrastructure.exception.MessageNotFoundException;
import com.spoony.backend.infrastructure.persistence.entity.BenevolentMessageEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaBenevolentMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private JpaBenevolentMessageRepository messageRepository;

    private MessageController controller;

    @BeforeEach
    void setUp() {
        controller = new MessageController(messageRepository);
    }

    @Test
    void should_ReturnRandomMessage_When_ContextValid() {
        BenevolentMessageEntity msg1 = new BenevolentMessageEntity("WELCOME", "messages.welcome.return");
        BenevolentMessageEntity msg2 = new BenevolentMessageEntity("WELCOME", "messages.welcome.no_rush");
        when(messageRepository.findByContextAndActiveTrue("WELCOME")).thenReturn(List.of(msg1, msg2));

        var response = controller.getRandomMessage("WELCOME", null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        MessageResponse data = response.getBody().getData();
        assertThat(data.getContext()).isEqualTo("WELCOME");
        assertThat(data.getKey()).startsWith("messages.welcome.");
    }

    @Test
    void should_Throw404_When_NoMessagesForContext() {
        when(messageRepository.findByContextAndActiveTrue("INVALID")).thenReturn(List.of());

        assertThatThrownBy(() -> controller.getRandomMessage("INVALID", null))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @Test
    void should_ReturnSingleMessage_When_OnlyOneExists() {
        BenevolentMessageEntity msg = new BenevolentMessageEntity("REST", "messages.rest.listen_body");
        when(messageRepository.findByContextAndActiveTrue("REST")).thenReturn(List.of(msg));

        var response = controller.getRandomMessage("REST", null);

        assertThat(response.getBody().getData().getKey()).isEqualTo("messages.rest.listen_body");
    }

    @Test
    void should_AcceptLocaleParam() {
        BenevolentMessageEntity msg = new BenevolentMessageEntity("ZERO_ENERGY", "messages.zero_energy.comfort");
        when(messageRepository.findByContextAndActiveTrue("ZERO_ENERGY")).thenReturn(List.of(msg));

        var response = controller.getRandomMessage("ZERO_ENERGY", "fr");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
