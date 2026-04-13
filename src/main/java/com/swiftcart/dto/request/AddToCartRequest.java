package com.swiftcart.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 9999, message = "Quantity cannot exceed 9999")
    private Integer quantity;
}
