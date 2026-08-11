package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Page<Settlement> findByVendorId(Long vendorId, Pageable pageable);
    Optional<Settlement> findBySettlementNumber(String settlementNumber);
    Page<Settlement> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
