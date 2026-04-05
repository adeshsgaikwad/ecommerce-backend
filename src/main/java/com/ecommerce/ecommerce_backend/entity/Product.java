package com.ecommerce.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", indexes = {
    // Indexed because we filter by category and seller very frequently
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_seller",   columnList = "seller_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // TEXT type in DB — no length limit for long descriptions
    @Column(columnDefinition = "TEXT")
    private String description;

    // BigDecimal for money — never use float or double for prices
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQty;

    private String imageUrl;

    // Many products → one category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Many products → one seller (User with role = SELLER)
    // seller_id stored here enables ownership checks
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // Soft delete — deactivating hides product without losing history
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
