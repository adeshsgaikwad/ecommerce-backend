package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Order;
import com.ecommerce.ecommerce_backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Buyer: fetch their own orders (paginated)
    Page<Order> findByUserId(Long userId, Pageable pageable);

    // Buyer: fetch their own orders filtered by status
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    // Ownership check — ensures buyer can only access their own order
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    // Seller: fetch all orders that contain at least one of their items
    // Joins order_items to find orders relevant to this seller
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderItems oi " +
           "WHERE oi.seller.id = :sellerId")
    Page<Order> findOrdersBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    // Seller: fetch their orders filtered by status
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderItems oi " +
           "WHERE oi.seller.id = :sellerId AND o.status = :status")
    Page<Order> findOrdersBySellerIdAndStatus(
            @Param("sellerId") Long sellerId,
            @Param("status") OrderStatus status,
            Pageable pageable);
}
