package com.swiftcart.controller;

import com.swiftcart.dto.response.OrderResponse;
import com.swiftcart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<OrderResponse>> sellerOrders() {
        return ResponseEntity.ok(orderService.listSellerOrders());
    }
}
