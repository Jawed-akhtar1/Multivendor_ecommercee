package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.VendorOrder;
import com.multivendor.ecommerce.entity.enums.OrderStatus;
import com.multivendor.ecommerce.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorOrderRepository extends JpaRepository<VendorOrder, Long> {

    Page<VendorOrder> findByVendorId(Long vendorId, Pageable pageable);

    List<VendorOrder> findByOrderId(Long orderId);

    Optional<VendorOrder> findBySubOrderNumber(String subOrderNumber);

    // Settlement-eligible: delivered, paid for, and not already part of a settlement.
    @Query("""
           SELECT vo FROM VendorOrder vo
           WHERE vo.vendor.id = :vendorId
             AND vo.status = :deliveredStatus
             AND vo.settlement IS NULL
             AND vo.order.paymentStatus = :paidStatus
           """)
    List<VendorOrder> findSettlementEligible(@Param("vendorId") Long vendorId,
                                              @Param("deliveredStatus") OrderStatus deliveredStatus,
                                              @Param("paidStatus") PaymentStatus paidStatus);

    Page<VendorOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
