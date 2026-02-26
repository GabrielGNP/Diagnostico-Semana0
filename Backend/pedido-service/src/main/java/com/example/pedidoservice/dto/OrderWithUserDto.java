package com.example.pedidoservice.dto;

import com.example.pedidoservice.messaging.UserResponse;
import com.example.pedidoservice.model.State;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderWithUserDto {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("idUser")
    private Integer idUser;

    @JsonProperty("state")
    private State state;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("user")
    private UserResponse user;

    public OrderWithUserDto() {
    }

    public OrderWithUserDto(Integer id, String name, String description, Integer idUser, State state, Boolean active, UserResponse user) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.idUser = idUser;
        this.state = state;
        this.active = active;
        this.user = user;
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
