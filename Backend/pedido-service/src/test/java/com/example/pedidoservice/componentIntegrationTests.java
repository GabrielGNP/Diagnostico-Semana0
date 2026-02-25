package com.example.pedidoservice;

import com.example.pedidoservice.controller.OrderController;
import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.model.Order;
import com.example.pedidoservice.model.State;
import com.example.pedidoservice.service.OrderService;
import com.example.pedidoservice.repository.OrderRepository;
import com.example.pedidoservice.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración de componentes del pedido-service.
 * Verifica la integración entre Controller, Service, Repository y Mapper.
 * Prueba flujos de negocio completos del sistema de órdenes.
 */
@SpringBootTest
@DisplayName("Component Integration Tests - Order Service")
@Disabled("Pruebas de integración antiguas deshabilitadas temporalmente")
class ComponentIntegrationTests {

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMapper orderMapper;

    @BeforeEach
    public void setUp() {
        // Limpia el repositorio antes de cada test
        orderRepository.findAll().forEach(order -> orderRepository.deleteById(order.getId()));
    }

    @Test
    @DisplayName("Integration: Create Order - Controller to Repository")
    public void testCreateOrderComponentIntegration() {
        // Arrange
        OrderDto orderDto = new OrderDto();
        orderDto.setName("Test Order");
        orderDto.setDescription("Integration Test Order");
        orderDto.setIdUser(1);

        // Act
        ResponseEntity<?> response = orderController.createOrder(orderDto);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(201, response.getStatusCode().value(), "Status code should be 201");
        Object respBody = response.getBody();
        assertNotNull(respBody, "Response body should not be null");
        assertInstanceOf(OrderDto.class, respBody, "Response body should be an OrderDto");
        OrderDto bodyDto = (OrderDto) respBody;
        assertEquals("Test Order", bodyDto.getName(), "Order name should match");
        assertEquals(State.PROCESSING, bodyDto.getState(), "Initial state should be PROCESSING");
        assertTrue(bodyDto.isActive(), "Order should be active");
        assertEquals(1, bodyDto.getIdUser(), "User ID should match");
    }

    @Test
    @DisplayName("Integration: Get Order by ID - Complete Flow")
    public void testGetOrderByIdComponentIntegration() {
        // Arrange
        OrderDto orderDto = new OrderDto();
        orderDto.setName("Order for Retrieval");
        orderDto.setDescription("Testing retrieval flow");
        orderDto.setIdUser(2);

        ResponseEntity<?> createResponse = orderController.createOrder(orderDto);
        Object createBody = createResponse.getBody();
        assertNotNull(createBody, "Created response body should not be null");
        assertInstanceOf(OrderDto.class, createBody, "Created response body should be OrderDto");
        int orderId = ((OrderDto) createBody).getId();

        // Act
        ResponseEntity<?> getResponse = orderController.showOrderById(orderId, null);

        // Assert
        assertNotNull(getResponse, "Get response should not be null");
        assertEquals(200, getResponse.getStatusCode().value(), "Status code should be 200");
        Object getBody = getResponse.getBody();
        assertNotNull(getBody, "Get response body should not be null");
        assertInstanceOf(OrderDto.class, getBody, "Get response body should be an OrderDto");
        OrderDto fetchedDto = (OrderDto) getBody;
        assertEquals(orderId, fetchedDto.getId(), "Order ID should match");
        assertEquals("Order for Retrieval", fetchedDto.getName(), "Order name should match");
    }

    @Test
    @DisplayName("Integration: Delete Order - Complete Flow")
    public void testDeleteOrderComponentIntegration() {
        // Arrange
        OrderDto orderDto = new OrderDto();
        orderDto.setName("Order to Delete");
        orderDto.setDescription("Testing deletion flow");
        orderDto.setIdUser(3);

        ResponseEntity<?> createResponse = orderController.createOrder(orderDto);
        Object createBody = createResponse.getBody();
        assertNotNull(createBody, "Created response body should not be null");
        assertInstanceOf(OrderDto.class, createBody, "Created response body should be OrderDto");
        int orderId = ((OrderDto) createBody).getId();

        // Act
        ResponseEntity<?> deleteResponse = orderController.deleteOrder(orderId);
        ResponseEntity<?> getResponse = orderController.showOrderById(orderId, null);

        // Assert
        assertNotNull(deleteResponse, "Delete response should not be null");
        assertEquals(200, deleteResponse.getStatusCode().value(), "Status code should be 200");
        assertEquals(404, getResponse.getStatusCode().value(), "Deleted order should return 404");
    }

    @Test
    @DisplayName("Integration: Mapper - DTO to Entity and back")
    public void testMapperComponentIntegration() {
        // Arrange
        OrderDto originalDto = new OrderDto();
        originalDto.setName("Mapper Test Order");
        originalDto.setDescription("Testing mapper conversion");
        originalDto.setIdUser(4);

        // Act
        Order entity = orderMapper.toEntity(originalDto);
        OrderDto convertedDto = orderMapper.toDto(entity);

        // Assert
        assertNotNull(entity, "Entity should not be null");
        assertNotNull(convertedDto, "Converted DTO should not be null");
        assertEquals(originalDto.getName(), entity.getName(), "Entity name should match");
        assertEquals(originalDto.getName(), convertedDto.getName(), "DTO name should match after conversion");
        assertEquals(originalDto.getDescription(), convertedDto.getDescription(), "Description should match");
    }

    @Test
    @DisplayName("Integration: Service Layer - Multiple Operations")
    public void testServiceLayerComponentIntegration() {
        // Arrange
        OrderDto orderDto1 = new OrderDto();
        orderDto1.setName("Service Test Order 1");
        orderDto1.setDescription("First test order");
        orderDto1.setIdUser(5);

        OrderDto orderDto2 = new OrderDto();
        orderDto2.setName("Service Test Order 2");
        orderDto2.setDescription("Second test order");
        orderDto2.setIdUser(6);

        // Act
        OrderDto created1 = orderService.createOrder(orderDto1);
        OrderDto created2 = orderService.createOrder(orderDto2);
        OrderDto retrieved = orderService.showOrderById(created1.getId());

        // Assert
        assertNotNull(created1, "First order should be created");
        assertNotNull(created2, "Second order should be created");
        assertNotNull(retrieved, "Order should be retrievable");
        assertEquals(created1.getId(), retrieved.getId(), "Retrieved order ID should match");
        assertEquals(2, orderRepository.findAll().size(), "Repository should contain exactly 2 created orders");
    }

    @Test
    @DisplayName("Integration: Order State Management")
    public void testOrderStateManagementIntegration() {
        // Arrange
        OrderDto orderDto = new OrderDto();
        orderDto.setName("State Test Order");
        orderDto.setDescription("Testing state management");
        orderDto.setIdUser(7);

        // Act
        ResponseEntity<?> response = orderController.createOrder(orderDto);
        Object respBody = response.getBody();

        // Assert
        assertNotNull(respBody, "Order should be created");
        assertInstanceOf(OrderDto.class, respBody, "Created body should be OrderDto");
        OrderDto createdOrder = (OrderDto) respBody;
        assertEquals(State.PROCESSING, createdOrder.getState(), "Initial state should be PROCESSING");
        assertTrue(createdOrder.isActive(), "Order should be created as active");
    }

    @Test
    @DisplayName("Integration: Non-existent Order Retrieval")
    public void     testNonExistentOrderRetrieval() {
        // Act
        ResponseEntity<?> response = orderController.showOrderById(99999, null);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(404, response.getStatusCode().value(), "Status code should be 404 for non-existent order");
        assertNull(response.getBody(), "Response body should be null for non-existent order");
    }

    @Test
    @DisplayName("Integration: Repository Persistence")
    public void testRepositoryPersistence() {
        // Arrange
        Order order = new Order();
        order.setName("Persistence Test Order");
        order.setDescription("Testing repository persistence");
        order.setIdUser(8);
        order.setState(State.PROCESSING);
        order.setActive(true);
        order.setId(100);

        // Act
        Order saved = orderRepository.save(order);
        var retrieved = orderRepository.findById(saved.getId());

        // Assert
        assertNotNull(saved, "Order should be saved");
        assertTrue(retrieved.isPresent(), "Order should be retrievable from repository");
        assertEquals("Persistence Test Order", retrieved.get().getName(), "Order name should persist");
        assertEquals(8, retrieved.get().getIdUser(), "User ID should persist");
    }

}
