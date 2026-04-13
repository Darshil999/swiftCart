package com.swiftcart.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Reads the authenticated principal from {@link SecurityContextHolder}.
 * Never use client-supplied identifiers for ownership — always derive identity here.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserDetailsImpl requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            throw new AccessDeniedException("Invalid authentication principal");
        }
        return (UserDetailsImpl) principal;
    }
}
