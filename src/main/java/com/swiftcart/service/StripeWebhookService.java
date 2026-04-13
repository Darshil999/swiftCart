package com.swiftcart.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.swiftcart.config.StripeProperties;
import com.swiftcart.entity.Order;
import com.swiftcart.entity.OrderStatus;
import com.swiftcart.exception.WebhookVerificationException;
import com.swiftcart.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Verifies Stripe webhook signatures and applies idempotent order updates after payment.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private final StripeProperties stripeProperties;
    private final OrderRepository orderRepository;
    private final CartService cartService;

    /**
     * @param payload       raw JSON body (must not be re-serialized)
     * @param stripeSignature value of {@code Stripe-Signature} header
     */
    @Transactional
    public void handleStripeEvent(String payload, String stripeSignature) {
        if (!StringUtils.hasText(stripeProperties.getWebhookSecret())) {
            throw new WebhookVerificationException(
                    "Stripe webhook signing secret is not configured (application.stripe.webhook-secret).");
        }
        if (!StringUtils.hasText(stripeSignature)) {
            throw new WebhookVerificationException("Missing Stripe-Signature header.");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, stripeSignature, stripeProperties.getWebhookSecret().trim());
        } catch (SignatureVerificationException e) {
            throw new WebhookVerificationException("Invalid Stripe webhook signature.", e);
        }

        if (!"checkout.session.completed".equals(event.getType())) {
            log.debug("Ignoring unhandled Stripe event type: {}", event.getType());
            return;
        }

        var deserializer = event.getDataObjectDeserializer();
        if (!deserializer.getObject().isPresent()) {
            log.warn("checkout.session.completed event {} has no embedded object", event.getId());
            return;
        }

        StripeObject obj = deserializer.getObject().get();
        if (!(obj instanceof Session session)) {
            log.warn("Unexpected Stripe object type for checkout.session.completed: {}", obj.getClass());
            return;
        }

        if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            log.info("Checkout session {} payment_status is {}, skipping PAID transition", session.getId(), session.getPaymentStatus());
            return;
        }

        String orderIdStr = session.getMetadata() != null ? session.getMetadata().get("orderId") : null;
        if (!StringUtils.hasText(orderIdStr)) {
            log.error("Stripe session {} missing metadata orderId", session.getId());
            return;
        }

        long orderId;
        try {
            orderId = Long.parseLong(orderIdStr);
        } catch (NumberFormatException e) {
            log.error("Invalid orderId metadata on session {}: {}", session.getId(), orderIdStr);
            return;
        }

        Order order = orderRepository.findById(orderId)
                .orElse(null);
        if (order == null) {
            log.error("No order found for id {} from Stripe session {}", orderId, session.getId());
            return;
        }

        if (order.getStripeCheckoutSessionId() != null
                && !order.getStripeCheckoutSessionId().equals(session.getId())) {
            log.error("Stripe session id mismatch for order {}: expected {}, got {}", orderId, order.getStripeCheckoutSessionId(), session.getId());
            return;
        }

        if (session.getAmountTotal() != null) {
            long expectedCents = order.getTotalAmount()
                    .movePointRight(2)
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();
            if (!session.getAmountTotal().equals(expectedCents)) {
                log.error("Amount mismatch for order {}: DB total (cents) {} vs Stripe {} — not marking PAID",
                        orderId, expectedCents, session.getAmountTotal());
                return;
            }
        }

        if (order.getStatus() == OrderStatus.PAID) {
            log.debug("Order {} already PAID — idempotent webhook ack", orderId);
            return;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} is in status {}, expected PENDING before marking PAID", orderId, order.getStatus());
            return;
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        String buyerIdStr = session.getMetadata().get("buyerId");
        if (StringUtils.hasText(buyerIdStr)) {
            try {
                long buyerId = Long.parseLong(buyerIdStr);
                cartService.clearCartForUser(buyerId);
            } catch (NumberFormatException e) {
                log.warn("Invalid buyerId metadata on session {}: {}", session.getId(), buyerIdStr);
            }
        } else {
            cartService.clearCartForUser(order.getBuyer().getId());
        }

        log.info("Order {} marked PAID via Stripe webhook; cart cleared for buyer", orderId);
    }
}
