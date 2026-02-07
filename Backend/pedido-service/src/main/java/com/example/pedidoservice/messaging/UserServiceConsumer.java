package com.example.pedidoservice.messaging;

import com.example.pedidoservice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserServiceConsumer {

    private final Map<Integer, UserResponse> userResponses = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    @RabbitListener(queues = RabbitMQConfig.USER_RESPONSE_QUEUE)
    public void receiveUserResponse(UserResponse response) {
        System.out.println("User response received: " + response);
        if (response != null && response.getId() != null) {
            userResponses.put(response.getId(), response);
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    public UserResponse getUserResponse(int userId, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (userResponses.containsKey(userId)) {
                return userResponses.remove(userId);
            }
            
            synchronized (lock) {
                try {
                    lock.wait(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // Cleanup if still waiting
        userResponses.remove(userId);
        return null;
    }
}

