package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByVendorOrderId(Long vendorOrderId);

    @Query("""
           SELECT COUNT(oi) > 0 FROM OrderItem oi
           WHERE oi.product.id = :productId
             AND oi.vendorOrder.order.user.id = :userId
             AND oi.vendorOrder.status = com.multivendor.ecommerce.entity.enums.OrderStatus.DELIVERED
           """)
    boolean existsDeliveredPurchase(@Param("userId") Long userId, @Param("productId") Long productId);
}
