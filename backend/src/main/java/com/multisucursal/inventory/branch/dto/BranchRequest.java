package com.multisucursal.inventory.branch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BranchRequest(
    @NotBlank
    @Size(max = 30)
    String code,
    @NotBlank
    @Size(max = 120)
    String name,
    @NotBlank
    @Size(max = 150)
    String city,
    @NotBlank
    @Size(max = 255)
    String address,
    @Size(max = 80)
    String managerName,
    @Size(max = 30)
    String phone,
    @Email
    @Size(max = 120)
    String email,
    boolean active
) {
}

