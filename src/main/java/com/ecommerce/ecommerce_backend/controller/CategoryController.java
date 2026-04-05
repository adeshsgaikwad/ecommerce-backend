package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.response.ApiResponse;
import com.ecommerce.ecommerce_backend.entity.Category;
import com.ecommerce.ecommerce_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // GET /api/categories — public: all root categories
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getRootCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getRootCategories()));
    }

    // GET /api/categories/{id}/children — public: subcategories
    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<Category>>> getChildren(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getChildren(id)));
    }

    // POST /api/categories — ADMIN only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @RequestParam String name,
            @RequestParam String slug,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) Long parentId) {
        Category category = categoryService.create(name, slug, description, imageUrl, parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(category));
    }

    // DELETE /api/categories/{id} — ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}