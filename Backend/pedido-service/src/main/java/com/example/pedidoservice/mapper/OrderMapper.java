package com.example.pedidoservice.mapper;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderDto(
                order.getId(),
                order.getName(),
                order.getDescription(),
                order.getIdUser(),
                order.getState(),
                order.isActive());
    }

    public Order toEntity(OrderDto orderDto) {
        if (orderDto == null) {
            return null;
        }
        return new Order(
                orderDto.getId(),
                orderDto.getName(),
                orderDto.getDescription(),
                orderDto.getIdUser(),
                orderDto.getState(),
                orderDto.isActive());
    }
}
