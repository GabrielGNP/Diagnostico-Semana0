package com.example.pedidoservice.messaging;

import com.example.pedidoservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
public class UserServiceConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserServiceConsumer.class);

    private final UserResponseCache cache;

    public UserServiceConsumer(UserResponseCache cache) {
        this.cache = cache;
    }

    @RabbitListener(queues = RabbitMQConfig.USER_RESPONSE_QUEUE)
    public void receiveUserResponse(UserResponse response) {
        // Received asynchronously from RabbitMQ - delegate storage to cache
        log.debug("User response received: {}", response);
        cache.store(response);
    }

    public UserResponse getUserResponse(int userId, long timeoutMs) {
        /**
         * Wait up to `timeoutMs` milliseconds for a previously received
         * `UserResponse` for `userId` to be available in the internal map.
         * This method polls the concurrent map with short waits using `lock`.
         *
         * If a response is found it is removed from the map and returned.
         * If the timeout elapses, `null` is returned.
         *
         * Note: callers should keep the timeout short to avoid blocking request
         * handling threads for long periods.
         */
        return cache.awaitResponse(userId, timeoutMs);
    }
}

