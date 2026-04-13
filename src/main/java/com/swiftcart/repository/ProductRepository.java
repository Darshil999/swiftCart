package com.swiftcart.repository;

import com.swiftcart.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "seller"})
    @Override
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = {"category", "seller"})
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithCategoryAndSeller();

    @EntityGraph(attributePaths = {"category", "seller"})
    List<Product> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category", "seller"})
    List<Product> findBySellerId(Long sellerId);

    @EntityGraph(attributePaths = {"category", "seller"})
    List<Product> findByCategoryIdAndSellerId(Long categoryId, Long sellerId);
}
