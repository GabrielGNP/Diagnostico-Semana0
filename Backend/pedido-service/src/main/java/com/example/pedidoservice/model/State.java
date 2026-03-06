package com.example.pedidoservice.model;

public enum State {
    PROCESSING,
    TRAVELING_TO_WAREHOUSE,
    IN_WAREHOUSE,
    TRAVELING_TO_YOUR_HOUSE,
    ON_THE_STREET,
    DELIVERED,
    CANCELED,
}
