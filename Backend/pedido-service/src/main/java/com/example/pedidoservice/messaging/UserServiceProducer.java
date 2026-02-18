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
    /**
     * Send a user info request to the user service via RabbitMQ.
     * The routing keys and exchange are defined in `RabbitMQConfig`.
     * This method does not wait for a response; a corresponding
     * `UserServiceConsumer` will receive the response asynchronously.
     *
     * @param userId the id of the user to request
     */
    UserRequest request = new UserRequest(userId);
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.USER_EXCHANGE,
        RabbitMQConfig.USER_REQUEST_ROUTING_KEY,
        request
    );
    System.out.println("User info request sent for userId: " + userId);
    }
}
