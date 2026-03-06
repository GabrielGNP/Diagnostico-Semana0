package com.example.usuarioservice.exception;

public class UsuarioYaExisteException extends RuntimeException {
    public UsuarioYaExisteException(String mensaje) {
        super(mensaje);
    }
    
    public UsuarioYaExisteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
