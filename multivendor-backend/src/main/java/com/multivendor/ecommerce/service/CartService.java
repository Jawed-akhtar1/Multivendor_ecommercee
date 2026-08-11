package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.request.CartItemRequest;
import com.multivendor.ecommerce.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId);
    CartResponse addItem(Long userId, CartItemRequest request);
    CartResponse updateItem(Long userId, Long cartItemId, Integer quantity);
    CartResponse removeItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}
