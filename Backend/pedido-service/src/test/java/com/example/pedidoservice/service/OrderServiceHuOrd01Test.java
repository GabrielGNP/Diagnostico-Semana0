package com.example.pedidoservice.service;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.mapper.OrderMapper;
import com.example.pedidoservice.model.Order;
import com.example.pedidoservice.model.State;
import com.example.pedidoservice.repository.OrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ✅ GREEN PHASE → REFACTOR - Tests para HU-ORD-01: Listado de pedidos desde PostgreSQL
 *
 * User Story: HU-ORD-01
 * Título: Listado completo de pedidos almacenados en PostgreSQL
 * 
 * Estado TDD: REFACTOR ♻️
 * - Tests pasando ✅
 * - Código implementado ✅
 * - Refactorización aplicada para mejorar legibilidad y mantenibilidad
 *
 * Criterios de Aceptación cubiertos:
 * - CA-01: Listado exitoso con pedidos activos
 * - CA-02: Listado vacío cuando no hay pedidos activos
 *
 * Requisitos Funcionales:
 * - FR-ORD-01-01: Recuperar pedidos de tabla orders en PostgreSQL
 * - FR-ORD-01-02: Incluir campos id, name, description, idUser, state, active
 * 
 * Nota: Según HU-DB-09, solo se deben retornar pedidos con active=true
 */
@DisplayName("♻️ REFACTOR - HU-ORD-01: Listado de Pedidos Active-Only")
class OrderServiceHuOrd01Test {

    private OrderJpaRepository orderJpaRepository;

    private OrderMapper orderMapper;

    private OrderService orderService;

    @BeforeEach
    void setupService() {
        // Crear mocks manualmente para evitar dependencia de la inicialización automática
        orderJpaRepository = mock(OrderJpaRepository.class);
        orderMapper = mock(OrderMapper.class);
        // Construir el servicio inyectando los mocks
        orderService = new OrderService(orderJpaRepository, orderMapper, null);
    }

    // ========== HELPER METHODS ==========

    /**
     * Creates a test order with the specified parameters.
     * Helper method to reduce code duplication and improve test readability.
     */
    private Order createTestOrder(int id, String name, String description, int idUser, State state, boolean active) {
        return new Order(id, name, description, idUser, state, active);
    }

    /**
     * Configures the OrderMapper mock to convert Order entities to DTOs.
     * Helper method to centralize mock configuration.
     */
    private void setupMapperMock() {
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
    }

    // ========== TESTS ==========

    /**
     * ♻️ REFACTOR TEST #1
     *
     * CA-01: Listado exitoso con pedidos activos
     *
     * GIVEN: Existen 2 pedidos activos y 2 inactivos en PostgreSQL
     * WHEN: Se invoca findAllActiveOrders()
     * THEN: Se retornan SOLO los 2 pedidos activos (active=true)
     *
     * Mejoras aplicadas en REFACTOR:
     * - Uso de métodos helper para crear pedidos (DRY principle)
     * - Configuración centralizada del mapper mock
     * - Nombres más descriptivos en las assertions
     */
    @Test
    @DisplayName("♻️ REFACTOR: findAllActiveOrders debe retornar solo pedidos activos (active=true)")
    void findAllActiveOrders_shouldReturnOnlyActiveOrders() {
        // GIVEN - Setup: 2 pedidos activos, 2 inactivos
        Order activeOrder1 = createTestOrder(1, "Pedido Activo 1", "Descripción 1", 5, State.PROCESSING, true);
        Order activeOrder2 = createTestOrder(2, "Pedido Activo 2", "Descripción 2", 10, State.DELIVERED, true);

        // El servicio usa orderJpaRepository.findAllActive(), por lo que el mock debe devolver solo los activos
        when(orderJpaRepository.findAllActive()).thenReturn(Arrays.asList(activeOrder1, activeOrder2));

        setupMapperMock();

        // WHEN - Ejecutar método bajo test
        List<OrderDto> result = orderService.findAllActiveOrders();
        
        // THEN - Verificaciones
        assertNotNull(result, "El resultado no debe ser null");
        assertEquals(2, result.size(), "Debe retornar exactamente 2 pedidos activos");

        // Verificar que SOLO contiene pedidos activos
        assertTrue(result.stream().allMatch(OrderDto::isActive),
                   "Todos los pedidos retornados deben tener active=true");

        // Verificar que NO contiene pedidos inactivos
        boolean containsInactiveOrder = result.stream()
            .anyMatch(dto -> dto.getId() == 3 || dto.getId() == 4);
        assertFalse(containsInactiveOrder, "NO debe incluir pedidos con active=false");

        // Verificar interacción con repository
        verify(orderJpaRepository, times(1)).findAllActive();
        verify(orderMapper, times(2)).toDto(any(Order.class)); // Solo 2 mapeos (pedidos activos)
    }

    /**
     * ♻️ REFACTOR TEST #2
     *
     * CA-02: Listado vacío cuando no hay pedidos activos
     * 
     * GIVEN: Solo existen pedidos inactivos en PostgreSQL
     * WHEN: Se invoca findAllActiveOrders()
     * THEN: Se retorna lista vacía []
     * 
     * Mejoras aplicadas en REFACTOR:
     * - Uso de métodos helper (createTestOrder)
     * - Assertions más expresivas
     */
    @Test
    @DisplayName("♻️ REFACTOR: findAllActiveOrders debe retornar lista vacía si no hay pedidos activos")
    void findAllActiveOrders_shouldReturnEmptyListWhenNoActiveOrders() {
        // GIVEN - Solo hay pedidos inactivos

        when(orderJpaRepository.findAllActive()).thenReturn(Collections.emptyList());
        setupMapperMock();

        // WHEN
        List<OrderDto> result = orderService.findAllActiveOrders();
        
        // THEN
        assertNotNull(result, "El resultado no debe ser null");
        assertTrue(result.isEmpty(), "Debe retornar lista vacía cuando solo hay pedidos inactivos");
        assertEquals(0, result.size(), "El tamaño de la lista debe ser 0");

        verify(orderJpaRepository, times(1)).findAllActive();
        verify(orderMapper, never()).toDto(any(Order.class)); // No se debe mapear ningún pedido
    }

    /**
     * ♻️ REFACTOR TEST #3
     *
     * Edge Case: Base de datos completamente vacía
     * 
     * GIVEN: No existen pedidos en PostgreSQL (tabla vacía)
     * WHEN: Se invoca findAllActiveOrders()
     * THEN: Se retorna lista vacía []
     *
     * Mejoras aplicadas en REFACTOR:
     * - Configuración centralizada del mock
     * - Assertions más descriptivas
     */
    @Test
    @DisplayName("♻️ REFACTOR: findAllActiveOrders debe retornar lista vacía cuando BD está vacía")
    void findAllActiveOrders_shouldReturnEmptyListWhenDatabaseEmpty() {
        // GIVEN - BD vacía
        when(orderJpaRepository.findAllActive()).thenReturn(Collections.emptyList());
        setupMapperMock();

        // WHEN
        List<OrderDto> result = orderService.findAllActiveOrders();
        
        // THEN
        assertNotNull(result, "El resultado no debe ser null");
        assertTrue(result.isEmpty(), "Debe retornar lista vacía cuando BD está vacía");
        assertEquals(0, result.size(), "El tamaño debe ser 0");

        verify(orderJpaRepository, times(1)).findAllActive();
        verify(orderMapper, never()).toDto(any(Order.class)); // Sin datos, no hay mapeos
    }
}
