package com.swiftcart.controller;

import com.swiftcart.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stripe sends signed webhook events here. No JWT — authenticity is verified via {@code Stripe-Signature}.
 */
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private static final String STRIPE_SIGNATURE = "Stripe-Signature";

    private final StripeWebhookService stripeWebhookService;

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripe(
            @RequestBody String payload,
            @RequestHeader(value = STRIPE_SIGNATURE, required = false) String stripeSignature) {
        stripeWebhookService.handleStripeEvent(payload, stripeSignature);
        return ResponseEntity.ok().build();
    }
}
