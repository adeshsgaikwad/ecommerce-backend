package com.ecommerce.ecommerce_backend.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// POJO serialized to JSON and sent over Kafka topic "seller.applied"
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerAppliedEvent {
    private Long userId;
    private String userEmail;
    private String shopName;
}