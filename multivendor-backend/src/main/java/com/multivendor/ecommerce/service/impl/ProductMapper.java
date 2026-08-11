package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.dto.response.ProductResponse;
import com.multivendor.ecommerce.entity.Product;
import com.multivendor.ecommerce.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductReviewRepository productReviewRepository;

    public ProductResponse toResponse(Product product) {
        Double avgRating = productReviewRepository.findAverageRatingForProduct(product.getId());
        Long reviewCount = productReviewRepository.countForProduct(product.getId());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .sku(product.getSku())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .vendorId(product.getVendor().getId())
                .vendorStoreName(product.getVendor().getStoreName())
                .averageRating(avgRating)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .build();
    }
}
