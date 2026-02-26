package com.example.pedidoservice.messaging;

public interface IUserInfoClient {
    UserResponse fetchUserInfo(Integer userId, long timeoutMs);
}
