package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Fetch all items in a specific order
    List<OrderItem> findByOrderId(Long orderId);

    // Seller: fetch only their items within a specific order
    List<OrderItem> findByOrderIdAndSellerId(Long orderId, Long sellerId);

    // Seller dashboard: total revenue — sum of (unit_price × quantity) for their items
    @Query("SELECT COALESCE(SUM(oi.unitPrice * oi.quantity), 0) " +
           "FROM OrderItem oi WHERE oi.seller.id = :sellerId")
    BigDecimal calculateTotalRevenueForSeller(@Param("sellerId") Long sellerId);

    // Seller: count total items sold
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) " +
           "FROM OrderItem oi WHERE oi.seller.id = :sellerId")
    Long countTotalItemsSoldBySeller(@Param("sellerId") Long sellerId);
}
