package com.swiftcart.controller;

import com.swiftcart.dto.response.CheckoutSessionResponse;
import com.swiftcart.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * Persists a PENDING order from the Redis cart, then returns a Stripe Checkout URL.
     */
    @PostMapping("/session")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession() {
        return ResponseEntity.ok(checkoutService.createCheckoutSession());
    }
}
