package com.example.pedidoservice.dto;

import com.example.pedidoservice.model.State;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;
    @NotBlank(message = "El campo 'name' es requerido")
    private String name;
    @NotNull(message = "El campo 'description' es requerido")
    private String description;
    @NotNull(message = "El campo 'idUser' es requerido")
    @Min(value = 1, message = "El campo 'idUser' debe ser un valor positivo válido (mayor que cero)")
    private Integer idUser;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private State state;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean active;

    public OrderDto() {
    }

    public OrderDto(Integer id, String name, String description, Integer idUser, State state, Boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.idUser = idUser;
        this.state = state;
        this.active = active;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
