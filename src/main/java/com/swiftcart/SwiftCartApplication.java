package com.swiftcart;

import com.swiftcart.config.CartProperties;
import com.swiftcart.config.StripeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({CartProperties.class, StripeProperties.class})
public class SwiftCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(SwiftCartApplication.class, args);
    }
}
