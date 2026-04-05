package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Fetch by URL slug — used in product filter queries
    Optional<Category> findBySlug(String slug);

    // Check duplicate slug during category creation
    boolean existsBySlug(String slug);

    // Fetch all root categories (parent = null means top-level)
    List<Category> findByParentIsNull();

    // Fetch all subcategories under a given parent
    List<Category> findByParentId(Long parentId);

    // Fetch only active categories for public API
    List<Category> findByIsActiveTrue();
}