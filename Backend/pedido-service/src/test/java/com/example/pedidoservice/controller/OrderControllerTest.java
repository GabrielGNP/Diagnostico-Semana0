package com.example.pedidoservice.controller;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.dto.OrderStateUpdateDto;
import com.example.pedidoservice.dto.OrderWithUserDto;
import com.example.pedidoservice.exception.OrderNotFoundException;
import com.example.pedidoservice.model.State;
import com.example.pedidoservice.service.OrderService;
import com.example.pedidoservice.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private OrderService orderService = Mockito.mock(OrderService.class);

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createOrder_returns201AndLocation() throws Exception {
        OrderDto input = new OrderDto(null, "Test Order", "Desc", 1, State.PROCESSING, true);
        OrderDto created = new OrderDto(1, "Test Order", "Desc", 1, State.PROCESSING, true);

        when(orderService.createOrder(any(OrderDto.class))).thenReturn(created);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void showOrderById_found_returns200() throws Exception {
        OrderDto dto = new OrderDto(1, "Test", "Desc", 2, State.PROCESSING, true);
        when(orderService.showOrderById(1)).thenReturn(dto);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void showOrderById_notFound_returns404() throws Exception {
        when(orderService.showOrderById(999)).thenThrow(new OrderNotFoundException("Pedido con ID 999 no encontrado"));

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    @Test
    void listOrders_returnsList() throws Exception {
        OrderDto a = new OrderDto(1, "A", "d", 1, State.PROCESSING, true);
        OrderDto b = new OrderDto(2, "B", "d", 2, State.PROCESSING, true);
        when(orderService.findAllActiveOrders()).thenReturn(List.of(a, b));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void changeState_returnsUpdatedOrder() throws Exception {
        OrderStateUpdateDto dto = new OrderStateUpdateDto(State.DELIVERED);
        OrderDto updated = new OrderDto(1, "X", "d", 1, State.DELIVERED, true);

        when(orderService.changeStateOrder(1, State.DELIVERED)).thenReturn(updated);

        mockMvc.perform(patch("/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("DELIVERED"));
    }

    @Test
    void showOrderWithUser_returnsOrderWithUser() throws Exception {
        OrderWithUserDto withUser = new OrderWithUserDto();
        withUser.setId(1);
        withUser.setName("Test");
        when(orderService.getOrderWithUserInfo(1)).thenReturn(withUser);

        mockMvc.perform(get("/orders/1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test"));
    }

}
