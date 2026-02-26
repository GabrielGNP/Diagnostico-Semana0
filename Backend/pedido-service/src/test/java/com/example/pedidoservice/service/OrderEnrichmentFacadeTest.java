package com.example.pedidoservice.service;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.dto.OrderWithUserDto;
import com.example.pedidoservice.messaging.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEnrichmentFacadeTest {

    @Mock
    private IUserEnrichmentClient userEnrichmentClient;

    @InjectMocks
    private OrderEnrichmentFacade orderEnrichmentFacade;

    @Test
    @DisplayName("UE-01 - Enriquecer orden con datos de usuario exitosamente")
    @Tag("critical")
    void enrich_shouldReturnOrderWithUser_whenUserServiceReturnsData() {
        // Arrange
        OrderDto orderDto = new OrderDto(1, "Item", "Desc", 10, null, true);
        UserResponse user = new UserResponse(10, "John", "john@test.com", true);
        when(userEnrichmentClient.fetchUserInfo(10)).thenReturn(user);

        // Act
        OrderWithUserDto result = orderEnrichmentFacade.enrich(orderDto);

        // Assert
        assertNotNull(result);
        assertEquals(orderDto.getId(), result.getId());
        assertEquals(orderDto.getName(), result.getName());
        assertEquals(orderDto.getIdUser(), result.getIdUser());
        assertNotNull(result.getUser());
        assertEquals(10, result.getUser().getId());
        assertEquals("John", result.getUser().getName());
    }

    @Test
    @DisplayName("UE-02 - Enriquecer orden null retorna null")
    @Tag("high")
    void enrich_shouldReturnNull_whenOrderDtoIsNull() {
        // Act
        OrderWithUserDto result = orderEnrichmentFacade.enrich(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("UE-03 - Enriquecer orden cuando el servicio de usuario falla")
    @Tag("high")
    void enrich_shouldReturnOrderWithNullUser_whenClientThrows() {
        // Arrange
        OrderDto orderDto = new OrderDto(2, "Other", "Desc2", 999, null, true);
        doThrow(new RuntimeException("user service down")).when(userEnrichmentClient).fetchUserInfo(999);

        // Act
        OrderWithUserDto result = orderEnrichmentFacade.enrich(orderDto);

        // Assert
        assertNotNull(result);
        assertEquals(orderDto.getId(), result.getId());
        assertNull(result.getUser(), "User should be null when enrichment fails");
    }
}
