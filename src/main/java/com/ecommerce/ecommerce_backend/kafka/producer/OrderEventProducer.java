package com.ecommerce.ecommerce_backend.kafka.producer;

import com.ecommerce.ecommerce_backend.entity.OrderItem;
import com.ecommerce.ecommerce_backend.kafka.event.OrderPlacedEvent;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserRepository userRepository;

    private static final String TOPIC = "order.placed";

    public void publishOrderPlaced(Long orderId, Long buyerId, List<OrderItem> items) {
        String buyerEmail = userRepository.findById(buyerId)
                .map(u -> u.getEmail()).orElse("");

        // Build seller item list — consumers use this to notify each seller
        List<OrderPlacedEvent.SellerItem> sellerItems = items.stream()
                .map(item -> OrderPlacedEvent.SellerItem.builder()
                        .sellerId(item.getSeller().getId())
                        .sellerEmail(item.getSeller().getEmail())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(orderId)
                .buyerId(buyerId)
                .buyerEmail(buyerEmail)
                .sellerItems(sellerItems)
                .build();

        // orderId as key ensures all events for same order go to same partition (ordering)
        kafkaTemplate.send(TOPIC, String.valueOf(orderId), event);
        log.info("Published order.placed event for orderId: {}", orderId);
    }
}