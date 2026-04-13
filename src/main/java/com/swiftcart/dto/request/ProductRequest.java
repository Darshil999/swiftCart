package com.swiftcart.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Payload for creating or updating a product.
 * Seller identity is never accepted here — it comes from the SecurityContext only.
 */
@Data
public class ProductRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 4000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be at least 0.01")
    private BigDecimal price;

    @NotNull
    private Long categoryId;
}
