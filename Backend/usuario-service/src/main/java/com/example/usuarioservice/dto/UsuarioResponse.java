package com.example.usuarioservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.usuarioservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {
    private Integer id;
    
    @JsonProperty("name")
    private String nombre;
    
    @JsonProperty("mail")
    private String email;
    
    @JsonProperty("active")
    private boolean activo;
    
    /**
     * Factory method para convertir User a Response
     */
    public static UsuarioResponse from(User usuario) {
        return UsuarioResponse.builder()
            .id(usuario.getId())
            .nombre(usuario.getName())
            .email(usuario.getMail())
            .activo(usuario.isActive())
            .build();
    }
}
