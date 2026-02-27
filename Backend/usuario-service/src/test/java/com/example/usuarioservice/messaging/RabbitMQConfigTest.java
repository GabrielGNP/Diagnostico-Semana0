package com.example.usuarioservice.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RabbitMQConfig.
 * Tests bean creation and configuration of RabbitMQ components.
 */
@DisplayName("RabbitMQConfig - bean configuration")
class RabbitMQConfigTest {

    private final com.example.usuarioservice.config.RabbitMQConfig config =
        new com.example.usuarioservice.config.RabbitMQConfig();

    @Test
    @DisplayName("userRequestQueue bean is created with correct name")
    void userRequestQueue_shouldBeCreatedWithCorrectName() {
        // When
        Queue queue = config.userRequestQueue();

        // Then
        assertNotNull(queue);
        assertEquals("user-request-queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    @DisplayName("userResponseQueue bean is created with correct name")
    void userResponseQueue_shouldBeCreatedWithCorrectName() {
        // When
        Queue queue = config.userResponseQueue();

        // Then
        assertNotNull(queue);
        assertEquals("user-response-queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    @DisplayName("userExchange bean is created as DirectExchange")
    void userExchange_shouldBeCreatedAsDirectExchange() {
        // When
        DirectExchange exchange = config.userExchange();

        // Then
        assertNotNull(exchange);
        assertEquals("user-exchange", exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    @DisplayName("constants have correct values")
    void constants_shouldHaveCorrectValues() {
        assertEquals("user-request-queue", com.example.usuarioservice.config.RabbitMQConfig.USER_REQUEST_QUEUE);
        assertEquals("user-response-queue", com.example.usuarioservice.config.RabbitMQConfig.USER_RESPONSE_QUEUE);
        assertEquals("user-exchange", com.example.usuarioservice.config.RabbitMQConfig.USER_EXCHANGE);
        assertEquals("user.request", com.example.usuarioservice.config.RabbitMQConfig.USER_REQUEST_ROUTING_KEY);
        assertEquals("user.response", com.example.usuarioservice.config.RabbitMQConfig.USER_RESPONSE_ROUTING_KEY);
    }
}

