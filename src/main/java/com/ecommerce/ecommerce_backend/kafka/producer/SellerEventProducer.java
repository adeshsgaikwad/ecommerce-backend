package com.ecommerce.ecommerce_backend.kafka.producer;

import com.ecommerce.ecommerce_backend.kafka.event.SellerAppliedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "seller.applied";

    public void publishSellerApplied(Long userId, String userEmail, String shopName) {
        SellerAppliedEvent event = SellerAppliedEvent.builder()
                .userId(userId)
                .userEmail(userEmail)
                .shopName(shopName)
                .build();

        kafkaTemplate.send(TOPIC, String.valueOf(userId), event);
        log.info("Published seller.applied event for userId: {}", userId);
    }
}