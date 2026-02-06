package com.example.pedidoservice.dto;

import com.example.pedidoservice.model.State;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private int id;
    private String name;
    private String description;
    private int idUser;
    private State state;
    private boolean active;
}
