package com.example.pedidoservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
        webRequest = Mockito.mock(WebRequest.class);
        Mockito.when(webRequest.getDescription(false)).thenReturn("uri=/test");
    }

    @Test
    void handleOrderNotFound_returns404() {
        OrderNotFoundException ex = new OrderNotFoundException("Pedido con ID 999 no encontrado");
        ResponseEntity<ErrorResponse> resp = handler.handleOrderNotFound(ex, webRequest);

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getError()).isEqualTo("Not Found");
        assertThat(body.getMessage()).contains("999");
        assertThat(body.getPath()).isEqualTo("/test");
    }

    @Test
    void handleIllegalArgument_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad param");
        ResponseEntity<ErrorResponse> resp = handler.handleIllegalArgument(ex, webRequest);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getError()).isEqualTo("Bad Request");
        assertThat(body.getMessage()).isEqualTo("Bad param");
    }

    @Test
    void handleValidationErrors_returns400_withValidationMap() {
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("orderDto", "name", "must not be null");
        Mockito.when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> resp = handler.handleValidationErrors(ex, webRequest);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getError()).isEqualTo("Validation Failed");
        assertThat(body.getValidationErrors()).containsKey("name");
    }

    @Test
    void handleHttpMessageNotReadable_returns400() {
        HttpMessageNotReadableException ex = Mockito.mock(HttpMessageNotReadableException.class);
        ResponseEntity<ErrorResponse> resp = handler.handleHttpMessageNotReadable(ex, webRequest);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Request body is missing or malformed");
    }

    @Test
    void handleOrderCreation_returns500() {
        OrderCreationException ex = new OrderCreationException("Failed to persist order");
        ResponseEntity<ErrorResponse> resp = handler.handleOrderCreation(ex, webRequest);

        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getError()).isEqualTo("Internal Server Error");
        assertThat(body.getMessage()).contains("Failed to persist order");
    }

    @Test
    void handleGlobalException_returns500_generic() {
        Exception ex = new RuntimeException("Boom");
        ResponseEntity<ErrorResponse> resp = handler.handleGlobalException(ex, webRequest);

        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("An unexpected error occurred");
    }

}
