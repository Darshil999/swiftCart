package com.swiftcart.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory stand-in for {@link StringRedisTemplate} so integration tests run without Docker/Redis.
 * Production uses real Redis via Spring Boot auto-configuration.
 */
@TestConfiguration
public class RedisTestMockConfig {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = Mockito.mock(ValueOperations.class);

        Mockito.when(ops.get(Mockito.anyString()))
                .thenAnswer(invocation -> store.get(invocation.getArgument(0)));

        Mockito.doAnswer(invocation -> {
                    String key = invocation.getArgument(0);
                    String value = invocation.getArgument(1);
                    store.put(key, value);
                    return null;
                })
                .when(ops)
                .set(Mockito.anyString(), Mockito.anyString(), Mockito.any(Duration.class));

        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        Mockito.when(template.opsForValue()).thenReturn(ops);
        Mockito.when(template.delete(Mockito.anyString()))
                .thenAnswer(invocation -> store.remove(invocation.getArgument(0)) != null);
        return template;
    }
}
