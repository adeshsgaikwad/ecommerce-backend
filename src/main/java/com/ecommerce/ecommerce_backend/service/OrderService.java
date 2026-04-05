package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.request.OrderRequest;
import com.ecommerce.ecommerce_backend.dto.response.OrderItemResponse;
import com.ecommerce.ecommerce_backend.dto.response.OrderResponse;
import com.ecommerce.ecommerce_backend.dto.response.PagedResponse;
import com.ecommerce.ecommerce_backend.dto.response.PaymentResponse;
import com.ecommerce.ecommerce_backend.entity.*;
import com.ecommerce.ecommerce_backend.enums.OrderStatus;
import com.ecommerce.ecommerce_backend.enums.PaymentStatus;
import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.exception.OutOfStockException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.kafka.producer.OrderEventProducer;
import com.ecommerce.ecommerce_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;
    private final OrderEventProducer orderEventProducer;

    // @Transactional: stock decrement + order save + payment save happen atomically
    // If payment fails, everything rolls back — no partial saves
    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Ownership check — buyer can only use their own address
        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("Invalid address");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Validate stock and build order items
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findAvailableProduct(
                            itemReq.getProductId(), itemReq.getQuantity())
                    .orElseThrow(() -> new OutOfStockException(
                            "Product " + itemReq.getProductId() + " is out of stock or unavailable"));

            // Decrement stock atomically within this transaction
            product.setStockQty(product.getStockQty() - itemReq.getQuantity());
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .seller(product.getSeller())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())   // snapshot price at time of purchase
                    .build();

            orderItems.add(item);
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        // Build and save the order
        Order order = Order.builder()
                .user(buyer)
                .address(address)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(totalAmount)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Link each item to the saved order
        orderItems.forEach(item -> item.setOrder(savedOrder));
        orderItemRepository.saveAll(orderItems);
        savedOrder.setOrderItems(orderItems);

        // Create a PENDING payment record — updated after gateway callback
        Payment payment = Payment.builder()
                .order(savedOrder)
                .gateway("RAZORPAY")
                .status(PaymentStatus.PENDING)
                .amount(totalAmount)
                .currency("INR")
                .build();
        paymentRepository.save(payment);

        // Clear the buyer's Redis cart after successful order
        cartService.clearCart(userId);

        // Publish Kafka event — consumed by notification, inventory, seller services
        orderEventProducer.publishOrderPlaced(savedOrder.getId(), userId, orderItems);

        return mapToResponse(savedOrder, payment);
    }

    // Buyer: fetch their own orders (ownership enforced by query)
    public PagedResponse<OrderResponse> getMyOrders(Long userId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("placedAt").descending());
        Page<Order> page = orderRepository.findByUserId(userId, pageable);
        return buildPagedResponse(page);
    }

    // Buyer: get a specific order — findByIdAndUserId ensures ownership
    public OrderResponse getMyOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return mapToResponse(order, payment);
    }

    // Seller: fetch orders that contain their items
    public PagedResponse<OrderResponse> getSellerOrders(Long sellerId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("placedAt").descending());
        Page<Order> page = orderRepository.findOrdersBySellerId(sellerId, pageable);
        return buildPagedResponse(page);
    }

    // Seller: update order status — only allowed transitions: PACKED or SHIPPED
    @Transactional
    public OrderResponse updateStatus(Long sellerId, Long orderId, OrderStatus newStatus) {
        if (newStatus != OrderStatus.PACKED && newStatus != OrderStatus.SHIPPED) {
            throw new BadRequestException("Sellers can only set status to PACKED or SHIPPED");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify this seller actually has items in this order
        boolean sellerHasItems = order.getOrderItems().stream()
                .anyMatch(item -> item.getSeller().getId().equals(sellerId));

        if (!sellerHasItems) {
            throw new BadRequestException("You do not have items in this order");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return mapToResponse(order, payment);
    }

    // Buyer: cancel their own order
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel an order that is already shipped or delivered");
        }

        // Restore stock for each item on cancellation
        order.getOrderItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStockQty(product.getStockQty() + item.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return mapToResponse(order, payment);
    }

    // ─── MAPPERS ─────────────────────────────────────────────────────────────

    private OrderResponse mapToResponse(Order order, Payment payment) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .buyerName(order.getUser().getName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .street(order.getAddress().getStreet())
                .city(order.getAddress().getCity())
                .state(order.getAddress().getState())
                .pincode(order.getAddress().getPincode())
                .orderItems(itemResponses)
                .payment(payment != null ? mapPaymentToResponse(payment) : null)
                .placedAt(order.getPlacedAt())
                .build();
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImage(item.getProduct().getImageUrl())
                .sellerId(item.getSeller().getId())
                .sellerName(item.getSeller().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    private PaymentResponse mapPaymentToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .gateway(payment.getGateway())
                .gatewayTxnId(payment.getGatewayTxnId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private PagedResponse<OrderResponse> buildPagedResponse(Page<Order> page) {
        return PagedResponse.<OrderResponse>builder()
                .content(page.getContent().stream()
                        .map(o -> mapToResponse(o,
                                paymentRepository.findByOrderId(o.getId()).orElse(null)))
                        .collect(Collectors.toList()))
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}