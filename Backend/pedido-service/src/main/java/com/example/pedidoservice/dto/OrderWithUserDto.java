package com.example.pedidoservice.dto;

import com.example.pedidoservice.messaging.UserResponse;
import com.example.pedidoservice.model.State;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderWithUserDto {
    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("idUser")
    private int idUser;

    @JsonProperty("state")
    private State state;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("user")
    private UserResponse user;

    public OrderWithUserDto() {
    }

    public OrderWithUserDto(int id, String name, String description, int idUser, State state, boolean active, UserResponse user) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.idUser = idUser;
        this.state = state;
        this.active = active;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "OrderWithUserDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", idUser=" + idUser +
                ", state=" + state +
                ", active=" + active +
                ", user=" + user +
                '}';
    }
}
