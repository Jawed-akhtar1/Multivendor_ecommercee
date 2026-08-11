package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.dto.request.CartItemRequest;
import com.multivendor.ecommerce.dto.response.CartResponse;
import com.multivendor.ecommerce.entity.Cart;
import com.multivendor.ecommerce.entity.CartItem;
import com.multivendor.ecommerce.entity.Product;
import com.multivendor.ecommerce.entity.User;
import com.multivendor.ecommerce.exception.BadRequestException;
import com.multivendor.ecommerce.exception.ForbiddenException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.CartItemRepository;
import com.multivendor.ecommerce.repository.CartRepository;
import com.multivendor.ecommerce.repository.ProductRepository;
import com.multivendor.ecommerce.repository.UserRepository;
import com.multivendor.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponse getCart(Long userId) {
        return toResponse(getOrCreateCart(userId));
    }

    @Override
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new BadRequestException("This product is no longer available");
        }
        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Only " + product.getStock() + " unit(s) left in stock");
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (item == null) {
            item = CartItem.builder().cart(cart).product(product).quantity(request.getQuantity()).build();
            cart.getItems().add(item);
        } else {
            int newQty = item.getQuantity() + request.getQuantity();
            if (product.getStock() < newQty) {
                throw new BadRequestException("Only " + product.getStock() + " unit(s) left in stock");
            }
            item.setQuantity(newQty);
        }

        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Override
    public CartResponse updateItem(Long userId, Long cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = getOwnedItemOrThrow(cart, cartItemId);

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            return toResponse(cart);
        }

        if (item.getProduct().getStock() < quantity) {
            throw new BadRequestException("Only " + item.getProduct().getStock() + " unit(s) left in stock");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Override
    public CartResponse removeItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = getOwnedItemOrThrow(cart, cartItemId);
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toResponse(cart);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartItem getOwnedItemOrThrow(Cart cart, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ForbiddenException("This cart item does not belong to you");
        }
        return item;
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return cartRepository.save(Cart.builder().user(user).build());
        });
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream().map(item -> CartResponse.CartItemResponse.builder()
                .cartItemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .imageUrl(item.getProduct().getImageUrl())
                .price(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .availableStock(item.getProduct().getStock())
                .build()).toList();

        BigDecimal total = items.stream()
                .map(CartResponse.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalAmount(total)
                .build();
    }
}
