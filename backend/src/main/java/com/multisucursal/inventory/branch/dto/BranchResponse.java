package com.multisucursal.inventory.branch.dto;

public record BranchResponse(
    Long id,
    String code,
    String name,
    String city,
    String address,
    String managerName,
    String phone,
    String email,
    boolean active
) {
}

