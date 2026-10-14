package com.instakill.interaction.application;

import com.instakill.interaction.domain.LikeCreatedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

class NoOpDomainEventPublisherTest {

    @Test
    void publishDoesNotThrowException() {
        NoOpDomainEventPublisher publisher = new NoOpDomainEventPublisher();
        Object event = new LikeCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        
        assertThatCode(() -> publisher.publish(event)).doesNotThrowAnyException();
    }
}

