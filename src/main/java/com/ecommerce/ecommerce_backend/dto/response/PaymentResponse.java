package com.ecommerce.ecommerce_backend.dto.response;

import com.ecommerce.ecommerce_backend.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String gateway;
    private String gatewayTxnId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime paidAt;
}