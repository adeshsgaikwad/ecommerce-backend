package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.entity.Category;
import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNull();
    }

    public List<Category> getChildren(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public Category create(String name, String slug, String description,
                           String imageUrl, Long parentId) {
        if (categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Slug already in use: " + slug);
        }

        Category parent = null;
        if (parentId != null) {
            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .imageUrl(imageUrl)
                .parent(parent)
                .isActive(true)
                .build();

        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        // Soft delete
        category.setIsActive(false);
        categoryRepository.save(category);
    }
}