package com.example.pedidoservice.messaging;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RabbitMQUserInfoClient implements IUserInfoClient {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQUserInfoClient.class);

    private final UserServiceProducer producer;
    private final UserServiceConsumer consumer;

    public RabbitMQUserInfoClient(UserServiceProducer producer, UserServiceConsumer consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    @Override
    public UserResponse fetchUserInfo(Integer userId, long timeoutMs) {
        try {
            producer.requestUserInfo(userId);
            return consumer.getUserResponse(userId, timeoutMs);
        } catch (Exception ex) {
            log.warn("Error fetching user info for userId={}", userId, ex);
            return null;
        }
    }
}
