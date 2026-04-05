package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.response.PaymentResponse;
import com.ecommerce.ecommerce_backend.entity.Payment;
import com.ecommerce.ecommerce_backend.enums.PaymentStatus;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // Called by payment gateway webhook after payment is processed
    // In production this would verify HMAC signature from Razorpay/Stripe
    @Transactional
    public PaymentResponse handleGatewayCallback(String gatewayTxnId, String status, String rawResponse) {
        Payment payment = paymentRepository.findByGatewayTxnId(gatewayTxnId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        log.info("Payment callback received for txnId: {} with status: {}", gatewayTxnId, status);

        if ("SUCCESS".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        // Store raw gateway response for audit / dispute resolution
        payment.setGatewayResponse(rawResponse);
        paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    // Fetch payment details for an order
    public PaymentResponse getByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for this order"));
        return mapToResponse(payment);
    }

    // Mock payment — used in dev/test environment to simulate success
    @Transactional
    public PaymentResponse mockPaymentSuccess(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayTxnId("MOCK_TXN_" + System.currentTimeMillis());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("Mock payment success for orderId: {}", orderId);
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .gateway(payment.getGateway())
                .gatewayTxnId(payment.getGatewayTxnId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paidAt(payment.getPaidAt())
                .build();
    }
}