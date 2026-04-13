package com.swiftcart.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftcart.config.CartProperties;
import com.swiftcart.dto.request.AddToCartRequest;
import com.swiftcart.dto.response.CartItemResponse;
import com.swiftcart.dto.response.CartResponse;
import com.swiftcart.entity.Product;
import com.swiftcart.exception.ResourceNotFoundException;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.security.SecurityUtils;
import com.swiftcart.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private static final String CART_KEY_PREFIX = "cart:";
    private static final TypeReference<List<CartItemResponse>> CART_JSON_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate stringRedisTemplate;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final CartProperties cartProperties;

    public CartResponse getCart() {
        Long userId = currentUserId();
        List<CartItemResponse> items = readCart(userId);
        return toCartResponse(items);
    }

    public CartResponse addToCart(AddToCartRequest request) {
        Long userId = currentUserId();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        List<CartItemResponse> items = new ArrayList<>(readCart(userId));

        Optional<CartItemResponse> existing = items.stream()
                .filter(i -> i.getProductId().equals(product.getId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItemResponse line = existing.get();
            line.setQuantity(line.getQuantity() + request.getQuantity());
            line.setProductName(product.getName());
            line.setPrice(product.getPrice());
        } else {
            items.add(CartItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(request.getQuantity())
                    .build());
        }

        writeCart(userId, items);
        return toCartResponse(items);
    }

    public CartResponse removeItem(Long productId) {
        Long userId = currentUserId();
        List<CartItemResponse> items = new ArrayList<>(readCart(userId));
        boolean removed = items.removeIf(i -> i.getProductId().equals(productId));
        if (removed) {
            if (items.isEmpty()) {
                stringRedisTemplate.delete(cartKey(userId));
            } else {
                writeCart(userId, items);
            }
        }
        return toCartResponse(items);
    }

    public void clearCart() {
        Long userId = currentUserId();
        stringRedisTemplate.delete(cartKey(userId));
    }

    /**
     * Snapshot of Redis cart lines for the given user (e.g. checkout). Does not require the caller to be that user.
     */
    public List<CartItemResponse> getCartItemsForUser(Long userId) {
        return new ArrayList<>(readCart(userId));
    }

    /**
     * Clears Redis cart after confirmed payment (webhook). Never trust client-supplied user id for buyer-facing APIs.
     */
    public void clearCartForUser(Long userId) {
        stringRedisTemplate.delete(cartKey(userId));
    }

    private Long currentUserId() {
        UserDetailsImpl user = SecurityUtils.requireCurrentUser();
        return user.getId();
    }

    private String cartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    private List<CartItemResponse> readCart(Long userId) {
        String raw = stringRedisTemplate.opsForValue().get(cartKey(userId));
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<CartItemResponse> parsed = objectMapper.readValue(raw, CART_JSON_TYPE);
            return parsed != null ? new ArrayList<>(parsed) : new ArrayList<>();
        } catch (Exception e) {
            log.warn("Failed to parse cart JSON for user {} — treating as empty: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    private void writeCart(Long userId, List<CartItemResponse> items) {
        try {
            String json = objectMapper.writeValueAsString(items);
            stringRedisTemplate.opsForValue().set(cartKey(userId), json, cartProperties.getTtl());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize cart", e);
        }
    }

    private CartResponse toCartResponse(List<CartItemResponse> items) {
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return CartResponse.builder()
                .items(items)
                .totalPrice(total)
                .build();
    }
}
