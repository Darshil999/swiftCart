package com.swiftcart.service;

import com.swiftcart.dto.response.OrderItemResponse;
import com.swiftcart.dto.response.OrderResponse;
import com.swiftcart.entity.Order;
import com.swiftcart.entity.OrderItem;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> listBuyerOrders() {
        Long buyerId = SecurityUtils.requireCurrentUser().getId();
        return orderRepository.findByBuyerIdWithItems(buyerId).stream()
                .map(this::toBuyerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Orders that include at least one line sold by this seller; each response lists only that seller's lines
     * and a subtotal for those lines.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> listSellerOrders() {
        Long sellerId = SecurityUtils.requireCurrentUser().getId();
        return orderRepository.findDistinctBySellerProducts(sellerId).stream()
                .map(o -> toSellerSliceResponse(o, sellerId))
                .collect(Collectors.toList());
    }

    private OrderResponse toBuyerResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderResponse toSellerSliceResponse(Order order, Long sellerId) {
        List<OrderItemResponse> items = order.getItems().stream()
                .filter(i -> i.getProduct().getSeller().getId().equals(sellerId))
                .map(this::toItemResponse)
                .collect(Collectors.toList());
        BigDecimal subtotal = items.stream()
                .map(OrderItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(subtotal)
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        return OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getPrice())
                .lineTotal(lineTotal)
                .build();
    }
}
