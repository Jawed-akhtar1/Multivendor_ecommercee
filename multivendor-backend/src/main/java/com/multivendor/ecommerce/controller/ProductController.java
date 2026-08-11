package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.request.ReviewRequest;
import com.multivendor.ecommerce.dto.response.ProductResponse;
import com.multivendor.ecommerce.dto.response.ReviewResponse;
import com.multivendor.ecommerce.service.ProductReviewService;
import com.multivendor.ecommerce.service.ProductService;
import com.multivendor.ecommerce.util.ApiResponse;
import com.multivendor.ecommerce.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductReviewService productReviewService;

    // Browse Products / Search Products / Filter Products (Customer Module)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> browse(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponse> products;
        if (keyword != null || categoryId != null || minPrice != null || maxPrice != null) {
            products = productService.search(keyword, categoryId, minPrice, maxPrice, pageable);
        } else {
            products = productService.getAllActive(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    // --- Reviews / Ratings (Customer Module) ---
    // Rating aggregate feeds into the vendor's commission rate — see CommissionServiceImpl.

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(productReviewService.getReviewsForProduct(id, pageable)));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = productReviewService.submitReview(SecurityUtils.getCurrentUserId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Review submitted", response));
    }

    @GetMapping("/{id}/reviews/mine")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReview(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                productReviewService.getMyReviewForProduct(SecurityUtils.getCurrentUserId(), id)));
    }

    @DeleteMapping("/{id}/reviews/mine")
    public ResponseEntity<ApiResponse<Void>> deleteMyReview(@PathVariable Long id) {
        productReviewService.deleteMyReview(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Review removed", null));
    }
}
