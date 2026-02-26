package com.example.pedidoservice.messaging;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RabbitMQUserInfoClientTest {

    @Test
    void fetchUserInfo_invokesProducerAndConsumer_returnsResponse() {
        UserServiceProducer producer = Mockito.mock(UserServiceProducer.class);
        UserServiceConsumer consumer = Mockito.mock(UserServiceConsumer.class);
        RabbitMQUserInfoClient client = new RabbitMQUserInfoClient(producer, consumer);

        UserResponse expected = new UserResponse(7, "Name", "n@x.com", true);
        when(consumer.getUserResponse(7, 3000)).thenReturn(expected);

        UserResponse actual = client.fetchUserInfo(7, 3000);

        verify(producer).requestUserInfo(7);
        verify(consumer).getUserResponse(7, 3000);
        assertThat(actual).isSameAs(expected);
    }

    @Test
    void fetchUserInfo_whenProducerThrows_returnsNull() {
        UserServiceProducer producer = Mockito.mock(UserServiceProducer.class);
        UserServiceConsumer consumer = Mockito.mock(UserServiceConsumer.class);
        RabbitMQUserInfoClient client = new RabbitMQUserInfoClient(producer, consumer);

        doThrow(new RuntimeException("boom")).when(producer).requestUserInfo(8);

        UserResponse actual = client.fetchUserInfo(8, 1000);

        // producer was invoked and threw; method should catch and return null
        verify(producer).requestUserInfo(8);
        assertThat(actual).isNull();
    }
}
