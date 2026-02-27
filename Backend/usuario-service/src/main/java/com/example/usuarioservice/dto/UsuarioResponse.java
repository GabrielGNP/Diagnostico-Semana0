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
     * Factory method para convertir User a Response.
     * Maneja null-safety para User y sus campos.
     *
     * @param usuario Usuario a convertir (puede ser null)
     * @return UsuarioResponse con valores nulos tratados adecuadamente
     * @throws IllegalArgumentException si usuario es null
     */
    public static UsuarioResponse from(User usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser null");
        }

        return UsuarioResponse.builder()
            .id(usuario.getId())
            .nombre(usuario.getName() != null ? usuario.getName() : "")
            .email(usuario.getMail() != null ? usuario.getMail() : "")
            .activo(usuario.isActive())
            .build();
    }

    /**
     * Factory method alternativo que retorna Optional para casos donde null es aceptable.
     *
     * @param usuario Usuario a convertir (puede ser null)
     * @return Optional con UsuarioResponse, vacío si usuario es null
     */
    public static java.util.Optional<UsuarioResponse> fromOptional(User usuario) {
        if (usuario == null) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(from(usuario));
    }
}
