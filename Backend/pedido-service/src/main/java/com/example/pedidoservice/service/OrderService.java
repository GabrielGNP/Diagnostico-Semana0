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

    /**
     * Service that manages orders.
     *
     * Responsibilities:
     * - CRUD operations on orders using the JSON-backed repository
     * - Coordinate with the user service via RabbitMQ to enrich orders with user info
     *
     * Threading / timeouts:
     * - Requests for user information are synchronous from the caller perspective
     *   and use a short timeout to avoid blocking the request thread for too long.
     */

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
        /**
         * Create a new order from DTO.
         * - Maps DTO to entity, assigns a new ID, sets default state and persists.
         * - Returns the saved DTO representation.
         *
         * Note: ID assignment is a simple max+1 strategy used by the file-based
         * repository implementation and may not be suitable for concurrent producers.
         */
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
        /**
         * Retrieve an order and attempt to append user information obtained
         * via the asynchronous user service. The method sends a user info
         * request over RabbitMQ and blocks up to `USER_REQUEST_TIMEOUT` ms
         * waiting for the response.
         *
         * If the user service cannot be reached or a response isn't received
         * within the timeout the returned `OrderWithUserDto` will include a
         * null user payload and the order data is still returned.
         */
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


    /**
     * Lists all orders from the database.
     *
     * @deprecated Use {@link #findAllActiveOrders()} instead for production use.
     * This method includes inactive (soft-deleted) orders and should only be used
     * for administrative or audit purposes.
     *
     * @return List of all orders (active and inactive)
     */
    @Deprecated
    public List<OrderDto> listAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all active orders from PostgreSQL database.
     *
     * Implementation details:
     * - Queries only active orders directly from database (optimized query)
     * - Uses JPA @Query for better performance
     * - Maps entities to DTOs
     *
     * Business Rules (HU-ORD-01):
     * - Only returns orders where active=true
     * - Returns empty list if no active orders exist
     * - Orders with active=false are excluded (soft-deleted)
     *
     * Performance:
     * - Should respond in < 200ms for up to 1000 records (NFR-ORD-01-01)
     * - Uses database-level filtering (more efficient than stream filtering)
     * - Connection pooling via HikariCP
     *
     * @return List of active orders as DTOs, empty list if none exist
     */
    public List<OrderDto> findAllActiveOrders() {
        return orderRepository.findAllActive().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }


    public OrderDto showOrderById(int id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElse(null);
    }
}
