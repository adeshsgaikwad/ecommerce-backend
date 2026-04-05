package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.request.LoginRequest;
import com.ecommerce.ecommerce_backend.dto.request.RegisterRequest;
import com.ecommerce.ecommerce_backend.dto.response.ApiResponse;
import com.ecommerce.ecommerce_backend.dto.response.AuthResponse;
import com.ecommerce.ecommerce_backend.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register — public, no JWT needed
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, null));
    }

    // POST /api/auth/verify-otp?email=x&otp=y — public
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(
            @RequestParam @Email String email,
            @RequestParam @NotBlank String otp) {
        String message = authService.verifyOtp(email, otp);
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // POST /api/auth/login — public
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}