package com.multisucursal.inventory.auth.dto;

import com.multisucursal.inventory.user.entity.Role;

public record AuthResponse(
    String token,
    String tokenType,
    Long userId,
    String name,
    String email,
    Role role
) {
}

