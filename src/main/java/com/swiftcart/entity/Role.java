package com.swiftcart.entity;

/**
 * Roles are stored as strings in the DB via @Enumerated(EnumType.STRING).
 * Spring Security expects "ROLE_" prefix when using hasRole(); we handle this
 * in UserDetailsImpl by returning "ROLE_" + role.name().
 */
public enum Role {
    BUYER,
    SELLER,
    ADMIN
}
