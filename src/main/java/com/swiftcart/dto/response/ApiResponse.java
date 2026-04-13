package com.swiftcart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Generic wrapper for simple success/error messages.
 */
@Data
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
}
