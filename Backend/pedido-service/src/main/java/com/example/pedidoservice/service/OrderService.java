package com.example.pedidoservice.service;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.mapper.OrderMapper;
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
