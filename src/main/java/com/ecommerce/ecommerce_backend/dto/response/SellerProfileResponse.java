package com.ecommerce.ecommerce_backend.dto.response;

import com.ecommerce.ecommerce_backend.enums.SellerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfileResponse {
    private Long id;
    private Long userId;
    private String sellerName;
    private String email;
    private String shopName;
    private String gstNumber;
    private SellerStatus status;
    private String rejectionReason;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
}