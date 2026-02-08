package com.example.usuarioservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String USER_REQUEST_QUEUE = "user-request-queue";
    public static final String USER_RESPONSE_QUEUE = "user-response-queue";

    // Exchange names
    public static final String USER_EXCHANGE = "user-exchange";

    // Routing keys
    public static final String USER_REQUEST_ROUTING_KEY = "user.request";
    public static final String USER_RESPONSE_ROUTING_KEY = "user.response";

    // Queues
    @Bean
    public Queue userRequestQueue() {
        return new Queue(USER_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue userResponseQueue() {
        return new Queue(USER_RESPONSE_QUEUE, true);
    }

    // Exchange
    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(USER_EXCHANGE, true, false);
    }

    // Bindings
    @Bean
    public Binding userRequestBinding(Queue userRequestQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(userRequestQueue)
                .to(userExchange)
                .with(USER_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Binding userResponseBinding(Queue userResponseQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(userResponseQueue)
                .to(userExchange)
                .with(USER_RESPONSE_ROUTING_KEY);
    }
}
