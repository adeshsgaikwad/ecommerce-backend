package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Seller: fetch only their own products (with pagination)
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    // Public: fetch active products by category (with pagination)
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    // Public: fetch all active products (with pagination)
    Page<Product> findByIsActiveTrue(Pageable pageable);

    // Ownership check — verify product belongs to seller before edit/delete
    Optional<Product> findByIdAndSellerId(Long id, Long sellerId);

    // Public: keyword search across name and description
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Check stock before checkout — only active products with enough stock
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isActive = true AND p.stockQty >= :qty")
    Optional<Product> findAvailableProduct(@Param("id") Long id, @Param("qty") int qty);

    // Seller dashboard: count their active listings
    long countBySellerIdAndIsActiveTrue(Long sellerId);
}