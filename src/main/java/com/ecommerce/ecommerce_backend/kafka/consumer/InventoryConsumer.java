package com.ecommerce.ecommerce_backend.kafka.consumer;

import com.ecommerce.ecommerce_backend.kafka.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Listens to order.placed and logs inventory confirmation
// In production: could sync with a warehouse or ERP system
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryConsumer {

    @KafkaListener(topics = "order.placed", groupId = "inventory-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("InventoryConsumer: processing stock confirmation for orderId: {}",
                event.getOrderId());

        // Stock was already decremented in OrderService @Transactional block
        // This consumer confirms and could sync to external warehouse
        event.getSellerItems().forEach(item ->
                log.info("Stock confirmed — productId: {} qty: {}",
                        item.getProductId(), item.getQuantity())
        );
    }
}