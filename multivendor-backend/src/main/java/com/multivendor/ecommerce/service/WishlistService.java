package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.response.ProductResponse;

import java.util.List;

public interface WishlistService {
    List<ProductResponse> getMyWishlist(Long userId);
    void addToWishlist(Long userId, Long productId);
    void removeFromWishlist(Long userId, Long productId);
}
