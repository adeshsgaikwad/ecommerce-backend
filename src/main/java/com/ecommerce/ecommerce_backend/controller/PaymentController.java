package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.response.ApiResponse;
import com.ecommerce.ecommerce_backend.dto.response.PaymentResponse;
import com.ecommerce.ecommerce_backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // POST /api/payments/callback — called by Razorpay/Stripe webhook (public)
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<PaymentResponse>> handleCallback(
            @RequestParam String gatewayTxnId,
            @RequestParam String status,
            @RequestBody(required = false) String rawResponse) {
        PaymentResponse response = paymentService.handleGatewayCallback(
                gatewayTxnId, status, rawResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET /api/payments/order/{orderId} — authenticated user
    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByOrderId(orderId)));
    }

    // POST /api/payments/mock/{orderId} — dev/test only: simulate payment success
    @PostMapping("/mock/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> mockPayment(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.mockPaymentSuccess(orderId)));
    }
}