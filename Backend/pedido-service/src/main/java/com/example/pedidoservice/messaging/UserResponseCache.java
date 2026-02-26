package com.example.pedidoservice.messaging;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserResponseCache {

    private final Map<Integer, UserResponse> userResponses = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    public void store(UserResponse response) {
        if (response != null && response.getId() != null) {
            userResponses.put(response.getId(), response);
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    public UserResponse awaitResponse(int userId, long timeoutMs) {
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

        userResponses.remove(userId);
        return null;
    }
}
