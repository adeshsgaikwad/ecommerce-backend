package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.request.OrderRequest;
import com.ecommerce.ecommerce_backend.dto.response.ApiResponse;
import com.ecommerce.ecommerce_backend.dto.response.OrderResponse;
import com.ecommerce.ecommerce_backend.dto.response.PagedResponse;
import com.ecommerce.ecommerce_backend.enums.OrderStatus;
import com.ecommerce.ecommerce_backend.security.UserPrincipal;
import com.ecommerce.ecommerce_backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders — BUYER only
    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", response));
    }

    // GET /api/orders/my — BUYER: their own orders
    @GetMapping("/my")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getMyOrders(currentUser.getId(), pageNo, pageSize)));
    }

    // GET /api/orders/my/{orderId} — BUYER: one specific order (ownership enforced)
    @GetMapping("/my/{orderId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<OrderResponse>> getMyOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getMyOrderById(currentUser.getId(), orderId)));
    }

    // DELETE /api/orders/my/{orderId}/cancel — BUYER: cancel their order
    @DeleteMapping("/my/{orderId}/cancel")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.cancelOrder(currentUser.getId(), orderId)));
    }

    // GET /api/orders/seller — SELLER: orders containing their items
    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getSellerOrders(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getSellerOrders(currentUser.getId(), pageNo, pageSize)));
    }

    // PATCH /api/orders/seller/{orderId}/status — SELLER: update status
    @PatchMapping("/seller/{orderId}/status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.updateStatus(currentUser.getId(), orderId, status)));
    }
}