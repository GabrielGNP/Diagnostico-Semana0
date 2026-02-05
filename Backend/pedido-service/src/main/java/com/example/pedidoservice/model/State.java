package com.example.pedidoservice.model;

public enum State {
    PROCESSING,
    TRAVELINGTOWAREHOUSE,
    IN_WAREHOUSE,
    TRAVELINGTOYOURHOUSE,
    ONTHESTREET,
    DELIVERED,
    CANCELED
}
