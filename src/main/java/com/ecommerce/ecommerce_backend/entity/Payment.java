package com.ecommerce.ecommerce_backend.entity;

import com.ecommerce.ecommerce_backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One payment → one order (1-to-1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // Which gateway was used — "RAZORPAY", "STRIPE", "UPI" etc.
    @Column(nullable = false)
    private String gateway;

    // ID returned by payment gateway — used to verify and reconcile
    @Column(unique = true)
    private String gatewayTxnId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Currency code — "INR", "USD" etc.
    @Column(nullable = false, length = 5)
    @Builder.Default
    private String currency = "INR";

    // Null until payment is completed
    private LocalDateTime paidAt;

    // Stores raw gateway response — useful for debugging disputes
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
