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
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<OrderResponse>> myOrders() {
        return ResponseEntity.ok(orderService.listBuyerOrders());
    }
}
