package com.example.pedidoservice.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResponseTest {

    @Test
    void defaultConstructor_and_settersGetters_work() {
        ErrorResponse er = new ErrorResponse();

        LocalDateTime now = LocalDateTime.now();
        er.setTimestamp(now);
        er.setStatus(400);
        er.setError("Bad Request");
        er.setMessage("Invalid payload");
        er.setPath("/orders");
        er.setValidationErrors(Map.of("name", "must not be empty"));

        assertThat(er.getTimestamp()).isEqualTo(now);
        assertThat(er.getStatus()).isEqualTo(400);
        assertThat(er.getError()).isEqualTo("Bad Request");
        assertThat(er.getMessage()).isEqualTo("Invalid payload");
        assertThat(er.getPath()).isEqualTo("/orders");
        assertThat(er.getValidationErrors()).containsEntry("name", "must not be empty");
    }

    @Test
    void builder_populates_all_fields() {
        LocalDateTime t = LocalDateTime.of(2026, 2, 26, 12, 0);
        Map<String, String> map = Map.of("idUser", "must be positive");

        ErrorResponse er = ErrorResponse.builder()
                .timestamp(t)
                .status(422)
                .error("Unprocessable Entity")
                .message("Validation failed")
                .path("/orders")
                .validationErrors(map)
                .build();

        assertThat(er.getTimestamp()).isEqualTo(t);
        assertThat(er.getStatus()).isEqualTo(422);
        assertThat(er.getError()).isEqualTo("Unprocessable Entity");
        assertThat(er.getMessage()).isEqualTo("Validation failed");
        assertThat(er.getPath()).isEqualTo("/orders");
        assertThat(er.getValidationErrors()).isSameAs(map);
    }
}
