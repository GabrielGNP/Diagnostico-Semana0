package com.example.pedidoservice.model;

public class Order {
    private int id;
    private String name;
    private String description;
    private int idUser;
    private State state;
    private boolean active;

    public Order() {
    }

    public Order(int id, String name, String description, int idUser, State state, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.idUser = idUser;
        this.state = state;
        this.active = active;
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
}
