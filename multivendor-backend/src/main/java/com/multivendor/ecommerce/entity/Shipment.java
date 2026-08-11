package com.multivendor.ecommerce.entity;

import com.multivendor.ecommerce.entity.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks the Shiprocket booking for a single vendor's sub-order (VendorOrder)
 * — vendor-wise, since each vendor packs and ships their own items
 * independently. A multi-vendor Order can therefore have several Shipments,
 * one per VendorOrder, each with its own courier/AWB/tracking status.
 */
@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_order_id", nullable = false, unique = true)
    private VendorOrder vendorOrder;

    // Shiprocket's own identifiers, needed for later tracking/cancel calls
    private Long shiprocketOrderId;
    private Long shiprocketShipmentId;

    private String courierName;
    private String awbNumber;
    private String trackingUrl;
    private String estimatedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.NOT_CREATED;

    // Populated when a Shiprocket API call fails, so admins can see why
    // without digging through server logs.
    @Column(length = 1000)
    private String lastError;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
