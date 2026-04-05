package com.ecommerce.ecommerce_backend.dto.response;

import com.ecommerce.ecommerce_backend.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String name;
    private String email;
    private Role role;
}