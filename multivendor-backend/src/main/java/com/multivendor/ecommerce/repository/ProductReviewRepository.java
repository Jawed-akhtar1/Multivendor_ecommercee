package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    Page<ProductReview> findByProductId(Long productId, Pageable pageable);

    Optional<ProductReview> findByProductIdAndUserId(Long productId, Long userId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId")
    Double findAverageRatingForProduct(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId")
    Long countForProduct(@Param("productId") Long productId);

    /** Vendor-wide average across every review on every one of their products — drives commission tiering. */
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.vendor.id = :vendorId")
    Double findAverageRatingForVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.vendor.id = :vendorId")
    Long countForVendor(@Param("vendorId") Long vendorId);
}
