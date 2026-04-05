package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProductRepository productRepository;

    // Cart stored as Redis Hash: cart:{userId} → { productId: quantity }
    private static final String CART_PREFIX = "cart:";
    private static final long CART_TTL_DAYS = 7;

    private String cartKey(Long userId) {
        return CART_PREFIX + userId;
    }

    // Add item or increase quantity
    public Map<String, String> addItem(Long userId, Long productId, int quantity) {
        // Verify product exists and is active
        productRepository.findById(productId)
                .filter(p -> p.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found or inactive"));

        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        String key = cartKey(userId);
        String field = String.valueOf(productId);

        // Increment if already in cart, otherwise set
        String existing = (String) redisTemplate.opsForHash().get(key, field);
        int newQty = (existing != null ? Integer.parseInt(existing) : 0) + quantity;

        redisTemplate.opsForHash().put(key, field, String.valueOf(newQty));
        // Reset TTL on every update so cart stays alive while user is active
        redisTemplate.expire(key, CART_TTL_DAYS, TimeUnit.DAYS);

        return getCart(userId);
    }

    // Update quantity of a specific item
    public Map<String, String> updateItem(Long userId, Long productId, int quantity) {
        String key = cartKey(userId);
        String field = String.valueOf(productId);

        if (!Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(key, field))) {
            throw new ResourceNotFoundException("Item not found in cart");
        }

        if (quantity < 1) {
            // Quantity 0 means remove
            redisTemplate.opsForHash().delete(key, field);
        } else {
            redisTemplate.opsForHash().put(key, field, String.valueOf(quantity));
        }

        redisTemplate.expire(key, CART_TTL_DAYS, TimeUnit.DAYS);
        return getCart(userId);
    }

    // Remove a single item from cart
    public Map<String, String> removeItem(Long userId, Long productId) {
        String key = cartKey(userId);
        redisTemplate.opsForHash().delete(key, String.valueOf(productId));
        return getCart(userId);
    }

    // Get all items in cart — returns { productId: quantity } map
    public Map<String, String> getCart(Long userId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey(userId));
        if (entries.isEmpty()) return Collections.emptyMap();

        Map<String, String> cart = new HashMap<>();
        entries.forEach((k, v) -> cart.put(String.valueOf(k), String.valueOf(v)));
        return cart;
    }

    // Clear entire cart — called after successful checkout
    public void clearCart(Long userId) {
        redisTemplate.delete(cartKey(userId));
    }
}