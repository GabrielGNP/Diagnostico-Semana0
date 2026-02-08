package com.example.pedidoservice.messaging;

import com.example.pedidoservice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserServiceProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void requestUserInfo(int userId) {
        UserRequest request = new UserRequest(userId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_REQUEST_ROUTING_KEY,
                request
        );
        System.out.println("User info request sent for userId: " + userId);
    }
}
