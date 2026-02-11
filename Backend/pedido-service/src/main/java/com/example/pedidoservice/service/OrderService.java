package com.example.pedidoservice.service;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.dto.OrderWithUserDto;
import com.example.pedidoservice.mapper.OrderMapper;
import com.example.pedidoservice.messaging.UserResponse;
import com.example.pedidoservice.messaging.UserServiceConsumer;
import com.example.pedidoservice.messaging.UserServiceProducer;
import com.example.pedidoservice.model.Order;
import com.example.pedidoservice.model.State;
import com.example.pedidoservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserServiceProducer userServiceProducer;

    @Autowired
    private UserServiceConsumer userServiceConsumer;

    private static final long USER_REQUEST_TIMEOUT = 3000; // 3 seconds timeout

    public OrderDto createOrder(OrderDto orderDto) {
        Order order = orderMapper.toEntity(orderDto);
        order.setState(State.PROCESSING); // Default state? Prompt didn't specify, but PROCESSING is first.
        order.setActive(true);
        // ID generation? JSON repo doesn't auto-increment.
        // Simple auto-increment logic:
        int maxId = orderRepository.findAll().stream().mapToInt(Order::getId).max().orElse(0);
        order.setId(maxId + 1);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    public void deleteOrder(int id) {
        orderRepository.deleteById(id);
    }

    public OrderDto changeStateOrder(int id, State newState) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setState(newState);
            Order savedOrder = orderRepository.save(order);
            return orderMapper.toDto(savedOrder);
        }
        return null;
    }

    public List<OrderDto> listOrdersByIdUser(int idUser) {
        return orderRepository.findByUserId(idUser).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public OrderWithUserDto getOrderWithUserInfo(int orderId) {
        // Get the order first
        OrderDto orderDto = showOrderById(orderId);
        if (orderDto == null) {
            return null;
        }
        
        // Request user information via RabbitMQ using the orderId's userId
        int idUser = orderDto.getIdUser();
        UserResponse userResponse = null;
        try {
            userServiceProducer.requestUserInfo(idUser);
            // Wait for user response
            userResponse = userServiceConsumer.getUserResponse(idUser, USER_REQUEST_TIMEOUT);
        } catch (Exception ex) {
            // Log and continue — return order with null user if messaging fails
            System.err.println("Error requesting/receiving user info for userId=" + idUser + ": " + ex.getMessage());
        }
        
        // Map to OrderWithUserDto including user information
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

    public List<OrderDto> listAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public OrderDto showOrderById(int id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElse(null);
    }
}

