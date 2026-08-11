package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.dto.response.ProductResponse;
import com.multivendor.ecommerce.entity.Product;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.ProductRepository;
import com.multivendor.ecommerce.service.CategoryService;
import com.multivendor.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    @Override
    public Page<ProductResponse> getAllActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(productMapper::toResponse);
    }

    @Override
    public Page<ProductResponse> search(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        List<Long> categoryIds = categoryId != null ? categoryService.getDescendantIds(categoryId) : null;
        return productRepository.search(keyword, categoryIds, minPrice, maxPrice, pageable).map(productMapper::toResponse);
    }

    @Override
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.toResponse(product);
    }

    @Override
    public long countInCategoryTree(Long categoryId) {
        List<Long> categoryIds = categoryService.getDescendantIds(categoryId);
        return productRepository.countByCategoryIds(categoryIds);
    }
}
