package com.example.pedidoservice.messaging;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class UserResponseCacheTest {

    @Test
    void store_and_awaitResponse_success() throws Exception {
        UserResponseCache cache = new UserResponseCache();
        AtomicReference<UserResponse> result = new AtomicReference<>();

        Thread waiter = new Thread(() -> {
            UserResponse r = cache.awaitResponse(10, 2000);
            result.set(r);
        });

        waiter.start();

        // give waiter a moment to start waiting
        Thread.sleep(100);

        UserResponse resp = new UserResponse(10, "Test", "t@x.com", true);
        cache.store(resp);

        waiter.join(1000);
        assertThat(result.get()).isNotNull();
        assertThat(result.get().getId()).isEqualTo(10);
    }

    @Test
    void store_null_isIgnored() {
        UserResponseCache cache = new UserResponseCache();
        cache.store(null);
        // nothing to assert other than no exceptions; ensure awaitResponse times out
        UserResponse res = cache.awaitResponse(999, 50);
        assertThat(res).isNull();
    }

    @Test
    void awaitResponse_timeout_returnsNull() {
        UserResponseCache cache = new UserResponseCache();
        UserResponse res = cache.awaitResponse(12345, 100);
        assertThat(res).isNull();
    }

    @Test
    void awaitResponse_interrupted_returnsNull_and_setsInterruptedFlag() throws Exception {
        UserResponseCache cache = new UserResponseCache();
        AtomicReference<UserResponse> result = new AtomicReference<>();
        AtomicBoolean interruptedFlag = new AtomicBoolean(false);

        Thread waiter = new Thread(() -> {
            UserResponse r = cache.awaitResponse(55, 5000);
            result.set(r);
            interruptedFlag.set(Thread.currentThread().isInterrupted());
        });

        waiter.start();
        Thread.sleep(100);
        waiter.interrupt();
        waiter.join(1000);

        assertThat(result.get()).isNull();
        assertThat(interruptedFlag.get()).isTrue();
    }
}
