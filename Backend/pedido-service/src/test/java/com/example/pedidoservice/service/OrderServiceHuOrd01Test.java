package com.example.pedidoservice.service;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.mapper.OrderMapper;
import com.example.pedidoservice.model.Order;
import com.example.pedidoservice.model.State;
import com.example.pedidoservice.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 🔴 RED PHASE - Tests para HU-ORD-01: Listado de pedidos desde PostgreSQL
 * 
 * User Story: HU-ORD-01
 * Título: Listado completo de pedidos almacenados en PostgreSQL
 * 
 * IMPORTANTE TDD:
 * - Este test debe COMPILAR ✅ (sintaxis correcta)
 * - Este test debe FALLAR ❌ al ejecutarse (método findAllActiveOrders() NO EXISTE)
 * 
 * Criterios de Aceptación cubiertos:
 * - CA-01: Listado exitoso con pedidos existentes
 * - CA-02: Listado vacío cuando no hay pedidos
 * 
 * Requisitos Funcionales:
 * - FR-ORD-01-01: Recuperar pedidos de tabla orders en PostgreSQL
 * - FR-ORD-01-02: Incluir campos id, name, description, idUser, state, active
 * 
 * Nota: Según HU-DB-09, solo se deben retornar pedidos con active=true
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("🔴 RED - HU-ORD-01: Listado de Pedidos Active-Only")
class OrderServiceHuOrd01Test {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    /**
     * 🔴 RED TEST #1
     * 
     * CA-01: Listado exitoso con pedidos activos
     * 
     * GIVEN: Existen 2 pedidos activos y 1 inactivo en PostgreSQL
     * WHEN: Se invoca findAllActiveOrders()
     * THEN: Se retornan SOLO los 2 pedidos activos (active=true)
     * 
     * ❌ DEBE FALLAR porque el método findAllActiveOrders() NO EXISTE en OrderService
     */
    @Test
    @DisplayName("RED: findAllActiveOrders debe retornar solo pedidos activos (active=true)")
    void findAllActiveOrders_shouldReturnOnlyActiveOrders() {
        // GIVEN - Setup: 2 pedidos activos, 1 inactivo
        Order activeOrder1 = new Order(1, "Pedido Activo 1", "Descripción 1", 5, State.PROCESSING, true);
        Order activeOrder2 = new Order(2, "Pedido Activo 2", "Descripción 2", 10, State.DELIVERED, true);
        Order inactiveOrder = new Order(3, "Pedido Eliminado", "Soft-deleted", 15, State.CANCELED, false);
        
        // Mock repository retorna todos (incluyendo inactivos)
        List<Order> allOrders = Arrays.asList(activeOrder1, activeOrder2, inactiveOrder);
        when(orderRepository.findAll()).thenReturn(allOrders);
        
        // Mock mapper convierte Orders a DTOs
        when(orderMapper.toDto(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return new OrderDto(
                order.getId(),
                order.getName(),
                order.getDescription(),
                order.getIdUser(),
                order.getState(),
                order.isActive()
            );
        });
        
        // WHEN - Ejecutar método bajo test
        // ❌ ESTE MÉTODO NO EXISTE - El test debe FALLAR aquí
        List<OrderDto> result = orderService.findAllActiveOrders();
        
        // THEN - Verificaciones
        assertNotNull(result, "El resultado no debe ser null");
        assertEquals(2, result.size(), "Debe retornar solo 2 pedidos activos");
        
        // Verificar que NO contiene el pedido inactivo
        boolean containsInactive = result.stream()
            .anyMatch(dto -> dto.getId() == 3);
        assertFalse(containsInactive, "NO debe incluir pedidos con active=false");
        
        // Verificar que llamó al repository
        verify(orderRepository).findAll();
    }

    /**
     * 🔴 RED TEST #2
     * 
     * CA-02: Listado vacío cuando no hay pedidos activos
     * 
     * GIVEN: No existen pedidos activos en PostgreSQL
     * WHEN: Se invoca findAllActiveOrders()
     * THEN: Se retorna lista vacía []
     * 
     * ❌ DEBE FALLAR porque el método findAllActiveOrders() NO EXISTE
     */
    @Test
    @DisplayName("RED: findAllActiveOrders debe retornar lista vacía si no hay pedidos activos")
    void findAllActiveOrders_shouldReturnEmptyListWhenNoActiveOrders() {
        // GIVEN - Solo hay pedidos inactivos
        Order inactiveOrder1 = new Order(1, "Pedido Eliminado 1", "Soft-deleted", 5, State.CANCELED, false);
        Order inactiveOrder2 = new Order(2, "Pedido Eliminado 2", "Soft-deleted", 10, State.CANCELED, false);
        
        when(orderRepository.findAll()).thenReturn(Arrays.asList(inactiveOrder1, inactiveOrder2));
        
        // WHEN
        // ❌ ESTE MÉTODO NO EXISTE - El test debe FALLAR aquí
        List<OrderDto> result = orderService.findAllActiveOrders();
        
        // THEN
        assertNotNull(result, "El resultado no debe ser null");
        assertTrue(result.isEmpty(), "Debe retornar lista vacía cuando no hay pedidos activos");
        
        verify(orderRepository).findAll();
    }

    /**
     * 🔴 RED TEST #3
     * 
     * Edge Case: Base de datos completamente vacía
     * 
     * GIVEN: No existen pedidos en PostgreSQL (tabla vacía)
     * WHEN: Se invoca findAllActiveOrders()
     * THEN: Se retorna lista vacía []
     */
    @Test
    @DisplayName("RED: findAllActiveOrders debe retornar lista vacía cuando BD está vacía")
    void findAllActiveOrders_shouldReturnEmptyListWhenDatabaseEmpty() {
        // GIVEN - BD vacía
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        
        // WHEN
        // ❌ ESTE MÉTODO NO EXISTE - El test debe FALLAR aquí
        List<OrderDto> result = orderService.findAllActiveOrders();
        
        // THEN
        assertNotNull(result, "El resultado no debe ser null");
        assertTrue(result.isEmpty(), "Debe retornar lista vacía cuando BD está vacía");
        
        verify(orderRepository).findAll();
    }
}
