package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.dto.request.ReviewRequest;
import com.multivendor.ecommerce.dto.response.ReviewResponse;
import com.multivendor.ecommerce.entity.Product;
import com.multivendor.ecommerce.entity.ProductReview;
import com.multivendor.ecommerce.entity.User;
import com.multivendor.ecommerce.exception.ForbiddenException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.OrderItemRepository;
import com.multivendor.ecommerce.repository.ProductRepository;
import com.multivendor.ecommerce.repository.ProductReviewRepository;
import com.multivendor.ecommerce.repository.UserRepository;
import com.multivendor.ecommerce.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Reviews are deliberately open to any customer (not gated on having bought
 * the item) — verifiedPurchase is tracked and surfaced instead of blocking,
 * since requiring a purchase would make it impossible to ever bootstrap
 * reviews for a new product. Note this does mean rating-driven commission
 * (CommissionServiceImpl) can in principle be influenced by unverified
 * reviews; the minimumReviewsForAdjustment threshold exists partly to blunt
 * that.
 */
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public ReviewResponse submitReview(Long userId, Long productId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean verifiedPurchase = orderItemRepository.existsDeliveredPurchase(userId, productId);

        ProductReview review = productReviewRepository.findByProductIdAndUserId(productId, userId).orElse(null);
        if (review == null) {
            review = ProductReview.builder()
                    .product(product)
                    .user(user)
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .verifiedPurchase(verifiedPurchase)
                    .build();
        } else {
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setVerifiedPurchase(verifiedPurchase);
            review.setUpdatedAt(LocalDateTime.now());
        }

        review = productReviewRepository.save(review);
        return toResponse(review);
    }

    @Override
    public Page<ReviewResponse> getReviewsForProduct(Long productId, Pageable pageable) {
        return productReviewRepository.findByProductId(productId, pageable).map(this::toResponse);
    }

    @Override
    public ReviewResponse getMyReviewForProduct(Long userId, Long productId) {
        ProductReview review = productReviewRepository.findByProductIdAndUserId(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("You haven't reviewed this product yet"));
        return toResponse(review);
    }

    @Override
    public void deleteMyReview(Long userId, Long productId) {
        ProductReview review = productReviewRepository.findByProductIdAndUserId(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("You haven't reviewed this product"));
        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException("This review does not belong to you");
        }
        productReviewRepository.delete(review);
    }

    private ReviewResponse toResponse(ProductReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .verifiedPurchase(review.isVerifiedPurchase())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
