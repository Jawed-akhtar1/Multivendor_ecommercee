package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByVendorId(Long vendorId, Pageable pageable);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdInAndActiveTrue(List<Long> categoryIds, Pageable pageable);

    @Query("""
           SELECT p FROM Product p
           WHERE p.active = true
             AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:categoryIds IS NULL OR p.category.id IN :categoryIds)
             AND (:minPrice IS NULL OR p.price >= :minPrice)
             AND (:maxPrice IS NULL OR p.price <= :maxPrice)
           """)
    Page<Product> search(@Param("keyword") String keyword,
                          @Param("categoryIds") List<Long> categoryIds,
                          @Param("minPrice") BigDecimal minPrice,
                          @Param("maxPrice") BigDecimal maxPrice,
                          Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.category.id IN :categoryIds")
    long countByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

    boolean existsBySku(String sku);
}
