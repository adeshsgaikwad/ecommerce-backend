package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.request.AddressRequest;
import com.ecommerce.ecommerce_backend.dto.response.ApiResponse;
import com.ecommerce.ecommerce_backend.entity.Address;
import com.ecommerce.ecommerce_backend.security.UserPrincipal;
import com.ecommerce.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/me/addresses — any authenticated user
    @GetMapping("/me/addresses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Address>>> getMyAddresses(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Address> addresses = userService.getMyAddresses(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    // POST /api/users/me/addresses
    @PostMapping("/me/addresses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Address>> addAddress(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody AddressRequest request) {
        Address address = userService.addAddress(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(address));
    }

    // PUT /api/users/me/addresses/{addressId}
    @PutMapping("/me/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Address>> updateAddress(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        Address address = userService.updateAddress(currentUser.getId(), addressId, request);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    // DELETE /api/users/me/addresses/{addressId}
    @DeleteMapping("/me/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long addressId) {
        userService.deleteAddress(currentUser.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }
}