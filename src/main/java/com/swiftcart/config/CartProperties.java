package com.swiftcart.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "application.cart")
@Getter
@Setter
public class CartProperties {

    /**
     * Cart TTL in Redis; refreshed on every cart mutation (add / remove line).
     */
    private Duration ttl = Duration.ofHours(24);
}
