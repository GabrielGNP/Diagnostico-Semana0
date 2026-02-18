package com.example.pedidoservice.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRequest {
    @JsonProperty("userId")
    private int userId;

    /**
     * Simple DTO used to request user information across services.
     * The payload contains only the `userId` to look up.
     */

    public UserRequest() {
    }

    public UserRequest(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserRequest{" +
                "userId=" + userId +
                '}';
    }
}
