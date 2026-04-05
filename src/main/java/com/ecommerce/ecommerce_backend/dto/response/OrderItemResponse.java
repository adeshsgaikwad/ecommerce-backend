package com.ecommerce.ecommerce_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Long sellerId;
    private String sellerName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}