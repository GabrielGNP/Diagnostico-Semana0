package com.example.pedidoservice.service;

import com.example.pedidoservice.messaging.UserResponse;

/**
 * Abstraction for fetching user enrichment information.
 */
public interface IUserEnrichmentClient {
    UserResponse fetchUserInfo(Integer userId);
}
