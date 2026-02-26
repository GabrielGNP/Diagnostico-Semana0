package com.example.pedidoservice.mapper;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.mapper.OrderMapper;
import com.example.pedidoservice.model.Order;
import com.example.pedidoservice.model.State;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void toDto_convertsEntityToDto() {
        Order entity = new Order(1, "Name", "Desc", 5, State.PROCESSING, true);

        OrderDto dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getName()).isEqualTo("Name");
        assertThat(dto.getDescription()).isEqualTo("Desc");
        assertThat(dto.getIdUser()).isEqualTo(5);
        assertThat(dto.getState()).isEqualTo(State.PROCESSING);
        assertThat(dto.isActive()).isTrue();
    }

    @Test
    void toDto_null_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toEntity_convertsDtoToEntity() {
        OrderDto dto = new OrderDto(2, "Other", "D", 3, State.DELIVERED, false);

        Order entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2);
        assertThat(entity.getName()).isEqualTo("Other");
        assertThat(entity.getDescription()).isEqualTo("D");
        assertThat(entity.getIdUser()).isEqualTo(3);
        assertThat(entity.getState()).isEqualTo(State.DELIVERED);
        assertThat(entity.isActive()).isFalse();
    }

    @Test
    void toEntity_null_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }
}
