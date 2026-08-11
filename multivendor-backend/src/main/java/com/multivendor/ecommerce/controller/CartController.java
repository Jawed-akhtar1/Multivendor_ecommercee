package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.request.CartItemRequest;
import com.multivendor.ecommerce.dto.response.CartResponse;
import com.multivendor.ecommerce.service.CartService;
import com.multivendor.ecommerce.util.ApiResponse;
import com.multivendor.ecommerce.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(SecurityUtils.getCurrentUserId())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addItem(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Added to cart", response));
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long cartItemId, @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("'quantity' is required"));
        }
        CartResponse response = cartService.updateItem(SecurityUtils.getCurrentUserId(), cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", response));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable Long cartItemId) {
        CartResponse response = cartService.removeItem(SecurityUtils.getCurrentUserId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
