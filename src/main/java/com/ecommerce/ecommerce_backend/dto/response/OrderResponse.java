package com.ecommerce.ecommerce_backend.dto.response;

import com.ecommerce.ecommerce_backend.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String buyerName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private List<OrderItemResponse> orderItems;
    private PaymentResponse payment;
    private LocalDateTime placedAt;
}