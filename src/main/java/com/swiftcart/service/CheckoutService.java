package com.swiftcart.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.swiftcart.config.StripeProperties;
import com.swiftcart.dto.response.CartItemResponse;
import com.swiftcart.dto.response.CheckoutSessionResponse;
import com.swiftcart.entity.Order;
import com.swiftcart.entity.OrderItem;
import com.swiftcart.entity.OrderStatus;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.User;
import com.swiftcart.exception.EmptyCartException;
import com.swiftcart.exception.ResourceNotFoundException;
import com.swiftcart.exception.StripeCheckoutException;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.security.SecurityUtils;
import com.swiftcart.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a persisted {@link Order} first, then opens a Stripe Checkout Session.
 * If Stripe fails, the transaction rolls back so no orphan PENDING order remains.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StripeProperties stripeProperties;

    @Transactional
    public CheckoutSessionResponse createCheckoutSession() {
        UserDetailsImpl principal = SecurityUtils.requireCurrentUser();
        Long buyerId = principal.getId();

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + buyerId));

        List<CartItemResponse> cartLines = cartService.getCartItemsForUser(buyerId);
        if (cartLines == null || cartLines.isEmpty()) {
            throw new EmptyCartException("Your cart is empty. Add items before checkout.");
        }

        Set<Long> productIds = cartLines.stream().map(CartItemResponse::getProductId).collect(Collectors.toSet());
        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new ResourceNotFoundException("One or more products in your cart no longer exist.");
        }
        Map<Long, Product> productById = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemResponse line : cartLines) {
            Product product = productById.get(line.getProductId());
            BigDecimal lineTotal = line.getPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            total = total.add(lineTotal);
            orderItems.add(OrderItem.builder()
                    .product(product)
                    .quantity(line.getQuantity())
                    .price(line.getPrice())
                    .build());
        }
        total = total.setScale(2, RoundingMode.HALF_UP);

        Order order = Order.builder()
                .buyer(buyer)
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .build();
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.getItems().add(item);
        }

        order = orderRepository.save(order);

        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeProperties.getSuccessUrl())
                .setCancelUrl(stripeProperties.getCancelUrl())
                .setClientReferenceId(String.valueOf(order.getId()))
                .putMetadata("orderId", String.valueOf(order.getId()))
                .putMetadata("buyerId", String.valueOf(buyerId));

        for (CartItemResponse line : cartLines) {
            long unitAmountCents = toSmallestCurrencyUnit(line.getPrice(), stripeProperties.getCurrency());
            sessionBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) line.getQuantity())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency(stripeProperties.getCurrency())
                                            .setUnitAmount(unitAmountCents)
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(line.getProductName())
                                                            .build())
                                            .build())
                            .build());
        }

        if (!org.springframework.util.StringUtils.hasText(Stripe.apiKey)) {
            throw new StripeCheckoutException("Stripe is not configured (missing application.stripe.secret-key).", null);
        }

        try {
            Session session = Session.create(sessionBuilder.build());
            order.setStripeCheckoutSessionId(session.getId());
            orderRepository.save(order);

            return CheckoutSessionResponse.builder()
                    .orderId(order.getId())
                    .checkoutUrl(session.getUrl())
                    .stripeCheckoutSessionId(session.getId())
                    .build();
        } catch (StripeException e) {
            log.error("Stripe Checkout Session creation failed for order {}: {}", order.getId(), e.getMessage());
            throw new StripeCheckoutException("Payment provider unavailable. Please try again later.", e);
        }
    }

    /**
     * Stripe amounts are in the smallest currency unit (e.g. cents for usd).
     */
    private static long toSmallestCurrencyUnit(BigDecimal majorUnit, String currency) {
        String c = currency == null ? "usd" : currency.toLowerCase();
        int fractionDigits = java.util.Currency.getInstance(c.toUpperCase()).getDefaultFractionDigits();
        if (fractionDigits < 0) {
            fractionDigits = 2;
        }
        BigDecimal factor = BigDecimal.TEN.pow(fractionDigits);
        return majorUnit.multiply(factor).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
