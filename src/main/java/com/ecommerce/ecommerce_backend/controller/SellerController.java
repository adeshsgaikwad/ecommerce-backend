package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.request.SellerApplyRequest;
import com.ecommerce.ecommerce_backend.dto.response.ApiResponse;
import com.ecommerce.ecommerce_backend.dto.response.SellerProfileResponse;
import com.ecommerce.ecommerce_backend.security.UserPrincipal;
import com.ecommerce.ecommerce_backend.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    // POST /api/seller/apply — any authenticated user (BUYER applies to become SELLER)
    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> apply(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SellerApplyRequest request) {
        SellerProfileResponse response = sellerService.apply(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", response));
    }

    // GET /api/seller/me — SELLER: view their own profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        SellerProfileResponse response = sellerService.getMyProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}