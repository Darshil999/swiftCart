package com.swiftcart.controller;

import com.swiftcart.dto.request.AddToCartRequest;
import com.swiftcart.dto.response.CartResponse;
import com.swiftcart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUYER')")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartResponse> add(@Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> remove(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(productId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clear() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
