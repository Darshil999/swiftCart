package com.swiftcart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Single line in the cart (API and JSON stored in Redis).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
