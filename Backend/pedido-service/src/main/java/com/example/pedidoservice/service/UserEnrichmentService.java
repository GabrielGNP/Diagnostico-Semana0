package com.example.pedidoservice.service;

import com.example.pedidoservice.messaging.IUserInfoClient;
import com.example.pedidoservice.messaging.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserEnrichmentService {
    private static final Logger log = LoggerFactory.getLogger(UserEnrichmentService.class);

    private final IUserInfoClient userInfoClient;

    @Value("${user.service.timeout:3000}")
    private long userRequestTimeout;

    public UserEnrichmentService(IUserInfoClient userInfoClient) {
        this.userInfoClient = userInfoClient;
    }

    public UserResponse fetchUserInfo(Integer userId) {
        try {
            return userInfoClient.fetchUserInfo(userId, userRequestTimeout);
        } catch (Exception ex) {
            log.warn("Error fetching user info for userId={}", userId, ex);
            return null;
        }
    }
}
