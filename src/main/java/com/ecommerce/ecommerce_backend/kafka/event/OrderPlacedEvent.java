package com.ecommerce.ecommerce_backend.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// POJO serialized to JSON and sent over Kafka topic "order.placed"
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    private Long orderId;
    private Long buyerId;
    private String buyerEmail;
    private BigDecimal totalAmount;
    private List<SellerItem> sellerItems;  // grouped by seller for notifications

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerItem {
        private Long sellerId;
        private String sellerEmail;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}