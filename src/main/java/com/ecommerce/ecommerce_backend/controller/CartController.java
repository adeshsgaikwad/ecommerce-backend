package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.response.ApiResponse;
import com.ecommerce.ecommerce_backend.security.UserPrincipal;
import com.ecommerce.ecommerce_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // GET /api/cart — BUYER only
    @GetMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCart(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(currentUser.getId())));
    }

    // POST /api/cart?productId=1&quantity=2 — BUYER only
    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> addToCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        Map<String, String> cart = cartService.addItem(currentUser.getId(), productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    // PATCH /api/cart?productId=1&quantity=3 — BUYER only
    @PatchMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam Long productId,
            @RequestParam int quantity) {
        Map<String, String> cart = cartService.updateItem(currentUser.getId(), productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    // DELETE /api/cart/{productId} — BUYER only
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> removeFromCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long productId) {
        Map<String, String> cart = cartService.removeItem(currentUser.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    // DELETE /api/cart — BUYER only: clear entire cart
    @DeleteMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}