package com.swiftcart.repository;

import com.swiftcart.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT o FROM Order o WHERE o.buyer.id = :buyerId ORDER BY o.createdAt DESC")
    List<Order> findByBuyerIdWithItems(@Param("buyerId") Long buyerId);

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.seller"})
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i JOIN i.product p WHERE p.seller.id = :sellerId ORDER BY o.createdAt DESC")
    List<Order> findDistinctBySellerProducts(@Param("sellerId") Long sellerId);
}
