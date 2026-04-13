package com.swiftcart.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
public class StripeConfig {

    private final StripeProperties stripeProperties;

    @PostConstruct
    public void initApiKey() {
        if (StringUtils.hasText(stripeProperties.getSecretKey())) {
            Stripe.apiKey = stripeProperties.getSecretKey().trim();
        }
    }
}
