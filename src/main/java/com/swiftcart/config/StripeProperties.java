package com.swiftcart.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Stripe test-mode keys and Checkout URLs. Override via env or application-local.yml in dev.
 */
@ConfigurationProperties(prefix = "application.stripe")
@Getter
@Setter
public class StripeProperties {

    /**
     * Secret API key (sk_test_...).
     */
    private String secretKey = "";

    /**
     * Signing secret for webhook endpoint (whsec_...).
     */
    private String webhookSecret = "";

    /**
     * Public URL after successful payment. Use {CHECKOUT_SESSION_ID} where Stripe should substitute the session id.
     */
    private String successUrl = "http://localhost:3000/checkout/success?session_id={CHECKOUT_SESSION_ID}";

    /**
     * Public URL if user cancels Checkout.
     */
    private String cancelUrl = "http://localhost:3000/checkout/cancel";

    /**
     * ISO currency code for Checkout line items (e.g. usd).
     */
    private String currency = "usd";
}
