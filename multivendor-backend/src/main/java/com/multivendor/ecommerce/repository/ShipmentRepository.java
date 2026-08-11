package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByVendorOrderId(Long vendorOrderId);
    Optional<Shipment> findByAwbNumber(String awbNumber);
}
