package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.request.ReviewRequest;
import com.multivendor.ecommerce.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductReviewService {

    /** Creates the review, or updates the reviewer's existing one for this product (one per customer per product). */
    ReviewResponse submitReview(Long userId, Long productId, ReviewRequest request);

    Page<ReviewResponse> getReviewsForProduct(Long productId, Pageable pageable);

    ReviewResponse getMyReviewForProduct(Long userId, Long productId);

    void deleteMyReview(Long userId, Long productId);
}
