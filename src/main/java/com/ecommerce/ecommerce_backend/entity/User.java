package com.ecommerce.ecommerce_backend.entity;

import com.ecommerce.ecommerce_backend.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Unique index — used on every login query
    @Column(nullable = false, unique = true)
    private String email;

    // BCrypt hashed — never store plain text
    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 15)
    private String phone;

    // Stored as string in DB via @Enumerated
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Soft delete / ban support — admin can deactivate without deleting
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // One user can have many addresses
    // mappedBy = field name in Address entity that owns the FK
    // cascade = deleting user also deletes their addresses
    // orphanRemoval = if address removed from list, delete it from DB
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    // One user can have many orders
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    // A seller is also a user — one optional seller profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private SellerProfile sellerProfile;
}
