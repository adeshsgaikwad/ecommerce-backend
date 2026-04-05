package com.ecommerce.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items", indexes = {
    // Seller queries their items across all orders using this index
    @Index(name = "idx_order_item_seller", columnList = "seller_id"),
    @Index(name = "idx_order_item_order",  columnList = "order_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many items → one order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Many items → one product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // seller_id stored directly here — critical for multi-seller orders
    // A single order can contain items from different sellers
    // This lets each seller see only their own items
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private Integer quantity;

    // Price snapshot at time of purchase
    // If product price changes later, history stays accurate
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Convenience getter — total for this line item
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
