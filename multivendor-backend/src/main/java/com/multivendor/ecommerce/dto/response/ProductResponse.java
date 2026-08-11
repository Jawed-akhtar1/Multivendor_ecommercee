package com.multivendor.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private Integer stock;
    private String imageUrl;
    private boolean active;
    private Long categoryId;
    private String categoryName;
    private Long vendorId;
    private String vendorStoreName;
    private Double averageRating;
    private Long reviewCount;
}
