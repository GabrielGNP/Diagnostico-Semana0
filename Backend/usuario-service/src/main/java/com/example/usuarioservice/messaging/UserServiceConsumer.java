package com.example.usuarioservice.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.usuarioservice.config.RabbitMQConfig;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.service.UserRepository;

@Component
public class UserServiceConsumer {

    @Autowired
    private UserServiceProducer producer;

    @Autowired
    private UserRepository userRepository;

    @RabbitListener(queues = RabbitMQConfig.USER_REQUEST_QUEUE)
    public void receiveUserRequest(UserRequest request) {
        System.out.println("User request received: " + request);
        
        if (producer == null) {
            System.err.println("Producer not available");
            return;
        }

        int userId = request.getUserId();
        User user = userRepository.findById(userId);
        
        if (user != null) {
            UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getMail(),
                user.isActive()
            );
            producer.sendUserResponse(response);
        } else {
            System.out.println("User not found with id: " + userId);
        }
    }
}
