package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.request.ProductRequest;
import com.ecommerce.ecommerce_backend.dto.response.PagedResponse;
import com.ecommerce.ecommerce_backend.dto.response.ProductResponse;
import com.ecommerce.ecommerce_backend.entity.Category;
import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.CategoryRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // ─── PUBLIC ENDPOINTS ────────────────────────────────────────────────────

    // @Cacheable: first call hits DB and caches result in Redis
    // Subsequent calls with same productId return from cache instantly
    @Cacheable(value = "products", key = "#productId")
    public ProductResponse getById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToResponse(product);
    }

    @Cacheable(value = "products", key = "'all_' + #pageNo + '_' + #pageSize")
    public PagedResponse<ProductResponse> getAllActive(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        Page<Product> page = productRepository.findByIsActiveTrue(pageable);
        return buildPagedResponse(page);
    }

    public PagedResponse<ProductResponse> getByCategory(Long categoryId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        Page<Product> page = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return buildPagedResponse(page);
    }

    public PagedResponse<ProductResponse> search(String keyword, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        Page<Product> page = productRepository.searchByKeyword(keyword, pageable);
        return buildPagedResponse(page);
    }

    // ─── SELLER ENDPOINTS ────────────────────────────────────────────────────

    public PagedResponse<ProductResponse> getMyProducts(Long sellerId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        Page<Product> page = productRepository.findBySellerId(sellerId, pageable);
        return buildPagedResponse(page);
    }

    @Transactional
    public ProductResponse create(Long sellerId, ProductRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQty(request.getStockQty())
                .imageUrl(request.getImageUrl())
                .category(category)
                .seller(seller)
                .isActive(true)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    // @CacheEvict: after update, invalidate the cached version so next read is fresh
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponse update(Long sellerId, Long productId, ProductRequest request) {
        // findByIdAndSellerId ensures seller can only update their own product
        Product product = productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new BadRequestException("Product not found or you don't have permission"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQty(request.getStockQty());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void delete(Long sellerId, Long productId) {
        Product product = productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new BadRequestException("Product not found or you don't have permission"));

        // Soft delete — keeps order history intact
        product.setIsActive(false);
        productRepository.save(product);
    }

    // ─── MAPPER ──────────────────────────────────────────────────────────────

    public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQty(product.getStockQty())
                .imageUrl(product.getImageUrl())
                .isActive(product.getIsActive())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getName())
                .shopName(product.getSeller().getSellerProfile() != null
                        ? product.getSeller().getSellerProfile().getShopName() : null)
                .createdAt(product.getCreatedAt())
                .build();
    }

    private PagedResponse<ProductResponse> buildPagedResponse(Page<Product> page) {
        return PagedResponse.<ProductResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}