package com.example.pedidoservice.dto;

import com.example.pedidoservice.model.State;
import jakarta.validation.constraints.NotNull;

public class OrderStateUpdateDto {
    @NotNull(message = "El campo 'state' es requerido")
    private State state;

    public OrderStateUpdateDto() {}

    public OrderStateUpdateDto(State state) { this.state = state; }

    public State getState() { return state; }

    public void setState(State state) { this.state = state; }
}
