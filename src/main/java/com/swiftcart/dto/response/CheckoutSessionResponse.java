package com.swiftcart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionResponse {
    private Long orderId;
    /**
     * Hosted Checkout URL to redirect the buyer's browser to.
     */
    private String checkoutUrl;
    private String stripeCheckoutSessionId;
}
