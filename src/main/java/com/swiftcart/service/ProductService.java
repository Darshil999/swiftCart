package com.swiftcart.service;

import com.swiftcart.dto.request.ProductRequest;
import com.swiftcart.dto.response.CategorySummaryResponse;
import com.swiftcart.dto.response.ProductResponse;
import com.swiftcart.dto.response.SellerSummaryResponse;
import com.swiftcart.entity.Category;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.User;
import com.swiftcart.exception.ResourceNotFoundException;
import com.swiftcart.repository.CategoryRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.security.SecurityUtils;
import com.swiftcart.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts(Long categoryId, Long sellerId) {
        List<Product> products;
        if (categoryId != null && sellerId != null) {
            products = productRepository.findByCategoryIdAndSellerId(categoryId, sellerId);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else if (sellerId != null) {
            products = productRepository.findBySellerId(sellerId);
        } else {
            products = productRepository.findAllWithCategoryAndSeller();
        }
        return products.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        UserDetailsImpl current = SecurityUtils.requireCurrentUser();
        User seller = userRepository.findById(current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + current.getId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .price(request.getPrice())
                .category(category)
                .seller(seller)
                .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        UserDetailsImpl current = SecurityUtils.requireCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        assertSellerOwnsProduct(current.getId(), product);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setCategory(category);

        return toResponse(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        UserDetailsImpl current = SecurityUtils.requireCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        assertSellerOwnsProduct(current.getId(), product);

        productRepository.delete(product);
    }

    private void assertSellerOwnsProduct(Long authenticatedSellerId, Product product) {
        if (!product.getSeller().getId().equals(authenticatedSellerId)) {
            throw new AccessDeniedException("You can only modify or delete your own products");
        }
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(CategorySummaryResponse.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build())
                .seller(SellerSummaryResponse.builder()
                        .id(product.getSeller().getId())
                        .name(product.getSeller().getName())
                        .email(product.getSeller().getEmail())
                        .build())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
