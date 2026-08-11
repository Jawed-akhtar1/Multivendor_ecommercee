package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.response.ProductResponse;
import com.multivendor.ecommerce.service.WishlistService;
import com.multivendor.ecommerce.util.ApiResponse;
import com.multivendor.ecommerce.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyWishlist() {
        return ResponseEntity.ok(ApiResponse.success(wishlistService.getMyWishlist(SecurityUtils.getCurrentUserId())));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> add(@PathVariable Long productId) {
        wishlistService.addToWishlist(SecurityUtils.getCurrentUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Added to wishlist", null));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(SecurityUtils.getCurrentUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", null));
    }
}
