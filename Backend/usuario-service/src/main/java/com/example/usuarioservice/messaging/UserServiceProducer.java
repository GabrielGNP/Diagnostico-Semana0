package com.example.usuarioservice.messaging;

import com.example.usuarioservice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserServiceProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendUserResponse(UserResponse response) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_RESPONSE_ROUTING_KEY,
                response
        );
        System.out.println("User response sent: " + response);
    }
}
