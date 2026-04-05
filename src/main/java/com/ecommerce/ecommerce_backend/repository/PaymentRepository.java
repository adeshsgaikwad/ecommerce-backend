package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Payment;
import com.ecommerce.ecommerce_backend.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Fetch payment record for a given order
    Optional<Payment> findByOrderId(Long orderId);

    // Verify and reconcile payment using gateway's transaction ID
    Optional<Payment> findByGatewayTxnId(String gatewayTxnId);

    // Admin: fetch all payments by status (for reconciliation)
    List<Payment> findByStatus(PaymentStatus status);
}