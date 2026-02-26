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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de usuarios.
 * Responsabilidad única: Mapear requests HTTP a casos de uso del servicio.
 */
@RestController
@RequestMapping("/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3001,http://localhost:3000}")
public class UsuarioController {

    private static final String API_PATH = "/api/v1/usuarios";
    
    private final IUsuarioService usuarioService;
    
    /**
     * GET /api/v1/usuarios
     * Obtiene todos los usuarios
     */
    @GetMapping
    public ResponseEntity<Collection<UsuarioResponse>> obtenerTodos() {
        log.info("GET {} - Obteniendo todos los usuarios", API_PATH);
        
        List<UsuarioResponse> usuarios = mapToResponses(usuarioService.obtenerTodos());

        return ResponseEntity.ok(usuarios);
    }
    
    /**
     * GET /api/v1/usuarios/{identificador}
     * Obtiene un usuario por ID o email
     */
    @GetMapping("/{identificador}")
    public ResponseEntity<UsuarioResponse> obtenerPorIdentificador(
            @PathVariable String identificador) {
        log.info("GET {}/{} - Obteniendo usuario", API_PATH, identificador);
        
        return usuarioService.obtenerPorIdentificador(identificador)
            .map(this::mapToResponse)
            .map(ResponseEntity::ok)
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
        log.info("POST {} - Creando nuevo usuario: {}", API_PATH, request.getEmail());
        
        User usuario = usuarioService.crear(request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapToResponse(usuario));
    }
    
    /**
     * PUT /api/v1/usuarios/{id}
     * Actualiza completamente un usuario
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable int id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        log.info("PUT {}/{} - Actualizando usuario", API_PATH, id);
        
        return usuarioService.actualizar(id, request)
            .map(this::mapToResponse)
            .map(ResponseEntity::ok)
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
        log.info("PATCH {}/{} - Actualizando parcialmente usuario", API_PATH, id);
        
        return usuarioService.actualizarParcial(id, request)
            .map(this::mapToResponse)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado: " + id));
    }
    
    /**
     * DELETE /api/v1/usuarios/{id}
     * Elimina un usuario
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        log.info("DELETE {}/{} - Eliminando usuario", API_PATH, id);
        
        if (!usuarioService.eliminar(id)) {
            throw new UsuarioNotFoundException("Usuario no encontrado: " + id);
        }
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Convierte una colección de usuarios a una lista de responses.
     * Método privado para facilitar testing indirecto.
     *
     * @param users Colección de usuarios del dominio
     * @return Lista de UsuarioResponse
     */
    private List<UsuarioResponse> mapToResponses(Collection<User> users) {
        return users.stream()
            .map(UsuarioResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * Convierte un usuario del dominio a DTO de respuesta.
     * Método privado para facilitar testing indirecto.
     *
     * @param user Usuario del dominio
     * @return UsuarioResponse
     */
    private UsuarioResponse mapToResponse(User user) {
        return UsuarioResponse.from(user);
    }
}
