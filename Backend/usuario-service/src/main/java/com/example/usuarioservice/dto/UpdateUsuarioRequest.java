package com.example.usuarioservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUsuarioRequest {
    
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    
    @Email(message = "El email debe ser válido")
    private String email;
    
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasena;
    
    private Boolean activo;
}
