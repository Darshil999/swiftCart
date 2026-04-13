package com.swiftcart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();

    /**
     * Sum of {@code price * quantity} for all lines — computed on read, not persisted in Redis.
     */
    private BigDecimal totalPrice;
}
