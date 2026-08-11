package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.response.CategoryResponse;
import com.multivendor.ecommerce.entity.Category;
import com.multivendor.ecommerce.service.CategoryService;
import com.multivendor.ecommerce.service.ProductService;
import com.multivendor.ecommerce.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    // Public: browse categories/subcategories for filtering (admin-shaped: raw entities)
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean topLevelOnly) {
        List<Category> categories = topLevelOnly ? categoryService.getTopLevel() : categoryService.getAll();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /** Storefront-shaped: every category (top-level and sub) with its active product count, for nav/tiles. */
    @GetMapping("/storefront")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getStorefrontCategories() {
        List<CategoryResponse> categories = categoryService.getAll().stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .parentId(c.getParent() != null ? c.getParent().getId() : null)
                        .productCount(productService.countInCategoryTree(c.getId()))
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getById(id)));
    }

    /** Root-to-leaf path for breadcrumbs, e.g. Electronics > Phones > Smartphones. */
    @GetMapping("/{id}/breadcrumb")
    public ResponseEntity<ApiResponse<List<Category>>> getBreadcrumb(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getBreadcrumb(id)));
    }
}
