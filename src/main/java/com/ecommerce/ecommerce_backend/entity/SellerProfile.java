package com.ecommerce.ecommerce_backend.entity;

import com.ecommerce.ecommerce_backend.enums.SellerStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one with User — seller_profiles.user_id is the FK column
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String shopName;

    // GST number — unique per seller
    @Column(unique = true)
    private String gstNumber;

    private String bankAccount;

    private String ifscCode;

    // PENDING → APPROVED or REJECTED by admin
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SellerStatus status = SellerStatus.PENDING;

    // Reason admin rejected — shown to seller
    private String rejectionReason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime appliedAt;

    // Null until admin approves
    private LocalDateTime approvedAt;
}
