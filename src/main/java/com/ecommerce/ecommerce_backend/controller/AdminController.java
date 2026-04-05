package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.response.ApiResponse;
import com.ecommerce.ecommerce_backend.dto.response.SellerProfileResponse;
import com.ecommerce.ecommerce_backend.enums.SellerStatus;
import com.ecommerce.ecommerce_backend.service.SellerService;
import com.ecommerce.ecommerce_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // All endpoints in this controller require ADMIN
public class AdminController {

    private final SellerService sellerService;
    private final UserService userService;

    // GET /api/admin/sellers?status=PENDING — fetch seller applications by status
    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<SellerProfileResponse>>> getSellersByStatus(
            @RequestParam(defaultValue = "PENDING") SellerStatus status) {
        return ResponseEntity.ok(ApiResponse.success(sellerService.getByStatus(status)));
    }

    // POST /api/admin/sellers/{profileId}/approve — approve a seller application
    @PostMapping("/sellers/{profileId}/approve")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> approveSeller(
            @PathVariable Long profileId) {
        SellerProfileResponse response = sellerService.approve(profileId);
        return ResponseEntity.ok(ApiResponse.success("Seller approved successfully", response));
    }

    // POST /api/admin/sellers/{profileId}/reject — reject with a reason
    @PostMapping("/sellers/{profileId}/reject")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> rejectSeller(
            @PathVariable Long profileId,
            @RequestParam String reason) {
        SellerProfileResponse response = sellerService.reject(profileId, reason);
        return ResponseEntity.ok(ApiResponse.success("Seller application rejected", response));
    }

    // PATCH /api/admin/users/{userId}/deactivate — ban a user
    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long userId) {
        userService.getById(userId).setIsActive(false);
        return ResponseEntity.ok(ApiResponse.success("User deactivated", null));
    }
}