package com.example.usuarioservice.controller;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.dto.UsuarioResponse;
import com.example.usuarioservice.exception.UsuarioNotFoundException;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.service.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de usuarios.
 * Responsabilidad única: Mapear requests HTTP a casos de uso del servicio.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3001,http://localhost:3000}")
public class UsuarioController {
    
    private final IUsuarioService usuarioService;
    
    /**
     * GET /api/v1/usuarios
     * Obtiene todos los usuarios
     */
    @GetMapping
    public ResponseEntity<Collection<UsuarioResponse>> obtenerTodos() {
        log.info("GET /api/v1/usuarios - Obteniendo todos los usuarios");
        
        Collection<UsuarioResponse> usuarios = usuarioService.obtenerTodos()
            .stream()
            .map(UsuarioResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(usuarios);
    }
    
    /**
     * GET /api/v1/usuarios/{identificador}
     * Obtiene un usuario por ID o email
     */
    @GetMapping("/{identificador}")
    public ResponseEntity<UsuarioResponse> obtenerPorIdentificador(
            @PathVariable String identificador) {
        log.info("GET /api/v1/usuarios/{} - Obteniendo usuario", identificador);
        
        return usuarioService.obtenerPorIdentificador(identificador)
            .map(u -> ResponseEntity.ok(UsuarioResponse.from(u)))
            .orElseThrow(() -> new UsuarioNotFoundException(
                "Usuario no encontrado: " + identificador
            ));
    }
    
    /**
     * POST /api/v1/usuarios
     * Crea un nuevo usuario
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody CreateUsuarioRequest request) {
        log.info("POST /api/v1/usuarios - Creando nuevo usuario: {}", request.getEmail());
        
        User usuario = usuarioService.crear(request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(UsuarioResponse.from(usuario));
    }
    
    /**
     * PUT /api/v1/usuarios/{id}
     * Actualiza completamente un usuario
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable int id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        log.info("PUT /api/v1/usuarios/{} - Actualizando usuario", id);
        
        return usuarioService.actualizar(id, request)
            .map(u -> ResponseEntity.ok(UsuarioResponse.from(u)))
            .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado: " + id));
    }
    
    /**
     * PATCH /api/v1/usuarios/{id}
     * Actualiza parcialmente un usuario
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarParcial(
            @PathVariable int id,
            @RequestBody UpdateUsuarioRequest request) {
        log.info("PATCH /api/v1/usuarios/{} - Actualizando parcialmente usuario", id);
        
        return usuarioService.actualizarParcial(id, request)
            .map(u -> ResponseEntity.ok(UsuarioResponse.from(u)))
            .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado: " + id));
    }
    
    /**
     * DELETE /api/v1/usuarios/{id}
     * Elimina un usuario
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        log.info("DELETE /api/v1/usuarios/{} - Eliminando usuario", id);
        
        if (!usuarioService.eliminar(id)) {
            throw new UsuarioNotFoundException("Usuario no encontrado: " + id);
        }
        
        return ResponseEntity.noContent().build();
    }
}
