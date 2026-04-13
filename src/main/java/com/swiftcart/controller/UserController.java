package com.swiftcart.controller;

import com.swiftcart.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Example of secured endpoints using @PreAuthorize for role-based authorization.
 * These endpoints require a valid JWT in the Authorization header.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    /**
     * Accessible by any authenticated user regardless of role.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(Map.of(
                "id",    currentUser.getId(),
                "email", currentUser.getEmail(),
                "role",  currentUser.getAuthorities().iterator().next().getAuthority()
        ));
    }

    /**
     * Only ADMIN can access this endpoint.
     * hasRole("ADMIN") checks for "ROLE_ADMIN" authority under the hood.
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("Welcome, Admin!");
    }

    /**
     * Accessible by SELLER or ADMIN.
     */
    @GetMapping("/seller-dashboard")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<String> sellerDashboard() {
        return ResponseEntity.ok("Welcome to the Seller Dashboard!");
    }
}
