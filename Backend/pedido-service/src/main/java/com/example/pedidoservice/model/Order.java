package com.example.pedidoservice.model;

import jakarta.persistence.*;

/**
 * Order Entity - Mapped to 'orders' table in PostgreSQL
 *
 * User Story: HU-ORD-01
 * Database: PostgreSQL
 * Table: orders
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "id_user", nullable = false)
    private Integer idUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 50)
    private State state;

    @Column(name = "active", nullable = false)
    private Boolean active;

    public Order() {
    }

    public Order(Integer id, String name, String description, Integer idUser, State state, Boolean active) {
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
