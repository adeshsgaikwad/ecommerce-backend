package com.ecommerce.ecommerce_backend.kafka.consumer;

import com.ecommerce.ecommerce_backend.kafka.event.OrderPlacedEvent;
import com.ecommerce.ecommerce_backend.kafka.event.SellerAppliedEvent;
import com.ecommerce.ecommerce_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    // Listens to order.placed — sends confirmation to buyer
    // and new order alert to each unique seller in the order
    @KafkaListener(topics = "order.placed", groupId = "notification-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("NotificationConsumer received order.placed for orderId: {}", event.getOrderId());

        // Notify buyer
        notificationService.sendOrderConfirmationEmail(event.getBuyerEmail(), event.getOrderId());

        // Notify each unique seller (avoid duplicate alerts if seller has multiple items)
        Set<Long> notifiedSellers = new HashSet<>();
        event.getSellerItems().forEach(item -> {
            if (notifiedSellers.add(item.getSellerId())) {
                notificationService.sendNewOrderAlertToSeller(
                        item.getSellerEmail(), event.getOrderId());
            }
        });
    }

    // Listens to seller.applied — notifies admin (or logs for now)
    @KafkaListener(topics = "seller.applied", groupId = "notification-group")
    public void handleSellerApplied(SellerAppliedEvent event) {
        log.info("New seller application received from userId: {} shopName: {}",
                event.getUserId(), event.getShopName());
        // In production: send an email to admin or create an admin dashboard notification
    }
}