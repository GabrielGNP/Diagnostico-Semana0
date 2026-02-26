package com.example.pedidoservice.service;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.dto.OrderWithUserDto;
import com.example.pedidoservice.messaging.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderEnrichmentFacade {
    private static final Logger log = LoggerFactory.getLogger(OrderEnrichmentFacade.class);

    private final IUserEnrichmentClient userEnrichmentClient;

    public OrderEnrichmentFacade(IUserEnrichmentClient userEnrichmentClient) {
        this.userEnrichmentClient = userEnrichmentClient;
    }

    public OrderWithUserDto enrich(OrderDto orderDto) {
        if (orderDto == null) {
            return null;
        }

        UserResponse userResponse = null;
        try {
            userResponse = userEnrichmentClient.fetchUserInfo(orderDto.getIdUser());
        } catch (Exception ex) {
            log.warn("Error enriching order id={} userId={}", orderDto.getId(), orderDto.getIdUser(), ex);
        }

        return new OrderWithUserDto(
                orderDto.getId(),
                orderDto.getName(),
                orderDto.getDescription(),
                orderDto.getIdUser(),
                orderDto.getState(),
                orderDto.isActive(),
                userResponse
        );
    }
}
