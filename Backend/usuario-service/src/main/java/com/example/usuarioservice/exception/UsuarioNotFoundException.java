package com.example.usuarioservice.exception;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(String mensaje) {
        super(mensaje);
    }
    
    public UsuarioNotFoundException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
