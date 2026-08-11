package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.dto.response.ProductResponse;
import com.multivendor.ecommerce.entity.Product;
import com.multivendor.ecommerce.entity.User;
import com.multivendor.ecommerce.entity.WishlistItem;
import com.multivendor.ecommerce.exception.BadRequestException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.ProductRepository;
import com.multivendor.ecommerce.repository.UserRepository;
import com.multivendor.ecommerce.repository.WishlistItemRepository;
import com.multivendor.ecommerce.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResponse> getMyWishlist(Long userId) {
        return wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(WishlistItem::getProduct)
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public void addToWishlist(Long userId, Long productId) {
        if (wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            return; // already there — idempotent
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!product.isActive()) {
            throw new BadRequestException("This product is no longer available");
        }

        wishlistItemRepository.save(WishlistItem.builder().user(user).product(product).build());
    }

    @Override
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
