package com.example.usuarioservice.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRequest {
    @JsonProperty("userId")
    private int userId;

    public UserRequest() {
    }

    /**
     * Messaging DTO used by other services to request a user lookup.
     * Carries only the numeric `userId`.
     */

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
