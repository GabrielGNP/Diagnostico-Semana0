package com.example.pedidoservice.messaging;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceConsumerTest {

    @Test
    void receiveUserResponse_delegatesToCache() {
        UserResponseCache cache = Mockito.mock(UserResponseCache.class);
        UserServiceConsumer consumer = new UserServiceConsumer(cache);

        UserResponse resp = new UserResponse(10, "John", "j@x.com", true);
        consumer.receiveUserResponse(resp);

        verify(cache).store(resp);
    }

    @Test
    void getUserResponse_delegatesToCache() {
        UserResponseCache cache = Mockito.mock(UserResponseCache.class);
        UserServiceConsumer consumer = new UserServiceConsumer(cache);

        UserResponse expected = new UserResponse(5, "A", "a@x.com", true);
        when(cache.awaitResponse(5, 1000)).thenReturn(expected);

        UserResponse actual = consumer.getUserResponse(5, 1000);
        assertThat(actual).isSameAs(expected);
    }
}
