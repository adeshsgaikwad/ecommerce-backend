package com.ecommerce.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // URL-friendly identifier e.g. "mobile-phones"
    // Used in GET /api/products?category=mobile-phones
    @Column(nullable = false, unique = true)
    private String slug;

    private String description;

    private String imageUrl;

    // Self-referencing FK — allows tree structure
    // Electronics → Mobiles → Smartphones
    // parent = null means it is a root category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // All child categories under this one
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
