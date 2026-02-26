package com.example.pedidoservice.messaging;

import com.example.pedidoservice.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UserServiceProducerTest {

    @Test
    void requestUserInfo_sendsMessageToExchange() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        UserServiceProducer producer = new UserServiceProducer(rabbitTemplate);

        producer.requestUserInfo(10);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(RabbitMQConfig.USER_EXCHANGE),
                org.mockito.ArgumentMatchers.eq(RabbitMQConfig.USER_REQUEST_ROUTING_KEY),
                payloadCaptor.capture()
        );

        Object payload = payloadCaptor.getValue();
        assertThat(payload).isInstanceOf(UserRequest.class);
        assertThat(((UserRequest) payload).getUserId()).isEqualTo(10);
    }
}
