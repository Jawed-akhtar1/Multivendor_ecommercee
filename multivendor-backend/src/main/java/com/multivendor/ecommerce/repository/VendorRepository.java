package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    List<Vendor> findByApprovedFalse();
    List<Vendor> findByApprovedTrue();
}
