package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    Page<ProductResponse> getAllActive(Pageable pageable);

    /** categoryId, if given, also matches every descendant subcategory — see CategoryService.getDescendantIds. */
    Page<ProductResponse> search(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    ProductResponse getById(Long id);

    /** Active product count for a category, including its subcategories — used for category tiles. */
    long countInCategoryTree(Long categoryId);
}
